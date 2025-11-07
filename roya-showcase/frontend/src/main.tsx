import { render } from 'preact';
import { App } from './app';
import './index.css';
import './signals/darkMode'; // Initialize dark mode

render(<App />, document.getElementById('app')!);

