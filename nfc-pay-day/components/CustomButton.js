import {TouchableOpacity, Text} from "react-native";

export const CustomButton = ({ title, onPress, disabled = false, style, textStyle }) => (
    <TouchableOpacity
        style={{...styles.button, ...style}}
        onPress={onPress}
        disabled={disabled}
    >
        <Text style={{...styles.buttonText, ...textStyle}}>{title}</Text>
    </TouchableOpacity>
);

const styles = {
    button: {
        backgroundColor: '#6366f1', // Tailwind bg-indigo-500
        paddingVertical: 14,
        paddingHorizontal: 20,
        borderRadius: 10, // Tailwind rounded-xl
        alignItems: 'center',
        marginTop: 15,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.1,
        shadowRadius: 6,
        elevation: 8,
        width: '100%', // Ensure button takes full width
    },
    buttonText: {
        color: 'white',
        fontSize: 18,
        fontWeight: '600',
        // fontFamily: 'Inter_600SemiBold',
    }
}