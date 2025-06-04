// --- Mock SQLite Database and Encryption Utility ---
// This is a SIMULATION for the web environment.
// In a real Expo app, you'd use `expo-sqlite` and a proper crypto library.

import SessionStorage from 'react-native-session-storage';

const MOCK_DB_NAME = process.env.EXPO_PUBLIC_SQLITE_DATABASE;
const MOCK_ENCRYPTION_KEY = process.env.EXPO_PUBLIC_SQLITE_PASSWORD;

// Simple Base64 "encryption" for demo purposes only. NOT real crypto.
export const encryptData = (text) => {
    if (!text) return '';
    // In a real app, use a strong AES encryption with a securely managed key
    return btoa(text); // Base64 encode
};

export const decryptData = (encryptedText) => {
    if (!encryptedText) return '';
    try {
        // In a real app, use a strong AES decryption with the same key
        return atob(encryptedText); // Base64 decode
    } catch (e) {
        console.error('Decryption failed:', e);
        return 'Decryption Error';
    }
};

// In-memory mock for SQLite operations
export const mockDb = {
    _data: [], // Stores encrypted payment records
    init: async () => {
        console.log(`Mock SQLite database '${MOCK_DB_NAME}' initialized.`);
        // Simulate loading existing data from SessionStorage if available
        const storedData = SessionStorage.getItem(MOCK_DB_NAME);
        if (storedData) {
            mockDb._data = JSON.parse(storedData);
            console.log(`Loaded ${mockDb._data.length} records from mock DB.`);
        }
    },
    executeSql: async (query, params = []) => {
        console.log(`Mock DB: Executing SQL: ${query} with params:`, params);
        // Simulate INSERT, SELECT operations
        if (query.startsWith('INSERT INTO payments')) {
            const [id, encryptedAmount, encryptedDescription, date] = params;
            mockDb._data.push({ id, encryptedAmount, encryptedDescription, date });
            SessionStorage.setItem(MOCK_DB_NAME, JSON.stringify(mockDb._data)); // Persist mock data
            return { rowsAffected: 1 };
        } else if (query.startsWith('SELECT * FROM payments')) {
            return { rows: mockDb._data.map(row => ({ ...row })) }; // Return a copy
        }
        return { rowsAffected: 0, rows: [] };
    }
};