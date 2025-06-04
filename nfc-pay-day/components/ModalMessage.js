import {Modal, View, Text} from "react-native";
import {CustomButton} from "../components/CustomButton";

export const MessageModal = ({ visible, message, onClose }) => (
    <Modal
        visible={visible}
    >
        <View style={styles.modalBackground}>
            <View style={styles.modalContainer}>
                <Text style={styles.modalMessage}>{message}</Text>
                <CustomButton title="OK" onPress={onClose} />
            </View>
        </View>
    </Modal>
);

const styles = {
    modalBackground: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
    },
    modalContainer: {
        backgroundColor: 'white',
        borderRadius: 15,
        padding: 25,
        alignItems: 'center',
        boxShadow: '0 6px 10px rgba(0,0,0,0.3)', // Web equivalent of shadow
        width: '80%',
        maxWidth: '400px', // Limit width on larger screens
    },
    modalMessage: {

    }
}