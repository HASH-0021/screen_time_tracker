import {Pressable, StyleSheet, Text, View} from 'react-native';
import {useSelector} from 'react-redux';

import {themes} from '../helpers/colors';

const PeriodSelector: React.FC<any> = ({period, setPeriod}) => {
  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  return (
    <View
      style={[styles.selector, theme.basic, {borderColor: theme.basic.color}]}>
      <Pressable
        onPress={() => setPeriod('weekly')}
        style={[
          styles.option,
          styles.leftOption,
          period === 'weekly' ? {backgroundColor: 'blue'} : {},
          {borderColor: theme.basic.color},
        ]}>
        <Text
          style={[
            styles.optionText,
            theme.basic,
            period === 'weekly'
              ? {backgroundColor: 'blue', color: 'white', fontWeight: '600'}
              : {},
          ]}>
          Weekly
        </Text>
      </Pressable>
      <Pressable
        onPress={() => setPeriod('monthly')}
        style={[
          styles.option,
          period === 'monthly' ? {backgroundColor: 'blue'} : {},
          {borderColor: theme.basic.color},
        ]}>
        <Text
          style={[
            styles.optionText,
            theme.basic,
            period === 'monthly'
              ? {backgroundColor: 'blue', color: 'white', fontWeight: '600'}
              : {},
          ]}>
          Monthly
        </Text>
      </Pressable>
      <Pressable
        onPress={() => setPeriod('yearly')}
        style={[
          styles.option,
          styles.rightOption,
          period === 'yearly' ? {backgroundColor: 'blue'} : {},
          {borderColor: theme.basic.color},
        ]}>
        <Text
          style={[
            styles.optionText,
            theme.basic,
            period === 'yearly'
              ? {backgroundColor: 'blue', color: 'white', fontWeight: '600'}
              : {},
          ]}>
          Yearly
        </Text>
      </Pressable>
    </View>
  );
};

const styles = StyleSheet.create({
  selector: {
    marginVertical: 10,
    marginHorizontal: 'auto',
    padding: 2,
    flexDirection: 'row',
    borderWidth: 1.8,
    borderRadius: 20,
    elevation: 10,
  },
  option: {
    paddingVertical: 5,
    paddingHorizontal: 10,
  },
  leftOption: {
    paddingRight: 7,
    borderTopLeftRadius: 20,
    borderBottomLeftRadius: 20,
    borderRightWidth: 1,
  },
  rightOption: {
    paddingLeft: 7,
    borderTopRightRadius: 20,
    borderBottomRightRadius: 20,
    borderLeftWidth: 1,
  },
  optionText: {
    fontSize: 15,
  },
});

export default PeriodSelector;
