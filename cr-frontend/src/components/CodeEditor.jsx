import Editor from '@monaco-editor/react'

function CodeEditor({ code, setCode, darkMode, language, fontSize }) {
  // Map our language names to Monaco language identifiers
  const getMonacoLanguage = (lang) => {
    const languageMap = {
      'C': 'c',
      'C++': 'cpp',
      'Java': 'java',
      'Python': 'python'
    }
    return languageMap[lang] || 'plaintext'
  }

  return (
    <div className={`w-full h-full border-2 overflow-hidden ${
      darkMode ? 'border-green-500 shadow-lg shadow-green-500/20' : 'border-green-400 shadow-lg shadow-green-400/20'
    }`}>
      <Editor
        height="100%"
        language={getMonacoLanguage(language)}
        value={code}
        onChange={(value) => setCode(value || '')}
        theme={darkMode ? 'vs-dark' : 'vs-light'}
        options={{
          minimap: { enabled: false },
          fontSize: fontSize,
          lineNumbers: 'on',
          scrollBeyondLastLine: false,
          automaticLayout: true,
          tabSize: 4,
          wordWrap: 'on',
          fontFamily: "'Consolas', 'Courier New', monospace",
          lineNumbersMinChars: 3,
          renderLineHighlight: 'all',
          scrollbar: {
            verticalScrollbarSize: 10,
            horizontalScrollbarSize: 10,
          },
        }}
      />
    </div>
  )
}

export default CodeEditor
