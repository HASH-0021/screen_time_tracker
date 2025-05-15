import {Pressable, ScrollView, StyleSheet, Text, View} from 'react-native';
import {useSelector} from 'react-redux';
import Monicon from '@monicon/native';

import NavigationSection from '../sub_components/NavigationSection';
import {themes} from '../helpers/colors';

interface SettingsListProps {
  name: string;
  screen: string;
}

const Settings: React.FC<any> = ({navigation}) => {
  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  const settingsList: SettingsListProps[] = [
    {
      name: 'Notifications',
      screen: 'Notifications',
    },
    {
      name: 'Import / Export',
      screen: 'ImportExport',
    },
    {
      name: 'Synchronization',
      screen: 'Synchronization',
    },
    {
      name: 'Themes',
      screen: 'Themes',
    },
  ];

  return (
    <View style={[theme.basic, styles.settings]}>
      <NavigationSection
        navigation={navigation}
        screens={[
          {
            name: 'PeriodicStats',
            icon: 'lucide:bar-chart-3',
            color: 'orange',
          },
          {
            name: 'HomeScreen',
            icon: 'mdi:home',
            color: 'orange',
          },
        ]}
      />
      <Text style={[styles.heading, theme.basic]}>Settings</Text>
      <ScrollView contentContainerStyle={styles.settingsList}>
        {settingsList.map((setting, idx) => (
          <Pressable
            style={[
              styles.settingsItem,
              theme.basic,
              {borderTopWidth: idx ? 0 : 1, borderColor: theme.basic.color},
            ]}
            onPress={() => {
              navigation.navigate(setting.screen);
            }}
            android_ripple={{
              color: '#ddd5',
              borderless: false,
              radius: 400,
              foreground: true,
            }}
            key={`setting-${idx}`}>
            {({pressed}) => (
              <>
                <Text style={[styles.settingsName, theme.basic]}>
                  {setting.name}
                </Text>
                <Monicon
                  name="humbleicons:chevron-right"
                  size={20}
                  color={pressed ? 'limegreen' : theme.basic.color}
                />
              </>
            )}
          </Pressable>
        ))}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  settings: {
    paddingVertical: '2%',
    flex: 1,
  },
  heading: {
    marginVertical: 10,
    fontSize: 40,
    fontWeight: '700',
    textAlign: 'center',
  },
  settingsList: {
    marginVertical: 10,
    width: '100%',
  },
  settingsItem: {
    padding: '4%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderBottomWidth: 1,
  },
  settingsName: {
    fontSize: 25,
  },
});

export default Settings;
