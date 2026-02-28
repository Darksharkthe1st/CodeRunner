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
- **Code_Helper Agent**: LangChain4j-powered AI agent using Google Gemini 2.5 Flash
- **Autonomous Testing**: Agent can execute code independently to verify fixes using `@Tool` methods
- **Context-Aware**: Understands your code, language, input, and execution results
- **Persistent Chat**: Multi-turn conversations that persist across tab switches
- **Markdown Support**: Rich text formatting with syntax highlighting in responses
- **Debug Assistance**: Get help identifying bugs, design flaws, and errors with AI-verified solutions
- **Real-time Help**: Chat interface for interactive code debugging and learning
- **Tool Invocation**: Agent autonomously decides when to test code (used sparingly per system prompt)

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
- **Spring Boot 4.0.1** - REST API framework with @Scheduled task support
- **Java 21** - Backend runtime (targets Java 25 for compilation)
- **Docker** - Containerized code execution for all languages
- **Maven** - Build and dependency management
- **LangChain4j Spring Boot Starter 1.11.0-beta19** - AI agent framework with @AiService and @Tool
- **LangChain4j Google AI Gemini 1.11.0** - Gemini model integration
- **Google Gemini 2.5 Flash** - LLM for agent-based code assistance
- **ExecutorService** - Fixed thread pool (10 workers) for async execution queue
- **ConcurrentHashMap** - Thread-safe execution tracking
- **Spring Boot Actuator** - Health monitoring and metrics
- **Micrometer Prometheus** - Metrics collection

## Architecture

CodeRunner uses a modern **asynchronous execution queue model** with **LangChain4j agent framework** for intelligent code assistance.

### Execution Service & Queue Model

The backend uses `CodeExecutionService` with a fixed thread pool to handle concurrent code executions:

```
User Submission → UUID Generated → Submitted to Queue (10 worker threads) →
Async Execution → Docker Container → Output Collection →
Poll for Results (RUNNING/FINISHED) → Cleanup
```

**Key Features**:
- **Fixed Thread Pool**: 10 concurrent execution workers using Java `ExecutorService`
- **ConcurrentHashMap**: Thread-safe UUID-based execution tracking
- **Polling Model**: Frontend polls `/check` endpoint with UUID for status updates
- **Scheduled Cleanup**: Automatic removal of expired executions every 30 seconds (60+ second old results)
- **Non-blocking**: Submit endpoint returns immediately with UUID for tracking

### Request Flow (IDE Execution)
1. User writes code in the browser editor
2. Frontend sends code + language to `POST /submit` endpoint
3. `CodeExecutionService` creates `CodeExecution` thread, assigns UUID, submits to thread pool
4. UUID returned immediately to frontend (non-blocking)
5. Frontend polls `POST /check` endpoint with UUID every 500ms
6. Worker thread executes code in Docker container:
   - **Java**: `eclipse-temurin:21-alpine` container
   - **Python**: `python:3.12-alpine` container
   - **C**: `alpine:latest` + GCC compilation
7. Output streamed through multi-threaded readers (stdout/stderr)
8. Results stored in `CodeExecution` object, marked as FINISHED
9. Frontend receives complete `RunResult` DTO on next poll
10. Execution removed from queue after retrieval or expires after 60 seconds

### AI Assistant Flow (Agent with Tool Invocation)

CodeRunner integrates **LangChain4j Spring Boot** with a custom agent (`CodeHelperAssistant`) that can **test code autonomously**:

```
User Message → /llm/message → GeminiService → CodeHelperAssistant.chat() →
Gemini 2.5 Flash analyzes context + chat history →
[OPTIONAL] Agent invokes CodeExecutionTools.executeCode() →
Tool submits to same execution queue → Poll for results → Tool returns output →
Agent interprets results → Generates debugging advice → Returns to user
```

**Key Features**:
- **@AiService Interface**: `CodeHelperAssistant` is a LangChain4j agent interface with system prompt
- **Tool Invocation**: Agent can call `@Tool` methods in `CodeExecutionTools` to test code
- **Context Awareness**: Receives full chat history, current code, language, input, and execution results
- **Autonomous Testing**: Agent decides when to execute code to verify fixes (use sparingly per system prompt)
- **Read-Only Design**: Agent cannot modify user code directly, only suggests fixes
- **Output Truncation**: Tool results limited to 1MB to prevent token overflow

### Agent Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    LangChain4j Framework                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  CodeHelperAssistant (@AiService)                           │
│  ├── System Prompt: "You are Code_Helper..."               │
│  ├── chat(List<ChatMessage>) method                         │
│  └── Automatically wired to ChatModel bean                  │
│                                                              │
│  CodeExecutionTools (@Component)                            │
│  ├── @Tool: executeCode(code, language, input)             │
│  ├── Submits to CodeExecutionService                        │
│  ├── Polls for results (20 min timeout)                     │
│  └── Returns truncated output (1MB limit)                   │
│                                                              │
│  LLMConfig (@Configuration)                                 │
│  ├── Creates ChatModel Spring Bean                          │
│  ├── Model: gemini-2.5-flash                                │
│  └── API Key from environment variable                      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
                  CodeExecutionService
                  (Shared execution queue)
