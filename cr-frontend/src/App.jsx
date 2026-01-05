import './App.css'
import TextWindow from './components/TextWindow'
import { useState, useEffect } from 'react'

function App() {
  const [text, setText] = useState('')
  const [response, setResponse] = useState('')
  const [supportedLanguages, setSupportedLanguages] = useState(["C"])
  const [selectedLanguage, setSelectedLanguage] = useState('C')

  useEffect(() => {
    const fetchSupportedLanguages = async () => {
      try {
        const res = await fetch('http://localhost:8080/supported')
        const languages = await res.json()
        setSupportedLanguages(languages)
        if (languages.length > 0) {
          setSelectedLanguage(languages[0])
        }
      } catch (error) {
        console.error('Error fetching supported languages:', error)
        setSupportedLanguages(["C"])
        setSelectedLanguage("C")
      }
    }

    fetchSupportedLanguages()
  }, [])

  const handleSubmit = async () => {
    try {
      // First request to /submit
      const submitRes = await fetch('http://localhost:8080/submit', {
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

      // const submitData = await submitRes.json()
      // console.log('Submit Response:', submitData)

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
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="bg-blue-600 text-white p-4 shadow-lg">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold">Code Runner</h1>
          <div className="flex items-center gap-4">
            <select
              value={selectedLanguage}
              onChange={(e) => setSelectedLanguage(e.target.value)}
              className="px-4 py-2 border-2 border-white bg-blue-500 text-white rounded-lg focus:outline-none focus:border-blue-300"
            >
              {supportedLanguages.map((lang) => (
                <option key={lang} value={lang}>
                  {lang}
                </option>
              ))}
            </select>
            <button
              onClick={handleSubmit}
              className="px-6 py-2 bg-white text-blue-600 rounded-lg hover:bg-blue-50 transition-colors font-semibold"
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
          <h2 className="text-lg font-semibold mb-2">Code</h2>
          <div className="flex-1">
            <TextWindow text={text} setText={setText} />
          </div>
        </div>

        {/* Right Side - Result Box */}
        <div className="flex flex-col">
          <h2 className="text-lg font-semibold mb-2">Output</h2>
          <textarea
            value={response}
            readOnly
            className="flex-1 p-4 border-2 border-gray-300 rounded-2xl resize-none bg-gray-50 font-mono text-sm"
            placeholder="Results will appear here..."
          />
        </div>

        
      </div>
    </div>
  )
}

export default App
