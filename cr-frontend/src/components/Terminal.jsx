function Terminal({ output, darkMode, fontSize, runData }) {
  // If runData is provided, format it with colors
  if (runData) {
    return (
      <div className={`w-full h-full border-2 overflow-hidden ${
        darkMode ? 'border-green-500 shadow-lg shadow-green-500/20' : 'border-green-400 shadow-lg shadow-green-400/20'
      }`}>
        <div className={`w-full h-full overflow-auto ${
          darkMode ? 'bg-black' : 'bg-gray-900'
        }`}>
          <div className="p-4 font-mono" style={{ fontSize: `${fontSize}px` }}>
            <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
              {'> Execution Result'}
            </div>
            <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
              {'====================================='}
            </div>
            <div className="my-2"></div>

            {runData.success ? (
              <>
                <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
                  {'✓ Status: SUCCESS'}
                </div>
                <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
                  {`⏱ Runtime: ${runData.runtime}ms`}
                </div>
                <div className="my-2"></div>
                <div className={darkMode ? 'text-green-400' : 'text-green-500'}>Output:</div>
                <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
                  {'-------------------------------------'}
                </div>
                <div className={darkMode ? 'text-green-400' : 'text-green-500'} style={{ whiteSpace: 'pre-wrap' }}>
                  {runData.output || '(no output)'}
                </div>
                <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
                  {'-------------------------------------'}
                </div>
              </>
            ) : (
              <>
                <div className="text-red-500">
                  {'✗ Status: FAILED'}
                </div>
                <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
                  {`⏱ Runtime: ${runData.runtime}ms`}
                </div>
                <div className="my-2"></div>

                {runData.error && (
                  <>
                    <div className="text-red-500">Error:</div>
                    <div className="text-red-500">
                      {'-------------------------------------'}
                    </div>
                    <div className="text-red-400" style={{ whiteSpace: 'pre-wrap' }}>
                      {runData.error}
                    </div>
                    <div className="text-red-500">
                      {'-------------------------------------'}
                    </div>
                  </>
                )}

                {runData.output && (
                  <>
                    <div className="my-2"></div>
                    <div className={darkMode ? 'text-green-400' : 'text-green-500'}>Output:</div>
                    <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
                      {'-------------------------------------'}
                    </div>
                    <div className={darkMode ? 'text-green-400' : 'text-green-500'} style={{ whiteSpace: 'pre-wrap' }}>
                      {runData.output}
                    </div>
                    <div className={darkMode ? 'text-green-400' : 'text-green-500'}>
                      {'-------------------------------------'}
                    </div>
                  </>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    )
  }

  // Fallback to plain text output
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
