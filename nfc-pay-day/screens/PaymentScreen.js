import {Alert, SafeAreaView, ScrollView, Text} from "react-native";
import {useContext, useEffect, useState} from "react";
import {PaymentContext} from "../state/PaymentProvider";
import {AppHeader} from "../components/AppHeader";
import {CustomInput} from "../components/CustomInput";
import {CustomButton} from "../components/CustomButton";
import {MessageModal} from "../components/ModalMessage";

// Mock expo-local-authentication
const LocalAuthentication = {
    hasHardwareAsync: async () => true, // Always true for web demo
    isEnrolledAsync: async () => true, // Always true for web demo
    authenticateAsync: async () => ({ success: true }), // Always succeed for web demo
};
// Mock expo-crypto
const Crypto = {
    randomUUID: () => Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15),
};

export const PaymentScreen = ({ navigation }) => {
    const { delegatePaymentToProvider, processingPayment } = useContext(PaymentContext);
    const [amount, setAmount] = useState('');
    const [description, setDescription] = useState('');
    const [mockScannedCardDetails, setMockScannedCardDetails] = useState(null); // Stores mock card details from QR/NFC
    const [modalVisible, setModalVisible] = useState(false);
    const [modalMessage, setModalMessage] = useState('');
    const [isQrOrNfcScanned, setIsQrOrNfcScanned] = useState(false); // To track if details are populated

    // --- NFC/QR Simulation ---
    const [scanningInProgress, setScanningInProgress] = useState(false);
    const [scannerSupported, setScannerSupported] = useState(false); // Represents NFC or QR scanner support

    useEffect(() => {
        // For this demo, we'll assume scanner (NFC/QR) is "supported" conceptually.
        setScannerSupported(true);
    }, []);

    const simulateScan = async () => {
        if (!scannerSupported) {
            setModalMessage('Scanning functionality is not supported on this device.');
            setModalVisible(true);
            return;
        }

        setScanningInProgress(true);
        setModalMessage('Simulating scan... (NFC or QR Code)');
        setModalVisible(true);

        await new Promise(resolve => setTimeout(resolve, 3000));

        // Simulate data obtained from NFC or QR code scan
        const scannedAmount = '49.99';
        const scannedDescription = 'Groceries at SuperMart';
        const scannedCardDetails = {
            cardHolderName: 'Jane Doe',
            cardNumber: '5444333322221111', // Mock card number
            expiryDate: '10/26',
            cvv: '456',
        };

        setAmount(scannedAmount);
        setDescription(scannedDescription);
        setMockScannedCardDetails(scannedCardDetails);
        setIsQrOrNfcScanned(true);

        setModalMessage('Scan successful. Details populated. Please confirm.');
        setTimeout(() => {
            setModalVisible(false);
            setScanningInProgress(false);
        }, 1000);
    };

    const handlePayment = async () => {
        if (!amount || !description || !mockScannedCardDetails) {
            setModalMessage('Please scan to populate payment details first.');
            setModalVisible(true);
            return;
        }

        // Biometric authentication (optional security layer)
        const hasHardware = await LocalAuthentication.hasHardwareAsync();
        const isEnrolled = await LocalAuthentication.isEnrolledAsync();

        if (hasHardware && isEnrolled) {
            const authResult = await LocalAuthentication.authenticateAsync({
                promptMessage: 'Authenticate to confirm payment',
                fallbackLabel: 'Enter Passcode',
            });
            if (!authResult.success) {
                setModalMessage('Biometric authentication failed or cancelled.');
                setModalVisible(true);
                return;
            }
        } else {
            console.warn('Biometric authentication not available or enrolled.');
        }

        // --- Simulate Adyen/Stripe Tokenization ---
        // In a real app, you would use the Adyen/Stripe SDK to tokenize the card data.
        // This part would typically happen on a secure payment terminal or via a secure SDK
        // after the card data is captured (either by NFC or manual entry into a secure form).
        // The details from mockScannedCardDetails are used here to represent the data
        // that *would* be tokenized by the payment provider's SDK.
        const paymentToken = `tok_${Crypto.randomUUID()}`; // Mock payment token

        const success = await delegatePaymentToProvider(
            { paymentMethod: { type: 'scheme', token: paymentToken }, amount: { value: parseFloat(amount) * 100, currency: 'USD' }, description: description }
        );
        if (success.success) {
            // Clear form on success
            setAmount('');
            setDescription('');
            setMockScannedCardDetails(null);
            setIsQrOrNfcScanned(false);
            navigation.navigate('PaymentResult', { result: 'success', transactionId: success.pspReference });
        } else {
            navigation.navigate('PaymentResult', { result: 'failure', message: success.message });
        }
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Make Payment" showBack onBackPress={() => navigation.goBack()} />
            <ScrollView style={styles.screenContainer}>
                <Text style={styles.title}>New Payment</Text>

                <Text style={styles.nfcDisclaimer}>
                    Disclaimer: This app simulates NFC/QR payment. In a real scenario, direct reading of bank card data via NFC/QR is not allowed for security and compliance reasons. A certified payment terminal or SDK would handle secure card data capture.
                    Payment processing is delegated to a mock payment provider (like Adyen/Stripe).
                </Text>

                <CustomButton
                    title={scanningInProgress ? "Scanning..." : "Simulate Scan (NFC/QR)"}
                    onPress={simulateScan}
                    disabled={scanningInProgress || processingPayment}
                    style={styles.nfcButton}
                    textStyle={styles.nfcButtonText}
                />

                {isQrOrNfcScanned && (
                    <>
                        <Text style={styles.sectionTitle}>Scanned Payment Details</Text>
                        <CustomInput label="Amount ($)" value={amount} onChangeText={setAmount} placeholder="e.g., 25.50" keyboardType="numeric" editable={true} />
                        <CustomInput label="Description" value={description} onChangeText={setDescription} placeholder="e.g., Coffee purchase" editable={true} />
                        <CustomInput label="Cardholder Name" value={mockScannedCardDetails?.cardHolderName} editable={false} />
                        <CustomInput label="Card Number (last 4)" value={mockScannedCardDetails?.cardNumber?.slice(-4)} editable={false} />
                        <CustomInput label="Expiry Date" value={mockScannedCardDetails?.expiryDate} editable={false} />

                        <CustomButton
                            title={processingPayment ? "Processing..." : "Confirm & Complete Payment"}
                            onPress={handlePayment}
                            disabled={processingPayment || scanningInProgress}
                        />
                    </>
                )}
                {!isQrOrNfcScanned && (
                    <Text style={styles.emptyHistory}>Scan NFC or QR code to populate payment details.</Text>
                )}
            </ScrollView>
            <MessageModal visible={modalVisible} message={modalMessage} onClose={() => setModalVisible(false)} />
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
    sectionTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#1f2937',
        marginBottom: 15,
        marginTop: 10,
        // fontFamily: 'Inter_600SemiBold',
        textAlign: 'center',
    },
    nfcButton: {
        backgroundColor: '#22c55e', // Tailwind bg-green-500
        marginBottom: 20,
    },
    nfcButtonText: {
        fontSize: 20,
    }
}