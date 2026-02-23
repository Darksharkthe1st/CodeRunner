# CodeRunner

A full-stack web-based IDE that allows users to write, compile, and execute code in multiple programming languages directly in their browser. Built with React and Spring Boot, CodeRunner provides a safe, containerized execution environment using Docker, enhanced with AI-powered code assistance.

Access ***LIVE*** at **[https://code-runner-eta.vercel.app/](https://code-runner-eta.vercel.app/)**

## Features

### Core Functionality
- **Multi-Language Support**: Currently supports Java, Python, and C with full execution capabilities
- **Real-Time Code Execution**: Write code and see results instantly with asynchronous processing
- **Syntax Highlighting**: Custom regex-based syntax highlighter for all supported languages
- **Safe Execution Environment**: Docker containerization for C/C++/Python code provides isolated execution
- **Input/Output Handling**: Support for custom input via stdin with dedicated input panel
- **Code Templates**: Quick-start templates for each supported language
- **Execution Limits**: 60-second timeout and output size limits to prevent abuse

### AI-Powered Code Assistant
- **Code_Helper**: Integrated AI assistant powered by Google Gemini
- **Context-Aware**: Understands your code, language, input, and execution results
- **Persistent Chat**: Multi-turn conversations that persist across tab switches
- **Markdown Support**: Rich text formatting with syntax highlighting in responses
- **Debug Assistance**: Get help identifying bugs, design flaws, and errors
- **Real-time Help**: Chat interface for interactive code debugging and learning

### User Interface
- **Responsive UI**: Clean, two-column layout with code editor and output console
- **Dark/Light Mode**: Toggle between terminal-style themes
- **Adjustable Font Size**: Zoom in/out controls for comfortable reading
- **Resizable Panels**: Draggable divider to adjust output/input panel heights
- **Tab Interface**: Switch between Input and Code_Helper tabs seamlessly
- **Error Handling**: Comprehensive error capture with visual alerts for compilation and runtime errors

## Tech Stack

### Frontend
- **React 19** - UI framework
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Styling
- **react-markdown** - Markdown rendering for AI responses
- **remark-gfm** - GitHub Flavored Markdown support
- **Custom Syntax Highlighter** - Regex-based highlighting for C, C++, Java, Python

### Backend
- **Spring Boot 4.0.1** - REST API framework
- **Java 21** - Backend runtime
- **Docker** - Containerized code execution
- **Maven** - Build and dependency management
- **LangChain4j** - AI integration framework
- **Google Gemini** - LLM for code assistance
- **Spring Boot Actuator** - Health monitoring and metrics
- **Micrometer Prometheus** - Metrics collection

## Architecture

### Request Flow
1. User writes code in the browser editor
2. Frontend sends code + language to `POST /submit` endpoint
3. Backend creates `CodeSubmission` object, stores in `UserData` service with UUID
4. Backend initiates asynchronous `CodeExecution` thread
5. Frontend polls `POST /check` endpoint for execution status
6. Code is executed:
   - **Java**: Docker container with Eclipse Temurin 21 Alpine
   - **Python**: Docker container with Python 3.12 Alpine
   - **C**: Docker container with Alpine Linux + GCC
7. Output streamed through multi-threaded readers (stdout/stderr)
8. Results returned as JSON via `RunResult` DTO when status becomes FINISHED

### AI Assistant Flow
1. User types message in Code_Helper tab
2. Frontend sends message with conversation history, current code, and execution results to `POST /llm/message`
3. Backend constructs context with system prompt and conversation history
4. GeminiService processes request via LangChain4j
5. AI response returned and rendered as markdown in chat interface
6. Chat history persists in app state across tab switches

### Key Components

#### Frontend (`cr-frontend/src/`)
- `App.jsx` - Main application with split-pane layout, state management, and polling logic
- `components/CodeEditor.jsx` - Code editor with overlay for syntax highlighting
- `components/Terminal.jsx` - Output display with execution animations
- `components/ChatInterface.jsx` - AI chat interface with markdown rendering
- `components/SyntaxHighlighter.jsx` - Language-specific syntax highlighting
- `components/Alert.jsx` - Success/error notification system
- `components/ExecutingAnimation.jsx` - Visual feedback during code execution

#### Backend (`src/main/java/com/cr/coderunner/`)
- `controller/IDEController.java` - REST API endpoints for code execution
- `controller/LLMController.java` - REST API endpoints for AI chat
- `controller/HealthController.java` - Health check endpoints
- `model/CodeSubmission.java` - Core execution engine with Docker integration
- `model/CodeExecution.java` - Async execution wrapper thread
- `service/UserData.java` - In-memory submission storage with UUID tracking
- `service/GeminiService.java` - AI service integration with Google Gemini
- `service/WebConfig.java` - CORS configuration
- `dto/UserChat.java` - Data transfer object for chat messages

## Prerequisites

- **Docker** - Required for all language execution (C, Python, Java)
- **Java 21+** - Backend runtime
- **Node.js** (v18+) - Frontend development
- **Maven** - Backend build tool
- **Google Gemini API Key** - For AI code assistant features

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/CodeRunner.git
cd CodeRunner
```

### 2. Backend Setup
```bash
# Set up environment variables
# Create .env file with your Gemini API key
echo "GEMINI_API_KEY=your_api_key_here" > .env

# Build the backend
mvn clean install

# Run Spring Boot server (starts on http://localhost:8080)
mvn spring-boot:run
```

### 3. Frontend Setup
```bash
# Navigate to frontend directory
cd cr-frontend

# Install dependencies
npm install

# Create .env file for API URL
echo "VITE_API_URL=http://localhost:8080" > .env

# Start development server (starts on http://localhost:5173)
npm run dev
```

### 4. Docker Setup
Ensure Docker is running and the necessary images are available:
```bash
docker pull alpine:latest
docker pull python:3.12-alpine
docker pull eclipse-temurin:21-alpine
```

## Usage

### Running Code
1. Open your browser to `http://localhost:5173`
2. Select a programming language from the dropdown
3. Write your code in the editor (or use the provided template)
4. (Optional) Switch to Input tab and provide stdin input
5. Click "RUN" to execute
6. View output, errors, and execution time in the output panel
7. Click "STOP" to terminate execution if needed

### Using Code_Helper
1. Switch to the "CODE_HELPER" tab in the lower right panel
2. Type your question or describe the issue you're facing
3. Click send or press Enter to submit your message
4. Receive AI-powered assistance based on your code context
5. Continue the conversation to refine solutions
6. Chat history persists when switching between Input and Code_Helper tabs

## API Endpoints

### Code Execution Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/submit` | Submit code for execution, returns UUID |
| POST | `/check` | Check execution status by UUID |
| GET | `/check/{id}` | Check if submission exists (legacy) |
| GET | `/get_template` | Get code template for language |
| GET | `/supported` | Get list of supported languages |

### AI Assistant Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/llm/message` | Send message to AI with code context |
| POST | `/llm/ask` | Simple prompt endpoint (testing) |

### Health & Monitoring
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Basic health check |
| GET | `/actuator/health` | Detailed health status |
| GET | `/actuator/prometheus` | Prometheus metrics |

### Example Request
```bash
# Submit code for execution
curl -X POST http://localhost:8080/submit \
  -H "Content-Type: application/json" \
  -d '{
    "code": "print(\"Hello World\")",
    "language": "Python",
    "problem": "test",
    "input": ""
  }'

# Response: UUID string
# "a1b2c3d4-e5f6-7890-abcd-ef1234567890"

# Check execution status
curl -X POST http://localhost:8080/check \
  -H "Content-Type: application/json" \
  -d '"a1b2c3d4-e5f6-7890-abcd-ef1234567890"'

# Response when FINISHED:
{
  "success": true,
  "runtime": 0.123,
  "output": "Hello World\n",
  "error": "",
  "exitStatus": "",
  "status": "FINISHED"
}

# Ask AI for help
curl -X POST http://localhost:8080/llm/message \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "system", "content": "You are Code_Helper..."},
      {"role": "user", "content": "Why is my code not working?"}
    ],
    "code": {
      "code": "print(x)",
      "language": "Python",
      "input": "",
      "problem": "test"
    },
    "result": {
      "success": false,
      "output": "",
      "error": "NameError: name 'x' is not defined"
    }
  }'
```

## Project Structure

```
CodeRunner/
├── cr-frontend/                 # React frontend
│   ├── src/
│   │   ├── components/         # React components
│   │   │   ├── Alert.jsx
│   │   │   ├── ChatInterface.jsx
│   │   │   ├── CodeEditor.jsx
│   │   │   ├── ExecutingAnimation.jsx
│   │   │   ├── SyntaxHighlighter.jsx
│   │   │   ├── Terminal.jsx
│   │   │   └── TextWindow.jsx
│   │   ├── App.jsx             # Main application
│   │   ├── App.css             # Styles including custom scrollbars
│   │   └── main.jsx            # Entry point
│   ├── public/                 # Static assets
│   ├── .env                    # Environment variables (API URL)
│   └── package.json
├── src/main/java/com/cr/coderunner/
│   ├── controller/             # REST controllers
│   │   ├── IDEController.java
│   │   ├── LLMController.java
│   │   ├── HealthController.java
│   │   └── ProblemController.java
│   ├── model/                  # Domain models
│   │   ├── CodeSubmission.java
│   │   └── CodeExecution.java
│   ├── service/                # Business logic
│   │   ├── UserData.java
│   │   ├── GeminiService.java
│   │   └── WebConfig.java
│   └── dto/                    # Data transfer objects
│       ├── RunResult.java
│       └── UserChat.java
├── .test/                      # Temporary execution files (git-ignored)
├── .env                        # Environment variables (API keys)
├── Dockerfile                  # Production Docker image
├── docker-entrypoint.sh        # Docker startup script
├── pom.xml                     # Maven configuration
├── CLAUDE.md                   # Development guidelines
└── README.md
```

## Development

### Running Tests
```bash
# Backend tests
mvn test

# Frontend linting
cd cr-frontend && npm run lint
```

### Building for Production

#### Backend
```bash
mvn clean package
java -jar target/CodeRunner-0.0.1-SNAPSHOT.jar
```

#### Frontend
```bash
cd cr-frontend
npm run build
# Built files will be in dist/
```

#### Docker
```bash
# Build image
docker build -t coderunner .

# Run with Docker-in-Docker support
docker run --privileged --cgroupns=host -p 8080:8080 \
  -e GEMINI_API_KEY=your_api_key_here \
  coderunner
```

## Roadmap

### In Progress
- [x] AI-powered code assistant with context awareness
- [x] Multi-turn chat conversations
- [x] Markdown rendering in AI responses
- [x] Persistent chat history across UI navigation

### Planned Features
- [ ] Add C++ language support (execution engine ready)
- [ ] JavaScript/TypeScript language support
- [ ] Migrate from in-memory storage to SQL database
- [ ] Implement user authentication and sessions
- [ ] Add problem/challenge system (ProblemController)
- [ ] Syntax error highlighting in editor (inline diagnostics)
- [ ] Code sharing functionality with shareable links
- [ ] Execution history and saved code snippets
- [ ] Custom compilation flags and runtime arguments
- [ ] Support for additional languages (Go, Rust, Ruby)
- [ ] Code formatting and linting integration
- [ ] Multi-file project support
- [ ] Collaborative editing features
- [ ] Performance profiling and memory usage tracking

## Technical Details

### Execution Safety
- **Timeouts**: 60-second hard limit on execution time
- **Output Limits**: Protection against memory overflow from excessive output
- **Containerization**: Isolated Docker containers for all language execution
- **Resource Limits**: Docker resource constraints (commented in code, ready to enable)
- **Multi-threading**: Separate threads for stdout/stderr to prevent deadlocks
- **Cleanup**: Automatic removal of temporary files and Docker containers
- **Error Handling**: Graceful handling of container failures and timeouts

### Docker Execution Details
All languages now use Docker for consistent, isolated execution:

```bash
# Python execution
docker run --name <uuid> --rm -v /host/path:/sandbox \
  python:3.12-alpine sh -c \
  "python3 sandbox/code.py < sandbox/input.txt"

# C execution
docker run --name <uuid> --rm -v /host/path:/sandbox \
  alpine:latest sh -c \
  "apk add --no-cache gcc musl-dev && \
   gcc sandbox/code.c -o sandbox/main && \
   ./sandbox/main < sandbox/input.txt"

# Java execution
docker run --name <uuid> --rm -v /host/path:/sandbox \
  eclipse-temurin:21-alpine sh -c \
  "java sandbox/code.java < sandbox/input.txt"
```

### Polling Architecture
The frontend uses a polling mechanism to check execution status:
1. Submit returns UUID immediately
2. Frontend polls `/check` endpoint every 500ms
3. Backend returns status: RUNNING, FINISHED, or NONEXISTENT
4. Polling stops when status is FINISHED or NONEXISTENT
5. User can abort execution during polling

### AI Integration
- **LangChain4j**: Framework for LLM integration
- **Google Gemini**: Large language model for code assistance
- **Context Building**: System prompt + conversation history + code context
- **Structured Messages**: Role-based message format (system, user, agent)
- **Error Recovery**: Graceful handling of API failures

### Supported Languages

| Language | Execution Method | Docker Image | Status |
|----------|------------------|--------------|--------|
| Java | eclipse-temurin:21-alpine | Yes | ✅ Fully Supported |
| Python | python:3.12-alpine | Yes | ✅ Fully Supported |
| C | alpine:latest + GCC | Yes | ✅ Fully Supported |
| C++ | alpine:latest + G++ | Planned | 🚧 In Development |

## Configuration

### Environment Variables

#### Backend (.env in root)
```bash
GEMINI_API_KEY=your_gemini_api_key_here
```

#### Frontend (cr-frontend/.env)
```bash
VITE_API_URL=http://localhost:8080
```

### CORS Configuration
By default, the backend allows requests from:
- `http://localhost:5173` (Vite dev server)
- Configurable in `WebConfig.java`

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Known Issues & TODOs

- Remove debug print statements (e.g., System.out in LLMController.java:27)
- Optimize Docker image pulling and caching
- Implement proper session management for chat history
- Add rate limiting for API endpoints
- Improve error messages for Docker failures
- Add unit tests for CodeSubmission execution logic
- Document GeminiService configuration options

## License

This project is open source and available under the [MIT License](LICENSE).

## Acknowledgments

- Built with Spring Boot and React
- Uses Docker for safe code execution
- Powered by Google Gemini AI
- LangChain4j for LLM integration
- Inspired by online code editors like LeetCode and HackerRank
- React-markdown for rich text rendering

---

**Note**: This project is under active development. The AI code assistant feature is newly integrated and may require a Google Gemini API key for full functionality. Some features may be incomplete or subject to change.
