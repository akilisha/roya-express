import {SafeAreaView, ScrollView, View, Text} from "react-native";
import {useContext} from "react";
import {PaymentContext} from "../state/PaymentProvider";
import {AppHeader} from "../components/AppHeader";

export const PaymentHistoryScreen = ({ navigation }) => {
    const { paymentHistory } = useContext(PaymentContext);

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Payment History" showBack onBackPress={() => navigation.goBack()} />
            <View style={styles.screenContainer}>
                <Text style={styles.title}>Your Transactions</Text>
                {paymentHistory.length === 0 ? (
                    <Text style={styles.emptyHistory}>No payments recorded yet.</Text>
                ) : (
                    <ScrollView style={styles.historyList}>
                        {paymentHistory.map(payment => (
                            <View key={payment.id} style={styles.historyItem}>
                                <Text style={styles.historyDescription}>{payment.description}</Text>
                                <Text style={styles.historyAmount}>${payment.amount}</Text>
                                <Text style={styles.historyDate}>{payment.date}</Text>
                            </View>
                        ))}
                    </ScrollView>
                )}
            </View>
        </SafeAreaView>
    );
};

const styles = {
    safeArea: {
        flex: 1,
        backgroundColor: '#f3f4f6', // Tailwind bg-gray-100
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#1f2937', // Tailwind text-gray-900
        marginBottom: 20,
        textAlign: 'center',
        // fontFamily: 'Inter_700Bold',
    },
    historyList: {
        marginTop: 20,
    },
    emptyHistory: {
        textAlign: 'center',
        marginTop: 50,
        fontSize: 16,
        color: '#6b7280',
        // fontFamily: 'Inter_400Regular',
    },
    historyItem: {
        backgroundColor: 'white',
        borderRadius: 10,
        padding: 15,
        marginBottom: 10,
        boxShadow: '0 2px 4px rgba(0,0,0,0.08)', // Web equivalent of shadow
        borderLeftWidth: 5,
        borderLeftColor: '#4F46E5',
    },
    historyDescription: {
        fontSize: 18,
        fontWeight: '600',
        color: '#1f2937',
        marginBottom: 5,
        // fontFamily: 'Inter_600SemiBold',
    },
    historyAmount: {
        fontSize: 16,
        color: '#374151',
        marginBottom: 3,
        // fontFamily: 'Inter_500Medium',
    },
    historyDate: {
        fontSize: 14,
        color: '#6b7280',
        // fontFamily: 'Inter_400Regular',
    },
    screenContainer: {
        flex: 1,
        padding: 20,
    }
}