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

  return (<>
    <TextWindow text={text} setText={setText} />
    <div className="flex justify-center items-center gap-4 mt-4">
      <select
        value={selectedLanguage}
        onChange={(e) => setSelectedLanguage(e.target.value)}
        className="px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
      >
        {supportedLanguages.map((lang) => (
          <option key={lang} value={lang}>
            {lang}
          </option>
        ))}
      </select>
      <button
        onClick={handleSubmit}
        className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
      >
        Submit
      </button>
    </div>
    {response && (
      <div className="w-full max-w-2xl mx-auto p-4">
        <textarea
          value={response}
          readOnly
          className="w-full h-64 p-4 border-2 border-gray-300 rounded-2xl resize-none bg-gray-50 font-mono text-sm"
        />
      </div>
    )}
  </>
  )
}

export default App
