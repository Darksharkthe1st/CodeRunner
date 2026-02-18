import { useState } from 'react'

function ChatInterface({ darkMode, fontSize, apiUrl }) {
  const [messages, setMessages] = useState('')
  const [inputText, setInputText] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const handleSendMessage = async () => {
    if (!inputText.trim()) return

    setIsLoading(true)
    setMessages('> Completing Request...')

    try {
      const response = await fetch(`${apiUrl}/llm/ask`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(inputText)
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const result = await response.text()
      setMessages(`> User:\n${inputText}\n\n> AI Response:\n${result}`)
      setInputText('')
    } catch (error) {
      console.error('Error sending message:', error)
      setMessages(`> Error\n=====================================\n\n${error.message}`)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="w-full h-full flex flex-col">
      {/* Non-editable Message Display Window (70%) */}
      <div
        className={`border-2 overflow-auto ${
          darkMode ? 'border-green-500 bg-black' : 'border-green-400 bg-gray-900'
        }`}
        style={{ height: '70%' }}
      >
        <div
          className={`w-full h-full p-4 font-mono whitespace-pre-wrap ${
            darkMode ? 'text-green-400' : 'text-green-500'
          }`}
          style={{ fontSize: `${fontSize}px` }}
        >
          {messages || '> Code Helper\n> Ready to assist...'}
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
          }`}
          style={{ fontSize: `${fontSize}px` }}
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
