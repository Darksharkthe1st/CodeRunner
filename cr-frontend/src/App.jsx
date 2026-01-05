import './App.css'
import TextWindow from './components/TextWindow'
import { useState, useEffect } from 'react'

const SUPPORTED_LANGUAGES = ["C", "C++", "Java", "Python"]

function App() {
  const [text, setText] = useState('')
  const [response, setResponse] = useState('')
  const [selectedLanguage, setSelectedLanguage] = useState('C')
  const [darkMode, setDarkMode] = useState(false)

  useEffect(() => {
    const fetchTemplate = async () => {
      try {
        const res = await fetch(`http://localhost:8080/get_template?language=${encodeURIComponent(selectedLanguage)}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          }
        })

        const template = await res.text()
        setText(template)
      } catch (error) {
        console.error('Error fetching template:', error)
      }
    }

    fetchTemplate()
  }, [selectedLanguage])

  const handleSubmit = async () => {
    try {
      // First request to /submit
      await fetch('http://localhost:8080/submit', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code: text,
          language: selectedLanguage,
          problem: "two"
        })
      })

      // Second request to /run
      const runRes = await fetch('http://localhost:8080/run', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          input: ""
        })
      })

      const runData = await runRes.json()
      setResponse(JSON.stringify(runData, null, 2))
      console.log('Run Response:', runData)
    } catch (error) {
      setResponse(`Error: ${error.message}`)
      console.error('Error:', error)
    }
  }

  return (
    <div className={`min-h-screen flex flex-col ${darkMode ? 'bg-gray-900' : 'bg-white'}`}>
      {/* Header */}
      <header className={`${darkMode ? 'bg-gray-800' : 'bg-blue-600'} text-white p-4 shadow-lg`}>
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold">Code Runner</h1>
          <div className="flex items-center gap-4">
            <button
              onClick={() => setDarkMode(!darkMode)}
              className="px-4 py-2 bg-white bg-opacity-20 rounded-lg hover:bg-opacity-30 transition-colors"
            >
              {darkMode ? '☀️' : '🌙'}
            </button>
            <select
              value={selectedLanguage}
              onChange={(e) => setSelectedLanguage(e.target.value)}
              className={`px-4 py-2 border-2 ${darkMode ? 'border-gray-600 bg-gray-700' : 'border-white bg-blue-500'} text-white rounded-lg focus:outline-none focus:border-blue-300`}
            >
              {SUPPORTED_LANGUAGES.map((lang) => (
                <option key={lang} value={lang}>
                  {lang}
                </option>
              ))}
            </select>
            <button
              onClick={handleSubmit}
              className={`px-6 py-2 ${darkMode ? 'bg-blue-500 hover:bg-blue-600' : 'bg-white text-blue-600 hover:bg-blue-50'} rounded-lg transition-colors font-semibold`}
            >
              Submit
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex-1 grid grid-cols-2 gap-4 p-4">

        {/* Left Side - Input Box */}
        <div className="flex flex-col">
          <h2 className={`text-lg font-semibold mb-2 ${darkMode ? 'text-white' : 'text-gray-900'}`}>Code</h2>
          <div className="flex-1">
            <TextWindow text={text} setText={setText} darkMode={darkMode} language={selectedLanguage} />
          </div>
        </div>

        {/* Right Side - Result Box */}
        <div className="flex flex-col">
          <h2 className={`text-lg font-semibold mb-2 ${darkMode ? 'text-white' : 'text-gray-900'}`}>Output</h2>
          <textarea
            value={response}
            readOnly
            className={`flex-1 p-4 border-2 rounded-2xl resize-none font-mono text-sm ${
              darkMode
                ? 'bg-gray-800 border-gray-600 text-gray-100'
                : 'bg-gray-50 border-gray-300 text-gray-900'
            }`}
            placeholder="Results will appear here..."
          />
        </div>


      </div>
    </div>
  )
}

export default App
