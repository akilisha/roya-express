import {useContext, useState} from "react";
import {PaymentContext} from "../state/PaymentProvider";
import {SafeAreaView, View, Text} from "react-native";
import {AppHeader} from "../components/AppHeader";
import {CustomInput} from "../components/CustomInput";
import {CustomButton} from "../components/CustomButton";
import {MessageModal} from "../components/ModalMessage";

export const SettingsScreen = ({ navigation }) => {
    const { securitySettings, updateSecuritySettings } = useContext(PaymentContext);
    const [maxAmount, setMaxAmount] = useState(String(securitySettings.maxPaymentAmount));
    const [dailyCap, setDailyCap] = useState(String(securitySettings.dailyPaymentCap));
    const [modalVisible, setModalVisible] = useState(false);
    const [modalMessage, setModalMessage] = useState('');

    const handleSaveSettings = () => {
        const newMaxAmount = parseFloat(maxAmount);
        const newDailyCap = parseFloat(dailyCap);

        if (isNaN(newMaxAmount) || isNaN(newDailyCap) || newMaxAmount <= 0 || newDailyCap <= 0) {
            setModalMessage('Please enter valid positive numbers for limits.');
            setModalVisible(true);
            return;
        }

        updateSecuritySettings({
            maxPaymentAmount: newMaxAmount,
            dailyPaymentCap: newDailyCap,
        });
        setModalMessage('Security settings updated successfully!');
        setModalVisible(true);
    };

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="Security Settings" showBack onBackPress={() => navigation.goBack()} />
            <View style={styles.screenContainer}>
                <Text style={styles.title}>Configure Payment Limits</Text>
                <CustomInput
                    label="Maximum Single Payment Amount ($)"
                    value={maxAmount}
                    onChangeText={setMaxAmount}
                    keyboardType="numeric"
                    placeholder="e.g., 500"
                />
                <CustomInput
                    label="Daily Payment Cap ($)"
                    value={dailyCap}
                    onChangeText={setDailyCap}
                    keyboardType="numeric"
                    placeholder="e.g., 1000"
                />
                <CustomButton title="Save Settings" onPress={handleSaveSettings} />

                <View style={styles.currentLimitsContainer}>
                    <Text style={styles.currentLimitsTitle}>Current Daily Usage:</Text>
                    <Text style={styles.currentLimitsText}>
                        Spent Today: ${securitySettings.dailyPaymentCount.toFixed(2)} / ${securitySettings.dailyPaymentCap.toFixed(2)}
                    </Text>
                </View>
            </View>
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
    currentLimitsContainer: {
        backgroundColor: 'white',
        borderRadius: 10,
        padding: 15,
        marginTop: 20,
        boxShadow: '0 2px 4px rgba(0,0,0,0.08)', // Web equivalent of shadow
        borderLeftWidth: 5,
        borderLeftColor: '#22c55e',
    },
    currentLimitsTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        marginBottom: 10,
        color: '#1f2937',
        // fontFamily: 'Inter_600SemiBold',
    },
    currentLimitsText: {
        fontSize: 16,
        color: '#374151',
        // fontFamily: 'Inter_500Medium',
    },
}