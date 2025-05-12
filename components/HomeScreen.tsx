import {useEffect, useState} from 'react';
import {
  Alert,
  AppState,
  AppStateStatus,
  NativeModules,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';
import {useDispatch, useSelector} from 'react-redux';

import {updateSettingsData} from '../redux/features/settingsSlice';
import {updateColorSchemeValue} from '../redux/features/colorSchemeSlice';
import ScreenTimePieChart from '../sub_components/ScreenTimePieChart';
import AppList from '../sub_components/AppList';
import NavigationSection from '../sub_components/NavigationSection';
import {convertTimeFormat, getTotalScreenTime} from '../helpers/tools';
import {themes} from '../helpers/colors';

const HomeScreen: React.FC<any> = ({navigation}) => {
  // find prop type
  const colorSchemeValue = useColorScheme();
  const [appState, setAppState] = useState(AppState.currentState);
  const [usageAccess, setUsageAccess] = useState(false);
  const [usageStats, setUsageStats] = useState([]);
  const [refreshing, setRefreshing] = useState(false);

  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);
  const dispatch = useDispatch();

  const {ForegroundUsageModule, DataHandlerModule, SyncScheduleModule} =
    NativeModules;

  useEffect(() => {
    checkUsageAccessPermission();
    updateColorScheme();
  }, []);

  useEffect(() => {
    const handleAppStateChange = (nextAppState: AppStateStatus) => {
      if (appState !== 'active' && nextAppState === 'active') {
        checkUsageAccessPermission();
      }
      setAppState(nextAppState);
    };

    const subscription = AppState.addEventListener(
      'change',
      handleAppStateChange,
    );

    return () => {
      subscription.remove();
    };
  }, [appState]);

  const checkUsageAccessPermission = async () => {
    try {
      const hasUsageAccessPermission =
        await ForegroundUsageModule.hasUsageAccessPermission();

      if (!hasUsageAccessPermission) {
        Alert.alert(
          'Permission Alert',
          'This app requires usage access permission to function.',
          [
            {
              text: 'Grant Permission',
              onPress: openUsageAccessSettings,
            },
          ],
        );
      } else {
        fetchUsageData();
        updateSettings();
        await SyncScheduleModule.scheduleDailySync();
      }

      setUsageAccess(hasUsageAccessPermission);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const openUsageAccessSettings = async () => {
    try {
      await ForegroundUsageModule.openUsageAccessSettings();
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const fetchUsageData = async () => {
    try {
      setRefreshing(true);
      const usageData = await ForegroundUsageModule.getForegroundUsage();

      const sortedData = usageData.sort(
        (a: any, b: any) => b.totalTimeInForeground - a.totalTimeInForeground,
      );

      console.log(sortedData);

      setRefreshing(false);
      setUsageStats(sortedData);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const updateSettings = async () => {
    try {
      const settings = await DataHandlerModule.getSettingsData();
      dispatch(updateSettingsData(settings));
      console.log(settings);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const updateColorScheme = () => {
    dispatch(updateColorSchemeValue(colorSchemeValue));
  };

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';
  const theme = isDarkMode ? themes.dark : themes.light;

  const totalScreenTime = getTotalScreenTime(usageStats);

  return (
    <ScrollView
      style={theme.basic}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={fetchUsageData} />
      }>
      {usageAccess && (
        <View style={styles.homeScreen}>
          <NavigationSection
            navigation={navigation}
            screens={[
              {
                name: 'PeriodicStats',
                icon: 'lucide:bar-chart-3',
                color: 'orange',
              },
              {
                name: 'Settings',
                icon: 'ion:settings-sharp',
                color: 'gray',
              },
            ]}
          />
          <ScreenTimePieChart usageStats={usageStats} />
          <View style={styles.totalScreenTimeContainer}>
            <Text style={[styles.totalScreenTimeText, theme.basic]}>
              Total Screen Time :{' '}
            </Text>
            {!settingsData.isDailyTotalGoalEnabled && (
              <Text style={[styles.totalScreenTimeText, theme.basic]}>
                {convertTimeFormat(totalScreenTime)}
              </Text>
            )}
            {settingsData.isDailyTotalGoalEnabled &&
              (totalScreenTime < settingsData.dailyTotalGoalGoodUsage ? (
                <Text
                  style={[styles.totalScreenTimeText, {color: 'limegreen'}]}>
                  {convertTimeFormat(totalScreenTime)}
                </Text>
              ) : totalScreenTime < settingsData.dailyTotalGoalBadUsage ? (
                <Text style={[styles.totalScreenTimeText, {color: 'orange'}]}>
                  {convertTimeFormat(totalScreenTime)}
                </Text>
              ) : (
                <Text style={[styles.totalScreenTimeText, {color: 'red'}]}>
                  {convertTimeFormat(totalScreenTime)}
                </Text>
              ))}
          </View>
          <AppList usageStats={usageStats} isDailyList={true} />
        </View>
      )}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  homeScreen: {
    paddingVertical: '2%',
    alignItems: 'center',
  },
  totalScreenTimeContainer: {
    marginVertical: 10,
    padding: '4%',
    flexDirection: 'row',
  },
  totalScreenTimeText: {
    fontSize: 18,
    fontWeight: '500',
  },
});

export default HomeScreen;