```

**How Tool Invocation Works**:
1. User asks: "Why doesn't this code work?"
2. Agent analyzes code context and error messages
3. Agent decides to test a fix hypothesis
4. Agent invokes `CodeExecutionTools.executeCode("fixed_code", "Java", "test_input")`
5. Tool submits to execution queue, polls for completion
6. Tool returns results: `"Compilation error on line 5: missing semicolon"`
7. Agent incorporates results into response: "The issue is a missing semicolon. Here's the corrected version..."

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
- `App.jsx` - Main application with split-pane layout, state management, and polling logic
- `components/CodeEditor.jsx` - Code editor with overlay for syntax highlighting
- `components/Terminal.jsx` - Output display with execution animations
- `components/ChatInterface.jsx` - AI chat interface with markdown rendering
- `components/SyntaxHighlighter.jsx` - Language-specific syntax highlighting
- `components/Alert.jsx` - Success/error notification system
- `components/ExecutingAnimation.jsx` - Visual feedback during code execution

#### Backend (`src/main/java/com/cr/coderunner/`)
- `controller/IDEController.java` - REST API endpoints for code execution (`/submit`, `/check`, `/check_queue`)
- `controller/LLMController.java` - REST API endpoints for AI chat (`/llm/message`)
- `controller/HealthController.java` - Health check endpoints
- `model/CodeSubmission.java` - Core execution engine with Docker integration
- `model/CodeExecution.java` - Thread wrapper for async execution in queue
- `service/CodeExecutionService.java` - **Execution queue manager with 10-worker thread pool**
- `service/CodeExecutionTools.java` - **LangChain4j @Tool methods for agent code testing**
- `service/CodeHelperAssistant.java` - **LangChain4j @AiService agent interface**
- `service/LLMConfig.java` - **Spring Bean configuration for Gemini ChatModel**
- `service/UserData.java` - In-memory submission storage (legacy, for template retrieval)
- `service/GeminiService.java` - Delegates to CodeHelperAssistant agent
- `service/WebConfig.java` - CORS configuration
- `dto/UserChat.java` - Data transfer object for chat messages with code context
- `dto/RunResult.java` - Execution result with status (RUNNING/FINISHED/NONEXISTENT)

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
# Option 1: Add to application.properties
echo "gemini.api.key=your_api_key_here" >> src/main/resources/application.properties

# Option 2: Set as environment variable (for production)
export GEMINI_API_KEY=your_api_key_here

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

# Ask AI for help (agent may invoke tools autonomously)
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

# Response: Agent analyzes error, may test fix via CodeExecutionTools
# Agent internally calls: executeCode("x = 'Hello'\nprint(x)", "Python", "")
# Returns: "The error occurs because variable 'x' is undefined. Here's the corrected version..."

# Check execution queue status
curl http://localhost:8080/check_queue
# Response: List of active execution UUIDs with status
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
  -e gemini.api.key=your_api_key_here \
  coderunner
```

## Roadmap

### Recently Completed
- [x] AI-powered code assistant with context awareness
- [x] Multi-turn chat conversations
- [x] Markdown rendering in AI responses
- [x] Persistent chat history across UI navigation
- [x] **LangChain4j agent framework** with @AiService and @Tool integration
- [x] **Agent autonomous code testing** via tool invocation
- [x] **Asynchronous execution queue** with 10-worker thread pool
- [x] **UUID-based polling** for non-blocking code execution
- [x] **Scheduled cleanup** of stale executions

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
- **Timeouts**: 60-second hard limit on execution time per submission
- **Output Limits**: 1MB backend limit, 500KB frontend limit to prevent memory overflow
- **Containerization**: Isolated Docker containers for all language execution
- **Resource Limits**: Docker resource constraints (commented in code, ready to enable)
- **Multi-threading**: Separate threads for stdout/stderr to prevent deadlocks
- **Cleanup**: Automatic removal of temporary files and Docker containers
- **Error Handling**: Graceful handling of container failures and timeouts
- **Queue Overflow**: Thread pool queue can handle bursts; blocks if all 10 workers busy
- **Stale Execution Cleanup**: Scheduled task removes executions older than 60 seconds

### Agent Safety & Constraints

The CodeHelper agent is designed with specific constraints to ensure safe and appropriate usage:

**Design Principles**:
- **Read-Only**: Agent **cannot** modify user code directly in the editor
- **Suggestive**: Agent provides corrected code that users must manually copy/paste
- **Sparing Tool Use**: System prompt instructs agent to use code execution "sparingly"
- **No Code Modification**: Agent is a debugging assistant, not a code writer
- **Token Awareness**: Tool outputs truncated to prevent context overflow

**Why Read-Only?**:
This design decision prevents several issues:
1. Avoids overwriting user work without explicit consent
2. Keeps user in control of their code
3. Forces user to understand changes before applying them
4. Prevents accidental code corruption from agent errors
5. Maintains clear separation between user actions and agent suggestions

