# CodeRunner

Full-stack web IDE for writing, compiling, and executing code in multiple languages. Built with **React** and **Spring Boot** with **Docker** containerization and **LangChain4j AI agent** for intelligent code assistance.

**LIVE**: [https://code-runner-eta.vercel.app/](https://code-runner-eta.vercel.app/)

## Features

### Core Functionality
- **Multi-Language Support**: Java, Python, and C with full execution
- **Asynchronous Execution**: 10-worker thread pool with UUID-based polling
- **Docker Containerization**: Isolated execution environment for all languages
- **Syntax Highlighting**: Custom regex-based highlighter
- **60s Timeout & Output Limits**: Protection against abuse and memory overflow

### AI-Powered Code Assistant
- **LangChain4j Agent**: Google Gemini 2.5 Flash with @AiService and @Tool annotations
- **Autonomous Code Testing**: Agent can execute test code to verify fixes
- **Context-Aware**: Full access to code, chat history, and execution results
- **Read-Only Design**: Suggests fixes, user manually applies changes
- **Markdown Rendering**: Syntax-highlighted responses in chat interface

### User Interface
- **Split-Pane Layout**: Code editor + output console with resizable dividers
- **Dark/Light Mode**: Terminal-style theme toggle
- **Tab Interface**: Input and Code_Helper tabs with persistent chat
- **Font Adjustments**: Zoom controls for comfortable reading
- **Visual Feedback**: Execution animations and error alerts

## Tech Stack

### Frontend
- **React 19** + **Vite** + **Tailwind CSS**
- **react-markdown** + **remark-gfm** for AI response rendering
- Custom regex-based syntax highlighter

### Backend
- **Spring Boot 4.0.1** + **Java 21** + **Maven**
- **Docker** for containerized code execution
- **LangChain4j 1.11.0-beta19** with @AiService and @Tool support
- **Google Gemini 2.5 Flash** LLM integration
- **ExecutorService** (10-worker thread pool) + **ConcurrentHashMap** for async queue

## Architecture

CodeRunner uses a modern **asynchronous execution queue model** with **LangChain4j agent framework** for intelligent code assistance.

### Execution Service & Queue Model

```
User Submission → UUID Generated → Submitted to Queue (10 worker threads) →
Async Execution → Docker Container → Output Collection →
Poll for Results (RUNNING/FINISHED) → Cleanup
```

- 10-worker `ExecutorService` thread pool for concurrent executions
- UUID-based tracking with `ConcurrentHashMap`
- Non-blocking submit, frontend polls `/check` every 500ms
- Scheduled cleanup removes executions older than 60s (runs every 30s)

### Request Flow (IDE Execution)
1. Frontend → `POST /submit` → `CodeExecutionService` creates thread + UUID
2. UUID returned immediately (non-blocking)
3. Frontend polls `POST /check` with UUID every 500ms
4. Worker executes in Docker (Java/Python/C containers)
5. Results returned as `RunResult` with status: RUNNING/FINISHED/NONEXISTENT

### AI Assistant Flow (Agent with Tool Invocation)

```
User Message → /llm/message → GeminiService → CodeHelperAssistant.chat() →
Gemini 2.5 Flash analyzes context + chat history →
[OPTIONAL] Agent invokes CodeExecutionTools.executeCode() →
Tool submits to same execution queue → Poll for results → Tool returns output →
Agent interprets results → Generates debugging advice → Returns to user
```

- `CodeHelperAssistant` (@AiService) with system prompt defines agent behavior
- Agent invokes `@Tool` methods to autonomously test code
- Full context awareness: chat history + code + execution results
- Read-only design: suggests fixes, doesn't modify user code
- Output truncated to 1MB to prevent token overflow

### Agent Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    LangChain4j Framework                     │
├─────────────────────────────────────────────────────────────┤
│  CodeHelperAssistant (@AiService)                           │
│  ├── System Prompt + chat(List<ChatMessage>)               │
│  └── Auto-wired to ChatModel bean                           │
│                                                              │
│  CodeExecutionTools (@Component)                            │
│  ├── @Tool: executeCode(code, language, input)             │
│  └── Submits to CodeExecutionService, polls, returns output │
│                                                              │
│  LLMConfig (@Configuration)                                 │
│  └── Creates ChatModel bean (gemini-2.5-flash)             │
└─────────────────────────────────────────────────────────────┘
                           ↓
                  CodeExecutionService
```

### Old vs. New Architecture Comparison

| Aspect | Old Architecture | New Architecture |
|--------|------------------|------------------|
| **Execution Model** | Synchronous, blocking | Async queue with 10-worker thread pool |
| **Concurrency** | Single request blocks thread | Up to 10 parallel executions |
| **Result Tracking** | Direct response | UUID-based polling with ConcurrentHashMap |
| **Memory Management** | On-demand cleanup | Fixed pool + scheduled 30s cleanup |
| **LLM Integration** | Direct Gemini API calls | LangChain4j @AiService agent |
| **Code Testing** | Manual user testing only | Agent can autonomously execute test code |
| **Tool Framework** | N/A | @Tool methods for agent capabilities |
| **Context Building** | Simple message concatenation | Structured ChatMessage with code context injection |
| **Scalability** | Limited by sequential execution | Horizontal scalability with thread pool |
| **Response Time** | Waits for full execution | Immediate UUID return, async polling |

### Complete Data Flow

#### IDE User Flow (Direct Code Execution)
```
┌────────────┐
│   User     │
│  Browser   │
└─────┬──────┘
      │ 1. POST /submit {code, language, input}
      ▼
┌─────────────────────────────────────────┐
│     IDEController.postSubmission()      │
└─────┬───────────────────────────────────┘
      │ 2. Create CodeExecution thread
      ▼
┌─────────────────────────────────────────┐
│  CodeExecutionService.execute()         │
│  • Generate UUID                        │
│  • Store in ConcurrentHashMap           │
│  • Submit to ExecutorService pool       │
└─────┬───────────────────────────────────┘
      │ 3. Return UUID immediately
      ▼
┌────────────┐     ┌──────────────────────────────┐
│  Frontend  │────▶│  Worker Thread (1 of 10)     │
│  Polls     │     │  • CodeExecution.run()       │
│  /check    │     │  • CodeSubmission.run()      │
│  every     │     │  • Docker container exec     │
│  500ms     │     │  • Collect stdout/stderr     │
└─────┬──────┘     │  • Set done=true             │
      │            └──────────────┬───────────────┘
      │ 4. Poll: POST /check {uuid}
      ▼                           │
┌─────────────────────────────────▼───┐
│  CodeExecutionService.check()       │
│  • Lookup UUID in HashMap           │
│  • Return status + results          │
│  • Remove if FINISHED               │
└─────┬───────────────────────────────┘
      │ 5. Return RunResult
      │    status: RUNNING | FINISHED | NONEXISTENT
      ▼
┌────────────┐
│  Frontend  │
│  Displays  │
│  Output    │
└────────────┘

Background: @Scheduled cleanup every 30s removes old executions
```

#### AI Agent Flow (with Autonomous Tool Invocation)
```
┌────────────┐
│   User     │
│  Chat UI   │
└─────┬──────┘
      │ 1. POST /llm/message {messages, code, result}
      ▼
┌──────────────────────────────────────────┐
│    LLMController.messagePrompt()         │
│    • Convert UserChat to ChatMessages    │
│    • Inject code context between msgs    │
└─────┬────────────────────────────────────┘
      │ 2. Call GeminiService
      ▼
┌──────────────────────────────────────────┐
│    GeminiService.messageModel()          │
│    • Delegates to assistant              │
└─────┬────────────────────────────────────┘
      │ 3. Call agent
      ▼
┌──────────────────────────────────────────────────┐
│    CodeHelperAssistant.chat()                    │
│    (@AiService - LangChain4j agent)              │
│    • Analyzes context + chat history             │
│    • Reasons about user's problem                │
│    • Decides if tool invocation needed           │
└─────┬────────────────────────────────────────────┘
      │
      │ IF agent decides to test code:
      │ 4. Agent autonomously invokes tool
      ▼
┌──────────────────────────────────────────────────┐
│    CodeExecutionTools.executeCode()              │
│    (@Tool method)                                │
│    • Creates CodeSubmission                      │
│    • Submits to CodeExecutionService (same queue)│
│    • Polls for results (20 min timeout)          │
│    • Returns truncated output (1MB limit)        │
└─────┬────────────────────────────────────────────┘
      │ 5. Tool returns result to agent
      ▼
┌──────────────────────────────────────────────────┐
│    Agent interprets tool result                  │
│    • Verifies if fix worked                      │
│    • Generates debugging advice                  │
│    • Suggests corrected code                     │
└─────┬────────────────────────────────────────────┘
      │ 6. Return response
      ▼
┌────────────┐
│  Frontend  │
│  Displays  │
│  Markdown  │
└────────────┘
```

### Key Components

#### Frontend (`cr-frontend/src/`)
- `App.jsx` - Main app with polling logic
- `components/CodeEditor.jsx`, `Terminal.jsx`, `ChatInterface.jsx` - UI components
- `components/SyntaxHighlighter.jsx` - Regex-based highlighting

#### Backend (`src/main/java/com/cr/coderunner/`)
- `controller/IDEController.java` - `/submit`, `/check` endpoints
- `controller/LLMController.java` - `/llm/message` endpoint
- `model/CodeSubmission.java` - Docker execution engine
- `service/CodeExecutionService.java` - **10-worker thread pool queue manager**
- `service/CodeHelperAssistant.java` - **@AiService LangChain4j agent**
- `service/CodeExecutionTools.java` - **@Tool methods for agent**
- `service/LLMConfig.java` - ChatModel bean config

## Prerequisites

- **Docker**, **Java 21+**, **Node.js v18+**, **Maven**
- **Google Gemini API Key** for AI assistant

## Installation

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/CodeRunner.git && cd CodeRunner
```

### 2. Backend Setup
```bash
# Set gemini.api.key in application.properties or export GEMINI_API_KEY
echo "gemini.api.key=your_api_key_here" >> src/main/resources/application.properties
mvn clean install && mvn spring-boot:run
```

### 3. Frontend Setup
```bash
cd cr-frontend
npm install
echo "VITE_API_URL=http://localhost:8080" > .env
npm run dev
```

### 4. Docker Setup
```bash
docker pull alpine:latest python:3.12-alpine eclipse-temurin:21-alpine
```

## Usage

### Running Code
1. Open `http://localhost:5173`, select language
2. Write code (or load template), optionally add stdin input
3. Click "RUN" → polls for results → view output/errors
4. Click "STOP" to terminate if needed

### Using Code_Helper
1. Switch to "CODE_HELPER" tab
2. Ask questions about your code
3. Agent analyzes context, may autonomously test code via tools
4. Receive debugging suggestions with markdown formatting
5. Chat history persists across tab switches

## API Endpoints

### Code Execution Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/submit` | Submit code for execution, returns UUID |
| POST | `/check` | Check execution status by UUID (returns RunResult) |
| GET | `/check_queue` | List all active executions in queue (debug endpoint) |
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
# Submit code → returns UUID
curl -X POST http://localhost:8080/submit -H "Content-Type: application/json" \
  -d '{"code": "print(\"Hello\")", "language": "Python", "problem": "test", "input": ""}'

# Check status → returns RunResult
curl -X POST http://localhost:8080/check -H "Content-Type: application/json" \
  -d '"a1b2c3d4-e5f6-7890-abcd-ef1234567890"'

# Response: {"success": true, "runtime": 0.123, "output": "Hello\n", "status": "FINISHED"}

# Ask AI (agent may autonomously test code)
curl -X POST http://localhost:8080/llm/message -H "Content-Type: application/json" \
  -d '{
    "messages": [{"role": "user", "content": "Why is my code not working?"}],
    "code": {"code": "print(x)", "language": "Python"},
    "result": {"success": false, "error": "NameError: name '\''x'\'' is not defined"}
  }'

