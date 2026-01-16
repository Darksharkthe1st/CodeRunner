function Terminal({ output, darkMode, fontSize }) {
  return (
    <div className={`w-full h-full border-2 overflow-hidden ${
      darkMode ? 'border-green-500 shadow-lg shadow-green-500/20' : 'border-green-400 shadow-lg shadow-green-400/20'
    }`}>
      <div className={`w-full h-full overflow-auto ${
        darkMode ? 'bg-black' : 'bg-gray-900'
      }`}>
        <div
          className={`p-4 font-mono whitespace-pre-wrap ${
            darkMode ? 'text-green-400' : 'text-green-500'
          }`}
          style={{ fontSize: `${fontSize}px` }}
        >
          {output || '> Awaiting execution...'}
        </div>
      </div>
    </div>
  )
}

export default Terminal
