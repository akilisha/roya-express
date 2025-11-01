import { useState } from 'preact/hooks';
import { route } from 'preact-router';
import { testingAPI } from '../utils/api';

export function Testing({ user }) {
    if (!user) {
        route('/login');
        return null;
    }

    const [cacheKey, setCacheKey] = useState('test:key');
    const [cacheValue, setCacheValue] = useState(JSON.stringify({ message: 'Hello from cache!', count: 42 }, null, 2));
    const [cacheTTL, setCacheTTL] = useState(60);
    const [cacheResult, setCacheResult] = useState('');
    const [cacheLoading, setCacheLoading] = useState(false);

    const [metricsName, setMetricsName] = useState('test.operations');
    const [metricsIncrement, setMetricsIncrement] = useState(1);
    const [metricsResult, setMetricsResult] = useState('');
    const [metricsLoading, setMetricsLoading] = useState(false);

    const [rateLimitResult, setRateLimitResult] = useState('');
    const [rateLimitLoading, setRateLimitLoading] = useState(false);

    // Cache Testing
    async function handleCacheSet() {
        setCacheLoading(true);
        try {
            const value = JSON.parse(cacheValue);
            await testingAPI.setCache(cacheKey, value, cacheTTL);
            setCacheResult(JSON.stringify({ success: true, message: 'Cache entry set' }, null, 2));
        } catch (err) {
            setCacheResult(JSON.stringify({ success: false, error: err.message }, null, 2));
        } finally {
            setCacheLoading(false);
        }
    }

    async function handleCacheGet() {
        setCacheLoading(true);
        try {
            const result = await testingAPI.getCache(cacheKey);
            setCacheResult(JSON.stringify(result, null, 2));
        } catch (err) {
            setCacheResult(JSON.stringify({ success: false, error: err.message }, null, 2));
        } finally {
            setCacheLoading(false);
        }
    }

    async function handleCacheDelete() {
        setCacheLoading(true);
        try {
            await testingAPI.deleteCache(cacheKey);
            setCacheResult(JSON.stringify({ success: true, message: 'Cache entry deleted' }, null, 2));
        } catch (err) {
            setCacheResult(JSON.stringify({ success: false, error: err.message }, null, 2));
        } finally {
            setCacheLoading(false);
        }
    }

    // Metrics Testing
    async function handleIncrementCounter() {
        setMetricsLoading(true);
        try {
            const result = await testingAPI.incrementCounter(metricsName, parseInt(metricsIncrement));
            setMetricsResult(JSON.stringify(result, null, 2));
        } catch (err) {
            setMetricsResult(JSON.stringify({ success: false, error: err.message }, null, 2));
        } finally {
            setMetricsLoading(false);
        }
    }

    async function handleSetGauge() {
        setMetricsLoading(true);
        try {
            const result = await testingAPI.setGauge(metricsName + '.gauge', 42.5);
            setMetricsResult(JSON.stringify(result, null, 2));
        } catch (err) {
            setMetricsResult(JSON.stringify({ success: false, error: err.message }, null, 2));
        } finally {
            setMetricsLoading(false);
        }
    }

    async function handleRecordTimer() {
        setMetricsLoading(true);
        try {
            const result = await testingAPI.recordTimer(metricsName + '.timer', 150);
            setMetricsResult(JSON.stringify(result, null, 2));
        } catch (err) {
            setMetricsResult(JSON.stringify({ success: false, error: err.message }, null, 2));
        } finally {
            setMetricsLoading(false);
        }
    }

    // Rate Limiting Testing
    async function handleRateLimitTest(type) {
        setRateLimitLoading(true);
        try {
            const result = await testingAPI.testRateLimit(type);
            setRateLimitResult(JSON.stringify(result, null, 2));
        } catch (err) {
            setRateLimitResult(JSON.stringify({
                error: err.message || 'Rate limited',
                status: err.message.includes('429') ? 'Rate limited' : 'Success'
            }, null, 2));
        } finally {
            setRateLimitLoading(false);
        }
    }

    return (
        <div class="space-y-8">
            <div>
                <h1 class="text-3xl font-bold">Testing Dashboard</h1>
                <p class="text-gray-600 mt-2">Test DocuRoya features and endpoints</p>
            </div>

            {/* Cache Testing */}
            <div class="card">
                <h2 class="text-2xl font-bold mb-4">Cache Testing</h2>
                <div class="space-y-4">
                    <div class="grid md:grid-cols-3 gap-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Cache Key</label>
                            <input
                                type="text"
                                class="input"
                                value={cacheKey}
                                onInput={(e) => setCacheKey(e.target.value)}
                            />
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">TTL (seconds)</label>
                            <input
                                type="number"
                                class="input"
                                value={cacheTTL}
                                onInput={(e) => setCacheTTL(parseInt(e.target.value))}
                            />
                        </div>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Cache Value (JSON)</label>
                        <textarea
                            class="input font-mono text-sm"
                            rows="4"
                            value={cacheValue}
                            onInput={(e) => setCacheValue(e.target.value)}
                        />
                    </div>
                    <div class="flex space-x-2">
                        <button
                            onClick={handleCacheSet}
                            class="btn btn-primary"
                            disabled={cacheLoading}
                        >
                            Set Cache
                        </button>
                        <button
                            onClick={handleCacheGet}
                            class="btn btn-secondary"
                            disabled={cacheLoading}
                        >
                            Get Cache
                        </button>
                        <button
                            onClick={handleCacheDelete}
                            class="btn btn-danger"
                            disabled={cacheLoading}
                        >
                            Delete Cache
                        </button>
                    </div>
                    {cacheResult && (
                        <div class="bg-gray-50 rounded-lg p-4">
                            <pre class="text-sm whitespace-pre-wrap">{cacheResult}</pre>
                        </div>
                    )}
                </div>
            </div>

            {/* Metrics Testing */}
            <div class="card">
                <h2 class="text-2xl font-bold mb-4">Metrics Testing</h2>
                <div class="space-y-4">
                    <div class="grid md:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Metric Name</label>
                            <input
                                type="text"
                                class="input"
                                value={metricsName}
                                onInput={(e) => setMetricsName(e.target.value)}
                            />
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Increment By</label>
                            <input
                                type="number"
                                class="input"
                                value={metricsIncrement}
                                onInput={(e) => setMetricsIncrement(e.target.value)}
                            />
                        </div>
                    </div>
                    <div class="flex space-x-2">
                        <button
                            onClick={handleIncrementCounter}
                            class="btn btn-primary"
                            disabled={metricsLoading}
                        >
                            Increment Counter
                        </button>
                        <button
                            onClick={handleSetGauge}
                            class="btn btn-secondary"
                            disabled={metricsLoading}
                        >
                            Set Gauge
                        </button>
                        <button
                            onClick={handleRecordTimer}
                            class="btn btn-secondary"
                            disabled={metricsLoading}
                        >
                            Record Timer
                        </button>
                    </div>
                    {metricsResult && (
                        <div class="bg-gray-50 rounded-lg p-4">
                            <pre class="text-sm whitespace-pre-wrap">{metricsResult}</pre>
                        </div>
                    )}
                </div>
            </div>

            {/* Rate Limiting Testing */}
            <div class="card">
                <h2 class="text-2xl font-bold mb-4">Rate Limiting Testing</h2>
                <div class="space-y-4">
                    <p class="text-sm text-gray-600">
                        Test different rate limit configurations. Make several rapid requests to trigger rate limiting.
                    </p>
                    <div class="flex space-x-2">
                        <button
                            onClick={() => handleRateLimitTest('aggressive')}
                            class="btn btn-danger"
                            disabled={rateLimitLoading}
                        >
                            Test Aggressive (10/min)
                        </button>
                        <button
                            onClick={() => handleRateLimitTest('moderate')}
                            class="btn btn-secondary"
                            disabled={rateLimitLoading}
                        >
                            Test Moderate (50/min)
                        </button>
                        <button
                            onClick={() => handleRateLimitTest('per-ip')}
                            class="btn btn-secondary"
                            disabled={rateLimitLoading}
                        >
                            Test Per-IP (20/min)
                        </button>
                    </div>
                    {rateLimitResult && (
                        <div class="bg-gray-50 rounded-lg p-4">
                            <pre class="text-sm whitespace-pre-wrap">{rateLimitResult}</pre>
                        </div>
                    )}
                </div>
            </div>

            {/* Instructions */}
            <div class="card bg-blue-50 border border-blue-200">
                <h3 class="text-lg font-semibold text-blue-900 mb-2">💡 Testing Tips</h3>
                <ul class="text-sm text-blue-800 space-y-1">
                    <li>• Check the browser console for errors and network requests</li>
                    <li>• Open DevTools Network tab to inspect CORS headers</li>
                    <li>• Rapidly click rate limit buttons to test limits</li>
                    <li>• Visit <code class="bg-blue-100 px-1 rounded">/metrics</code> to see Prometheus metrics</li>
                </ul>
            </div>
        </div>
    );
}
