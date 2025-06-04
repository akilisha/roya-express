import {ActivityIndicator, SafeAreaView, View, Text} from "react-native";
import {useContext, useState} from "react";
import {AuthContext} from "../state/AuthProvider";
import {RegisterScreen} from "../screens/RegisterScreen";
import {LoginScreen} from "../screens/LoginScreen";
import {PaymentContext} from "../state/PaymentProvider";
import {AppHeader} from "./AppHeader";
import {CustomButton} from "./CustomButton";
import {PaymentScreen} from "../screens/PaymentScreen";
import {PaymentHistoryScreen} from "../screens/PaymentHistoryScreen";
import {SettingsScreen} from "../screens/SettingsScreen";

export const AppNavigator = () => {
    const { user, loading } = useContext(AuthContext);
    const [currentScreen, setCurrentScreen] = useState('Login'); // Default to Login

    if (loading) {
        return (
            <View style={styles.loadingContainer}>
                <ActivityIndicator size="large" color="#4F46E5" />
                <Text style={styles.loadingText}>Loading...</Text>
            </View>
        );
    }

    const navigate = (screenName) => setCurrentScreen(screenName);
    const goBack = () => {
        if (currentScreen === 'Register') setCurrentScreen('Login');
        if (currentScreen === 'Payment') setCurrentScreen('Home');
        if (currentScreen === 'History') setCurrentScreen('Home');
        if (currentScreen === 'Settings') setCurrentScreen('Home');
    };

    if (!user) {
        switch (currentScreen) {
            case 'Register':
                return <RegisterScreen navigation={{ navigate, goBack }} />;
            default:
                return <LoginScreen navigation={{ navigate, goBack }} />;
        }
    }

    // Authenticated user's main app
    const Home = () => {
        const { logout } = useContext(AuthContext);
        const { securitySettings } = useContext(PaymentContext);

        return (
            <SafeAreaView style={styles.safeArea}>
                <AppHeader title={`Welcome, ${user.username}!`} />
                <View style={styles.screenContainer}>
                    <Text style={styles.title}>Dashboard</Text>
                    <View style={styles.dashboardCard}>
                        <Text style={styles.dashboardCardTitle}>Daily Spending</Text>
                        <Text style={styles.dashboardCardText}>
                            ${securitySettings.dailyPaymentCount.toFixed(2)} / ${securitySettings.dailyPaymentCap.toFixed(2)} spent today
                        </Text>
                    </View>

                    <CustomButton title="Make New Payment" onPress={() => navigate('Payment')} />
                    <CustomButton title="View Payment History" onPress={() => navigate('History')} />
                    <CustomButton title="Security Settings" onPress={() => navigate('Settings')} />
                    <CustomButton title="Logout" onPress={logout} style={styles.logoutButton} textStyle={styles.logoutButtonText} />
                </View>
            </SafeAreaView>
        );
    };

    switch (currentScreen) {
        case 'Payment':
            return <PaymentScreen navigation={{ navigate, goBack }} />;
        case 'History':
            return <PaymentHistoryScreen navigation={{ navigate, goBack }} />;
        case 'Settings':
            return <SettingsScreen navigation={{ navigate, goBack }} />;
        default:
            return <Home />;
    }
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
    dashboardCard: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 10,
        color: '#1f2937',
        // fontFamily: 'Inter_600SemiBold',
    },
    dashboardCardTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        marginBottom: 10,
        color: '#1f2937',
        // fontFamily: 'Inter_600SemiBold',
    },
    dashboardCardText: {
        fontSize: 18,
        color: '#374151',
        // fontFamily: 'Inter_500Medium',
    },
    loadingContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#f3f4f6',
    },
    loadingText: {
        marginTop: 10,
        fontSize: 18,
        color: '#4F46E5',
        // fontFamily: 'Inter_400Regular', // Assuming Inter font is loaded
    },
    logoutButton: {
        backgroundColor: '#ef4444', // Tailwind bg-red-500
        marginTop: 30,
    },
    logoutButtonText: {
        fontWeight: 'bold',
    }
}