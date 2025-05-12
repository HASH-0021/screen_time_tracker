import {createSlice} from '@reduxjs/toolkit';

const settingsSlice = createSlice({
  name: 'settings',
  initialState: {},
  reducers: {
    updateSettingsData: (state, action) => {
      return action.payload;
    },
  },
});

export const {updateSettingsData} = settingsSlice.actions;
export default settingsSlice.reducer;
