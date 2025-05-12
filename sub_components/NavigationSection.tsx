import {Pressable, StyleSheet, View} from 'react-native';
import {useSelector} from 'react-redux';
import Monicon from '@monicon/native';

import {themes} from '../helpers/colors';

interface ScreenProps {
  name: string;
  icon: string;
  color: string;
}

interface NavigationSectionProps {
  navigation: any;
  screens: ScreenProps[];
}

const NavigationSection: React.FC<NavigationSectionProps> = ({
  navigation,
  screens,
}) => {
  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const isDarkMode: boolean =
    settingsData.theme === 'automatic'
      ? colorScheme === 'dark'
      : settingsData.theme === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  return (
    <View style={styles.nav}>
      {screens.map((screen, idx) => (
        <Pressable
          key={`icon-${idx}`}
          onPress={() => {
            if (screen.name === 'HomeScreen') {
              navigation.popToTop();
            } else if (
              screen.name === 'PeriodicStats' ||
              screen.name === 'Settings'
            ) {
              navigation.popToTop();
              navigation.navigate(screen.name);
            } else if (screen.name === 'Back') {
              navigation.goBack();
            } else {
              navigation.navigate(screen.name);
            }
          }}>
          {({pressed}) => (
            <Monicon
              name={screen.icon}
              size={25}
              color={pressed ? screen.color : theme.basic.color}
            />
          )}
        </Pressable>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  nav: {
    padding: '4%',
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
});

export default NavigationSection;
