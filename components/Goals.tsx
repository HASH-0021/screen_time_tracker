import {useEffect, useState} from 'react';
import {
  Alert,
  Button,
  NativeModules,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import {useDispatch, useSelector} from 'react-redux';

import {updateSettingsData} from '../redux/features/settingsSlice';
import NavigationSection from '../sub_components/NavigationSection';
import LoadingIndicator from '../sub_components/LoadingIndicator';
import {themes} from '../helpers/colors';
import InfoDialogBox from '../sub_components/InfoDialogBox';

const Goals: React.FC<any> = ({navigation}) => {
  const [isDtgEnabled, setIsDtgEnabled] = useState(false);
  const [dtgGuHours, setDtgGuHours] = useState(0);
  const [dtgGuMinutes, setDtgGuMinutes] = useState(0);
  const [dtgBuHours, setDtgBuHours] = useState(0);
  const [dtgBuMinutes, setDtgBuMinutes] = useState(0);
  const [isDagEnabled, setIsDagEnabled] = useState(false);
  const [dagGuHours, setDagGuHours] = useState(0);
  const [dagGuMinutes, setDagGuMinutes] = useState(0);
  const [dagBuHours, setDagBuHours] = useState(0);
  const [dagBuMinutes, setDagBuMinutes] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [indicatorColor, setIndicatorColor] = useState('#0000ff');
  const [isInfoVisible, setIsInfoVisible] = useState(false);
  const [loadingText, setLoadingText] = useState('Loading');

  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);
  const dispatch = useDispatch();

  const {DataHandlerModule, NotificationModule} = NativeModules;

  useEffect(() => {
    updateDTGState();
    updateDAGState();
  }, []);

  const updateDTGState = () => {
    const [tGH, tGM] = convertMillisToHHMM(
      settingsData.dailyTotalGoalGoodUsage,
    );
    const [tBH, tBM] = convertMillisToHHMM(settingsData.dailyTotalGoalBadUsage);
    setDtgGuHours(tGH);
    setDtgGuMinutes(tGM);
    setDtgBuHours(tBH);
    setDtgBuMinutes(tBM);
    setIsDtgEnabled(settingsData.isDailyTotalGoalEnabled);
  };

  const updateDAGState = () => {
    const [aGH, aGM] = convertMillisToHHMM(settingsData.dailyAppGoalGoodUsage);
    const [aBH, aBM] = convertMillisToHHMM(settingsData.dailyAppGoalBadUsage);
    setDagGuHours(aGH);
    setDagGuMinutes(aGM);
    setDagBuHours(aBH);
    setDagBuMinutes(aBM);
    setIsDagEnabled(settingsData.isDailyAppGoalEnabled);
  };

  const convertMillisToHHMM = (milliSeconds: number) => {
    const totalMinutes = Math.floor(milliSeconds / 60000);
    const minutes = totalMinutes % 60;
    const hours = Math.floor(totalMinutes / 60);
    return [hours, minutes];
  };

  const setDailyTotalGoal = async () => {
    if (
      isNaN(dtgGuHours) ||
      isNaN(dtgGuMinutes) ||
      isNaN(dtgBuHours) ||
      isNaN(dtgBuMinutes)
    ) {
      Alert.alert('Invalid', 'Usage goal values should be a whole number.');
    } else if (
      dtgGuHours > 23 ||
      dtgGuMinutes > 59 ||
      dtgBuHours > 23 ||
      dtgBuMinutes > 59
    ) {
      Alert.alert(
        'Invalid',
        'Goal hours should not exceed 23 and goal minutes should not exceed 59.',
      );
    } else {
      const goodUsage = (dtgGuHours * 60 + dtgGuMinutes) * 60000;
      const badUsage = (dtgBuHours * 60 + dtgBuMinutes) * 60000;
      if (goodUsage < badUsage) {
        if (isDtgEnabled) {
          setLoadingText('Updating Goal');
        } else {
          setLoadingText('Setting Goal');
        }
        setIsLoading(true);
        setIndicatorColor('#00ff00');
        try {
          const settings = await DataHandlerModule.setDailyTotalGoal(
            goodUsage,
            badUsage,
          );
          await NotificationModule.updateTotalGoalTime(goodUsage, badUsage);
          dispatch(updateSettingsData(settings));
          await new Promise(resolve =>
            setTimeout(() => {
              resolve('Done');
            }, 1000),
          );
          setIsDtgEnabled(true);
          setIsLoading(false);
        } catch (error: any) {
          Alert.alert('Error', error.message);
        }
      } else {
        Alert.alert(
          'Invalid',
          'Good usage goal should be less than bad usage goal.',
        );
      }
    }
  };

  const removeDailyTotalGoal = async () => {
    setIsLoading(true);
    setLoadingText('Removing Goal');
    setIndicatorColor('#ff0000');
    try {
      const settings = await DataHandlerModule.removeDailyTotalGoal();
      dispatch(updateSettingsData(settings));
      await new Promise(resolve =>
        setTimeout(() => {
          resolve('Done');
        }, 1000),
      );
      setIsDtgEnabled(false);
      setIsLoading(false);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const setDailyAppGoal = async () => {
    if (
      isNaN(dagGuHours) ||
      isNaN(dagGuMinutes) ||
      isNaN(dagBuHours) ||
      isNaN(dagBuMinutes)
    ) {
      Alert.alert('Invalid', 'Usage goal values should be a whole number.');
    } else if (
      dagGuHours > 23 ||
      dagGuMinutes > 59 ||
      dagBuHours > 23 ||
      dagBuMinutes > 59
    ) {
      Alert.alert(
        'Invalid',
        'Goal hours should not exceed 23 and goal minutes should not exceed 59.',
      );
    } else {
      const goodUsage = (dagGuHours * 60 + dagGuMinutes) * 60000;
      const badUsage = (dagBuHours * 60 + dagBuMinutes) * 60000;
      if (goodUsage < badUsage) {
        if (isDagEnabled) {
          setLoadingText('Updating Goal');
        } else {
          setLoadingText('Setting Goal');
        }
        setIsLoading(true);
        setIndicatorColor('#00ff00');
        try {
          const settings = await DataHandlerModule.setDailyAppGoal(
            goodUsage,
            badUsage,
          );
          await NotificationModule.updateAppGoalTime(goodUsage, badUsage);
          dispatch(updateSettingsData(settings));
          await new Promise(resolve =>
            setTimeout(() => {
              resolve('Done');
            }, 1000),
          );
          setIsDagEnabled(true);
          setIsLoading(false);
        } catch (error: any) {
          Alert.alert('Error', error.message);
        }
      } else {
        Alert.alert(
          'Invalid',
          'Good usage goal should be less than bad usage goal.',
        );
      }
    }
  };

  const removeDailyAppGoal = async () => {
    setIsLoading(true);
    setLoadingText('Removing Goal');
    setIndicatorColor('#ff0000');
    try {
      const settings = await DataHandlerModule.removeDailyAppGoal();
      dispatch(updateSettingsData(settings));
      await new Promise(resolve =>
        setTimeout(() => {
          resolve('Done');
        }, 1000),
      );
      setIsDagEnabled(false);
      setIsLoading(false);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  return (
    <View style={[theme.basic, styles.goals]}>
      <NavigationSection
        navigation={navigation}
        screens={[
          {
            name: 'Back',
            icon: 'humbleicons:chevron-left',
            color: 'blue',
          },
          {
            name: 'Info',
            icon: 'tabler:info-circle',
            color: 'yellow',
          },
        ]}
        setIsInfoVisible={setIsInfoVisible}
      />
      <Text style={[styles.heading, theme.basic]}>Goals</Text>
      <ScrollView contentContainerStyle={styles.goalsList}>
        <View style={[styles.goalSection, {borderColor: theme.basic.color}]}>
          <Text style={[styles.goalName, theme.basic]}>Daily Total Goal</Text>
          <View style={styles.goalInputSection}>
            <Text style={[styles.usageHeading, theme.basic]}>
              Good Usage Ending Time
            </Text>
            <View style={styles.inputContainer}>
              <View
                style={[
                  styles.inputBox,
                  {
                    borderColor:
                      isNaN(dtgGuHours) || dtgGuHours > 23
                        ? 'red'
                        : theme.basic.color,
                  },
                ]}>
                <TextInput
                  inputMode="numeric"
                  maxLength={2}
                  onChangeText={text => setDtgGuHours(Number(text))}
                  textAlign="right"
                  defaultValue={String(dtgGuHours)}
                  style={theme.basic}
                />
              </View>
              <Text style={theme.basic}>:</Text>
              <View
                style={[
                  styles.inputBox,
                  {
                    borderColor:
                      isNaN(dtgGuMinutes) || dtgGuMinutes > 59
                        ? 'red'
                        : theme.basic.color,
                  },
                ]}>
                <TextInput
                  inputMode="numeric"
                  maxLength={2}
                  onChangeText={text => setDtgGuMinutes(Number(text))}
                  textAlign="right"
                  defaultValue={String(dtgGuMinutes)}
                  style={theme.basic}
                />
              </View>
            </View>
            <Text style={[styles.usageHeading, theme.basic]}>
              Bad Usage Starting Time
            </Text>
            <View style={styles.inputContainer}>
              <View
                style={[
                  styles.inputBox,
                  {
                    borderColor:
                      isNaN(dtgBuHours) || dtgBuHours > 23
                        ? 'red'
                        : theme.basic.color,
                  },
                ]}>
                <TextInput
                  inputMode="numeric"
                  maxLength={2}
                  onChangeText={text => setDtgBuHours(Number(text))}
                  textAlign="right"
                  defaultValue={String(dtgBuHours)}
                  style={theme.basic}
                />
              </View>
              <Text style={theme.basic}>:</Text>
              <View
                style={[
                  styles.inputBox,
                  {
                    borderColor:
                      isNaN(dtgBuMinutes) || dtgBuMinutes > 59
                        ? 'red'
                        : theme.basic.color,
                  },
                ]}>
                <TextInput
                  inputMode="numeric"
                  maxLength={2}
                  onChangeText={text => setDtgBuMinutes(Number(text))}
                  textAlign="right"
                  defaultValue={String(dtgBuMinutes)}
                  style={theme.basic}
                />
              </View>
            </View>
          </View>
          <View style={styles.buttonContainer}>
            <View style={styles.buttons}>
              <Button
                title="Remove"
                onPress={removeDailyTotalGoal}
                disabled={!isDtgEnabled}
              />
            </View>
            <View style={styles.buttons}>
              <Button
                title={isDtgEnabled ? 'Update' : 'Set'}
                onPress={setDailyTotalGoal}
              />
            </View>
          </View>
          <Text style={[theme.basic, styles.goalDescription]}>
            Setting this goal helps by color coding daily total usage statistics
            throughout the app.
          </Text>
        </View>
        <View
          style={[
            styles.goalSection,
            {borderTopWidth: 0, borderColor: theme.basic.color},
          ]}>
          <Text style={[styles.goalName, theme.basic]}>Daily App Goal</Text>
          <View style={styles.goalInputSection}>
            <Text style={[styles.usageHeading, theme.basic]}>
              Good Usage Ending Time
            </Text>
            <View style={styles.inputContainer}>
              <View
                style={[
                  styles.inputBox,
                  {
                    borderColor:
                      isNaN(dagGuHours) || dagGuHours > 23
                        ? 'red'
                        : theme.basic.color,
                  },
                ]}>
                <TextInput
                  inputMode="numeric"
                  maxLength={2}
                  onChangeText={text => setDagGuHours(Number(text))}
                  textAlign="right"
                  defaultValue={String(dagGuHours)}
                  style={theme.basic}
                />
              </View>
              <Text style={theme.basic}>:</Text>
              <View
                style={[
                  styles.inputBox,
                  {
                    borderColor:
                      isNaN(dagGuMinutes) || dagGuMinutes > 59
                        ? 'red'
                        : theme.basic.color,
                  },
                ]}>
                <TextInput
                  inputMode="numeric"
                  maxLength={2}
                  onChangeText={text => setDagGuMinutes(Number(text))}
                  textAlign="right"
                  defaultValue={String(dagGuMinutes)}
                  style={theme.basic}
                />
              </View>
            </View>
            <Text style={[styles.usageHeading, theme.basic]}>
              Bad Usage Starting Time
            </Text>
            <View style={styles.inputContainer}>
              <View
                style={[
                  styles.inputBox,
                  {
                    borderColor:
                      isNaN(dagBuHours) || dagBuHours > 23
                        ? 'red'
                        : theme.basic.color,
                  },
                ]}>
                <TextInput
                  inputMode="numeric"
                  maxLength={2}
                  onChangeText={text => setDagBuHours(Number(text))}
                  textAlign="right"
                  defaultValue={String(dagBuHours)}
                  style={theme.basic}
                />
              </View>
              <Text style={theme.basic}>:</Text>
              <View
                style={[
                  styles.inputBox,
                  {
                    borderColor:
                      isNaN(dagBuMinutes) || dagBuMinutes > 59
                        ? 'red'
                        : theme.basic.color,
                  },
                ]}>
                <TextInput
                  inputMode="numeric"
                  maxLength={2}
                  onChangeText={text => setDagBuMinutes(Number(text))}
                  textAlign="right"
                  defaultValue={String(dagBuMinutes)}
                  style={theme.basic}
                />
              </View>
            </View>
          </View>
          <View style={styles.buttonContainer}>
            <View style={styles.buttons}>
              <Button
                title="Remove"
                onPress={removeDailyAppGoal}
                disabled={!isDagEnabled}
              />
            </View>
            <View style={styles.buttons}>
              <Button
                title={isDagEnabled ? 'Update' : 'Set'}
                onPress={setDailyAppGoal}
              />
            </View>
          </View>
          <Text style={[theme.basic, styles.goalDescription]}>
            Setting this goal helps by color coding daily app usage statistics
            throughout the app.
          </Text>
        </View>
      </ScrollView>
      <InfoDialogBox isVisible={isInfoVisible} setIsVisible={setIsInfoVisible}>
        <Text style={styles.infoDescription}>
          Setting the goals doesn't set the notification alert. If goal time is
          updated, then the notification alerts (if enabled) trigger using the
          updated goal time.
        </Text>
        <Text style={styles.infoDescription}>
          Good Usage : Usage is below good usage ending time. Screen time value
          is colored green.
        </Text>
        <Text style={styles.infoDescription}>
          Bad Usage : Usage is above bad usage starting time. Screen time value
          is colored red.
        </Text>
        <Text style={styles.infoDescription}>
          Moderate Usage : Usage is between good usage and bad usage. Screen
          time value is colored orange.
        </Text>
      </InfoDialogBox>
      <LoadingIndicator
        isLoading={isLoading}
        setIsLoading={setIsLoading}
        indicatorColor={indicatorColor}
        loadingText={loadingText}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  goals: {
    paddingVertical: '2%',
    flex: 1,
  },
  heading: {
    marginVertical: 10,
    fontSize: 35,
    fontWeight: '700',
    textAlign: 'center',
  },
  goalsList: {
    marginVertical: 10,
    width: '100%',
  },
  goalSection: {
    padding: '4%',
    borderTopWidth: 1,
    borderBottomWidth: 1,
  },
  goalName: {
    marginVertical: 5,
    fontSize: 20,
  },
  goalInputSection: {
    marginVertical: 5,
  },
  usageHeading: {
    marginVertical: 8,
    fontSize: 16,
    textAlign: 'center',
  },
  inputContainer: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  inputBox: {
    marginHorizontal: 5,
    width: 28,
    borderWidth: 2,
  },
  buttonContainer: {
    paddingHorizontal: 5,
    paddingVertical: 10,
    width: '100%',
    flex: 1,
    flexDirection: 'row',
  },
  buttons: {
    marginHorizontal: 5,
    flex: 0.5,
  },
  goalDescription: {
    marginVertical: '1%',
    color: 'gray',
    fontStyle: 'italic',
    fontSize: 12,
  },
  infoDescription: {
    marginVertical: 5,
    fontSize: 18,
    textAlign: 'left',
    color: 'black',
  },
});

export default Goals;
