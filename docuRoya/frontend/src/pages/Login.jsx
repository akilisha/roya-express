import {useState} from 'preact/hooks';
import {route} from 'preact-router';
import {authAPI} from '../utils/api';

export function Login({ onLogin, user }) {
    // Redirect if already logged in
    if (user) {
        route('/');
        return null;
    }

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    async function handleSubmit(e) {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const data = await authAPI.login(email, password);
            onLogin(data.user, data.token);
            route('/');
        } catch (err) {
            setError(err.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div class="max-w-md mx-auto card">
            <h1 class="text-2xl font-bold mb-6">Login</h1>

            {error && (
                <div class="bg-red-50 border border-red-200 rounded-md p-4 text-red-700 mb-4">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit} class="space-y-4">
                <div>
                    <label for="email" class="block text-sm font-medium text-gray-700 mb-1">
                        Email
                    </label>
                    <input
                        id="email"
                        type="email"
                        class="input"
                        value={email}
                        onInput={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>

                <div>
                    <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
                        Password
                    </label>
                    <input
                        id="password"
                        type="password"
                        class="input"
                        value={password}
                        onInput={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>

                <button
                    type="submit"
                    class="btn btn-primary w-full"
                    disabled={loading}
                >
                    {loading ? 'Logging in...' : 'Login'}
                </button>
            </form>

            <p class="text-center text-sm text-gray-600 mt-6">
                Don't have an account?{' '}
                <a href="/register" class="text-primary-600 hover:text-primary-700">
                    Register
                </a>
            </p>
        </div>
    );
}
