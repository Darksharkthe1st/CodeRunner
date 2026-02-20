import './App.css'
import CodeEditor from './components/CodeEditor'
import Terminal from './components/Terminal'
import Alert from './components/Alert'
import ChatInterface from './components/ChatInterface'
import { useState, useEffect, useRef } from 'react'

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
  const [inputText, setInputText] = useState('')
  const [activeTab, setActiveTab] = useState('Input')
  const [outputHeight, setOutputHeight] = useState(50) // Percentage of container height
  const [isDragging, setIsDragging] = useState(false)

  //Variables for polling tracking:
  const pollingIntervalRef = useRef(null);
  const [execID, setExecID] = useState(null);
  const rightPanelRef = useRef(null);
  


  const apiUrl = import.meta.env.VITE_API_URL;

  // Custom scrollbar styles
  const scrollbarStyle = {
    scrollbarWidth: 'thin',
    scrollbarColor: darkMode ? '#00FF00 #000000' : '#00CC00 #1a1a1a',
  }

  const scrollbarWebkitClass = darkMode ? 'terminal-scrollbar-dark' : 'terminal-scrollbar-light'

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

  // Cleanup polling on component unmount
  useEffect(() => {
    return () => {
      if (pollingIntervalRef.current) {
        clearInterval(pollingIntervalRef.current)
      }
    }
  }, [])

  // Handle dragging for resizable panels
  useEffect(() => {
    const handleMouseMove = (e) => {
      if (!isDragging || !rightPanelRef.current) return

      const rect = rightPanelRef.current.getBoundingClientRect()
      const newHeight = ((e.clientY - rect.top) / rect.height) * 100

      // Clamp between 20% and 80% to prevent panels from becoming too small
      const clampedHeight = Math.min(Math.max(newHeight, 20), 80)
      setOutputHeight(clampedHeight)
    }

    const handleMouseUp = () => {
      setIsDragging(false)
    }

    if (isDragging) {
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
    }
  }, [isDragging])

  const handleDragStart = () => {
    setIsDragging(true)
  }

  // Helper function to stop polling
  const stopPolling = () => {
    if (pollingIntervalRef.current) {
      clearInterval(pollingIntervalRef.current)
      pollingIntervalRef.current = null
    }
    setIsExecuting(false)
    setAbortController(null)
    setExecID(null)
  }

  // Polling function to check execution status
  const startPolling = (uuid) => {
    const pollInterval = setInterval(async () => {
      try {
        const checkRes = await fetch(`${apiUrl}/check`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(uuid)
        })

        const result = await checkRes.json()
        console.log('Poll result:', result)

        if (result.status === 'FINISHED') {
          // Execution completed
          setRunData(result)
          setResponse('') // Clear plain text response

          setAlertData({
            show: true,
            success: result.success,
            exitStatus: result.exitStatus || (result.success ? 'Execution completed successfully' : 'Execution failed')
          })

          stopPolling()
        } else if (result.status === 'NONEXISTENT') {
          // Execution error
          setRunData(null)
          setResponse(`> Error\n=====================================\n\nExecution error: Process not found`)
          setAlertData({
            show: true,
            success: false,
            exitStatus: 'Execution Error: Process not found'
          })

          stopPolling()
        }
        // If status === 'RUNNING', continue polling (do nothing)
      } catch (error) {
        console.error('Polling error:', error)
        setRunData(null)
        setResponse(`> Error\n=====================================\n\n${error.message}`)
        setAlertData({
          show: true,
          success: false,
          exitStatus: `Polling Error: ${error.message}`
        })

        stopPolling()
      }
    }, 500) // Poll every 500ms

    pollingIntervalRef.current = pollInterval
  }

  const handleSubmit = async () => {
    if (isExecuting) {
      // Stop execution
      if (abortController) {
        abortController.abort()
      }
      stopPolling()

      // Show termination message
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
      return
    }

    // Create new abort controller for this execution
    const controller = new AbortController()
    setAbortController(controller)
    setIsExecuting(true)
    setRunData(null)
    setResponse('')

    try {
      // First request to /submit - returns UUID
      const runRes = await fetch(`${apiUrl}/submit`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code: text,
          language: selectedLanguage,
          problem: "two",
          input: inputText
        }),
        signal: controller.signal
      })

      const uuid = await runRes.text()
      setExecID(uuid)
      console.log('Execution UUID:', uuid)

      // Start polling for results
      startPolling(uuid)
    } catch (error) {
      if (error.name === 'AbortError') {
        // User cancelled execution during submit
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
    <div className={`h-screen flex flex-col ${darkMode ? 'bg-black' : 'bg-gray-100'}`}>
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

        {/* Right Side - Terminal Output and Input */}
        <div ref={rightPanelRef} className="flex flex-col min-h-0">
          {/* Output Section */}
          <div style={{ height: `${outputHeight}%`, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
            <div className={`flex items-center gap-2 mb-2 pb-2 border-b-2 ${darkMode ? 'border-green-500' : 'border-green-400'}`}>
              <span className={`text-lg font-bold font-mono ${darkMode ? 'text-green-400' : 'text-green-600'}`}>{'>'}</span>
              <h2 className={`text-lg font-semibold font-mono tracking-wide ${darkMode ? 'text-green-400' : 'text-green-600'}`}>
                OUTPUT
              </h2>
            </div>
            <div className="flex-1 min-h-0">
              <Terminal output={response} darkMode={darkMode} fontSize={fontSize} runData={runData} isExecuting={isExecuting} />
            </div>
          </div>

          {/* Draggable Divider */}
          <div
            onMouseDown={handleDragStart}
            className={`relative h-4 cursor-row-resize transition-colors flex items-center justify-center ${
              isDragging
                ? darkMode ? 'bg-green-400' : 'bg-green-600'
                : darkMode ? 'bg-green-500 hover:bg-green-400' : 'bg-green-400 hover:bg-green-600'
            }`}
            style={{ flexShrink: 0 }}
          >
            <div className={`flex gap-1 ${darkMode ? 'text-black' : 'text-black'} select-none`} style={{ fontSize: '18px', fontWeight: 900, lineHeight: '1' }}>
              <span>⋮</span>
              <span>⋮</span>
              <span>⋮</span>
            </div>
          </div>

          {/* Input Section with Tabs */}
          <div style={{ height: `${100 - outputHeight}%`, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
            <div className={`flex items-center gap-2 mb-2 pb-2 mt-4 border-b-2 ${darkMode ? 'border-green-500' : 'border-green-400'}`}>
              <span className={`text-lg font-bold font-mono ${darkMode ? 'text-green-400' : 'text-green-600'}`}>{'>'}</span>
              <button
                onClick={() => setActiveTab('Input')}
                className={`text-lg font-semibold font-mono tracking-wide transition-colors ${
                  activeTab === 'Input'
                    ? darkMode ? 'text-green-400 underline' : 'text-green-600 underline'
                    : darkMode ? 'text-green-700 hover:text-green-500' : 'text-green-800 hover:text-green-600'
                }`}
              >
                INPUT
              </button>
              <span className={`text-lg font-mono ${darkMode ? 'text-green-400' : 'text-green-600'}`}>|</span>
              <button
                onClick={() => setActiveTab('CodeHelper')}
                className={`text-lg font-semibold font-mono tracking-wide transition-colors ${
                  activeTab === 'CodeHelper'
                    ? darkMode ? 'text-green-400 underline' : 'text-green-600 underline'
                    : darkMode ? 'text-green-700 hover:text-green-500' : 'text-green-800 hover:text-green-600'
                }`}
              >
                CODE_HELPER
              </button>
            </div>
            <div className="flex-1 min-h-0">
              {activeTab === 'Input' ? (
                <div className={`w-full h-full border-2 overflow-hidden ${
                  darkMode ? 'border-green-500 shadow-lg shadow-green-500/20' : 'border-green-400 shadow-lg shadow-green-400/20'
                }`}>
                  <textarea
                    value={inputText}
                    onChange={(e) => setInputText(e.target.value)}
                    className={`w-full h-full p-4 resize-none focus:outline-none font-mono ${
                      darkMode ? 'bg-black text-green-400' : 'bg-gray-900 text-green-500'
                    } ${scrollbarWebkitClass}`}
                    style={{ fontSize: `${fontSize}px`, ...scrollbarStyle }}
                    placeholder='> Enter input here...'
                    spellCheck="false"
                  />
                </div>
              ) : (
                <ChatInterface
                  darkMode={darkMode}
                  fontSize={fontSize}
                  apiUrl={apiUrl}
                  code={text}
                  language={selectedLanguage}
                  input={inputText}
                  result={runData}
                />
              )}
            </div>
          </div>
        </div>


      </div>
    </div>
  )
}

export default App
