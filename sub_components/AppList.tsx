import {View, Text, StyleSheet, Image} from 'react-native';
import {useSelector} from 'react-redux';

import {convertTimeFormat} from '../helpers/tools';
import {themes} from '../helpers/colors';

interface AppUsageProps {
  packageName: string;
  totalTimeInForeground: number;
  appName: string;
  appIconBase64: string;
}

interface AppListProps {
  usageStats: AppUsageProps[];
  isDailyList: boolean;
}

const AppList: React.FC<AppListProps> = ({usageStats, isDailyList}) => {
  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  return (
    <View style={styles.appList}>
      <Text style={[styles.heading, theme.basic]}>App List</Text>
      <View style={[styles.itemContainer, theme.basic]}>
        <Text
          style={[
            styles.headerText,
            {flex: 0.5, textAlign: 'center'},
            theme.basic,
          ]}>
          App
        </Text>
        <Text
          style={[
            styles.headerText,
            {flex: 0.5, textAlign: 'right'},
            theme.basic,
          ]}>
          Screen Time
        </Text>
      </View>
      {usageStats.map((appUsage: AppUsageProps, idx: number) => (
        <View key={`item-${idx}`} style={[styles.itemContainer, theme.basic]}>
          <View style={styles.appInfo}>
            <Image
              source={{uri: `data:image/png;base64,${appUsage.appIconBase64}`}}
              style={styles.appIcon}
            />
            <Text style={[styles.appText, theme.basic]}>
              {appUsage.appName}
            </Text>
          </View>
          {(!isDailyList || !settingsData.isDailyAppGoalEnabled) && (
            <Text style={[styles.appText, theme.basic]}>
              {convertTimeFormat(appUsage.totalTimeInForeground)}
            </Text>
          )}
          {isDailyList &&
            settingsData.isDailyAppGoalEnabled &&
            (appUsage.totalTimeInForeground <
            settingsData.dailyAppGoalGoodUsage ? (
              <Text style={[styles.appText, {color: 'limegreen'}]}>
                {convertTimeFormat(appUsage.totalTimeInForeground)}
              </Text>
            ) : appUsage.totalTimeInForeground <
              settingsData.dailyAppGoalBadUsage ? (
              <Text style={[styles.appText, {color: 'orange'}]}>
                {convertTimeFormat(appUsage.totalTimeInForeground)}
              </Text>
            ) : (
              <Text style={[styles.appText, {color: 'red'}]}>
                {convertTimeFormat(appUsage.totalTimeInForeground)}
              </Text>
            ))}
        </View>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  appList: {
    padding: '4%',
    paddingTop: 0,
  },
  heading: {
    marginVertical: 10,
    fontSize: 25,
    fontWeight: '700',
    textAlign: 'center',
  },
  itemContainer: {
    marginVertical: '2%',
    paddingVertical: '1%',
    paddingHorizontal: '2%',
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    borderRadius: 10,
    elevation: 10,
  },
  headerText: {
    fontSize: 15,
    fontWeight: '500',
  },
  appInfo: {
    flexDirection: 'row',
  },
  appIcon: {
    marginRight: 5,
    width: 22,
    height: 22,
  },
  appText: {
    fontSize: 15,
  },
});

export default AppList;
