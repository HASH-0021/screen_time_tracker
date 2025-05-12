/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useState, useEffect, useCallback} from 'react';

import {
  StatusBar,
  StyleSheet,
  useColorScheme,
  View,
  Platform,
} from 'react-native';

import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {Provider} from 'react-redux';

import store from './redux/store';
import HomeScreen from './components/HomeScreen';
import Settings from './components/Settings';
import Notifications from './components/Notifications';
import Goals from './components/Goals';
import Themes from './components/Themes';
import {themes} from './helpers/colors';
import ImportExport from './components/ImportExport';
import Synchronization from './components/Synchronization';
import PeriodicStats from './components/PeriodicStats';

const Stack = createNativeStackNavigator();

const App: React.FC = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const theme = isDarkMode ? themes.dark : themes.light;

  return (
    <Provider store={store}>
      <View style={[theme.basic, styles.app]}>
        <StatusBar {...theme.status} />
        <NavigationContainer>
          <Stack.Navigator initialRouteName="HomeScreen">
            <Stack.Screen
              name="HomeScreen"
              component={HomeScreen}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="PeriodicStats"
              component={PeriodicStats}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="Settings"
              component={Settings}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="Notifications"
              component={Notifications}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="Goals"
              component={Goals}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="ImportExport"
              component={ImportExport}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="Synchronization"
              component={Synchronization}
              options={{headerShown: false}}
            />
            <Stack.Screen
              name="Themes"
              component={Themes}
              options={{headerShown: false}}
            />
          </Stack.Navigator>
        </NavigationContainer>
      </View>
    </Provider>
  );
};

const styles = StyleSheet.create({
  app: {
    ...Platform.select({
      android: {
        paddingTop: Number(Platform.Version) > 34 ? StatusBar.currentHeight : 0,
        flex: 1,
      },
    }),
  },
});

export default App;
