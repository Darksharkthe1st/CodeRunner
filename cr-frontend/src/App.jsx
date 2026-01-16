import './App.css'
import CodeEditor from './components/CodeEditor'
import Terminal from './components/Terminal'
import Alert from './components/Alert'
import { useState, useEffect } from 'react'

const SUPPORTED_LANGUAGES = ["C", "C++", "Java", "Python"]

function App() {
  const [text, setText] = useState('')
  const [response, setResponse] = useState('')
  const [runData, setRunData] = useState(null)
  const [selectedLanguage, setSelectedLanguage] = useState('C')
  const [darkMode, setDarkMode] = useState(false)
  const [fontSize, setFontSize] = useState(14)
  const [alertData, setAlertData] = useState({ show: false, success: false, exitStatus: '' })

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

      const data = await runRes.json()

      // Store the run data for Terminal component
      setRunData(data)
      setResponse('') // Clear plain text response

      // Show alert with exit status
      setAlertData({
        show: true,
        success: data.success,
        exitStatus: data.exitStatus || (data.success ? 'Execution completed successfully' : 'Execution failed')
      })

      console.log('Run Response:', data)
    } catch (error) {
      // For network errors, display as plain text
      setRunData(null)
      setResponse(`> Error\n=====================================\n\n${error.message}`)
      setAlertData({
        show: true,
        success: false,
        exitStatus: `Network Error: ${error.message}`
      })
      console.error('Error:', error)
    }
  }

  const zoomIn = () => {
    setFontSize(prev => Math.min(prev + 2, 32))
  }

  const zoomOut = () => {
    setFontSize(prev => Math.max(prev - 2, 8))
  }

  const handleCloseAlert = () => {
    setAlertData({ show: false, success: false, exitStatus: '' })
  }

  return (
    <div className={`min-h-screen flex flex-col ${darkMode ? 'bg-black' : 'bg-gray-100'}`}>
      {/* Header */}
      <header className={`${darkMode ? 'bg-gray-950 border-b-2 border-green-500' : 'bg-gray-900 border-b-2 border-green-400'} text-white p-4 shadow-xl`}>
        <div className="flex justify-between items-center px-4">
          <h1 className={`text-2xl font-bold tracking-wider ${darkMode ? 'text-green-400' : 'text-green-500'}`}>
            {'>'} CODE_RUNNER
          </h1>
          <div className="flex items-center gap-3 relative">
            <button
              onClick={zoomOut}
              className={`w-10 h-10 border-2 ${darkMode ? 'border-green-500 text-green-400 hover:bg-green-500 hover:bg-opacity-10' : 'border-green-400 text-green-500 hover:bg-green-400 hover:bg-opacity-10'} transition-colors font-mono text-xl font-bold flex items-center justify-center`}
              title="Zoom Out"
            >
              -
            </button>
            <button
              onClick={zoomIn}
              className={`w-10 h-10 border-2 ${darkMode ? 'border-green-500 text-green-400 hover:bg-green-500 hover:bg-opacity-10' : 'border-green-400 text-green-500 hover:bg-green-400 hover:bg-opacity-10'} transition-colors font-mono text-xl font-bold flex items-center justify-center`}
              title="Zoom In"
            >
              +
            </button>
            <button
              onClick={() => setDarkMode(!darkMode)}
              className={`h-10 px-4 border-2 ${darkMode ? 'border-green-500 text-green-400 hover:bg-green-500 hover:bg-opacity-10' : 'border-green-400 text-green-500 hover:bg-green-400 hover:bg-opacity-10'} transition-colors font-mono`}
            >
              {darkMode ? '[LIGHT]' : '[DARK]'}
            </button>
            <select
              value={selectedLanguage}
              onChange={(e) => setSelectedLanguage(e.target.value)}
              className={`h-10 px-4 border-2 ${darkMode ? 'border-green-500 bg-black text-green-400' : 'border-green-400 bg-gray-900 text-green-500'} focus:outline-none focus:ring-2 focus:ring-green-500 font-mono`}
            >
              {SUPPORTED_LANGUAGES.map((lang) => (
                <option key={lang} value={lang} className="bg-black">
                  {lang}
                </option>
              ))}
            </select>
            <div className="relative">
              <button
                onClick={handleSubmit}
                className={`h-10 px-6 border-2 ${darkMode ? 'border-green-500 bg-green-500 bg-opacity-10 text-green-400 hover:bg-opacity-20' : 'border-green-400 bg-green-400 bg-opacity-10 text-green-500 hover:bg-opacity-20'} transition-colors font-semibold font-mono tracking-wider`}
              >
                [RUN]
              </button>
              {alertData.show && (
                <Alert
                  success={alertData.success}
                  exitStatus={alertData.exitStatus}
                  onClose={handleCloseAlert}
                  darkMode={darkMode}
                />
              )}
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex-1 grid grid-cols-2 gap-4 p-4">

        {/* Left Side - Code Editor */}
        <div className="flex flex-col">
          <div className={`flex items-center gap-2 mb-2 pb-2 border-b-2 ${darkMode ? 'border-green-500' : 'border-green-400'}`}>
            <span className={`text-lg font-bold font-mono ${darkMode ? 'text-green-400' : 'text-green-600'}`}>{'>'}</span>
            <h2 className={`text-lg font-semibold font-mono tracking-wide ${darkMode ? 'text-green-400' : 'text-green-600'}`}>
              EDITOR
            </h2>
          </div>
          <div className="flex-1">
            <CodeEditor code={text} setCode={setText} darkMode={darkMode} language={selectedLanguage} fontSize={fontSize} />
          </div>
        </div>

        {/* Right Side - Terminal Output */}
        <div className="flex flex-col">
          <div className={`flex items-center gap-2 mb-2 pb-2 border-b-2 ${darkMode ? 'border-green-500' : 'border-green-400'}`}>
            <span className={`text-lg font-bold font-mono ${darkMode ? 'text-green-400' : 'text-green-600'}`}>{'>'}</span>
            <h2 className={`text-lg font-semibold font-mono tracking-wide ${darkMode ? 'text-green-400' : 'text-green-600'}`}>
              TERMINAL
            </h2>
          </div>
          <div className="flex-1">
            <Terminal output={response} darkMode={darkMode} fontSize={fontSize} runData={runData} />
          </div>
        </div>


      </div>
    </div>
  )
}

export default App
