import { useState, useEffect } from 'react'

function ExecutingAnimation({ darkMode, fontSize }) {
  const [dots, setDots] = useState('')
  const [frame, setFrame] = useState(0)

  const spinnerFrames = ['⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏']

  useEffect(() => {
    const dotsInterval = setInterval(() => {
      setDots(prev => {
        if (prev.length >= 3) return ''
        return prev + '.'
      })
    }, 500)

    const frameInterval = setInterval(() => {
      setFrame(prev => (prev + 1) % spinnerFrames.length)
    }, 100)

    return () => {
      clearInterval(dotsInterval)
      clearInterval(frameInterval)
    }
  }, [spinnerFrames.length])

  return (
    <div className="p-4 font-mono" style={{ fontSize: `${fontSize}px` }}>
      <div className={`mb-4 ${darkMode ? 'text-cyan-400' : 'text-cyan-500'}`}>
        <span className="mr-3">{spinnerFrames[frame]}</span>
        <span>EXECUTING CODE<span className="inline-block w-8">{dots}</span></span>
      </div>

      <div className="mt-4 space-y-2">
        <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
          {'> Compiling source code...'}
        </div>
        <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
          {'> Running program...'}
        </div>
        <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
          {'> Waiting for output...'}
        </div>
      </div>
    </div>
  )
}

export default ExecutingAnimation
