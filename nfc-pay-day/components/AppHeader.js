import {Text, TouchableOpacity, View} from "react-native";
import {Ionicons} from "@expo/vector-icons";

export const AppHeader = ({ title, showBack = false, onBackPress }) => (
    <View style={styles.headerContainer}>
        {showBack && (
            <TouchableOpacity onPress={onBackPress} style={styles.backButton}>
                <Ionicons name="arrow-back" size={24} color="white" />
            </TouchableOpacity>
        )}
        <Text style={styles.headerTitle}>{title}</Text>
    </View>
);

const styles = {
    headerContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 16,
        backgroundColor: '#4F46E5', // Tailwind bg-indigo-600
        paddingTop: 40, // Adjust for notch
    },
    backButton: {
        paddingRight: 10,
    },
    headerTitle: {
        color: 'white',
        fontSize: 22,
        fontWeight: 'bold',
        // fontFamily: 'Inter_700Bold',
    }
}