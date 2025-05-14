import {useState} from 'react';
import {
  Alert,
  Button,
  NativeModules,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import {useSelector} from 'react-redux';
import {pick, pickDirectory} from '@react-native-documents/picker';

import NavigationSection from '../sub_components/NavigationSection';
import InfoDialogBox from '../sub_components/InfoDialogBox';
import LoadingIndicator from '../sub_components/LoadingIndicator';
import {themes} from '../helpers/colors';
import {decodeEncodedURI} from '../helpers/tools';

const ImportExport: React.FC<any> = ({navigation}) => {
  const [exportUri, setExportUri] = useState('Select Folder...');
  const [importUri, setImportUri] = useState('Select File...');
  const [importType, setImportType] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [indicatorColor, setIndicatorColor] = useState('#0000ff');
  const [isInfoVisible, setIsInfoVisible] = useState(false);
  const [loadingText, setLoadingText] = useState('Loading');

  const settingsData = useSelector((state: any) => state.settings);
  const colorScheme = useSelector((state: any) => state.colorScheme);

  const {DataHandlerModule} = NativeModules;

  const updateExportUri = async () => {
    try {
      const {uri} = await pickDirectory({
        requestLongTermAccess: false,
      });
      setExportUri(uri);
    } catch (err) {
      console.error(err);
    }
  };

  const exportToFile = async () => {
    setIsLoading(true);
    setLoadingText('Exporting');
    setIndicatorColor('#00ff00');
    try {
      await DataHandlerModule.exportToPublicFile(exportUri);
      await new Promise(resolve =>
        setTimeout(() => {
          resolve('Done');
        }, 1000),
      );
      setIsLoading(false);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    }
  };

  const updateImportUri = async () => {
    try {
      const [result] = await pick({
        mode: 'open',
      });
      const {uri, type} = result;
      setImportType(type ?? '');
      setImportUri(uri);
    } catch (err) {
      console.error(err);
    }
  };

  const importFromFile = async () => {
    setIsLoading(true);
    setLoadingText('Importing');
    setIndicatorColor('#00ff00');
    try {
      const data = await DataHandlerModule.importFromPublicFile(importUri);
      await new Promise(resolve =>
        setTimeout(() => {
          resolve('Done');
        }, 1000),
      );
      setIsLoading(false);
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
    <View style={[theme.basic, styles.importExport]}>
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
      <Text style={[styles.heading, theme.basic]}>Import / Export</Text>
      <ScrollView contentContainerStyle={styles.importExportList}>
        <View style={[styles.modeItem, {borderColor: theme.basic.color}]}>
          <Text style={[styles.modeName, theme.basic]}>Import</Text>
          <View style={styles.input}>
            <View style={[styles.pathField, {borderColor: theme.basic.color}]}>
              <Text style={styles.pathText}>
                {importUri === 'Select File...'
                  ? importUri
                  : decodeEncodedURI(importUri)}
              </Text>
            </View>
            <View style={[styles.buttonContainer, {flex: 0.3}]}>
              <Button title="Browse" onPress={updateImportUri} />
            </View>
          </View>
          <View style={styles.buttonContainer}>
            <Button
              title="Import"
              onPress={importFromFile}
              disabled={
                importUri === 'Select File...' ||
                importType !== 'application/json'
              }
              color={'limegreen'}
            />
          </View>
          <Text style={[theme.basic, styles.importExportDescription]}>
            This option imports files that were exported using this app.
          </Text>
        </View>
        <View
          style={[
            styles.modeItem,
            {borderTopWidth: 0, borderColor: theme.basic.color},
          ]}>
          <Text style={[styles.modeName, theme.basic]}>Export</Text>
          <View style={styles.input}>
            <View style={[styles.pathField, {borderColor: theme.basic.color}]}>
              <Text style={styles.pathText}>
                {exportUri === 'Select Folder...'
                  ? exportUri
                  : decodeEncodedURI(exportUri)}
              </Text>
            </View>
            <View style={[styles.buttonContainer, {flex: 0.3}]}>
              <Button title="Browse" onPress={updateExportUri} />
            </View>
          </View>
          <View style={styles.buttonContainer}>
            <Button
              title="Export"
              onPress={exportToFile}
              disabled={exportUri === 'Select Folder...'}
              color={'limegreen'}
            />
          </View>
          <Text style={[theme.basic, styles.importExportDescription]}>
            This option exports app data to a file named
            "screen_time_tracker_data.json".
          </Text>
        </View>
      </ScrollView>
      <InfoDialogBox isVisible={isInfoVisible} setIsVisible={setIsInfoVisible}>
        <Text style={styles.infoDescription}>
          While importing, the selected file shouldn't be corrupted, otherwise
          the import will not happen. Importing{' '}
          <Text style={{color: 'red'}}>overwrites</Text> the existing app data
          with new data from selected file, so beware before importing any
          modified files.
        </Text>
        <Text style={styles.infoDescription}>
          While exporting, if the file with the same name exists in the selected
          folder, then exporting <Text style={{color: 'red'}}>overwrites</Text>{' '}
          that file (not append to that file). If no file is present with same
          name in the selected folder, then a new file will be created.
        </Text>
      </InfoDialogBox>
      <LoadingIndicator
        isLoading={isLoading}
        setIsLoading={setIsLoading}
        indicatorColor={indicatorColor}
        loadingText={loadingText}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  importExport: {
    paddingVertical: '2%',
    flex: 1,
  },
  heading: {
    marginVertical: 10,
    fontSize: 35,
    fontWeight: '700',
    textAlign: 'center',
  },
  importExportList: {
    marginVertical: 10,
    width: '100%',
    alignItems: 'flex-start',
  },
  modeItem: {
    padding: '4%',
    borderTopWidth: 1,
    borderBottomWidth: 1,
  },
  modeName: {
    marginVertical: 5,
    fontSize: 20,
  },
  input: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
  },
  pathField: {
    margin: 5,
    padding: 5,
    borderWidth: 2,
    flex: 0.7,
  },
  pathText: {
    color: 'gray',
    fontSize: 14,
  },
  buttonContainer: {
    margin: 5,
  },
  importExportDescription: {
    marginVertical: '1%',
    color: 'gray',
    fontStyle: 'italic',
    fontSize: 15,
  },
  infoDescription: {
    marginVertical: 5,
    fontSize: 18,
    textAlign: 'left',
    color: 'black',
  },
});

export default ImportExport;
