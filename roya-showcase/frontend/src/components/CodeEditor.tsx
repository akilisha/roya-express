import { useEffect, useRef } from 'preact/hooks';
import { signal } from '@preact/signals';

// Monaco Editor component
export function CodeEditor({ 
  value = signal(''), 
  language = 'java', 
  onChange,
  height = '400px'
}: { 
  value?: ReturnType<typeof signal<string>>;
  language?: string;
  onChange?: (value: string) => void;
  height?: string;
}) {
  const containerRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<any>(null);

  useEffect(() => {
    if (!containerRef.current) return;

    let mounted = true;

    // Dynamic import Monaco Editor
    import('monaco-editor').then((monaco) => {
      if (!mounted || !containerRef.current) return;

      // Create editor instance
      editorRef.current = monaco.editor.create(containerRef.current, {
        value: value.value,
        language: language,
        theme: 'vs-dark',
        automaticLayout: true,
        minimap: { enabled: false },
        fontSize: 14,
        wordWrap: 'on',
        lineNumbers: 'on',
        scrollBeyondLastLine: false,
        tabSize: 4,
        insertSpaces: true,
        roundedSelection: false,
        readOnly: false,
        cursorStyle: 'line',
        fontFamily: 'Fira Code, Consolas, Monaco, monospace',
      });

      // Listen for changes
      editorRef.current.onDidChangeModelContent(() => {
        const newValue = editorRef.current.getValue();
        value.value = newValue;
        onChange?.(newValue);
      });
    }).catch(err => {
      console.error('Failed to load Monaco Editor:', err);
    });

    return () => {
      mounted = false;
      if (editorRef.current) {
        editorRef.current.dispose();
        editorRef.current = null;
      }
    };
  }, [language]);

  // Update editor when value changes externally
  useEffect(() => {
    if (editorRef.current && editorRef.current.getValue() !== value.value) {
      editorRef.current.setValue(value.value);
    }
  }, [value.value]);

  return (
    <div 
      ref={containerRef} 
      style={{ height, width: '100%', border: '1px solid #ccc', borderRadius: '4px' }}
    />
  );
}


