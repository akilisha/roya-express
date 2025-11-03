import {Link} from 'preact-router/match';

export function Layout({ children, user, onLogout }) {
    return (
        <div class="min-h-screen bg-gray-50">
            {/* Navigation */}
            <nav class="bg-white shadow-sm border-b border-gray-200">
                <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div class="flex justify-between h-16">
                        <div class="flex">
                            <Link href="/" class="flex items-center">
                                <span class="text-xl font-bold text-primary-600">DocuRoya</span>
                            </Link>
                            <div class="flex space-x-1 ml-10">
                                <Link activeClassName="nav-link-active" className="nav-link" href="/">
                                    Home
                                </Link>
                                <Link activeClassName="nav-link-active" className="nav-link" href="/articles">
                                    Articles
                                </Link>
                                {user && (
                                    <>
                                        <Link activeClassName="nav-link-active" className="nav-link" href="/upload">
                                            Upload
                                        </Link>
                                        <Link activeClassName="nav-link-active" className="nav-link" href="/chat">
                                            Chat
                                        </Link>
                                        <Link activeClassName="nav-link-active" className="nav-link" href="/testing">
                                            Testing
                                        </Link>
                                    </>
                                )}
                            </div>
                        </div>
                        <div class="flex items-center space-x-4">
                            {user ? (
                                <>
                                    <span class="text-sm text-gray-600">Logged in as {user.email}</span>
                                    <Link href="/" className="btn btn-secondary text-sm" onClick={onLogout}>
                                        Logout
                                    </Link>
                                </>
                            ) : (
                                <>
                                    <Link href="/login" className="btn btn-secondary text-sm">
                                        Login
                                    </Link>
                                    <Link href="/register" className="btn btn-primary text-sm">
                                        Register
                                    </Link>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </nav>

            {/* Main content */}
            <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {children}
            </main>

            {/* Footer */}
            <footer class="bg-white border-t border-gray-200 mt-16">
                <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <p class="text-center text-sm text-gray-500">
                        DocuRoya - A comprehensive knowledge base platform showcasing Roya framework
                    </p>
                </div>
            </footer>
        </div>
    );
}

