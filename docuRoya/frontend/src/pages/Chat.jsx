import { useState, useEffect, useRef } from 'preact/hooks';
import { route } from 'preact-router';
import { chatAPI } from '../utils/api';

export function Chat({ user }) {
    if (!user) {
        route('/login');
        return null;
    }

    const [connected, setConnected] = useState(false);
    const [messages, setMessages] = useState([]);
    const [inputMessage, setInputMessage] = useState('');
    const [stats, setStats] = useState(null);
    const [error, setError] = useState('');
    const wsRef = useRef(null);

    // Load stats on mount
    useEffect(() => {
        loadStats();
        const interval = setInterval(loadStats, 5000); // Refresh stats every 5s
        return () => clearInterval(interval);
    }, []);

    async function loadStats() {
        try {
            const statsData = await chatAPI.getStats();
            setStats(statsData);
        } catch (err) {
            console.error('Failed to load stats:', err);
        }
    }

    function connectWebSocket() {
        if (wsRef.current) {
            console.log('Already connected');
            return;
        }

        wsRef.current = chatAPI.connectWebSocket(
            (data) => {
                console.log('Received:', data);
                setMessages(prev => [...prev, data]);
            },
            (error) => {
                console.error('WebSocket error:', error);
                setError('WebSocket connection failed');
                setConnected(false);
            }
        );

        wsRef.current.onopen = () => {
            setConnected(true);
            setError('');
        };

        wsRef.current.onclose = () => {
            setConnected(false);
            wsRef.current = null;
        };
    }

    function disconnectWebSocket() {
        if (wsRef.current) {
            wsRef.current.close();
            wsRef.current = null;
            setConnected(false);
        }
    }

    async function sendMessage(e) {
        e.preventDefault();
        if (!inputMessage.trim()) return;

        try {
            await chatAPI.sendMessage('default', inputMessage);
            setInputMessage('');
            loadStats(); // Refresh stats after sending
        } catch (err) {
            setError(err.message || 'Failed to send message');
        }
    }

    function sendWebSocketMessage(e) {
        e.preventDefault();
        if (!inputMessage.trim() || !wsRef.current) return;

        try {
            wsRef.current.send(inputMessage);
            setInputMessage('');
        } catch (err) {
            setError('Failed to send WebSocket message');
        }
    }

    return (
        <div class="space-y-6">
            <h1 class="text-3xl font-bold">Chat System</h1>

            {/* Stats */}
            {stats && (
                <div class="card bg-blue-50 border-blue-200">
                    <h2 class="text-xl font-bold mb-2">Chat Statistics</h2>
                    <div class="grid grid-cols-3 gap-4 text-sm">
                        <div>
                            <p class="text-gray-600">Active Sessions</p>
                            <p class="text-2xl font-bold text-blue-600">{stats.activeSessions}</p>
                        </div>
                        <div>
                            <p class="text-gray-600">WS Connections</p>
                            <p class="text-2xl font-bold text-blue-600">{stats.wsConnections}</p>
                        </div>
                        <div>
                            <p class="text-gray-600">Total Messages</p>
                            <p class="text-2xl font-bold text-blue-600">{stats.totalMessages}</p>
                        </div>
                    </div>
                </div>
            )}

            {/* WebSocket Controls */}
            <div class="card">
                <h2 class="text-2xl font-bold mb-4">WebSocket Chat</h2>

                <div class="flex items-center gap-4 mb-4">
                    <div class="flex items-center gap-2">
                        <div class={`w-3 h-3 rounded-full ${connected ? 'bg-green-500' : 'bg-gray-400'}`}></div>
                        <span class="text-sm font-medium">
              {connected ? 'Connected' : 'Disconnected'}
            </span>
                    </div>
                    {!connected ? (
                        <button onClick={connectWebSocket} class="btn btn-primary btn-sm">
                            Connect
                        </button>
                    ) : (
                        <button onClick={disconnectWebSocket} class="btn btn-secondary btn-sm">
                            Disconnect
                        </button>
                    )}
                </div>

                {/* Messages */}
                <div class="border rounded-lg p-4 mb-4 bg-gray-50 min-h-[300px] max-h-[400px] overflow-y-auto">
                    {messages.length === 0 ? (
                        <p class="text-gray-500 text-center">No messages yet. Connect to start chatting!</p>
                    ) : (
                        <div class="space-y-2">
                            {messages.map((msg, idx) => (
                                <div key={idx} class="p-2 bg-white rounded border">
                                    <p class="text-xs text-gray-500 mb-1">
                                        {msg.type} {msg.from && `from ${msg.from}`}
                                    </p>
                                    <p class="text-sm">{msg.message}</p>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Input */}
                <form onSubmit={sendWebSocketMessage} class="flex gap-2">
                    <input
                        type="text"
                        value={inputMessage}
                        onInput={(e) => setInputMessage(e.target.value)}
                        placeholder="Type a message..."
                        class="input flex-1"
                        disabled={!connected}
                    />
                    <button
                        type="submit"
                        class="btn btn-primary"
                        disabled={!connected || !inputMessage.trim()}
                    >
                        Send
                    </button>
                </form>
            </div>

            {/* HTTP Chat */}
            <div class="card">
                <h2 class="text-2xl font-bold mb-4">HTTP Message Endpoint</h2>
                <p class="text-gray-600 mb-4 text-sm">
                    Send messages via POST endpoint (broadcasts to WebSocket clients)
                </p>

                <form onSubmit={sendMessage} class="flex gap-2">
                    <input
                        type="text"
                        value={inputMessage}
                        onInput={(e) => setInputMessage(e.target.value)}
                        placeholder="Type a message to send via HTTP..."
                        class="input flex-1"
                    />
                    <button
                        type="submit"
                        class="btn btn-secondary"
                        disabled={!inputMessage.trim()}
                    >
                        Send via HTTP
                    </button>
                </form>
            </div>

            {error && (
                <div class="bg-red-50 border border-red-200 rounded-md p-4 text-red-700">
                    {error}
                </div>
            )}

            {/* Instructions */}
            <div class="card bg-yellow-50 border-yellow-200">
                <h3 class="font-bold mb-2">💡 Testing Instructions</h3>
                <ul class="text-sm space-y-1 text-gray-700">
                    <li>• Click "Connect" to establish WebSocket connection</li>
                    <li>• Send messages via WebSocket or HTTP endpoint</li>
                    <li>• Open multiple browser tabs to chat with yourself</li>
                    <li>• Or use CLI: <code class="bg-white px-1 rounded">wscat -c ws://localhost:3003/ws/chat</code></li>
                </ul>
            </div>
        </div>
    );
}
