import {createContext, useState, useEffect, useCallback} from "react";
import {Alert} from "react-native";
import {encryptData, decryptData, mockDb} from "../service/SqliteService";

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
    const [savedPaymentMethods, setSavedPaymentMethods] = useState([]); // Mock saved payment methods

    // Initialize mock DB on component mount
    useEffect(() => {
        mockDb.init().then(r => null);
    }, []);

    const addPayment = useCallback(async (amount, description) => {
        const newPaymentId = Crypto.randomUUID();
        const encryptedAmount = encryptData(amount.toFixed(2));
        const encryptedDescription = encryptData(description);
        const date = new Date().toLocaleString();

        // Simulate saving to SQLite
        await mockDb.executeSql(
            'INSERT INTO payments (id, encryptedAmount, encryptedDescription, date) VALUES (?, ?, ?, ?)',
            [newPaymentId, encryptedAmount, encryptedDescription, date]
        );

        // Update local state (this would typically trigger a re-fetch from DB in a real app)
        // For this mock, we just add it to the local state directly after "saving"
        setPaymentHistory(prevHistory => [{
            id: newPaymentId,
            amount: parseFloat(amount).toFixed(2), // Stored unencrypted in local state for immediate display
            description: description,
            date: date,
        }, ...prevHistory]);

        setSecuritySettings(prevSettings => ({
            ...prevSettings,
            dailyPaymentCount: prevSettings.dailyPaymentCount + parseFloat(amount),
        }));
    }, []);

    const loadPaymentHistory = useCallback(async () => {
        // Simulate loading from SQLite
        const result = await mockDb.executeSql('SELECT * FROM payments');
        const decryptedHistory = result.rows.map(row => ({
            id: row.id,
            amount: decryptData(row.encryptedAmount),
            description: decryptData(row.encryptedDescription),
            date: row.date,
        }));
        setPaymentHistory(decryptedHistory.reverse()); // Reverse to show newest first
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


    // --- Adyen Simulation Functions ---
    const saveAdyenPaymentMethod = useCallback(async (paymentMethodData) => {
        setProcessingPayment(true);
        await new Promise(resolve => setTimeout(resolve, 1500)); // Simulate API call to save method

        // In a real scenario, paymentMethodData would be a token from Adyen SDK.
        // You'd send this to your backend to save the payment method for a shopper.
        console.log('Simulating saving payment method:', paymentMethodData);

        const success = Math.random() > 0.2; // 80% chance of success
        if (success) {
            const newSavedMethod = {
                id: Crypto.randomUUID(),
                name: paymentMethodData.type === 'scheme' ? `Card ending in ${paymentMethodData.number.slice(-4)}` : paymentMethodData.type,
                type: paymentMethodData.type,
                lastFour: paymentMethodData.number ? paymentMethodData.number.slice(-4) : '****',
            };
            setSavedPaymentMethods(prev => [...prev, newSavedMethod]);
            Alert.alert('Success', 'Payment method saved successfully.');
        } else {
            Alert.alert('Error', 'Failed to save payment method.');
        }
        setProcessingPayment(false);
        return success;
    }, []);

    const initiateAdyenPayment = useCallback(async (paymentData) => {
        setProcessingPayment(true);
        await new Promise(resolve => setTimeout(resolve, 2000)); // Simulate payment gateway API call

        // --- Security Checks (Client-side for demo) ---
        const amount = paymentData.amount.value / 100;
        if (amount > securitySettings.maxPaymentAmount) {
            Alert.alert('Payment Failed', `Payment amount exceeds maximum allowed: $${securitySettings.maxPaymentAmount}`);
            setProcessingPayment(false);
            return { success: false, resultCode: 'Refused', message: 'Amount too high' };
        }
        if (securitySettings.dailyPaymentCount + amount > securitySettings.dailyPaymentCap) {
            Alert.alert('Payment Failed', `Daily payment cap of $${securitySettings.dailyPaymentCap} exceeded.`);
            setProcessingPayment(false);
            return { success: false, resultCode: 'Refused', message: 'Daily cap exceeded' };
        }
        // --- End Security Checks ---

        // In a real scenario, paymentData would be a tokenized object from Adyen's SDK.
        // You'd send this to your backend, which then calls Adyen's /payments API.
        console.log(`Simulating Adyen payment processing for amount ${amount} and method:`, paymentData.paymentMethod);

        const success = Math.random() > 0.1; // 90% chance of success

        if (success) {
            await addPayment(amount, paymentData.description || 'Adyen Payment');
            Alert.alert('Payment Successful', `Successfully processed $${amount} via Adyen.`);
            setProcessingPayment(false);
            return { success: true, resultCode: 'Authorised', pspReference: Crypto.randomUUID() };
        } else {
            Alert.alert('Payment Failed', 'Adyen payment simulation failed.');
            setProcessingPayment(false);
            return { success: false, resultCode: 'Refused', message: 'Simulated Adyen failure' };
        }
    }, [addPayment, securitySettings]);

    return (
        <PaymentContext.Provider value={{
            paymentHistory,
            securitySettings,
            updateSecuritySettings,
            delegatePaymentToProvider: initiateAdyenPayment, // Renamed and adapted for Adyen flow
            processingPayment,
            loadPaymentHistory,
            savedPaymentMethods,
            saveAdyenPaymentMethod,
            initiateAdyenPayment, // Exposed for CheckoutScreen
        }}>
            {children}
        </PaymentContext.Provider>
    );
};