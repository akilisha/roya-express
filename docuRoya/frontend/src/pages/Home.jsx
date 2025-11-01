import { useEffect, useState } from 'preact/hooks';
import { articlesAPI } from '../utils/api';
import { format } from 'date-fns';
import { parseDate } from '../utils/date';

export function Home({ user }) {
    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        loadHotArticles();
    }, []);

    async function loadHotArticles() {
        try {
            const data = await articlesAPI.hot();
            setArticles(data.articles || []);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div class="space-y-8">
            {/* Hero Section */}
            <div class="text-center py-12">
                <h1 class="text-4xl font-bold text-gray-900 mb-4">
                    Welcome to DocuRoya
                </h1>
                <p class="text-xl text-gray-600 mb-8">
                    A comprehensive knowledge base platform built with Roya framework
                </p>
                {!user && (
                    <div class="space-x-4">
                        <a href="/register" class="btn btn-primary">
                            Get Started
                        </a>
                        <a href="/login" class="btn btn-secondary">
                            Login
                        </a>
                    </div>
                )}
            </div>

            {/* Hot Articles */}
            <div class="card">
                <h2 class="text-2xl font-bold mb-6">Hot Articles</h2>

                {loading && (
                    <div class="flex justify-center py-12">
                        <div class="loading-spinner"></div>
                    </div>
                )}

                {error && (
                    <div class="bg-red-50 border border-red-200 rounded-md p-4 text-red-700">
                        {error}
                    </div>
                )}

                {!loading && !error && articles.length === 0 && (
                    <p class="text-gray-500 text-center py-8">
                        No articles yet. {user ? 'Create your first article!' : 'Register to get started!'}
                    </p>
                )}

                {!loading && !error && articles.length > 0 && (
                    <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                        {articles.map(article => (
                            <div key={article.id} class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                                <h3 class="font-semibold text-lg mb-2">
                                    <a href={`/articles/${article.id}`} class="text-primary-600 hover:text-primary-700">
                                        {article.title}
                                    </a>
                                </h3>
                                <p class="text-gray-600 text-sm mb-3 line-clamp-3">
                                    {article.content.substring(0, 150)}...
                                </p>
                                {article.tags && article.tags.length > 0 && (
                                    <div class="flex flex-wrap gap-2 mb-3">
                                        {article.tags.map(tag => (
                                            <span key={tag} class="badge badge-primary">{tag}</span>
                                        ))}
                                    </div>
                                )}
                                <p class="text-xs text-gray-500">
                                    {format(parseDate(article.createdAt || article.created_at), 'MMM d, yyyy')}
                                </p>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Features Grid */}
            <div class="grid md:grid-cols-3 gap-6">
                <div class="card text-center">
                    <div class="text-4xl mb-4">📚</div>
                    <h3 class="font-semibold text-lg mb-2">Rich Documentation</h3>
                    <p class="text-gray-600 text-sm">
                        Create and organize your knowledge with a powerful article system
                    </p>
                </div>
                <div class="card text-center">
                    <div class="text-4xl mb-4">🔍</div>
                    <h3 class="font-semibold text-lg mb-2">Smart Search</h3>
                    <p class="text-gray-600 text-sm">
                        AI-powered search using vector embeddings and semantic similarity
                    </p>
                </div>
                <div class="card text-center">
                    <div class="text-4xl mb-4">🚀</div>
                    <h3 class="font-semibold text-lg mb-2">Lightning Fast</h3>
                    <p class="text-gray-600 text-sm">
                        Built on Roya with PostgreSQL, caching, and virtual threads
                    </p>
                </div>
            </div>
        </div>
    );
}

