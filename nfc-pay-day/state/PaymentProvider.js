import {createContext, useState, useEffect, useCallback} from "react";
import {Alert} from "react-native";

// Mock expo-crypto
const Crypto = {
    randomUUID: () => Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15),
};

export const PaymentContext = createContext(null);

export const PaymentProvider = ({ children }) => {
    const [paymentHistory, setPaymentHistory] = useState([]);
    const [securitySettings, setSecuritySettings] = useState({
        maxPaymentAmount: 500,
        dailyPaymentCap: 1000,
        dailyPaymentCount: 0,
    });
    const [processingPayment, setProcessingPayment] = useState(false);

    const addPayment = useCallback((amount, description) => {
        const newPayment = {
            id: Crypto.randomUUID(),
            amount: parseFloat(amount).toFixed(2),
            description,
            date: new Date().toLocaleString(),
        };
        setPaymentHistory(prevHistory => [newPayment, ...prevHistory]);
        setSecuritySettings(prevSettings => ({
            ...prevSettings,
            dailyPaymentCount: prevSettings.dailyPaymentCount + parseFloat(amount),
        }));
    }, []);

    const updateSecuritySettings = useCallback((newSettings) => {
        setSecuritySettings(prevSettings => ({ ...prevSettings, ...newSettings }));
    }, []);

    // Reset daily cap at midnight (simplified for demo)
    useEffect(() => {
        const resetDailyCap = () => {
            const now = new Date();
            const midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1, 0, 0, 0);
            const timeUntilMidnight = midnight.getTime() - now.getTime();

            const timeoutId = setTimeout(() => {
                setSecuritySettings(prevSettings => ({
                    ...prevSettings,
                    dailyPaymentCount: 0,
                }));
                // Schedule for next midnight
                resetDailyCap();
            }, timeUntilMidnight);

            return () => clearTimeout(timeoutId);
        };
        resetDailyCap();
    }, []);

    // Updated function to simulate delegating payment to a provider
    const delegatePaymentToProvider = async (paymentToken, amount, description) => {
        setProcessingPayment(true);
        await new Promise(resolve => setTimeout(resolve, 2000)); // Simulate payment gateway API call

        // --- Security Checks (Client-side for demo) ---
        if (amount > securitySettings.maxPaymentAmount) {
            Alert.alert('Payment Failed', `Payment amount exceeds maximum allowed: $${securitySettings.maxPaymentAmount}`);
            setProcessingPayment(false);
            return false;
        }
        if (securitySettings.dailyPaymentCount + amount > securitySettings.dailyPaymentCap) {
            Alert.alert('Payment Failed', `Daily payment cap of $${securitySettings.dailyPaymentCap} exceeded.`);
            setProcessingPayment(false);
            return false;
        }
        // --- End Security Checks ---

        // Simulate sending token to Adyen/Stripe backend
        console.log(`Simulating payment delegation to Adyen/Stripe with token: ${paymentToken}`);
        console.log(`Amount: $${amount}, Description: ${description}`);

        // In a real scenario, you'd make a fetch call to your backend here,
        // which would then call the Adyen/Stripe API with the token.
        // Example:
        // const response = await fetch('/api/process-payment', {
        //   method: 'POST',
        //   headers: { 'Content-Type': 'application/json' },
        //   body: JSON.stringify({ paymentToken, amount, description })
        // });
        // const result = await response.json();
        // if (result.success) { ... } else { ... }

        // For this simulation, we'll assume success if a token is provided.
        if (paymentToken) {
            addPayment(amount, description);
            Alert.alert('Payment Successful', `Successfully processed $${amount} for ${description} via Adyen/Stripe.`);
            setProcessingPayment(false);
            return true;
        } else {
            Alert.alert('Payment Failed', 'Payment token missing or invalid.');
            setProcessingPayment(false);
            return false;
        }
    };

    return (
        <PaymentContext.Provider value={{
            paymentHistory,
            securitySettings,
            updateSecuritySettings,
            delegatePaymentToProvider,
            processingPayment
        }}>
            {children}
        </PaymentContext.Provider>
    );
};