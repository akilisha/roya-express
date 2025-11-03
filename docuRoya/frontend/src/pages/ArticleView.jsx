import {useEffect, useState} from 'preact/hooks';
import {articlesAPI} from '../utils/api';
import {format} from 'date-fns';
import {parseDate} from '../utils/date';

export function ArticleView({ id, user }) {
    const [article, setArticle] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadArticle();
    }, [id]);

    async function loadArticle() {
        try {
            const data = await articlesAPI.get(id);
            setArticle(data);
        } catch (err) {
            console.error('Failed to load article:', err);
        } finally {
            setLoading(false);
        }
    }

    if (loading) {
        return (
            <div class="flex justify-center py-12">
                <div class="loading-spinner"></div>
            </div>
        );
    }

    if (!article) {
        return (
            <div class="card">
                <h1 class="text-2xl font-bold mb-4">Article Not Found</h1>
                <a href="/articles" class="btn btn-secondary">
                    Back to Articles
                </a>
            </div>
        );
    }

    return (
        <div class="space-y-6">
            <a href="/articles" class="text-primary-600 hover:text-primary-700">
                ← Back to Articles
            </a>

            <div class="card">
                <div class="flex justify-between items-start mb-6">
                    <div class="flex-1">
                        <h1 class="text-3xl font-bold mb-4">{article.title}</h1>
                        {article.tags && article.tags.length > 0 && (
                            <div class="flex flex-wrap gap-2 mb-4">
                                {article.tags.map(tag => (
                                    <span key={tag} class="badge badge-primary">{tag}</span>
                                ))}
                            </div>
                        )}
                        <p class="text-sm text-gray-500">
                            {format(parseDate(article.createdAt || article.created_at), 'MMMM d, yyyy')}
                        </p>
                    </div>
                    {user && (
                        <a href={`/articles/${article.id}/edit`} class="btn btn-secondary">
                            Edit
                        </a>
                    )}
                </div>

                <div class="prose max-w-none">
                    <p class="whitespace-pre-wrap">{article.content}</p>
                </div>
            </div>
        </div>
    );
}
