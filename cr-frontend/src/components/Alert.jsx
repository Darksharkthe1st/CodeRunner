import { useEffect } from 'react'

function Alert({ success, exitStatus, onClose, darkMode }) {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose()
    }, 5000)

    return () => clearTimeout(timer)
  }, [onClose])

  if (!exitStatus) return null

  return (
    <div className={`absolute top-full mt-2 right-0 z-50 border-2 ${
      success
        ? darkMode ? 'bg-blue-900 border-blue-500 text-blue-200' : 'bg-blue-100 border-blue-500 text-blue-900'
        : darkMode ? 'bg-red-900 border-red-500 text-red-200' : 'bg-red-100 border-red-500 text-red-900'
    } shadow-lg min-w-[300px] max-w-[400px]`}>
      <div className="flex items-start justify-between p-4">
        <div className="flex-1">
          <div className="font-mono text-sm font-bold mb-1">
            {success ? '[SUCCESS]' : '[ERROR]'}
          </div>
          <div className="font-mono text-xs whitespace-pre-wrap break-words">
            {exitStatus}
          </div>
        </div>
        <button
          onClick={onClose}
          className={`ml-3 text-xl font-bold leading-none hover:opacity-70 transition-opacity ${
            success
              ? darkMode ? 'text-blue-300' : 'text-blue-700'
              : darkMode ? 'text-red-300' : 'text-red-700'
          }`}
          aria-label="Close"
        >
          ×
        </button>
      </div>
    </div>
  )
}

export default Alert
