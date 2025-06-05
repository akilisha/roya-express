import {useState, useEffect} from "react";
import {CustomButton} from "./CustomButton";
import {CustomInput} from "./CustomInput";
import {TouchableOpacity, View, StyleSheet, Text} from "react-native";

// Mock expo-crypto
const Crypto = {
    randomUUID: () => Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15),
};

// These simulate the UI and callbacks of Adyen's React Native SDK.
// In a real app, you would import these from '@adyen/react-native'.
export const AdyenDropIn = ({ configuration, onComplete, onSubmit, onError }) => {
    const [cardDetails, setCardDetails] = useState({ number: '', expiry: '', cvv: '', holderName: '' });
    const [selectedMethod, setSelectedMethod] = useState('');
    const [savedMethods, setSavedMethods] = useState(configuration.storedPaymentMethods || []);

    const handleInputChange = (field, value) => {
        setCardDetails(prev => ({ ...prev, [field]: value }));
    };

    const handleSimulateSubmit = () => {
        // Basic validation
        if (!cardDetails.number || !cardDetails.expiry || !cardDetails.cvv || !cardDetails.holderName) {
            if (savedMethods.length === 0 || !selectedMethod) {
                onError({ name: 'ValidationError', message: 'Please enter card details or select a saved method.' });
                return;
            }
        }

        const paymentData = {
            paymentMethod: {
                type: selectedMethod || 'scheme', // 'scheme' for card, or selected method
                ...cardDetails,
            },
            amount: configuration.amount,
            clientKey: configuration.clientKey,
            environment: configuration.environment,
            returnUrl: configuration.returnUrl,
            // In a real scenario, this 'data' object would come from Adyen's SDK after user input
            // and would be securely tokenized.
            // For a real Adyen integration, `data` would contain encrypted card data or a token.
        };
        if (onSubmit) {
            // Simulate calling the onSubmit callback which typically sends data to your backend
            onSubmit(paymentData, { hide: () => console.log('Mock hide Adyen UI') });
        }
    };

    const handleSimulateComplete = (success) => {
        // This simulates the onComplete callback from Adyen after a payment is finalized
        if (onComplete) {
            const result = {
                resultCode: success ? 'Authorised' : 'Refused',
                pspReference: Crypto.randomUUID(),
                amount: configuration.amount,
            };
            onComplete(result, { hide: () => console.log('Mock hide Adyen UI') });
        }
    };

    useEffect(() => {
        // Simulate initial setup or fetching payment methods
        console.log('MockAdyenDropIn: Initialized with config:', configuration);
    }, [configuration]);


    return (
        <View style={styles.adyenDropInContainer}>
            <Text style={styles.adyenTitle}>Mock Adyen Drop-in UI</Text>
            <Text style={styles.adyenSubtitle}>Environment: {configuration.environment}</Text>

            {/* Simulated "Add New Card" form */}
            <View style={{ marginBottom: 20 }}>
                <Text style={styles.adyenSectionTitle}>New Card Details</Text>
                <CustomInput
                    label="Card Number"
                    value={cardDetails.number}
                    onChangeText={(val) => handleInputChange('number', val)}
                    keyboardType="numeric"
                    placeholder="XXXX XXXX XXXX XXXX"
                />
                <View style={styles.adyenInlineInputs}>
                    <CustomInput
                        label="Expiry (MM/YY)"
                        value={cardDetails.expiry}
                        onChangeText={(val) => handleInputChange('expiry', val)}
                        placeholder="MM/YY"
                        style={{ flex: 1, marginRight: 10 }}
                    />
                    <CustomInput
                        label="CVV"
                        value={cardDetails.cvv}
                        onChangeText={(val) => handleInputChange('cvv', val)}
                        keyboardType="numeric"
                        secureTextEntry
                        placeholder="XXX"
                        style={{ flex: 1 }}
                    />
                </View>
                <CustomInput
                    label="Cardholder Name"
                    value={cardDetails.holderName}
                    onChangeText={(val) => handleInputChange('holderName', val)}
                    placeholder="Full Name"
                />
            </View>

            {/* Simulated "Saved Payment Methods" section */}
            {savedMethods.length > 0 && (
                <View style={{ marginBottom: 20 }}>
                    <Text style={styles.adyenSectionTitle}>Saved Payment Methods</Text>
                    {savedMethods.map((method, index) => (
                        <TouchableOpacity
                            key={method.id}
                            style={[styles.savedMethodButton, selectedMethod === method.type && styles.selectedMethodButton]}
                            onPress={() => setSelectedMethod(method.type)}
                        >
                            <Text style={styles.savedMethodText}>{method.name} (...{method.lastFour})</Text>
                        </TouchableOpacity>
                    ))}
                </View>
            )}

            <CustomButton
                title={`Simulate Pay ${configuration.amount.value / 100} ${configuration.amount.currency}`}
                onPress={handleSimulateSubmit}
                style={styles.adyenPayButton}
            />

            <CustomButton
                title="Simulate Payment Success"
                onPress={() => handleSimulateComplete(true)}
                style={{...styles.adyenPayButton, backgroundColor: '#4CAF50'}}
            />
            <CustomButton
                title="Simulate Payment Failure"
                onPress={() => handleSimulateComplete(false)}
                style={{...styles.adyenPayButton, backgroundColor: '#F44336'}}
            />
        </View>
    );
};

const styles = StyleSheet.create({
    adyenDropInContainer: {
        padding: 15,
        backgroundColor: '#ffffff',
        borderRadius: 10,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 5,
        elevation: 3,
    },
    adyenTitle: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#000000',
        marginBottom: 10,
        textAlign: 'center',
    },
    adyenSubtitle: {
        fontSize: 16,
        color: '#6b7280',
        marginBottom: 20,
        textAlign: 'center',
    },
    adyenSectionTitle: {
        fontSize: 18,
        fontWeight: '600',
        color: '#374151',
        marginBottom: 10,
        borderBottomWidth: 1,
        borderBottomColor: '#d1d5db',
        paddingBottom: 5,
    },
    adyenInlineInputs: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 15,
    },
    savedMethodButton: {
        backgroundColor: '#f3f4f6',
        padding: 12,
        borderRadius: 8,
        marginBottom: 8,
        borderWidth: 1,
        borderColor: '#e5e7eb',
        alignItems: 'center',
    },
    selectedMethodButton: {
        borderColor: '#4F46E5', // Highlight selected method
        borderWidth: 2,
    },
    savedMethodText: {
        fontSize: 16,
        color: '#1f2937',
        fontWeight: '500',
    },
    adyenPayButton: {
        backgroundColor: '#007bff', // Adyen blue
        marginTop: 20,
    }
})