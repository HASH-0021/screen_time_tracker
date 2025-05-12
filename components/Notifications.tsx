import {useEffect, useState} from 'react';
import {
  Alert,
  AppState,
  AppStateStatus,
  NativeModules,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  View,
} from 'react-native';
import {useSelector} from 'react-redux';

import NavigationSection from '../sub_components/NavigationSection';
import {themes} from '../helpers/colors';

const Notifications: React.FC<any> = ({navigation}) => {
  // PSTN - Persistent screen time notification
  // DTGN - Daily total goal notification
  // DAGN - Daily app goal notification

  const [isNotificationsEnabled, setIsNotificationsEnabled] = useState(false);
  const [appState, setAppState] = useState(AppState.currentState);
  const [isPSTNEnabled, setIsPSTNEnabled] = useState(false);
  const [isDTGNEnabled, setIsDTGNEnabled] = useState(false);
  const [isDAGNEnabled, setIsDAGNEnabled] = useState(false);

  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const {NotificationModule} = NativeModules;

  useEffect(() => {
    updateInitialState();
    checkPostNotificationsPermission();
  }, []);

  useEffect(() => {
    const handleAppStateChange = (nextAppState: AppStateStatus) => {
      if (appState !== 'active' && nextAppState === 'active') {
        checkPostNotificationsPermission();
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

  const updateInitialState = async () => {
    const isPSTNRunning =
      await NotificationModule.isScreenTimeNotificationRunning();
    const isDTGNRunning =
      await NotificationModule.isTotalGoalNotificationRunning();
    const isDAGNRunning =
      await NotificationModule.isAppGoalNotificationRunning();
    setIsPSTNEnabled(isPSTNRunning);
    setIsDTGNEnabled(isDTGNRunning);
    setIsDAGNEnabled(isDAGNRunning);
  };

  const checkPostNotificationsPermission = async () => {
    const hasPostNotificationsPermission =
      await NotificationModule.checkPostNotificationsPermission();
    setIsNotificationsEnabled(hasPostNotificationsPermission);
  };

  const openNotificationSettings = async () => {
    try {
      await NotificationModule.openPostNotificationsSettings();
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const updatePSTNState = async () => {
    if (isPSTNEnabled) {
      await NotificationModule.stopScreenTimeNotification();
    } else {
      let hasBackgroundUsagePermission =
        await NotificationModule.hasBackgroundUsagePermission();
      if (!hasBackgroundUsagePermission) {
        await NotificationModule.openBackgroundUsageSettings();
      }
      await NotificationModule.startScreenTimeNotification();
    }
    setIsPSTNEnabled(prev => !prev);
  };

  const updateDTGNState = async () => {
    if (isDTGNEnabled) {
      await NotificationModule.stopTotalGoalNotification();
    } else {
      await NotificationModule.startTotalGoalNotification();
    }
    setIsDTGNEnabled(prev => !prev);
  };

  const updateDAGNState = async () => {
    if (isDAGNEnabled) {
      await NotificationModule.stopAppGoalNotification();
    } else {
      await NotificationModule.startAppGoalNotification();
    }
    setIsDAGNEnabled(prev => !prev);
  };

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  const notificationsList = [
    {
      name: 'persistent screen time',
      description:
        "Persistent screen time notification requires this app's notification to be enabled in the settings and background usage setting enabled to function properly. Enabling this notification can effect battery consumption. This is a persistent notification which displays self-updating current app's screen time.",
      value: isPSTNEnabled,
      onChange: updatePSTNState,
      disabled: !isNotificationsEnabled,
    },
    {
      name: 'daily total goal',
      description:
        'Daily total goal notification is shown whenever the total screen time of the device reaches moderate and bad usage. This notification requires the persistent screen time notification to be enabled.',
      value: isDTGNEnabled,
      onChange: updateDTGNState,
      disabled: !isPSTNEnabled,
    },
    {
      name: 'daily app goal',
      description:
        'Daily app goal notification is shown whenever the total screen time of the current app reaches moderate and bad usage. This notification requires the persistent screen time notification to be enabled.',
      value: isDAGNEnabled,
      onChange: updateDAGNState,
      disabled: !isPSTNEnabled,
    },
  ];

  return (
    <View style={[theme.basic, styles.notifications]}>
      <NavigationSection
        navigation={navigation}
        screens={[
          {
            name: 'Back',
            icon: 'humbleicons:chevron-left',
            color: 'blue',
          },
          {
            name: 'HomeScreen',
            icon: 'mdi:home',
            color: 'orange',
          },
        ]}
      />
      <Text style={[styles.heading, , theme.basic]}>Notifications</Text>
      <ScrollView contentContainerStyle={styles.notificationsList}>
        <Pressable
          onPress={openNotificationSettings}
          style={[
            styles.notificationsItem,
            theme.basic,
            {borderColor: theme.basic.color},
          ]}>
          <View style={styles.notificationsSetting}>
            <Text style={[styles.notificationsName, theme.basic]}>
              Open app notification settings
            </Text>
          </View>
          <Text style={[theme.basic, styles.notificationsDescription]}>
            Adjust the app's notification settings here.
          </Text>
        </Pressable>
        {notificationsList.map((notification, idx) => (
          <View
            style={[
              styles.notificationsItem,
              theme.basic,
              {borderTopWidth: 0, borderColor: theme.basic.color},
            ]}
            key={`notification-${idx}`}>
            <View style={styles.notificationsSetting}>
              <Text
                style={[
                  styles.notificationsName,
                  theme.basic,
                  {
                    flex: 0.7,
                    color: notification.disabled ? 'gray' : theme.basic.color,
                  },
                ]}>
                Enable {notification.name} notification
              </Text>
              <Switch
                trackColor={{
                  false: notification.disabled ? 'gray' : 'red',
                  true: 'limegreen',
                }}
                thumbColor={'lightgray'}
                ios_backgroundColor="red"
                onChange={notification.onChange}
                value={notification.value}
                disabled={notification.disabled}
                style={{flex: 0.3, transform: [{scale: 1.2}]}}
              />
            </View>
            <Text style={[theme.basic, styles.notificationsDescription]}>
              {notification.description}
            </Text>
          </View>
        ))}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  notifications: {
    paddingVertical: '2%',
    flex: 1,
  },
  heading: {
    marginVertical: 10,
    fontSize: 35,
    fontWeight: '700',
    textAlign: 'center',
  },
  notificationsList: {
    marginVertical: 10,
    width: '100%',
  },
  notificationsItem: {
    padding: '4%',
    borderTopWidth: 1,
    borderBottomWidth: 1,
  },
  notificationsSetting: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  notificationsName: {
    fontSize: 20,
  },
  notificationsDescription: {
    marginVertical: '1%',
    color: 'gray',
    fontStyle: 'italic',
    fontSize: 15,
  },
});

export default Notifications;
