const KEYWORDS = {
  C: [
    'auto', 'break', 'case', 'char', 'const', 'continue', 'default', 'do',
    'double', 'else', 'enum', 'extern', 'float', 'for', 'goto', 'if',
    'int', 'long', 'register', 'return', 'short', 'signed', 'sizeof', 'static',
    'struct', 'switch', 'typedef', 'union', 'unsigned', 'void', 'volatile', 'while',
    'include', 'define', 'ifdef', 'ifndef', 'endif', 'pragma'
  ],
  'C++': [
    'auto', 'break', 'case', 'char', 'const', 'continue', 'default', 'do',
    'double', 'else', 'enum', 'extern', 'float', 'for', 'goto', 'if',
    'int', 'long', 'register', 'return', 'short', 'signed', 'sizeof', 'static',
    'struct', 'switch', 'typedef', 'union', 'unsigned', 'void', 'volatile', 'while',
    'class', 'namespace', 'public', 'private', 'protected', 'virtual', 'friend',
    'inline', 'operator', 'template', 'typename', 'this', 'new', 'delete',
    'try', 'catch', 'throw', 'using', 'bool', 'true', 'false', 'nullptr',
    'include', 'define', 'ifdef', 'ifndef', 'endif', 'pragma'
  ],
  Java: [
    'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char',
    'class', 'const', 'continue', 'default', 'do', 'double', 'else', 'enum',
    'extends', 'final', 'finally', 'float', 'for', 'goto', 'if', 'implements',
    'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new', 'package',
    'private', 'protected', 'public', 'return', 'short', 'static', 'strictfp',
    'super', 'switch', 'synchronized', 'this', 'throw', 'throws', 'transient',
    'try', 'void', 'volatile', 'while', 'true', 'false', 'null'
  ],
  Python: [
    'False', 'None', 'True', 'and', 'as', 'assert', 'async', 'await', 'break',
    'class', 'continue', 'def', 'del', 'elif', 'else', 'except', 'finally',
    'for', 'from', 'global', 'if', 'import', 'in', 'is', 'lambda', 'nonlocal',
    'not', 'or', 'pass', 'raise', 'return', 'try', 'while', 'with', 'yield',
    'print', 'len', 'range', 'str', 'int', 'float', 'list', 'dict', 'set', 'tuple'
  ]
}

function SyntaxHighlighter({ code, language, darkMode }) {
  const highlightCode = (text) => {
    if (!text) return ''

    const keywords = KEYWORDS[language] || []

    // Escape HTML
    let escaped = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')

    // Find all special tokens (strings, comments) first
    const specialTokens = []
    let match

    // Find all strings (double quotes)
    const stringRegex = /"(?:[^"\\]|\\.)*"/g
    while ((match = stringRegex.exec(escaped)) !== null) {
      specialTokens.push({
        start: match.index,
        end: match.index + match[0].length,
        type: 'string',
        text: match[0]
      })
    }

    // Find all strings (single quotes)
    const singleStringRegex = /'(?:[^'\\]|\\.)*'/g
    while ((match = singleStringRegex.exec(escaped)) !== null) {
      specialTokens.push({
        start: match.index,
        end: match.index + match[0].length,
        type: 'string',
        text: match[0]
      })
    }

    // Find all comments (// style)
    const commentRegex = /\/\/.*/g
    while ((match = commentRegex.exec(escaped)) !== null) {
      specialTokens.push({
        start: match.index,
        end: match.index + match[0].length,
        type: 'comment',
        text: match[0]
      })
    }

    // Find all comments (# style for Python)
    if (language === 'Python') {
      const pythonCommentRegex = /#.*/g
      while ((match = pythonCommentRegex.exec(escaped)) !== null) {
        specialTokens.push({
          start: match.index,
          end: match.index + match[0].length,
          type: 'comment',
          text: match[0]
        })
      }
    }

    // Find all multi-line comments
    const multiCommentRegex = /\/\*[\s\S]*?\*\//g
    while ((match = multiCommentRegex.exec(escaped)) !== null) {
      specialTokens.push({
        start: match.index,
        end: match.index + match[0].length,
        type: 'comment',
        text: match[0]
      })
    }

    // Sort special tokens by start position
    specialTokens.sort((a, b) => a.start - b.start)

    // Build final token list with text tokens in between
    const allTokens = []
    let lastEnd = 0

    for (const token of specialTokens) {
      // Add text before this special token
      if (token.start > lastEnd) {
        allTokens.push({
          type: 'text',
          text: escaped.substring(lastEnd, token.start)
        })
      }
      // Add the special token
      allTokens.push(token)
      lastEnd = token.end
    }

    // Add remaining text
    if (lastEnd < escaped.length) {
      allTokens.push({
        type: 'text',
        text: escaped.substring(lastEnd)
      })
    }

    // Now highlight each token
    let result = ''
    for (const token of allTokens) {
      if (token.type === 'string') {
        result += `<span class="${darkMode ? 'text-green-400' : 'text-green-600'}">${token.text}</span>`
      } else if (token.type === 'comment') {
        result += `<span class="${darkMode ? 'text-gray-500' : 'text-gray-400'}">${token.text}</span>`
      } else if (token.type === 'text') {
        // Find all keywords and numbers in this text token
        const highlights = []

        // Find numbers
        const numberRegex = /\b(\d+\.?\d*|\.\d+)\b/g
        let match
        while ((match = numberRegex.exec(token.text)) !== null) {
          highlights.push({
            start: match.index,
            end: match.index + match[0].length,
            type: 'number',
            text: match[0]
          })
        }

        // Find keywords
        keywords.forEach(keyword => {
          const regex = new RegExp(`\\b${keyword}\\b`, 'g')
          while ((match = regex.exec(token.text)) !== null) {
            highlights.push({
              start: match.index,
              end: match.index + match[0].length,
              type: 'keyword',
              text: match[0]
            })
          }
        })

        // Sort highlights by position
        highlights.sort((a, b) => a.start - b.start)

        // Build highlighted text without overlaps
        let textResult = ''
        let lastPos = 0
        for (const highlight of highlights) {
          // Skip if overlapping with previous highlight
          if (highlight.start < lastPos) continue

          // Add text before highlight
          if (highlight.start > lastPos) {
            textResult += token.text.substring(lastPos, highlight.start)
          }

          // Add highlighted text
          if (highlight.type === 'number') {
            const numberColor = darkMode ? 'text-yellow-400' : 'text-orange-600'
            textResult += `<span class="${numberColor}">${highlight.text}</span>`
          } else if (highlight.type === 'keyword') {
            const keywordColor = darkMode ? 'text-blue-400' : 'text-blue-600'
            textResult += `<span class="${keywordColor} font-semibold">${highlight.text}</span>`
          }

          lastPos = highlight.end
        }

        // Add remaining text
        if (lastPos < token.text.length) {
          textResult += token.text.substring(lastPos)
        }

        result += textResult
      }
    }

    return result
  }

  return (
    <pre
      className={`w-full h-full p-4 font-sans text-base whitespace-pre-wrap break-words overflow-hidden pointer-events-none ${
        darkMode ? 'text-gray-100' : 'text-gray-900'
      }`}
      dangerouslySetInnerHTML={{ __html: highlightCode(code) }}
    />
  )
}

export default SyntaxHighlighter
