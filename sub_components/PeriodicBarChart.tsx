import {StyleSheet, View} from 'react-native';
import {useSelector} from 'react-redux';
import {BarChart} from 'react-native-gifted-charts';

import {themes} from '../helpers/colors';
import {calculateTotalTime} from '../helpers/tools';

interface BarDataProps {
  value: number;
  label: string;
}

const PeriodicBarChart: React.FC<any> = ({
  period,
  splitUsageData,
  setUsageData,
}) => {
  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const days = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];
  const months = [
    'JAN',
    'FEB',
    'MAR',
    'APR',
    'MAY',
    'JUN',
    'JUL',
    'AUG',
    'SEP',
    'OCT',
    'NOV',
    'DEC',
  ];

  const barData: BarDataProps[] = [];

  if (period === 'weekly') {
    for (let index = 0; index < 7; index++) {
      barData.push({
        value:
          calculateTotalTime(splitUsageData[String(index)]) / (3600 * 1000),
        label: days[index],
      });
    }
  } else if (period === 'monthly') {
    for (let index = 1; index <= 6; index++) {
      if (splitUsageData[String(index)] === undefined) {
        continue;
      }
      barData.push({
        value:
          calculateTotalTime(splitUsageData[String(index)]) / (3600 * 1000),
        label: `Week ${index}`,
      });
    }
  } else if (period === 'yearly') {
    for (let index = 1; index <= 12; index++) {
      barData.push({
        value:
          calculateTotalTime(splitUsageData[String(index)]) / (3600 * 1000),
        label: months[index - 1],
      });
    }
  }

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  return (
    <View style={styles.barChartContainer}>
      <BarChart
        width={period === 'weekly' ? 303 : period === 'monthly' ? 296 : 320}
        height={300}
        barWidth={20}
        spacing={
          period === 'weekly'
            ? 22
            : period === 'monthly'
            ? 30
            : period === 'yearly'
            ? 6.5
            : 10
        }
        barBorderRadius={4}
        data={barData}
        focusBarOnPress
        backgroundColor={theme.basic.backgroundColor}
        frontColor="blue"
        focusedBarConfig={{color: 'darkblue'}}
        disableScroll
        xAxisColor={theme.basic.color}
        yAxisColor={theme.basic.color}
        xAxisLabelTextStyle={{color: theme.basic.color}}
        yAxisTextStyle={{color: theme.basic.color}}
        rotateLabel
        maxValue={
          period === 'weekly'
            ? 25
            : period === 'monthly'
            ? 175
            : period === 'yearly'
            ? 750
            : 1000
        }
        stepValue={
          period === 'weekly'
            ? 5
            : period === 'monthly'
            ? 35
            : period === 'yearly'
            ? 150
            : 100
        }
        onPress={(item: BarDataProps, index: number) =>
          setUsageData(splitUsageData[String(index)])
        }
        isAnimated
      />
    </View>
  );
};

const styles = StyleSheet.create({
  barChartContainer: {
    padding: '4%',
    width: '100%',
  },
});

export default PeriodicBarChart;
