import {AppNavigator} from "./components/AppNavigator";
import {AuthProvider} from "./state/AuthProvider";
import {PaymentProvider} from "./state/PaymentProvider";

export default function App() {
  return (
      <AuthProvider>
        <PaymentProvider>
          <AppNavigator />
        </PaymentProvider>
      </AuthProvider>
  );
}
