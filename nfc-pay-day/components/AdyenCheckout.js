import {createContext, useContext, useCallback} from "react";

const AdyenCheckoutContext = createContext(null);
export const useAdyenCheckout = () => useContext(AdyenCheckoutContext);

// Mock AdyenCheckout class, usually used as context provider
// In a real app, this would wrap your components, and you'd use useAdyenCheckout hook.
export const AdyenCheckout = ({ configuration, children }) => {
    const start = useCallback((componentType, amount, description) => {
        // Simulate opening Adyen's UI for a specific componentType
        console.log(`Mock AdyenCheckout: Starting ${componentType} with amount ${amount.value} ${amount.currency}`);
        // In a real app, this would trigger the native Adyen UI to open
    }, []);

    return (
        <AdyenCheckoutContext.Provider value={{ start, configuration }}>
            {children}
        </AdyenCheckoutContext.Provider>
    );
};

