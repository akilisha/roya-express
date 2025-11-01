import { useEffect, useState } from 'preact/hooks';
import { route } from 'preact-router';
import { articlesAPI } from '../utils/api';
import { format } from 'date-fns';

export function Articles({ user }) {
    if (!user) {
        route('/login');
        return null;
    }

    const [articles, setArticles] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadArticles();
    }, []);

    async function loadArticles() {
        try {
            const data = await articlesAPI.list();
            setArticles(data.articles || []);
        } catch (err) {
            console.error('Failed to load articles:', err);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div class="space-y-6">
            <div class="flex justify-between items-center">
                <h1 class="text-3xl font-bold">Articles</h1>
                <a href="/articles/new" class="btn btn-primary">
                    + New Article
                </a>
            </div>

            {loading ? (
                <div class="flex justify-center py-12">
                    <div class="loading-spinner"></div>
                </div>
            ) : articles.length === 0 ? (
                <div class="card text-center py-12">
                    <p class="text-gray-500 mb-4">No articles yet</p>
                    <a href="/articles/new" class="btn btn-primary">
                        Create your first article
                    </a>
                </div>
            ) : (
                <div class="space-y-4">
                    {articles.map(article => (
                        <div key={article.id} class="card hover:shadow-lg transition-shadow">
                            <div class="flex justify-between items-start">
                                <div class="flex-1">
                                    <a href={`/articles/${article.id}`} class="block">
                                        <h2 class="text-xl font-semibold text-primary-600 hover:text-primary-700 mb-2">
                                            {article.title}
                                        </h2>
                                    </a>
                                    <p class="text-gray-600 mb-3">
                                        {article.content.substring(0, 200)}...
                                    </p>
                                    {article.tags && article.tags.length > 0 && (
                                        <div class="flex flex-wrap gap-2 mb-3">
                                            {article.tags.map(tag => (
                                                <span key={tag} class="badge badge-primary">{tag}</span>
                                            ))}
                                        </div>
                                    )}
                                    <p class="text-sm text-gray-500">
                                        {format(new Date(article.created_at), 'MMMM d, yyyy')}
                                    </p>
                                </div>
                                <a href={`/articles/${article.id}/edit`} class="btn btn-secondary text-sm ml-4">
                                    Edit
                                </a>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
