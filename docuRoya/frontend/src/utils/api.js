const API_BASE = '/api';

// Get auth token from localStorage
function getToken() {
    return localStorage.getItem('token');
}

// Create auth headers
function getAuthHeaders() {
    const token = getToken();
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
}

// Fetch with error handling
async function fetchJSON(url, options = {}) {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                ...getAuthHeaders(),
                ...options.headers
            }
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || data.message || 'Request failed');
        }

        return data;
    } catch (error) {
        console.error('API error:', error);
        throw error;
    }
}

// Fetch for file uploads (no JSON headers)
async function fetchFileUpload(url, formData) {
    const token = getToken();
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: token ? { 'Authorization': `Bearer ${token}` } : {},
            body: formData
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || data.message || 'Upload failed');
        }

        return data;
    } catch (error) {
        console.error('Upload error:', error);
        throw error;
    }
}

// Auth API
export const authAPI = {
    async register(email, password, name) {
        return fetchJSON(`${API_BASE}/auth/register`, {
            method: 'POST',
            body: JSON.stringify({ email, password, name })
        });
    },

    async login(email, password) {
        return fetchJSON(`${API_BASE}/auth/login`, {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    async me() {
        return fetchJSON(`${API_BASE}/me`);
    },

    async refresh(refreshToken) {
        return fetchJSON(`${API_BASE}/auth/refresh`, {
            method: 'POST',
            body: JSON.stringify({ refreshToken })
        });
    }
};

// Articles API
export const articlesAPI = {
    async list(limit = 20, offset = 0) {
        return fetchJSON(`${API_BASE}/articles?limit=${limit}&offset=${offset}`);
    },

    async get(id) {
        return fetchJSON(`${API_BASE}/articles/${id}`);
    },

    async create(title, content, tags) {
        return fetchJSON(`${API_BASE}/articles`, {
            method: 'POST',
            body: JSON.stringify({ title, content, tags })
        });
    },

    async update(id, title, content, tags) {
        return fetchJSON(`${API_BASE}/articles/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ title, content, tags })
        });
    },

    async delete(id) {
        return fetchJSON(`${API_BASE}/articles/${id}`, {
            method: 'DELETE'
        });
    },

    async hot() {
        return fetchJSON(`${API_BASE}/articles/hot`);
    }
};

// Upload API
export const uploadAPI = {
    async uploadFile(file) {
        // Convert file to base64
        const base64Content = await new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result.split(',')[1]);
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });

        return fetchJSON(`${API_BASE}/upload`, {
            method: 'POST',
            body: JSON.stringify({
                fileName: file.name,
                contentType: file.type,
                content: base64Content
            })
        });
    },

    async getPresignedUrl(key) {
        return fetchJSON(`${API_BASE}/upload/${key}/presigned`);
    },

    async listFiles(prefix) {
        return fetchJSON(`${API_BASE}/files?prefix=${prefix || 'uploads/'}`);
    }
};

// Chat API
export const chatAPI = {
    async sendMessage(message) {
        return fetchJSON(`${API_BASE}/chat/message`, {
            method: 'POST',
            body: JSON.stringify({ message })
        });
    },

    async getStats() {
        return fetchJSON(`${API_BASE}/chat/stats`);
    },

    // WebSocket helper
    connectWebSocket(onMessage, onError) {
        const wsUrl = `ws://localhost:3003/ws/chat`;
        const ws = new WebSocket(wsUrl);

        ws.onopen = () => console.log('WebSocket connected');
        ws.onmessage = (event) => onMessage(JSON.parse(event.data));
        ws.onerror = (error) => onError(error);
        ws.onclose = () => console.log('WebSocket disconnected');

        return ws;
    }
};

// Testing API
export const testingAPI = {
    async setCache(key, value, ttl) {
        return fetchJSON(`${API_BASE}/test/cache/set`, {
            method: 'POST',
            body: JSON.stringify({ key, value, ttl })
        });
    },

    async getCache(key) {
        return fetchJSON(`${API_BASE}/test/cache/get/${key}`);
    },

    async deleteCache(key) {
        return fetchJSON(`${API_BASE}/test/cache/invalidate/${key}`, {
            method: 'DELETE'
        });
    },

    async incrementCounter(name, increment = 1) {
        return fetchJSON(`${API_BASE}/test/metrics/counter`, {
            method: 'POST',
            body: JSON.stringify({ name, increment })
        });
    },

    async setGauge(name, value) {
        return fetchJSON(`${API_BASE}/test/metrics/gauge`, {
            method: 'POST',
            body: JSON.stringify({ name, value })
        });
    },

    async recordTimer(name, durationMs) {
        return fetchJSON(`${API_BASE}/test/metrics/timer`, {
            method: 'POST',
            body: JSON.stringify({ name, durationMs })
        });
    },

    async testRateLimit(type = 'aggressive') {
        return fetchJSON(`${API_BASE}/test/rate-limit/${type}`);
    }
};

// Metrics API
export async function getMetrics() {
    const response = await fetch('/metrics');
    return response.text();
}