# Check queue
curl http://localhost:8080/check_queue
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
│   ├── service/                # Business logic & AI integration
│   │   ├── CodeExecutionService.java    # Execution queue manager
│   │   ├── CodeExecutionTools.java      # LangChain4j agent tools
│   │   ├── CodeHelperAssistant.java     # LangChain4j @AiService agent
│   │   ├── LLMConfig.java               # ChatModel Spring Bean config
│   │   ├── GeminiService.java           # AI service facade
│   │   ├── UserData.java                # In-memory storage (legacy)
│   │   └── WebConfig.java               # CORS configuration
│   └── dto/                    # Data transfer objects
│       ├── RunResult.java
│       ├── UserChat.java
│       └── ChatBlock.java
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
mvn test                           # Backend
cd cr-frontend && npm run lint     # Frontend
```

### Building for Production
```bash
# Backend
mvn clean package && java -jar target/CodeRunner-0.0.1-SNAPSHOT.jar

# Frontend
cd cr-frontend && npm run build    # outputs to dist/

# Docker
docker build -t coderunner .
docker run --privileged --cgroupns=host -p 8080:8080 \
  -e gemini.api.key=your_api_key_here coderunner
```

## Roadmap

### Recently Completed
- [x] LangChain4j agent framework with @AiService and @Tool
- [x] Agent autonomous code testing via tool invocation
- [x] Asynchronous execution queue (10-worker thread pool)
- [x] UUID-based polling for non-blocking execution
- [x] Scheduled cleanup of stale executions

### Planned Features
- [ ] C++/JavaScript/TypeScript language support
- [ ] SQL database migration (currently in-memory)
- [ ] User authentication and session management
- [ ] Problem/challenge system
- [ ] Code sharing with shareable links

## Technical Details

### Execution Safety
- 60-second timeout, 1MB output limit (backend), 500KB (frontend)
- Docker containerization for isolated execution
- Multi-threaded stdout/stderr readers prevent deadlocks
- Automatic cleanup of temp files and containers
- Scheduled task removes stale executions (60s old, runs every 30s)

### Agent Safety & Constraints

- **Read-Only Design**: Agent suggests fixes, user manually applies (no direct code modification)
- **Sparing Tool Use**: System prompt limits autonomous testing frequency
- **Output Limits**: 1MB backend, 1000 chars for LLM context (prevents token overflow)
- **Pattern**: Agent analyzes → tests hypothesis via tool → suggests verified fix

```
Agent analyzes → Tests via tool → Verifies fix → Suggests to user
```

### Docker Execution Details

```bash
# Python
docker run --name <uuid> --rm -v /host:/sandbox python:3.12-alpine \
  sh -c "python3 sandbox/code.py < sandbox/input.txt"

