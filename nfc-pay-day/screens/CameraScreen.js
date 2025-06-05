import {CameraView, useCameraPermissions} from 'expo-camera';
import {useState} from 'react';
import {Alert, Button, SafeAreaView, StyleSheet, Text, TouchableOpacity, View} from 'react-native';
import {AppHeader} from "../components/AppHeader";

export function CameraScreen({navigation}) {
    const [facing, setFacing] = useState('back');
    const [permission, requestPermission] = useCameraPermissions();

    if (!permission) {
        // Camera permissions are still loading.
        return <View/>;
    }

    if (!permission.granted) {
        // Camera permissions are not granted yet.
        return (
            <SafeAreaView style={styles.safeArea}>
                <AppHeader title="QR Code Scanner" showBack onBackPress={() => navigation.goBack()}/>
                <View style={styles.container}>
                    <Text style={styles.message}>We need your permission to show the camera</Text>
                    <Button onPress={requestPermission} title="grant permission"/>
                </View>
            </SafeAreaView>
        );
    }

    function toggleCameraFacing() {
        setFacing(current => (current === 'back' ? 'front' : 'back'));
    }

    return (
        <SafeAreaView style={styles.safeArea}>
            <AppHeader title="QR Code Scanner" showBack onBackPress={() => navigation.goBack()}/>
            <View style={styles.container}>
                <CameraView style={styles.camera} facing={facing}>
                    <View style={styles.buttonContainer}>
                        <TouchableOpacity style={styles.button} onPress={toggleCameraFacing}>
                            <Text style={styles.text}>Flip Camera</Text>
                        </TouchableOpacity>
                    </View>
                </CameraView>
            </View>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    safeArea: {
        flex: 1,
        backgroundColor: '#f3f4f6', // Tailwind bg-gray-100
    },
    container: {
        flex: 1,
        justifyContent: 'center',
    },
    message: {
        textAlign: 'center',
        paddingBottom: 10,
    },
    camera: {
        flex: 1,
    },
    buttonContainer: {
        flex: 1,
        flexDirection: 'row',
        backgroundColor: 'transparent',
        margin: 64,
    },
    button: {
        flex: 1,
        alignSelf: 'flex-end',
        alignItems: 'center',
    },
    text: {
        fontSize: 24,
        fontWeight: 'bold',
        color: 'white',
    },
});
