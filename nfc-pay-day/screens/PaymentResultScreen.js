import {CustomButton} from "../components/CustomButton";
import {SafeAreaView, View, Text, StyleSheet} from "react-native";
import {AppHeader} from "../components/AppHeader";
import {Ionicons} from "@expo/vector-icons";

export const PaymentResultScreen = ({ navigation, route }) => {
    const { result, message, transactionId } = route.params || {};

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Payment Result" showBack onBackPress={() => navigation.navigate('Home')} />
            <View style={styles.screenContainer}>
                {result === 'success' ? (
                    <View style={styles.resultContainer}>
                        <Ionicons name="checkmark-circle-outline" size={80} color="#4CAF50" />
                        <Text style={styles.resultTitleSuccess}>Payment Successful!</Text>
                        <Text style={styles.resultMessage}>Transaction ID: {transactionId}</Text>
                        <Text style={styles.resultMessage}>Your payment was processed successfully.</Text>
                    </View>
                ) : (
                    <View style={styles.resultContainer}>
                        <Ionicons name="close-circle-outline" size={80} color="#F44336" />
                        <Text style={styles.resultTitleFailure}>Payment Failed!</Text>
                        <Text style={styles.resultMessage}>{message || 'An unknown error occurred during payment processing.'}</Text>
                    </View>
                )}
                <CustomButton title="Back to Home" onPress={() => navigation.navigate('Home')} style={styles.resultButton} />
                <CustomButton title="Try Again" onPress={() => navigation.navigate('Payment')} style={styles.resultButton} />
            </View>
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
    resultContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 20,
    },
    resultTitleSuccess: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#4CAF50',
        marginTop: 20,
        marginBottom: 10,
        textAlign: 'center',
    },
    resultMessage: {
        fontSize: 18,
        color: '#374151',
        textAlign: 'center',
        marginBottom: 10,
    },
    resultButton: {
        marginTop: 20,
    },
    resultTitleFailure: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#F44336',
        marginTop: 20,
        marginBottom: 10,
        textAlign: 'center',
    }
})