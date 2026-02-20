import { useState } from 'react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'

const SYSTEM_PROMPT = "You are Code_Helper, a sub-agent of Code_Runner, which is an online Remote Code Execution platform. You are given the user's code and a chat between the user, use the context to help them debug their code, pointing out design flaws, mistakes, and where errors may be arising from if present."

function ChatInterface({ darkMode, fontSize, apiUrl, code, language, input, result }) {
  const [messages, setMessages] = useState([])
  const [inputText, setInputText] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  // Custom scrollbar styles
  const scrollbarStyle = {
    scrollbarWidth: 'thin',
    scrollbarColor: darkMode ? '#00FF00 #000000' : '#00CC00 #1a1a1a',
  }

  const scrollbarWebkitClass = darkMode ? 'terminal-scrollbar-dark' : 'terminal-scrollbar-light'

  // Terminal-style markdown components
  const markdownComponents = {
    h1: ({node, ...props}) => (
      <h1 className={`font-mono font-bold text-xl mb-2 ${darkMode ? 'text-green-400' : 'text-green-500'}`} {...props} />
    ),
    h2: ({node, ...props}) => (
      <h2 className={`font-mono font-bold text-lg mb-2 ${darkMode ? 'text-green-400' : 'text-green-500'}`} {...props} />
    ),
    h3: ({node, ...props}) => (
      <h3 className={`font-mono font-bold text-base mb-2 ${darkMode ? 'text-green-400' : 'text-green-500'}`} {...props} />
    ),
    p: ({node, ...props}) => (
      <p className={`font-mono ${darkMode ? 'text-green-400' : 'text-green-500'}`} style={{ marginBottom: '0.5em' }} {...props} />
    ),
    code: ({node, inline, className, ...props}) => {
      // Check if this is inline code (not in a pre block)
      const isInline = inline !== false;
      return isInline ? (
        <code className={`font-mono px-1 ${darkMode ? 'text-green-300 bg-gray-900' : 'text-green-600 bg-gray-800'}`} {...props} />
      ) : (
        <code className={`font-mono ${darkMode ? 'text-green-300 bg-gray-900' : 'text-green-600 bg-gray-800'}`} {...props} />
      );
    },
    pre: ({node, ...props}) => (
      <pre className={`font-mono p-3 my-2 overflow-x-auto ${darkMode ? 'bg-gray-900 border border-green-500' : 'bg-gray-800 border border-green-400'}`} {...props} />
    ),
    a: ({node, ...props}) => (
      <a className={`font-mono underline ${darkMode ? 'text-green-300 hover:text-green-200' : 'text-green-600 hover:text-green-700'}`} style={{ display: 'inline' }} {...props} />
    ),
    ul: ({node, ...props}) => (
      <ul className={`font-mono list-disc list-inside mb-2 ${darkMode ? 'text-green-400' : 'text-green-500'}`} {...props} />
    ),
    ol: ({node, ...props}) => (
      <ol className={`font-mono list-decimal list-inside mb-2 ${darkMode ? 'text-green-400' : 'text-green-500'}`} {...props} />
    ),
    li: ({node, ...props}) => (
      <li className={`font-mono ${darkMode ? 'text-green-400' : 'text-green-500'}`} {...props} />
    ),
    blockquote: ({node, ...props}) => (
      <blockquote className={`font-mono border-l-4 pl-4 my-2 italic ${darkMode ? 'border-green-500 text-green-400' : 'border-green-400 text-green-500'}`} {...props} />
    ),
    strong: ({node, ...props}) => (
      <strong className={`font-mono font-bold ${darkMode ? 'text-green-300' : 'text-green-600'}`} style={{ display: 'inline' }} {...props} />
    ),
    em: ({node, ...props}) => (
      <em className={`font-mono italic ${darkMode ? 'text-green-400' : 'text-green-500'}`} style={{ display: 'inline' }} {...props} />
    ),
  }

  const handleSendMessage = async () => {
    if (!inputText.trim()) return

    const userMessage = inputText
    setIsLoading(true)

    // Add user message to the list (using lowercase role for consistency)
    setMessages(prev => [...prev, { role: 'user', content: userMessage }])
    setInputText('')

    // Add temporary loading message
    setMessages(prev => [...prev, { role: 'agent', content: 'Completing Request...' }])

    try {
      // Build messages array for API: system message + conversation history + current message
      const apiMessages = [
        { role: 'system', content: SYSTEM_PROMPT },
        ...messages.map(msg => ({ role: msg.role, content: msg.content })),
        { role: 'user', content: userMessage }
      ]

      // Build code object matching /submit route format
      const codeObject = {
        code: code,
        language: language,
        input: input,
        problem: "one"
      }

      // Build result object - use actual result or default structure
      const resultObject = result || {
        success: false,
        runtime: -1.0,
        output: "",
        error: "",
        exitStatus: "",
        status: "CODE WAS NEVER RAN, NO OUTPUT DATA HERE"
      }

      // Build complete request body
      const requestBody = {
        messages: apiMessages,
        code: codeObject,
        result: resultObject
      }

      const response = await fetch(`${apiUrl}/llm/message`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody)
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const agentResponse = await response.text()

      // Replace the loading message with the actual response
      setMessages(prev => {
        const newMessages = [...prev]
        newMessages[newMessages.length - 1] = { role: 'agent', content: agentResponse }
        return newMessages
      })
    } catch (error) {
      console.error('Error sending message:', error)

      // Replace the loading message with error message
      setMessages(prev => {
        const newMessages = [...prev]
        newMessages[newMessages.length - 1] = {
          role: 'agent',
          content: `Error\n=====================================\n\n${error.message}`
        }
        return newMessages
      })
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="w-full h-full flex flex-col min-h-0">
      {/* Non-editable Message Display Window (70%) */}
      <div
        className={`border-2 overflow-y-auto ${
          darkMode ? 'border-green-500 bg-black' : 'border-green-400 bg-gray-900'
        } ${scrollbarWebkitClass}`}
        style={{ height: '70%', ...scrollbarStyle }}
      >
        <div
          className={`w-full p-4 ${
            darkMode ? 'text-green-400' : 'text-green-500'
          }`}
          style={{ fontSize: `${fontSize}px` }}
        >
          {messages.length === 0 ? (
            <div className="font-mono whitespace-pre-wrap">> Code Helper{'\n'}> Ready to assist...</div>
          ) : (
            messages.map((msg, index) => (
              <div key={index} className="mb-4">
                <div className="font-mono font-bold mb-1">
                  {msg.role === 'user' ? '> User:' : '> AI Response:'}
                </div>
                <ReactMarkdown
                  remarkPlugins={[remarkGfm]}
                  components={markdownComponents}
                >
                  {msg.content}
                </ReactMarkdown>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Editable Input Window with Send Button (30%) */}
      <div
        className={`border-2 border-t-0 flex ${
          darkMode ? 'border-green-500 shadow-lg shadow-green-500/20' : 'border-green-400 shadow-lg shadow-green-400/20'
        }`}
        style={{ height: '30%' }}
      >
        <textarea
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          className={`flex-1 p-4 resize-none focus:outline-none font-mono border-r-2 ${
            darkMode ? 'bg-black text-green-400 border-green-500' : 'bg-gray-900 text-green-500 border-green-400'
          } ${scrollbarWebkitClass}`}
          style={{ fontSize: `${fontSize}px`, ...scrollbarStyle }}
          placeholder="> Type your message here..."
          spellCheck="false"
        />
        <button
          onClick={handleSendMessage}
          disabled={isLoading || !inputText.trim()}
          className={`flex items-center justify-center transition-colors font-bold ${
            isLoading || !inputText.trim()
              ? darkMode
                ? 'bg-black text-green-800 cursor-not-allowed'
                : 'bg-gray-900 text-green-800 cursor-not-allowed'
              : darkMode
                ? 'bg-black text-green-400 hover:bg-green-500 hover:bg-opacity-10'
                : 'bg-gray-900 text-green-500 hover:bg-green-400 hover:bg-opacity-10'
          }`}
          style={{ width: '50px' }}
          title="Send Message"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
          </svg>
        </button>
      </div>
    </div>
  )
}

export default ChatInterface
