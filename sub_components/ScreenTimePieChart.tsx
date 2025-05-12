import {useState} from 'react';
import {StyleSheet, View, Text} from 'react-native';
import {useSelector} from 'react-redux';
import {PieChart} from 'react-native-gifted-charts';

import {convertTimeFormat, getTotalScreenTime} from '../helpers/tools';
import {themes} from '../helpers/colors';

interface AppUsageProps {
  packageName: string;
  totalTimeInForeground: number;
  appName: string;
  appIconBase64: string;
}

interface AppListProps {
  usageStats: AppUsageProps[];
}

const ScreenTimePieChart: React.FC<AppListProps> = ({usageStats}) => {
  const [focusedIndex, setFocusedIndex] = useState(-1);

  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const changeFocusedIndex = (item: object, index: number) => {
    setFocusedIndex(prev => (prev === index ? -1 : index));
  };

  const totalScreenTime = getTotalScreenTime(usageStats);
  const usageChartData = usageStats.map(appUsage => {
    const appUsageValue = appUsage.totalTimeInForeground;
    const appUsagePercent = Math.floor((appUsageValue / totalScreenTime) * 100);

    return appUsagePercent > 5
      ? {
          value: appUsageValue,
          text: `${appUsagePercent}%`,
          externalLabelComponent: () => <Text>{appUsage.appName}</Text>,
          tooltipText: `${appUsage.appName} (${convertTimeFormat(
            appUsageValue,
          )})`,
        }
      : {
          value: appUsageValue,
          tooltipText: `${appUsage.appName} (${convertTimeFormat(
            appUsageValue,
          )})`,
        };
  });

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  return (
    <View style={styles.pieChartContainer}>
      <Text style={[styles.heading, theme.basic]}>Screen Time</Text>
      <PieChart
        data={usageChartData}
        // radius={50}
        showText
        onPress={changeFocusedIndex}
        // showExternalLabels={focusedIndex === -1}
        showTooltip
        // persistTooltip
        tooltipDuration={5000}
        tooltipBorderRadius={10}
        textSize={12}
        extraRadius={15}
        // isThreeD
        // tiltAngle="30deg"
        // shadow
        // shadowWidth={10}
        // focusOnPress
      />
    </View>
  );
};

const styles = StyleSheet.create({
  pieChartContainer: {
    padding: '4%',
  },
  heading: {
    marginVertical: 10,
    fontSize: 25,
    fontWeight: '700',
    textAlign: 'center',
  },
});

export default ScreenTimePieChart;
