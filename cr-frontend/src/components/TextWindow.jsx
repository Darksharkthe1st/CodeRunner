function TextWindow({ text, setText }) {
  return (
    <textarea
      value={text}
      onChange={(e) => setText(e.target.value)}
      className="w-full h-full p-4 border-2 border-gray-300 rounded-2xl resize-none focus:outline-none focus:border-blue-500 font-sans text-base"
      placeholder="Enter your code here..."
    />
  )
}

export default TextWindow
