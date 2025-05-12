import {StatusBarProps} from 'react-native';

interface ColorProps {
  backgroundColor: string;
  color: string;
  shadowColor: string;
}

interface SectionProps {
  basic: ColorProps;
  status: StatusBarProps;
}

interface ThemeProps {
  light: SectionProps;
  dark: SectionProps;
}

const themes: ThemeProps = {
  light: {
    basic: {
      backgroundColor: 'white',
      color: 'black',
      shadowColor: 'black',
    },
    status: {
      backgroundColor: 'whitesmoke',
      barStyle: 'dark-content',
    },
  },
  dark: {
    basic: {
      backgroundColor: 'black',
      color: 'white',
      shadowColor: 'white',
    },
    status: {
      backgroundColor: 'darkgray',
      barStyle: 'dark-content',
    },
  },
};

export {themes};
