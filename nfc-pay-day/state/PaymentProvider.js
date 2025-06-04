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


    // Updated function to simulate delegating payment to a provider
    const delegatePaymentToProvider = async (paymentToken, amount, description) => {
        setProcessingPayment(true);
        await new Promise(resolve => setTimeout(resolve, 2000));

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
            await addPayment(amount, description); // Use the async addPayment
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
            processingPayment,
            loadPaymentHistory // Expose load function
        }}>
            {children}
        </PaymentContext.Provider>
    );
};