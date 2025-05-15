const {getDefaultConfig, mergeConfig} = require('@react-native/metro-config');
const {withMonicon} = require('@monicon/metro');

/**
 * Metro configuration
 * https://reactnative.dev/docs/metro
 *
 * @type {import('@react-native/metro-config').MetroConfig}
 */
const config = {};

const configWithMonicon = withMonicon(getDefaultConfig(__dirname), {
  icons: [
    'mdi:home',
    'lucide:bar-chart-3',
    'ion:settings-sharp',
    'humbleicons:chevron-left',
    'humbleicons:chevron-right',
    'ic:baseline-sync',
    'teenyicons:tick-circle-outline',
    'tabler:info-circle',
    'lucide:goal',
  ],
  // Load all icons from the listed collections
  collections: [],
});

module.exports = mergeConfig(
  getDefaultConfig(__dirname),
  configWithMonicon,
  config,
);
