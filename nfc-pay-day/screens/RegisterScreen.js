import {useContext, useState} from "react";
import {AuthContext} from "../state/AuthProvider";
import {SafeAreaView, TouchableOpacity, View, Text} from "react-native";
import {AppHeader} from "../components/AppHeader";
import {CustomInput} from "../components/CustomInput";
import {CustomButton} from "../components/CustomButton";
import {MessageModal} from "../components/ModalMessage";

export const RegisterScreen = ({ navigation }) => {
    const { register, loading } = useContext(AuthContext);
    const [username, setUsername] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    // Removed modal states from here as they are now handled by AuthProvider

    const handleRegister = async () => {
        const success = await register(email, password);
        if (success) {
            navigation.navigate('Login'); // Navigate back to login after successful registration
        }
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Register" showBack onBackPress={() => navigation.goBack()} />
            <View style={styles.screenContainer}>
                <Text style={styles.title}>Create Your Account</Text>
                <CustomInput label="Email" value={email} onChangeText={setEmail} placeholder="Enter your email" keyboardType="email-address" />
                <CustomInput label="Password" value={password} onChangeText={setPassword} placeholder="Choose a password" secureTextEntry />
                <CustomButton
                    title={loading ? "Registering..." : "Register"}
                    onPress={handleRegister}
                    disabled={loading}
                />
                <TouchableOpacity onClick={() => navigation.navigate('Login')}>
                    <Text style={styles.linkText}>Already have an account? Login here.</Text>
                </TouchableOpacity>
            </View>
            {/* MessageModal is now handled by AuthProvider */}
        </SafeAreaView>
    );
};

const styles = {
    safeArea: {
        flex: 1,
        backgroundColor: '#f3f4f6', // Tailwind bg-gray-100
    },
    screenContainer: {
        flex: 1,
        padding: 20,
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#1f2937', // Tailwind text-gray-900
        marginBottom: 20,
        textAlign: 'center',
        // fontFamily: 'Inter_700Bold',
    },
    linkText: {
        color: '#4F46E5', // Tailwind text-indigo-600
        textAlign: 'center',
        marginTop: 20,
        fontSize: 16,
        // fontFamily: 'Inter_500Medium',
    }
}