# C
docker run --name <uuid> --rm -v /host:/sandbox alpine:latest \
  sh -c "apk add gcc musl-dev && gcc sandbox/code.c -o sandbox/main && ./sandbox/main"

# Java
docker run --name <uuid> --rm -v /host:/sandbox eclipse-temurin:21-alpine \
  sh -c "java sandbox/code.java < sandbox/input.txt"
```

### Execution Queue Architecture

```java
// 10-worker thread pool
private final ExecutorService executor = Executors.newFixedThreadPool(10);
private final ConcurrentHashMap<String, CodeExecution> results;

String uuid = executionService.execute(new CodeExecution(submission));
RunResult result = executionService.checkExecution(uuid);
// Status: "RUNNING" | "FINISHED" | "NONEXISTENT"
```

**Lifecycle**: Submit (UUID) → Worker executes → Store in HashMap → Poll → Cleanup

```java
@Scheduled(fixedRate = 30_000) // Cleanup every 30s
public void cleanExecutions() {
    // Removes executions older than 60 seconds
}
```

### AI Integration (LangChain4j Agent Framework)

```
┌──────────────────────────────────────────────────────────┐
│ LangChain4j Spring Boot Auto-Configuration              │
├──────────────────────────────────────────────────────────┤
│  @AiService Interface (CodeHelperAssistant)              │
│  • Interface with @SystemMessage + chat() method         │
│  • Auto-implemented by LangChain4j                       │
│                                                           │
│  @Tool Methods (CodeExecutionTools)                      │
│  • executeCode(code, language, input) → String           │
│  • Agent invokes autonomously during reasoning           │
│                                                           │
│  ChatModel Bean (LLMConfig)                              │
│  • GoogleAiGeminiChatModel (gemini-2.5-flash)           │
│  • Auto-injected into agent                              │
└──────────────────────────────────────────────────────────┘
```

**Dependencies**:
```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>1.11.0-beta19</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-google-ai-gemini</artifactId>
    <version>1.11.0</version>
