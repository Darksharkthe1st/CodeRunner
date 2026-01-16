// Neon Cyberpunk theme for Monaco Editor - Blue, Green, Orange, Yellow color scheme
export const hackerGreenDark = {
  base: 'vs-dark',
  inherit: true,
  rules: [
    // Base text
    { token: '', foreground: '00FF00', background: '000000' },

    // Comments - dim green
    { token: 'comment', foreground: '6A9955', fontStyle: 'italic' },
    { token: 'comment.line', foreground: '6A9955', fontStyle: 'italic' },
    { token: 'comment.block', foreground: '6A9955', fontStyle: 'italic' },

    // Keywords - neon blue
    { token: 'keyword', foreground: '00BFFF', fontStyle: 'bold' },
    { token: 'keyword.control', foreground: '00BFFF', fontStyle: 'bold' },
    { token: 'keyword.operator', foreground: '00BFFF' },
    { token: 'storage', foreground: '00BFFF', fontStyle: 'bold' },
    { token: 'storage.type', foreground: '00BFFF', fontStyle: 'bold' },

    // Operators - electric blue
    { token: 'operator', foreground: '00D4FF' },
    { token: 'delimiter', foreground: '00D4FF' },
    { token: 'delimiter.bracket', foreground: '00D4FF' },
    { token: 'delimiter.parenthesis', foreground: '00D4FF' },

    // Types and Classes - bright neon blue
    { token: 'type', foreground: '0099FF' },
    { token: 'type.identifier', foreground: '0099FF' },
    { token: 'class', foreground: '0099FF', fontStyle: 'bold' },
    { token: 'class.name', foreground: '0099FF', fontStyle: 'bold' },
    { token: 'struct', foreground: '0099FF', fontStyle: 'bold' },
    { token: 'interface', foreground: '0099FF', fontStyle: 'bold' },
    { token: 'enum', foreground: '0099FF', fontStyle: 'bold' },
    { token: 'typeParameter', foreground: '0099FF' },

    // Functions and Methods - cyan blue
    { token: 'function', foreground: '00DDFF', fontStyle: 'bold' },
    { token: 'function.call', foreground: '00DDFF' },
    { token: 'method', foreground: '00DDFF' },
    { token: 'member', foreground: '00DDFF' },
    { token: 'macro', foreground: '00DDFF', fontStyle: 'bold' },

    // Variables - bright green
    { token: 'variable', foreground: '00FF00' },
    { token: 'variable.name', foreground: '00FF00' },
    { token: 'variable.parameter', foreground: '00FF88' },
    { token: 'parameter', foreground: '00FF88' },
    { token: 'identifier', foreground: '00FF00' },

    // Properties - light green
    { token: 'property', foreground: '7FFF7F' },
    { token: 'attribute', foreground: '7FFF7F' },

    // Strings - yellow/gold
    { token: 'string', foreground: 'FFFF00' },
    { token: 'string.quoted', foreground: 'FFFF00' },
    { token: 'string.escape', foreground: 'FFD700', fontStyle: 'bold' },

    // Numbers - orange
    { token: 'number', foreground: 'FF8800' },
    { token: 'number.hex', foreground: 'FF8800' },
    { token: 'number.float', foreground: 'FF8800' },
    { token: 'constant.numeric', foreground: 'FF8800' },

    // Constants - bright orange
    { token: 'constant', foreground: 'FF6600', fontStyle: 'bold' },
    { token: 'constant.language', foreground: 'FF6600', fontStyle: 'bold' },

    // Regex - neon blue
    { token: 'regexp', foreground: '00BFFF' },

    // Tags - neon blue (for HTML/XML)
    { token: 'tag', foreground: '00BFFF' },

    // Annotations/Decorators - lime green
    { token: 'annotation', foreground: '00FF00', fontStyle: 'bold' },
    { token: 'decorator', foreground: '00FF00', fontStyle: 'bold' },

    // Namespace - cyan blue
    { token: 'namespace', foreground: '00DDDD' },

    // Labels - bright green
    { token: 'label', foreground: '00FF00' },
  ],
  colors: {
    'editor.foreground': '#00FF00',
    'editor.background': '#000000',
    'editorCursor.foreground': '#00FF00',
    'editor.lineHighlightBackground': '#0A0A0A',
    'editorLineNumber.foreground': '#008800',
    'editorLineNumber.activeForeground': '#00FF00',
    'editor.selectionBackground': '#264F78',
    'editor.inactiveSelectionBackground': '#1A3A4A',
    'editorIndentGuide.background': '#1A1A1A',
    'editorIndentGuide.activeBackground': '#00FF0033',
    'editorWhitespace.foreground': '#2A2A2A',
    'editor.findMatchBackground': '#515C6A',
    'editor.findMatchHighlightBackground': '#3A4A5A',
    'editor.wordHighlightBackground': '#575757',
    'editor.wordHighlightStrongBackground': '#004972',
    'editorBracketMatch.background': '#0064001A',
    'editorBracketMatch.border': '#00FF00',
    'scrollbarSlider.background': '#00FF0033',
    'scrollbarSlider.hoverBackground': '#00FF0055',
    'scrollbarSlider.activeBackground': '#00FF0077',
  }
}

