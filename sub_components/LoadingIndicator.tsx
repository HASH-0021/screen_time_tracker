import {ActivityIndicator, Modal, StyleSheet, Text, View} from 'react-native';

interface LoadingIndicatorProps {
  isLoading: boolean;
  setIsLoading: Function;
  indicatorColor: string;
  loadingText: string;
}

const LoadingIndicator: React.FC<LoadingIndicatorProps> = ({
  isLoading,
  setIsLoading,
  indicatorColor,
  loadingText,
}) => {
  return (
    <Modal
      visible={isLoading}
      transparent={true}
      animationType="fade"
      onRequestClose={() => {
        // This is required for Android hardware back button
        setIsLoading(false);
      }}>
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          <ActivityIndicator size="large" color={indicatorColor} />
          <Text style={styles.loadingText}>{loadingText}...</Text>
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
  modalContent: {
    backgroundColor: 'white',
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 10,
    fontSize: 16,
  },
});

export default LoadingIndicator;
