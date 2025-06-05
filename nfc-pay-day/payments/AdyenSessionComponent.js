import {AdyenCheckout} from '@adyen/react-native';
import {useCallback, useEffect} from 'react';
import {AdyenSessionCheckout} from "./AdyenSessionCheckout";
import {AppHeader} from "../components/AppHeader";
import {SafeAreaView, ScrollView, StyleSheet, Text} from "react-native";

export function AdyenSessionComponent({navigation, route}) {

    const {ADYEN_ENV, ADYEN_CLIENT_KEY} = process.env;
    const [session, setSession] = React.useState(null);

    const {amount = 1000, currency = 'USD', countryCode = 'US', description = "Checkout Purchase"} = route.params || {}; // Default amount in minor units

    useEffect(() => {
        fetch(`http://localhost:3000/pay/paymentSession`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                "amount": amount,
                "currency": currency,
                "countryCode": countryCode
            })
        })
            .then(res => res.json())
            .then(setSession)
    }, []);

    const adyenConfiguration = {
        environment: ADYEN_ENV, // When you're ready to accept real payments, change the value to a suitable live environment.
        clientKey: ADYEN_CLIENT_KEY,
        applepay: {
            merchantID: 'APPLE_PAY_MERCHANT_ID',
            merchantName: 'APPLE_PAY_MERCHANT_NAME'
        },
        dropin: {
            skipListWhenSinglePaymentMethod: true,
            showPreselectedStoredPaymentMethod: false
        },
    };

    const onComplete = useCallback((result, nativeComponent) => {
        // When this callback is executed, you must call `component.hide(true | false)` to dismiss the payment UI.
        nativeComponent.hide(true)
        navigation.navigate('PaymentResult', {result: 'success', transactionId: result.pspReference});
    }, []);
    const onError = useCallback((error, component) => {
        // Handle errors or termination by shopper.
        // When this callback is executed, you must now call `component.hide(false)` to dismiss the payment UI.
        component.hide(true)
        navigation.navigate('PaymentResult', {result: 'failure', message: result.message || 'Payment failed.'});
    }, []);

    return session ? (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Checkout" showBack onBackPress={() => navigation.goBack()}/>
            <ScrollView style={styles.screenContainer}>
                <Text style={styles.title}>Complete Your Purchase</Text>
                <Text style={styles.checkoutAmount}>Amount: ${amount / 100} {currency}</Text>
                <AdyenCheckout
                    config={adyenConfiguration}
                    session={session}
                    onComplete={onComplete}
                    onError={onError}
                >
                    <AdyenSessionCheckout/>
                </AdyenCheckout>
            </ScrollView>
        </SafeAreaView>
    ) : null;
}

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
})