export const hackerGreenLight = {
  base: 'vs',
  inherit: true,
  rules: [
    { token: '', foreground: '006600', background: 'F5F5F5' },
    { token: 'comment', foreground: '00AA00', fontStyle: 'italic' },
    { token: 'keyword', foreground: '008800', fontStyle: 'bold' },
    { token: 'operator', foreground: '008800' },
    { token: 'namespace', foreground: '009900' },
    { token: 'type', foreground: '00AA00' },
    { token: 'struct', foreground: '00AA00' },
    { token: 'class', foreground: '00AA00' },
    { token: 'interface', foreground: '00AA00' },
    { token: 'enum', foreground: '00AA00' },
    { token: 'typeParameter', foreground: '00AA00' },
    { token: 'function', foreground: '00BB00' },
    { token: 'member', foreground: '00BB00' },
    { token: 'macro', foreground: '00BB00' },
    { token: 'variable', foreground: '008800' },
    { token: 'parameter', foreground: '008800' },
    { token: 'property', foreground: '008800' },
    { token: 'label', foreground: '008800' },
    { token: 'string', foreground: '00AA00' },
    { token: 'string.escape', foreground: '008800', fontStyle: 'bold' },
    { token: 'number', foreground: '00CC00' },
    { token: 'regexp', foreground: '00BB00' },
    { token: 'delimiter', foreground: '006600' },
    { token: 'tag', foreground: '008800' },
    { token: 'annotation', foreground: '009900' },
    { token: 'decorator', foreground: '009900' },
    { token: 'attribute', foreground: '00AA00' },
    { token: 'constant', foreground: '00CC00', fontStyle: 'bold' },
    { token: 'identifier', foreground: '006600' },
  ],
  colors: {
    'editor.foreground': '#006600',
    'editor.background': '#F5F5F5',
    'editorCursor.foreground': '#008800',
    'editor.lineHighlightBackground': '#E8F5E8',
    'editorLineNumber.foreground': '#00AA00',
    'editorLineNumber.activeForeground': '#008800',
    'editor.selectionBackground': '#C8E6C8',
    'editor.inactiveSelectionBackground': '#D8F0D8',
    'editorIndentGuide.background': '#E0F0E0',
    'editorIndentGuide.activeBackground': '#C0E0C0',
    'editorWhitespace.foreground': '#D0E8D0',
    'editor.findMatchBackground': '#A8D8A8',
    'editor.findMatchHighlightBackground': '#C8E6C8',
    'editor.wordHighlightBackground': '#C8E6C8',
    'editor.wordHighlightStrongBackground': '#A8D8A8',
    'editorBracketMatch.background': '#C8E6C8',
    'editorBracketMatch.border': '#008800',
    'scrollbarSlider.background': '#88CC8866',
    'scrollbarSlider.hoverBackground': '#66BB6688',
    'scrollbarSlider.activeBackground': '#44AA4466',
  }
}
