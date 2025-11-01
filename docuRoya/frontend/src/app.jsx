import { useState, useEffect } from 'preact/hooks';
import { Router } from 'preact-router';
import { Layout } from './components/Layout';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Articles } from './pages/Articles';
import { ArticleView } from './pages/ArticleView';
import { ArticleEdit } from './pages/ArticleEdit';
import { Upload } from './pages/Upload';
import { Chat } from './pages/Chat';
import { Testing } from './pages/Testing';
import './app.css';

export function App() {
    const [user, setUser] = useState(null);

    // Load user from localStorage on mount
    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        const storedToken = localStorage.getItem('token');
        if (storedUser && storedToken) {
            setUser(JSON.parse(storedUser));
        }
    }, []);

    const handleLogin = (userData, token) => {
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
        localStorage.setItem('token', token);
    };

    const handleLogout = () => {
        setUser(null);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
    };

    return (
        <Layout user={user} onLogout={handleLogout}>
            <Router>
                <Home path="/" user={user} />
                <Login path="/login" onLogin={handleLogin} user={user} />
                <Register path="/register" onLogin={handleLogin} user={user} />
                <Articles path="/articles" user={user} />
                <ArticleEdit path="/articles/new" user={user} />
                <ArticleEdit path="/articles/:id/edit" user={user} />
                <ArticleView path="/articles/:id" user={user} />
                <Upload path="/upload" user={user} />
                <Chat path="/chat" user={user} />
                <Testing path="/testing" user={user} />
            </Router>
        </Layout>
    );
}

