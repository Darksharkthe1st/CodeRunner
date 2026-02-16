import './App.css'
import CodeEditor from './components/CodeEditor'
import Terminal from './components/Terminal'
import Alert from './components/Alert'
import { useState, useEffect } from 'react'

const SUPPORTED_LANGUAGES = ["Java", "Python", "C"]

function App() {
  const [text, setText] = useState('')
  const [response, setResponse] = useState('')
  const [runData, setRunData] = useState(null)
  const [selectedLanguage, setSelectedLanguage] = useState('Python')
  const [darkMode, setDarkMode] = useState(true)
  const [fontSize, setFontSize] = useState(14)
  const [alertData, setAlertData] = useState({ show: false, success: false, exitStatus: '' })
  const [isExecuting, setIsExecuting] = useState(false)
  const [abortController, setAbortController] = useState(null)

  const apiUrl = import.meta.env.VITE_API_URL;

  useEffect(() => {
    const fetchTemplate = async () => {
      try {
        console.log(`Fetching at ${apiUrl}/get_template?language=${encodeURIComponent(selectedLanguage)} ...`);
        const res = await fetch(`${apiUrl}/get_template?language=${encodeURIComponent(selectedLanguage)}`, {
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
    if (isExecuting) {
      // Stop execution
      if (abortController) {
        abortController.abort()
      }
      return
    }

    // Create new abort controller for this execution
    const controller = new AbortController()
    setAbortController(controller)
    setIsExecuting(true)
    setRunData(null)
    setResponse('')

    try {
      // First request to /submit
      const runRes = await fetch(`${apiUrl}/submit`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code: text,
          language: selectedLanguage,
          problem: "two",
          input: "default input"
        }),
        signal: controller.signal
      })

      console.log("THIS IS WORKING");

      // Second request to /run
      // const runRes = await fetch(`${apiUrl}/run`, {
      //   method: 'POST',
      //   headers: {
      //     'Content-Type': 'application/json',
      //   },
      //   body: JSON.stringify({
      //     input: ""
      //   }),
      //   signal: controller.signal
      // })

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
      if (error.name === 'AbortError') {
        // User cancelled execution
        setRunData(null)
        setResponse(
          '╔════════════════════════════════════════╗\n' +
          '║                                        ║\n' +
          '║   EXECUTION TERMINATED BY USER         ║\n' +
          '║                                        ║\n' +
          '╚════════════════════════════════════════╝\n\n' +
          '> Process was stopped during execution.\n'
        )
        setAlertData({
          show: true,
          success: false,
          exitStatus: 'Execution terminated by user'
        })
      } else {
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
    } finally {
      setIsExecuting(false)
      setAbortController(null)
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
          <h1 className={`text-2xl tracking-wider ${darkMode ? 'text-green-400' : 'text-green-500'}`} style={{ fontFamily: "'Cascadia Code', monospace", fontWeight: 700 }}>
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
                className={`h-10 px-6 border-2 transition-colors font-semibold font-mono tracking-wider ${
                  isExecuting
                    ? darkMode
                      ? 'border-red-500 bg-red-500 bg-opacity-10 text-red-400 hover:bg-opacity-20'
                      : 'border-red-400 bg-red-400 bg-opacity-10 text-red-500 hover:bg-opacity-20'
                    : darkMode
                      ? 'border-green-500 bg-green-500 bg-opacity-10 text-green-400 hover:bg-opacity-20'
                      : 'border-green-400 bg-green-400 bg-opacity-10 text-green-500 hover:bg-opacity-20'
                }`}
              >
                {isExecuting ? '[STOP]' : '[RUN]'}
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
      <div className="flex-1 grid grid-cols-2 gap-4 p-4 min-h-0">

        {/* Left Side - Code Editor */}
        <div className="flex flex-col min-h-0">
          <div className={`flex items-center gap-2 mb-2 pb-2 border-b-2 ${darkMode ? 'border-green-500' : 'border-green-400'}`}>
            <span className={`text-lg font-bold font-mono ${darkMode ? 'text-green-400' : 'text-green-600'}`}>{'>'}</span>
            <h2 className={`text-lg font-semibold font-mono tracking-wide ${darkMode ? 'text-green-400' : 'text-green-600'}`}>
              EDITOR
            </h2>
          </div>
          <div className="flex-1 min-h-0">
            <CodeEditor code={text} setCode={setText} darkMode={darkMode} language={selectedLanguage} fontSize={fontSize} />
          </div>
        </div>

        {/* Right Side - Terminal Output */}
        <div className="flex flex-col min-h-0">
          <div className={`flex items-center gap-2 mb-2 pb-2 border-b-2 ${darkMode ? 'border-green-500' : 'border-green-400'}`}>
            <span className={`text-lg font-bold font-mono ${darkMode ? 'text-green-400' : 'text-green-600'}`}>{'>'}</span>
            <h2 className={`text-lg font-semibold font-mono tracking-wide ${darkMode ? 'text-green-400' : 'text-green-600'}`}>
              TERMINAL
            </h2>
          </div>
          <div className="flex-1 min-h-0">
            <Terminal output={response} darkMode={darkMode} fontSize={fontSize} runData={runData} isExecuting={isExecuting} />
          </div>
        </div>


      </div>
    </div>
  )
}

export default App
