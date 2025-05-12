import {useEffect, useState} from 'react';
import {
  Alert,
  NativeModules,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {useSelector} from 'react-redux';

import NavigationSection from '../sub_components/NavigationSection';
import AppList from '../sub_components/AppList';
import PeriodicBarChart from '../sub_components/PeriodicBarChart';
import PeriodSelector from '../sub_components/PeriodSelector';
import {
  calculateTotalTime,
  convertTimeFormat,
  getMonthlyData,
  getWeeklyData,
  getYearlyData,
} from '../helpers/tools';
import {themes} from '../helpers/colors';

interface AppUsageProps {
  packageName: string;
  totalTimeInForeground: number;
  appName: string;
  appIconBase64: string;
}

const PeriodicStats: React.FC<any> = ({navigation}) => {
  const [screenTimeData, setScreenTimeData] = useState({});
  const [appsData, setAppsData]: [any, Function] = useState({});
  const [period, setPeriod] = useState('weekly');
  const [usageData, setUsageData] = useState({});
  const [splitUsageData, setSplitUsageData] = useState({});

  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  const {DataHandlerModule} = NativeModules;

  useEffect(() => {
    updateScreenTimeData();
  }, []);

  useEffect(() => {
    if (period === 'weekly') {
      console.log('updating weekly stats');
      const d = new Date();
      updateWeeklyUsageStats(d.getFullYear(), d.getMonth() + 1, d.getDate());
    } else if (period === 'monthly') {
      console.log('updating monthly stats');
      const d = new Date();
      updateMonthlyUsageStats(d.getFullYear(), d.getMonth() + 1);
    } else if (period === 'yearly') {
      console.log('updating yearly stats');
      const d = new Date();
      updateYearlyUsageStats(d.getFullYear());
    }
  }, [screenTimeData, period]);

  const updateScreenTimeData = async () => {
    try {
      const {data, app} = await DataHandlerModule.getScreenTimeData();
      setScreenTimeData(data);
      setAppsData(app);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const updateUsageData = (data: any) => {
    let updatedData: AppUsageProps[] = [];
    for (const packageName in data) {
      const appData: any = appsData[packageName];
      const appUsage: AppUsageProps = {
        packageName,
        totalTimeInForeground: data[packageName],
        appName: appData['appName'],
        appIconBase64: appData['appIconBase64'],
      };
      updatedData.push(appUsage);
    }
    const sortedData = updatedData.sort(
      (a: any, b: any) => b.totalTimeInForeground - a.totalTimeInForeground,
    );
    return sortedData;
  };

  const updateWeeklyUsageStats = (
    year: number,
    month: number,
    date: number,
  ) => {
    const {aggregatedWeeklyData, weeklyData} = getWeeklyData(
      screenTimeData,
      year,
      month,
      date,
    );
    setUsageData(aggregatedWeeklyData);
    setSplitUsageData(weeklyData);
  };

  const updateMonthlyUsageStats = (year: number, month: number) => {
    const {aggregatedMonthlyData, monthlyData} = getMonthlyData(
      screenTimeData,
      year,
      month,
    );
    setUsageData(aggregatedMonthlyData);
    setSplitUsageData(monthlyData);
  };

  const updateYearlyUsageStats = (year: number) => {
    const {aggregatedYearlyData, yearlyData} = getYearlyData(
      screenTimeData,
      year,
    );
    setUsageData(aggregatedYearlyData);
    setSplitUsageData(yearlyData);
  };

  return (
    <ScrollView style={theme.basic}>
      <View style={styles.periodicStats}>
        <NavigationSection
          navigation={navigation}
          screens={[
            {
              name: 'HomeScreen',
              icon: 'mdi:home',
              color: 'orange',
            },
            {
              name: 'Settings',
              icon: 'ion:settings-sharp',
              color: 'gray',
            },
          ]}
        />
        <PeriodSelector period={period} setPeriod={setPeriod} />
        <PeriodicBarChart
          period={period}
          splitUsageData={splitUsageData}
          setUsageData={setUsageData}
        />
        <View style={styles.totalScreenTimeContainer}>
          <Text style={[styles.totalScreenTimeText, theme.basic]}>
            Total Screen Time :{' '}
            {convertTimeFormat(calculateTotalTime(usageData))}
          </Text>
        </View>
        <AppList usageStats={updateUsageData(usageData)} isDailyList={false} />
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  periodicStats: {
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

export default PeriodicStats;
