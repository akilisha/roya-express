// Preact Signals setup
import { signal } from '@preact/signals';

// Example: Global state signals
export const codeExecutionState = signal({
  loading: false,
  output: '',
  error: ''
});

