import {Button, Modal, ScrollView, StyleSheet, Text, View} from 'react-native';

interface InfoDialogBoxProps {
  children: React.ReactNode;
  isVisible: boolean;
  setIsVisible: Function;
}

const InfoDialogBox: React.FC<InfoDialogBoxProps> = ({
  children,
  isVisible,
  setIsVisible,
}) => {
  return (
    <Modal
      visible={isVisible}
      transparent={true}
      animationType="fade"
      onRequestClose={() => {
        // This is required for Android hardware back button
        setIsVisible(false);
      }}>
      <View style={styles.modalOverlay}>
        <View style={styles.modalContentContainer}>
          <Text style={styles.infoHeading}>Info</Text>
          <View style={styles.infoContentContainer}>
            <ScrollView style={styles.infoContent}>
              {children}
            </ScrollView>
          </View>
          <Button title="Ok" onPress={() => setIsVisible(false)} />
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)', // Grey out the background
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContentContainer: {
    width: '85%',
    maxHeight: '60%',
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 15,
    alignItems: 'center',
    backgroundColor: 'lightgray',
  },
  infoHeading: {
    fontSize: 30,
    fontWeight: '500',
    color: 'black',
    textAlign: 'center',
  },
  infoContentContainer: {
    marginVertical: 10,
    paddingVertical: 2,
    paddingHorizontal: 10,
    borderWidth: 1,
    borderColor: "black",
    borderRadius: 10
  },
  infoContent: {
    flexGrow: 0,
    maxHeight: 350,
  }
});

export default InfoDialogBox;
