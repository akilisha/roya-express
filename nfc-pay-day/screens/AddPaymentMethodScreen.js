import {useContext} from "react";
import {Alert, SafeAreaView, ScrollView, StyleSheet, Text} from "react-native";
import {AppHeader} from "../components/AppHeader";
import {PaymentContext} from "../state/PaymentProvider";
import {AdyenDropIn} from "../components/AdyenDropIn";

export const AddPaymentMethodScreen = ({ navigation }) => {
    const { saveAdyenPaymentMethod, processingPayment } = useContext(PaymentContext);

    const adyenConfiguration = {
        environment: 'test', // or 'live-us', 'live-eu', etc.
        clientKey: 'YOUR_ADYEN_CLIENT_KEY', // Replace with your actual client key
        amount: { value: 0, currency: 'USD' }, // Amount can be 0 for just adding a method
        countryCode: 'US',
        returnUrl: 'myapp://', // Your app's return URL scheme
    };

    const handleAdyenSubmit = async (data, component) => {
        console.log('Adyen onSubmit callback (mock):', data);
        const success = await saveAdyenPaymentMethod(data.paymentMethod);
        if (success) {
            navigation.goBack(); // Go back to home or a success screen
        }
        // In a real app, you'd call component.hide(true/false) here.
    };

    const handleAdyenComplete = (result, component) => {
        // This callback is usually for handling additional actions like 3DS2
        console.log('Adyen onComplete callback (mock):', result);
    };

    const handleAdyenError = (error, component) => {
        console.error('Adyen onError callback (mock):', error);
        Alert.alert('Adyen Error', error.message || 'An unknown error occurred with Adyen.');
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Add Payment Method" showBack onBackPress={() => navigation.goBack()} />
            <ScrollView style={styles.screenContainer}>
                <Text style={styles.title}>Add Your Payment Method</Text>
                <Text style={styles.nfcDisclaimer}>
                    This section simulates Adyen's Drop-in component for adding a new payment method.
                    In a real application, Adyen's SDK would render a secure UI here.
                </Text>
                <AdyenDropIn
                    configuration={adyenConfiguration}
                    onSubmit={handleAdyenSubmit}
                    onComplete={handleAdyenComplete}
                    onError={handleAdyenError}
                    disabled={processingPayment}
                />
            </ScrollView>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
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
    nfcDisclaimer: {
        backgroundColor: '#fef3c7', // Tailwind bg-amber-100
        borderColor: '#fcd34d', // Tailwind border-amber-400
        borderWidth: 1,
        borderRadius: 8,
        padding: 15,
        marginBottom: 20,
        fontSize: 14,
        color: '#92400e', // Tailwind text-amber-800
        fontStyle: 'italic',
        // fontFamily: 'Inter_400Regular',
        textAlign: 'center',
    },
})