import { useAdyenCheckout } from '@adyen/react-native';
import {Button} from "react-native";

// Set your View to use AdyenCheckout as the context.
export const AdyenSessionCheckout = () => {
    const { start } = useAdyenCheckout();

    return (
    // Create a way, like a checkout button, that starts Drop-in.
        <Button
            title="Checkout"
            // Use dropIn to show the full list of available payment methods.
            onPress={() => { start('dropIn'); }} />
    );
};
