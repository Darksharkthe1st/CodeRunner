function TextWindow({ text, setText }) {
  return (
    <div className="w-full max-w-2xl mx-auto p-4">
      <textarea
        value={text}
        onChange={(e) => setText(e.target.value)}
        className="w-full h-64 p-4 border-2 border-gray-300 rounded-2xl resize-none focus:outline-none focus:border-blue-500 font-sans text-base"
        placeholder="Enter text here..."
      />
    </div>
  )
}

export default TextWindow
