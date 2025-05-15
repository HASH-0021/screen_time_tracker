import {useEffect, useState} from 'react';
import {
  Alert,
  NativeModules,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {useSelector} from 'react-redux';
import Monicon from '@monicon/native';

import NavigationSection from '../sub_components/NavigationSection';
import InfoDialogBox from '../sub_components/InfoDialogBox';
import {themes} from '../helpers/colors';
import {convertToTimeStamp} from '../helpers/tools';

const Synchronization: React.FC<any> = ({navigation}) => {
  const [syncTime, setSyncTime] = useState(0);
  const [isInfoVisible, setIsInfoVisible] = useState(false);

  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const {SynchronizationModule} = NativeModules;

  useEffect(() => {
    updateLastSyncTime();
  });

  const updateLastSyncTime = async () => {
    try {
      const lastSyncTime = await SynchronizationModule.getLastSyncTime();
      setSyncTime(Number(lastSyncTime));
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const synchronizeData = async () => {
    try {
      await SynchronizationModule.synchronizeData();
      updateLastSyncTime();
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
    <View style={[theme.basic, styles.synchronization]}>
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
      <Text style={[styles.heading, theme.basic]}>Synchronization</Text>
      <ScrollView contentContainerStyle={styles.syncList}>
        <View style={styles.syncItem}>
          <Text style={[styles.syncName, theme.basic]}>App Sync</Text>
          <View style={styles.syncSection}>
            <Text style={[styles.syncText, theme.basic]}>
              Last synchronization time : {convertToTimeStamp(syncTime)}
            </Text>
            <Pressable onPress={synchronizeData}>
              {({pressed}) => (
                <Monicon
                  name="ic:baseline-sync"
                  size={20}
                  color={pressed ? 'limegreen' : theme.basic.color}
                />
              )}
            </Pressable>
          </View>
          <Text style={[theme.basic, styles.syncDescription]}>
            App data can be synchronized at any time by pressing the button.
          </Text>
        </View>
      </ScrollView>
      <InfoDialogBox isVisible={isInfoVisible} setIsVisible={setIsInfoVisible}>
        <Text style={styles.infoDescription}>
          App sync will be done automatically everyday at mignight (12:00 AM).
          This daily app sync cannot be disabled and is helpful for the app's
          functionality.
        </Text>
      </InfoDialogBox>
    </View>
  );
};

const styles = StyleSheet.create({
  synchronization: {
    paddingVertical: '2%',
    flex: 1,
  },
  heading: {
    marginVertical: 10,
    fontSize: 35,
    fontWeight: '700',
    textAlign: 'center',
  },
  syncList: {
    marginVertical: 10,
    width: '100%',
  },
  syncItem: {
    padding: '4%',
  },
  syncName: {
    marginVertical: 5,
    fontSize: 20,
  },
  syncSection: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  syncText: {
    flex: 0.9,
    fontSize: 14,
  },
  syncDescription: {
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

export default Synchronization;
