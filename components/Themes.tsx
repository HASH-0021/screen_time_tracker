import {
  Alert,
  NativeModules,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {useDispatch, useSelector} from 'react-redux';

import {updateSettingsData} from '../redux/features/settingsSlice';
import NavigationSection from '../sub_components/NavigationSection';
import {themes} from '../helpers/colors';

const Themes: React.FC<any> = ({navigation}) => {
  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);
  const dispatch = useDispatch();

  const {DataHandlerModule} = NativeModules;

  const updateTheme = async (themeName: string) => {
    try {
      const settings = await DataHandlerModule.setTheme(themeName);
      dispatch(updateSettingsData(settings));
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
    <View style={[theme.basic, styles.themes]}>
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
      <Text style={[styles.heading, theme.basic]}>Themes</Text>
      <ScrollView contentContainerStyle={styles.themesList}>
        <Pressable
          onPress={() => updateTheme('automatic')}
          android_ripple={{
            color: '#ddd5',
            borderless: false,
            radius: 400,
            foreground: true,
          }}
          style={[styles.themesOption, {borderColor: theme.basic.color}]}>
          <Text style={[styles.themesName, theme.basic]}>Automatic</Text>
        </Pressable>
        <Pressable
          onPress={() => updateTheme('dark')}
          android_ripple={{
            color: '#ddd5',
            borderless: false,
            radius: 400,
            foreground: true,
          }}
          style={[
            styles.themesOption,
            {borderTopWidth: 0, borderColor: theme.basic.color},
          ]}>
          <Text style={[styles.themesName, theme.basic]}>Dark</Text>
        </Pressable>
        <Pressable
          onPress={() => updateTheme('light')}
          android_ripple={{
            color: '#ddd5',
            borderless: false,
            radius: 400,
            foreground: true,
          }}
          style={[
            styles.themesOption,
            {borderTopWidth: 0, borderColor: theme.basic.color},
          ]}>
          <Text style={[styles.themesName, theme.basic]}>Light</Text>
        </Pressable>
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  themes: {
    paddingVertical: '2%',
    flex: 1,
  },
  heading: {
    marginVertical: 10,
    fontSize: 35,
    fontWeight: '700',
    textAlign: 'center',
  },
  themesList: {
    marginVertical: 10,
    width: '100%',
  },
  themesOption: {
    padding: '4%',
    borderTopWidth: 1,
    borderBottomWidth: 1,
  },
  themesName: {
    fontSize: 20,
  },
});

export default Themes;
