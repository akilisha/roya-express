import { render } from 'preact';
import { App } from './app';
import './index.css';
import './signals/darkMode'; // Initialize dark mode
import { initAnalytics } from './utils/analytics';

// Initialize Google Analytics
initAnalytics();

render(<App />, document.getElementById('app')!);

