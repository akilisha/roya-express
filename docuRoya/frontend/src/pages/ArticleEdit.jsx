import { useEffect, useState } from 'preact/hooks';
import { route } from 'preact-router';
import { articlesAPI } from '../utils/api';

export function ArticleEdit({ id, user }) {
    if (!user) {
        route('/login');
        return null;
    }

    const isNew = id === 'new';
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [tags, setTags] = useState('');
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!isNew) {
            loadArticle();
        }
    }, [id]);

    async function loadArticle() {
        setLoading(true);
        try {
            const data = await articlesAPI.get(id);
            setTitle(data.title);
            setContent(data.content);
            setTags((data.tags || []).join(', '));
        } catch (err) {
            setError('Failed to load article');
        } finally {
            setLoading(false);
        }
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setSaving(true);
        setError('');

        try {
            const tagsArray = tags.split(',').map(t => t.trim()).filter(t => t);

            if (isNew) {
                await articlesAPI.create(title, content, tagsArray);
            } else {
                await articlesAPI.update(id, title, content, tagsArray);
            }

            route('/articles');
        } catch (err) {
            setError(err.message || 'Failed to save article');
        } finally {
            setSaving(false);
        }
    }

    if (loading) {
        return (
            <div class="flex justify-center py-12">
                <div class="loading-spinner"></div>
            </div>
        );
    }

    return (
        <div class="space-y-6">
            <a href="/articles" class="text-primary-600 hover:text-primary-700">
                ← Back to Articles
            </a>

            <div class="card">
                <h1 class="text-2xl font-bold mb-6">
                    {isNew ? 'New Article' : 'Edit Article'}
                </h1>

                {error && (
                    <div class="bg-red-50 border border-red-200 rounded-md p-4 text-red-700 mb-4">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} class="space-y-4">
                    <div>
                        <label for="title" class="block text-sm font-medium text-gray-700 mb-1">
                            Title
                        </label>
                        <input
                            id="title"
                            type="text"
                            class="input"
                            value={title}
                            onInput={(e) => setTitle(e.target.value)}
                            required
                        />
                    </div>

                    <div>
                        <label for="content" class="block text-sm font-medium text-gray-700 mb-1">
                            Content
                        </label>
                        <textarea
                            id="content"
                            class="input"
                            rows="15"
                            value={content}
                            onInput={(e) => setContent(e.target.value)}
                            required
                        />
                    </div>

                    <div>
                        <label for="tags" class="block text-sm font-medium text-gray-700 mb-1">
                            Tags (comma-separated)
                        </label>
                        <input
                            id="tags"
                            type="text"
                            class="input"
                            value={tags}
                            onInput={(e) => setTags(e.target.value)}
                            placeholder="java, tutorial, documentation"
                        />
                    </div>

                    <div class="flex space-x-4">
                        <button
                            type="submit"
                            class="btn btn-primary"
                            disabled={saving}
                        >
                            {saving ? 'Saving...' : 'Save'}
                        </button>
                        <a href="/articles" class="btn btn-secondary">
                            Cancel
                        </a>
                    </div>
                </form>
            </div>
        </div>
    );
}
