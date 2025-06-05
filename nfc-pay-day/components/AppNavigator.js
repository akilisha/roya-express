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
import {CameraScreen} from "../screens/CameraScreen";
import {PaymentResultScreen} from "../screens/PaymentResultScreen";
import {AddPaymentMethodScreen} from "../screens/AddPaymentMethodScreen";
import {CheckoutScreen} from "../screens/CheckoutScreen";
// import {AdyenSessionComponent} from "../payments/AdyenSessionComponent";

export const AppNavigator = () => {
    const { user, loading } = useContext(AuthContext);
    const [currentScreen, setCurrentScreen] = useState({name: 'Login', params: {}}); // Default to Login

    if (loading) {
        return (
            <View style={styles.loadingContainer}>
                <ActivityIndicator size="large" color="#4F46E5" />
                <Text style={styles.loadingText}>Loading...</Text>
            </View>
        );
    }

    const navigate = (screenName, params = {}) => setCurrentScreen(curr => ({...curr, name: screenName, params }));

    const goBack = () => {
        if (currentScreen.name === 'Register') { setCurrentScreen({ name: 'Login' }); }
        else if (currentScreen === 'Camera') { setCurrentScreen('Payment'); }
        else if (currentScreen.name === 'Payment' ||
            currentScreen.name === 'AddPaymentMethod' ||
            currentScreen.name === 'Checkout' ||
            currentScreen.name === 'History' ||
            currentScreen.name === 'Settings' ||
            currentScreen.name === 'PaymentResult') {
            setCurrentScreen({name: 'Home'});
        }
    };

    if (!user) {
        switch (currentScreen.name) {
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

                    <CustomButton title="Make New Payment (QR/NFC)" onPress={() => navigate('Payment')} />
                    <CustomButton title="Add Payment Method (Adyen)" onPress={() => navigate('AddPaymentMethod')} />
                    <CustomButton title="Make Payment (Adyen Checkout)" onPress={() => navigate('Checkout', { amount: 2500, description: 'Online Purchase' })} />
                    <CustomButton title="View Payment History" onPress={() => navigate('History')} />
                    <CustomButton title="Security Settings" onPress={() => navigate('Settings')} />
                    <CustomButton title="Logout" onPress={logout} style={styles.logoutButton} textStyle={styles.logoutButtonText} />
                </View>
            </SafeAreaView>
        );
    };

    switch (currentScreen.name) {
        case 'Camera':
            return <CameraScreen navigation={{ navigate, goBack }} />;
        case 'Payment':
            return <PaymentScreen navigation={{ navigate, goBack }} />;
        case 'AddPaymentMethod':
            return <AddPaymentMethodScreen navigation={{ navigate, goBack }} />;
        case 'Checkout':
            return <CheckoutScreen navigation={{ navigate, goBack }} route={currentScreen.params ? { params: currentScreen.params } : {}} />;
        case 'PaymentResult':
            return <PaymentResultScreen navigation={{ navigate, goBack }} route={currentScreen.params ? { params: currentScreen.params } : {}} />;
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