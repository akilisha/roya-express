import {useEffect, useState} from 'preact/hooks';
import {route} from 'preact-router';
import {uploadAPI} from '../utils/api';

export function Upload({ user }) {
    if (!user) {
        route('/login');
        return null;
    }

    const [file, setFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [uploaded, setUploaded] = useState(null);
    const [error, setError] = useState('');
    const [dragActive, setDragActive] = useState(false);
    const [files, setFiles] = useState([]);
    const [loadingFiles, setLoadingFiles] = useState(false);

    function handleDrag(e) {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === 'dragenter' || e.type === 'dragover') {
            setDragActive(true);
        } else if (e.type === 'dragleave') {
            setDragActive(false);
        }
    }

    function handleDrop(e) {
        e.preventDefault();
        e.stopPropagation();
        setDragActive(false);

        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            setFile(e.dataTransfer.files[0]);
        }
    }

    async function handleSubmit(e) {
        e.preventDefault();
        if (!file) return;

        setUploading(true);
        setError('');
        setUploaded(null);

        try {
            const data = await uploadAPI.uploadFile(file);
            setUploaded(data);
            // Refresh file list
            loadFiles();
        } catch (err) {
            setError(err.message || 'Upload failed');
        } finally {
            setUploading(false);
        }
    }

    async function loadFiles() {
        setLoadingFiles(true);
        try {
            const fileList = await uploadAPI.listFiles('uploads/');
            setFiles(fileList);
        } catch (err) {
            console.error('Failed to load files:', err);
        } finally {
            setLoadingFiles(false);
        }
    }

    useEffect(() => {
        loadFiles();
    }, []);

    function handleDownload(key) {
        const url = `http://localhost:3003/api/files/${encodeURIComponent(key)}`;
        window.open(url, '_blank');
    }

    return (
        <div class="space-y-6">
            <h1 class="text-3xl font-bold">Upload File</h1>

            <div class="card">
                <form onSubmit={handleSubmit} class="space-y-6">
                    <div
                        class={`border-2 border-dashed rounded-lg p-12 text-center transition-colors ${
                            dragActive
                                ? 'border-primary-500 bg-primary-50'
                                : 'border-gray-300 hover:border-gray-400'
                        }`}
                        onDragEnter={handleDrag}
                        onDragLeave={handleDrag}
                        onDragOver={handleDrag}
                        onDrop={handleDrop}
                    >
                        <input
                            type="file"
                            id="file"
                            class="hidden"
                            onChange={(e) => setFile(e.target.files[0])}
                        />
                        <label for="file" class="cursor-pointer">
                            <div class="text-4xl mb-4">📎</div>
                            {file ? (
                                <p class="text-lg font-medium text-gray-700">{file.name}</p>
                            ) : (
                                <>
                                    <p class="text-lg font-medium text-gray-700 mb-2">
                                        Drop your file here or click to browse
                                    </p>
                                    <p class="text-sm text-gray-500">
                                        Supports any file type
                                    </p>
                                </>
                            )}
                        </label>
                    </div>

                    {file && (
                        <div class="bg-gray-50 rounded-lg p-4">
                            <p class="text-sm text-gray-600">
                                <strong>File:</strong> {file.name}
                            </p>
                            <p class="text-sm text-gray-600">
                                <strong>Size:</strong> {(file.size / 1024).toFixed(2)} KB
                            </p>
                            <p class="text-sm text-gray-600">
                                <strong>Type:</strong> {file.type || 'Unknown'}
                            </p>
                        </div>
                    )}

                    {error && (
                        <div class="bg-red-50 border border-red-200 rounded-md p-4 text-red-700">
                            {error}
                        </div>
                    )}

                    {uploaded && (
                        <div class="bg-green-50 border border-green-200 rounded-md p-4">
                            <p class="text-green-800 font-medium mb-2">Upload Successful!</p>
                            <p class="text-sm text-green-700">
                                <strong>Key:</strong> {uploaded.key}
                            </p>
                            <p class="text-sm text-green-700">
                                <strong>Size:</strong> {uploaded.size} bytes
                            </p>
                        </div>
                    )}

                    <button
                        type="submit"
                        class="btn btn-primary w-full"
                        disabled={!file || uploading}
                    >
                        {uploading ? 'Uploading...' : 'Upload'}
                    </button>
                </form>
            </div>

            {/* File List */}
            <div class="card">
                <h2 class="text-2xl font-bold mb-4">Uploaded Files</h2>
                {loadingFiles ? (
                    <p class="text-gray-500">Loading files...</p>
                ) : files.length === 0 ? (
                    <p class="text-gray-500">No files uploaded yet.</p>
                ) : (
                    <div class="space-y-2">
                        {files.map((f) => (
                            <div key={f.key} class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                                <div class="flex-1">
                                    <p class="font-medium text-gray-700">{f.key.split('/').pop()}</p>
                                    <p class="text-sm text-gray-500">
                                        {(f.size / 1024).toFixed(2)} KB
                                    </p>
                                </div>
                                <button
                                    onClick={() => handleDownload(f.key)}
                                    class="btn btn-sm btn-secondary"
                                >
                                    Download
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