</dependency>
```

### Supported Languages

| Language | Execution Method | Docker Image | Status |
|----------|------------------|--------------|--------|
| Java | eclipse-temurin:21-alpine | Yes | ✅ Fully Supported |
| Python | python:3.12-alpine | Yes | ✅ Fully Supported |
| C | alpine:latest + GCC | Yes | ✅ Fully Supported |
| C++ | alpine:latest + G++ | Planned | 🚧 In Development |

## Configuration

### Environment Variables

```bash
# Backend (application.properties)
gemini.api.key=your_gemini_api_key_here

# Frontend (cr-frontend/.env)
VITE_API_URL=http://localhost:8080
```

**CORS**: Allows `http://localhost:5173` (configured in `WebConfig.java`)

## Contributing

Fork → Create branch → Commit → Push → Open PR

## Known Issues & TODOs

- Fine-tune agent prompt and add token usage tracking
- Optimize Docker image caching and add rate limiting
- Remove debug print statements
- Migrate to SQL database
- Add user authentication

## License

This project is open source and available under the [MIT License](LICENSE).

## Acknowledgments

Built with **Spring Boot**, **React**, **Docker**, **Google Gemini AI**, and **LangChain4j**. Inspired by LeetCode and HackerRank.

---

**Note**: Active development. AI assistant requires **Gemini API key**. Backend uses **async execution queue** (10-worker thread pool) and **LangChain4j agent framework** with autonomous code testing.
