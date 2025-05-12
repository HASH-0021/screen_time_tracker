import {createSlice} from '@reduxjs/toolkit';

const colorSchemeSlice = createSlice({
  name: 'colorScheme',
  initialState: 'automatic',
  reducers: {
    updateColorSchemeValue: (state, action) => {
      return action.payload;
    },
  },
});

export const {updateColorSchemeValue} = colorSchemeSlice.actions;
export default colorSchemeSlice.reducer;
