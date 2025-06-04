import {TextInput, View, Text} from "react-native";

export const CustomInput = ({ label, value, onChangeText, placeholder, secureTextEntry = false, keyboardType = 'default' }) => (
    <View style={styles.inputContainer}>
        <Text style={styles.inputLabel}>{label}</Text>
        <TextInput
            style={styles.input}
            value={value}
            onChangeText={onChangeText}
            placeholder={placeholder}
            type={keyboardType === 'numeric' ? 'number' : 'text'} // 'type' for web inputs
            secureTextEntry={secureTextEntry}
            placeholderTextColor="#9ca3af"
        />
    </View>
);

const styles = {
    inputContainer: {
        marginBottom: 15,
    },
    inputLabel: {
        fontSize: 16,
        color: '#374151', // Tailwind text-gray-700
        marginBottom: 5,
        // fontFamily: 'Inter_500Medium',
    },
    input: {
        backgroundColor: 'white',
        borderWidth: 1,
        borderColor: '#d1d5db', // Tailwind border-gray-300
        borderRadius: 8, // Tailwind rounded-lg
        padding: 12,
        fontSize: 16,
        color: '#1f2937',
        // fontFamily: 'Inter_400Regular',
        width: '100%', // Ensure input takes full width
        boxSizing: 'border-box', // Include padding in width
    }
}
