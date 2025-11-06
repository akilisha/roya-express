import { signal } from '@preact/signals';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export function useCodeExecution() {
  const output = signal('');
  const loading = signal(false);
  const error = signal('');

  const execute = async (code: string) => {
    loading.value = true;
    error.value = '';
    output.value = '';

    try {
      const response = await fetch(`${API_BASE_URL}/api/execute`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ code }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
      }

      const result = await response.json();
      
      if (result.success) {
        output.value = result.output || 'Code executed successfully (no output)';
      } else {
        error.value = result.error || 'Execution failed';
      }
    } catch (err: any) {
      error.value = err.message || 'Failed to execute code';
      console.error('Execution error:', err);
    } finally {
      loading.value = false;
    }
  };

  return { execute, output, loading, error };
}


