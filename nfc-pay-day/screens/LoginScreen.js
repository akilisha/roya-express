import {SafeAreaView, TouchableOpacity, View, Text} from "react-native";
import {useContext, useState} from "react";
import {AuthContext} from "../state/AuthProvider";
import {AppHeader} from "../components/AppHeader";
import {CustomInput} from "../components/CustomInput";
import {CustomButton} from "../components/CustomButton";

export const LoginScreen = ({ navigation }) => {
    const { login, loading, loginWithOAuth } = useContext(AuthContext);
    const [email, setEmail] = useState('zes.ty@aol.com');
    const [password, setPassword] = useState('Stay0ut1');
    // Removed modal states from here as they are now handled by AuthProvider

    const handleLogin = async () => {
        await login(email, password);
    };

    const handleGoogleLogin = async () => {
        await loginWithOAuth('Google');
    };

    const handleAuth0Login = async () => {
        await loginWithOAuth('Auth0');
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Login" />
            <View style={styles.screenContainer}>
                <Text style={styles.title}>Welcome Back!</Text>

                <CustomInput label="Email address" value={email} onChangeText={setEmail} placeholder="Enter your email address" />
                <CustomInput label="Password" value={password} onChangeText={setPassword} placeholder="Enter your password" secureTextEntry />
                <CustomButton
                    title={loading ? "Logging In..." : "Login"}
                    onPress={handleLogin}
                    disabled={loading}
                />

                <View style={styles.oauthDivider}>
                    <View style={styles.dividerLine} />
                    <Text style={styles.dividerText}>OR</Text>
                    <View style={styles.dividerLine} />
                </View>

                <CustomButton
                    title={loading ? "Logging in with Google..." : "Login with Google"}
                    onPress={handleGoogleLogin}
                    disabled={loading}
                    style={styles.googleButton}
                />
                <CustomButton
                    title={loading ? "Logging in with Auth0..." : "Login with Auth0"}
                    onPress={handleAuth0Login}
                    disabled={loading}
                    style={styles.auth0Button}
                />

                <TouchableOpacity onPress={() => navigation.navigate('Register')}>
                    <Text style={styles.linkText}>Don't have an account? Register here.</Text>
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
    },
    oauthDivider: {
        flexDirection: 'row',
        alignItems: 'center',
        marginVertical: 20,
    },
    dividerLine: {
        flex: 1,
        height: 1,
        backgroundColor: '#d1d5db', // Tailwind gray-300
    },
    dividerText: {
        marginHorizontal: 10,
        color: '#6b7280', // Tailwind gray-500
        fontSize: 16,
        // fontFamily: 'Inter_400Regular',
    },
    googleButton: {
        backgroundColor: '#db4437', // Google Red
    },
    auth0Button: {
        backgroundColor: '#eb5424', // Auth0 Orange
    },
}