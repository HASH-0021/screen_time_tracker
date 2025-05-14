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
          <ScrollView contentContainerStyle={styles.modalContent}>
            <Text style={styles.infoHeading}>Info</Text>
            {children}
            <Button title="Ok" onPress={() => setIsVisible(false)} />
          </ScrollView>
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
    width: '80%',
    maxHeight: '60%',
  },
  modalContent: {
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
    backgroundColor: 'lightgray',
  },
  infoHeading: {
    margin: 5,
    fontSize: 30,
    fontWeight: '500',
    color: 'black',
    textAlign: 'center',
  },
});

export default InfoDialogBox;
