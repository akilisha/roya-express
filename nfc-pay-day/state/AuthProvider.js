import {createContext, useState, useEffect} from "react";
import {Alert} from "react-native";
import {MessageModal} from "../components/ModalMessage";
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = process.env.EXPO_PUBLIC_SUPA_PROJECT_URL
const supabaseKey = process.env.EXPO_PUBLIC_SUPA_API_KEY
const supabase = createClient(supabaseUrl, supabaseKey)

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
            const { data: { user } } = await supabase.auth.getUser();
            setUser(user);
            // For demonstration, let's assume no user initially
            setLoading(false);
        };
        //set user value to skip authentication (for testing)
        checkUser().then(() => setUser({username: "Jimbob"}));
    }, []);

    const login = async (email, password) => {
        setLoading(true);
        if (email && password) {
            let { data: {user}, error } = await supabase.auth.signInWithPassword({
                email,
                password
            })
            setUser(user);
            setLoading(false);
            return true;
        } else {
            Alert.alert('Login Failed', 'Invalid email or password.');
            setLoading(false);
            return false;
        }
    };

    const register = async (email, password) => {
        setLoading(true);
        let { data: {user}, error } = await supabase.auth.signUp({
            email,
            password,
        });
        // In a real app, you'd hash the password and store user in a database
        if (!error) {
            setUser(user);
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

    const logout = async () => {
        let { error } = await supabase.auth.signOut();
        if(!error) {
            setUser(null);
            Alert.alert('Logged Out', 'You have been successfully logged out.');
        }
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