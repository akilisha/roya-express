import {useContext} from "react";
import {Alert, SafeAreaView, ScrollView, Text, StyleSheet} from "react-native";
import {AppHeader} from "../components/AppHeader";
import {PaymentContext} from "../state/PaymentProvider";
import {AdyenDropIn} from "../components/AdyenDropIn";

export const CheckoutScreen = ({ navigation, route }) => {
    const { initiateAdyenPayment, processingPayment, savedPaymentMethods } = useContext(PaymentContext);
    const { amount = 1000, description = "Checkout Purchase" } = route.params || {}; // Default amount in minor units

    const adyenConfiguration = {
        environment: 'test',
        clientKey: 'YOUR_ADYEN_CLIENT_KEY',
        amount: { value: amount, currency: 'USD' },
        countryCode: 'US',
        returnUrl: 'myapp://',
        storedPaymentMethods: savedPaymentMethods, // Pass saved methods to Adyen Drop-in
    };

    const handleAdyenSubmit = async (data, component) => {
        console.log('Adyen onSubmit callback (mock):', data);
        // Send data to your backend to initiate the /payments call
        const result = await initiateAdyenPayment({
            paymentMethod: data.paymentMethod,
            amount: data.amount,
            description: description, // Use description passed to screen
        });

        // Handle the result from your backend's payment processing
        if (result.success) {
            navigation.navigate('PaymentResult', { result: 'success', transactionId: result.pspReference });
        } else {
            navigation.navigate('PaymentResult', { result: 'failure', message: result.message || 'Payment failed.' });
        }
        // In a real app, you'd call component.hide(true/false) here to dismiss Adyen's UI
    };

    const handleAdyenComplete = (result, component) => {
        console.log('Adyen onComplete callback (mock):', result);
        // This callback is usually for handling additional actions like 3DS2 results
        // Navigation to result screen is handled by onSubmit's success/failure
    };

    const handleAdyenError = (error, component) => {
        console.error('Adyen onError callback (mock):', error);
        Alert.alert('Adyen Error', error.message || 'An unknown error occurred during checkout.');
        navigation.navigate('PaymentResult', { result: 'failure', message: error.message || 'Adyen error during checkout.' });
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Checkout" showBack onBackPress={() => navigation.goBack()} />
            <ScrollView style={styles.screenContainer}>
                <Text style={styles.title}>Complete Your Purchase</Text>
                <Text style={styles.checkoutAmount}>Amount: ${amount / 100} {adyenConfiguration.amount.currency}</Text>
                <Text style={styles.nfcDisclaimer}>
                    This section simulates Adyen's Drop-in component for making a payment.
                    In a real application, Adyen's SDK would render a secure UI here including saved methods.
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
    checkoutAmount: {
        fontSize: 22,
        fontWeight: 'bold',
        color: '#1f2937',
        marginBottom: 20,
        textAlign: 'center',
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
    }
})