**Tool Output Limits**:
- Backend: 1MB max output per execution
- LLM Context: 1000 characters max (via `displayStrShorter()`)
- Prevents: Token limit exceeded errors, excessive API costs, context window overflow

**Agent Tool Usage Pattern**:
```
Agent analyzes → Identifies potential fix → Tests hypothesis via tool →
Verifies fix works → Suggests corrected code to user
```

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

### Execution Queue Architecture

**CodeExecutionService** manages concurrent code executions using a fixed thread pool:

```java
// 10-worker thread pool for parallel executions
private final ExecutorService executor = Executors.newFixedThreadPool(10);

// Thread-safe result tracking
private final ConcurrentHashMap<String, CodeExecution> results;

// Submit execution (non-blocking)
String uuid = executionService.execute(new CodeExecution(submission));

// Poll for results
RunResult result = executionService.checkExecution(uuid);
// Status: "RUNNING" | "FINISHED" | "NONEXISTENT"
```

**Lifecycle**:
1. **Submit**: `execute()` generates UUID, submits `CodeExecution` thread to pool, returns UUID
2. **Execute**: Worker thread runs `CodeExecution.run()` → `CodeSubmission.run()` → Docker execution
3. **Track**: Execution stored in `ConcurrentHashMap` with UUID key
4. **Poll**: Frontend calls `checkExecution(uuid)` every 500ms
5. **Complete**: When done, execution marked with `done=true` and `completedAt` timestamp
6. **Cleanup**: Results removed from map on first retrieval (FINISHED status) or by scheduled cleanup (60+ seconds old)

**Scheduled Cleanup**:
```java
@Scheduled(fixedRate = 30_000) // Every 30 seconds
public void cleanExecutions() {
    // Removes executions older than 60 seconds
    // Prevents memory leaks from abandoned/forgotten submissions
}
```

### AI Integration (LangChain4j Agent Framework)

CodeRunner uses **LangChain4j Spring Boot Starter** for intelligent agent-based code assistance:

**Architecture**:
```
┌──────────────────────────────────────────────────────────┐
│ LangChain4j Spring Boot Auto-Configuration              │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  @AiService Interface (CodeHelperAssistant)              │
│  • Interface-based agent definition                      │
│  • @SystemMessage defines agent role/behavior            │
│  • chat(List<ChatMessage>) method signature              │
│  • Auto-implemented by LangChain4j at runtime            │
│                                                           │
│  @Tool Methods (CodeExecutionTools)                      │
│  • @Component class with @Tool annotated methods         │
│  • executeCode(code, language, input) → String           │
│  • Auto-discovered and wired to agent                    │
│  • Agent can invoke tools autonomously during reasoning  │
│                                                           │
│  ChatModel Bean (LLMConfig)                              │
│  • Spring @Bean creates GoogleAiGeminiChatModel          │
│  • Model: gemini-2.5-flash (fast, cost-effective)       │
│  • Auto-injected into agent                              │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

**Key Features**:
- **@AiService**: Interface-based agent definition with system prompt
- **@Tool**: Methods the agent can invoke autonomously (e.g., code execution)
- **Context Awareness**: Agent receives full chat history + code + execution results
- **Tool Invocation**: Agent decides when to test code to verify fixes
- **Output Limits**: Tool results truncated to 1MB (backend) / 1000 chars (LLM context)
- **Spring Integration**: Seamless dependency injection and auto-configuration

**Tool Usage Pattern**:
1. User: "Why is my code crashing?"
2. Agent analyzes code + error message from context
3. Agent invokes tool: `executeCode(modified_code, "Java", "test_input")`
4. Tool submits to execution queue, polls for results
5. Tool returns: "Output: Hello World\nSuccess: true"
6. Agent: "Your code had a NullPointerException on line 10. I tested a fix and it works. Copy this corrected version..."

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

#### Backend (application.properties or .env)
```bash
# Required for LangChain4j agent functionality
gemini.api.key=your_gemini_api_key_here
```

Note: The property `gemini.api.key` is injected via `@Value("${gemini.api.key}")` in `LLMConfig.java`

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

### Agent & LLM
- Fine-tune agent system prompt for optimal tool usage frequency
- Add token usage tracking and cost monitoring for Gemini API calls
- Implement conversation memory pruning for long chat sessions
- Add more specialized tools for agent (e.g., code linting, syntax checking)

### Execution Infrastructure
- Optimize Docker image pulling and caching
- Add rate limiting for API endpoints
- Improve error messages for Docker failures
- Add unit tests for CodeSubmission execution logic
- Implement execution priority queue for premium users

### General
- Remove debug print statements (e.g., "We got here bro" in CodeSubmission.java)
- Migrate UserData from in-memory to SQL database
- Implement proper session management for chat history persistence across sessions
- Add user authentication and authorization

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

**Note**: This project is under active development. The AI code assistant uses **LangChain4j agent framework** with autonomous code testing capabilities and requires a **Google Gemini API key** for full functionality. The backend uses an **asynchronous execution queue model** with 10-worker thread pool for handling concurrent code executions. Some features may be incomplete or subject to change.
