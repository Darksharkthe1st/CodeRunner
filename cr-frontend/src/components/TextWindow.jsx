import SyntaxHighlighter from './SyntaxHighlighter'

function TextWindow({ text, setText, darkMode, language }) {
  const handleKeyDown = (e) => {
    if (e.key === 'Tab') {
      e.preventDefault()
      const start = e.target.selectionStart
      const end = e.target.selectionEnd
      const newText = text.substring(0, start) + '\t' + text.substring(end)
      setText(newText)

      // Set cursor position after the inserted tab
      setTimeout(() => {
        e.target.selectionStart = e.target.selectionEnd = start + 1
      }, 0)
    } else if (e.key === 'Enter') {
      e.preventDefault()
      const start = e.target.selectionStart
      const end = e.target.selectionEnd

      // Find the start of the current line
      const lineStart = text.lastIndexOf('\n', start - 1) + 1
      const currentLine = text.substring(lineStart, start)

      // Extract the indentation (leading whitespace)
      const indentMatch = currentLine.match(/^[\t ]*/)
      const indent = indentMatch ? indentMatch[0] : ''

      // Insert newline with same indentation
      const newText = text.substring(0, start) + '\n' + indent + text.substring(end)
      setText(newText)

      // Set cursor position after the inserted indentation
      setTimeout(() => {
        e.target.selectionStart = e.target.selectionEnd = start + 1 + indent.length
      }, 0)
    }
  }

  return (
    <div className={`relative w-full h-full border-2 rounded-2xl overflow-hidden ${
      darkMode ? 'border-gray-600' : 'border-gray-300'
    }`}>
      {/* Syntax highlighted background */}
      <div className={`absolute inset-0 ${darkMode ? 'bg-gray-800' : 'bg-white'}`}>
        <SyntaxHighlighter code={text} language={language} darkMode={darkMode} />
      </div>

      {/* Transparent textarea overlay */}
      <textarea
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={handleKeyDown}
        className={`relative w-full h-full p-4 resize-none focus:outline-none bg-transparent caret-${
          darkMode ? 'white' : 'black'
        } font-sans text-base ${
          darkMode ? 'text-gray-100' : 'text-gray-900'
        }`}
        style={{
          color: 'transparent',
          caretColor: darkMode ? 'white' : 'black',
          WebkitTextFillColor: 'transparent'
        }}
        placeholder="Enter your code here..."
        spellCheck="false"
      />
    </div>
  )
}

export default TextWindow
