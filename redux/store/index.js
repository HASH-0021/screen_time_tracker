import {configureStore} from '@reduxjs/toolkit';
import settingsReducer from '../features/settingsSlice';
import colorSchemeReducer from '../features/colorSchemeSlice';

const store = configureStore({
  reducer: {
    settings: settingsReducer,
    colorScheme: colorSchemeReducer,
  },
});

export default store;
