import {createContext, useState, useEffect} from "react";
import {Alert} from "react-native";
import {MessageModal} from "../components/ModalMessage";

// Mock expo-crypto
const Crypto = {
    randomUUID: () => Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15),
};

const initialState = {
    user: null,
    loading: false,
    login: () => {},
    register: () => {},
    logout: () => {}
}
export const AuthContext = createContext(initialState);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Simulate checking for a logged-in user (e.g., from AsyncStorage)
        const checkUser = async () => {
            setLoading(true);
            // In a real app, you'd check for a token or user session
            await new Promise(resolve => setTimeout(resolve, 500)); // Corrected setTimeout
            // For demonstration, let's assume no user initially
            setLoading(false);
        };
        checkUser().then(() => null);
    }, []);

    const login = async (username, password) => {
        setLoading(true);
        await new Promise(resolve => setTimeout(resolve, 1000)); // Corrected setTimeout
        if (username === 'test' && password === 'password') {
            setUser({ id: 'user123', username: 'test', email: 'test@example.com' });
            setLoading(false);
            return true;
        } else {
            Alert.alert('Login Failed', 'Invalid username or password.');
            setLoading(false);
            return false;
        }
    };

    const register = async (username, email, password) => {
        setLoading(true);
        await new Promise(resolve => setTimeout(resolve, 1000)); // Corrected setTimeout
        // In a real app, you'd hash the password and store user in a database
        if (username && email && password) {
            setUser({ id: Crypto.randomUUID(), username, email });
            Alert.alert('Registration Successful', 'You can now log in.');
            setLoading(false);
            return true;
        } else {
            Alert.alert('Registration Failed', 'Please fill all fields.');
            setLoading(false);
            return false;
        }
    };

    // New mock OAuth login functions
    const loginWithOAuth = async (providerName) => {
        setLoading(true);
        setModalMessage(`Simulating login with ${providerName}...`);
        setModalVisible(true);
        await new Promise(resolve => setTimeout(resolve, 1500)); // Corrected setTimeout

        // In a real app, this would involve `expo-auth-session` and a backend call
        const success = Math.random() > 0.3; // 70% chance of success for demo

        if (success) {
            setUser({ id: Crypto.randomUUID(), username: `${providerName}User`, email: `${providerName.toLowerCase()}@example.com` });
            setModalMessage(`${providerName} login successful!`);
            setModalVisible(true);
        } else {
            setModalMessage(`${providerName} login failed. Please try again.`);
            setModalVisible(true);
        }
        setLoading(false);
        return success;
    };

    const logout = () => {
        setUser(null);
        Alert.alert('Logged Out', 'You have been successfully logged out.');
    };

    const [modalVisible, setModalVisible] = useState(false);
    const [modalMessage, setModalMessage] = useState('');

    return (
        <AuthContext.Provider value={{ user, loading, login, register, logout, loginWithOAuth }}>
            {children}
            <MessageModal visible={modalVisible} message={modalMessage} onClose={() => setModalVisible(false)} />
        </AuthContext.Provider>
    );
};