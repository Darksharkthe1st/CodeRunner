# CodeRunner

A full-stack web-based IDE that allows users to write, compile, and execute code in multiple programming languages directly in their browser. Built with React and Spring Boot, CodeRunner provides a safe, containerized execution environment using Docker.

## Features

- **Multi-Language Support**: Currently supports Java and C, with plans for C++ and Python
- **Real-Time Code Execution**: Write code and see results instantly
- **Syntax Highlighting**: Custom regex-based syntax highlighter for supported languages
- **Safe Execution Environment**: Docker containerization for C/C++ code provides isolated execution
- **Input/Output Handling**: Support for custom input via stdin
- **Code Templates**: Quick-start templates for each supported language
- **Responsive UI**: Clean, two-column layout with code editor and output console
- **Error Handling**: Comprehensive error capture for compilation and runtime errors
- **Execution Limits**: 10-second timeout and output size limits to prevent abuse

## Tech Stack

### Frontend
- **React 19** - UI framework
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Styling
- **Custom Syntax Highlighter** - Regex-based highlighting for C, C++, Java, Python

### Backend
- **Spring Boot 4.0.1** - REST API framework
- **Java 21** - Backend runtime
- **Docker** - Containerized code execution
- **Maven** - Build and dependency management

## Architecture

### Request Flow
1. User writes code in the browser editor
2. Frontend sends code to `POST /submit` endpoint
3. Backend stores submission in memory (UserData service)
4. Frontend sends `POST /run` with optional stdin input
5. Backend creates asynchronous CodeExecution thread
6. Code is executed:
   - **Java**: Direct JVM execution via ProcessBuilder
   - **C/C++**: Docker container with gcc compiler
7. Output streamed through multi-threaded readers (stdout/stderr)
8. Results returned as JSON via RunResult DTO

### Key Components

#### Frontend (`cr-frontend/src/`)
- `App.jsx` - Main application with split-pane editor and output
- `components/TextWindow.jsx` - Code editor with overlay for syntax highlighting
- `components/SyntaxHighlighter.jsx` - Language-specific syntax highlighting

#### Backend (`src/main/java/com/cr/coderunner/`)
- `controller/IDEController.java` - REST API endpoints
- `model/CodeSubmission.java` - Core execution engine
- `model/CodeExecution.java` - Async execution wrapper
- `service/UserData.java` - In-memory submission storage
- `service/WebConfig.java` - CORS configuration

## Prerequisites

- **Docker** - Required for C/C++ code execution
- **Java 21** - Backend runtime
- **Node.js** (v18+) - Frontend development
- **Maven** - Backend build tool

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/CodeRunner.git
cd CodeRunner
```

### 2. Backend Setup
```bash
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

1. Open your browser to `http://localhost:5173`
2. Select a programming language from the dropdown
3. Write your code in the editor (or use the provided template)
4. (Optional) Provide input in the input box
5. Click "Run" to execute
6. View output, errors, and execution time in the output panel

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/submit` | Submit code for compilation |
| POST | `/run` | Execute submitted code with optional input |
| GET | `/check/{id}` | Check if submission exists |
| GET | `/get_template/{lang}` | Get code template for language |
| GET | `/supported` | Get list of supported languages |

### Example Request
```bash
# Submit code
curl -X POST http://localhost:8080/submit \
  -H "Content-Type: application/json" \
  -d '{"code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello World\"); }}", "lang": "java"}'

# Run code (returns submission ID)
curl -X POST http://localhost:8080/run \
  -H "Content-Type: application/json" \
  -d '{"subId": "<submission-id>", "input": ""}'
```

## Project Structure

```
CodeRunner/
├── cr-frontend/                 # React frontend
│   ├── src/
│   │   ├── components/         # React components
│   │   ├── App.jsx             # Main application
│   │   └── main.jsx            # Entry point
│   ├── public/                 # Static assets
│   └── package.json
├── src/main/java/com/cr/coderunner/
│   ├── controller/             # REST controllers
│   ├── model/                  # Domain models
│   ├── service/                # Business logic
│   └── dto/                    # Data transfer objects
├── .test/                      # Temporary execution files (git-ignored)
├── Dockerfile                  # Production Docker image
├── docker-entrypoint.sh        # Docker startup script
├── pom.xml                     # Maven configuration
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
docker build -t coderunner .
docker run --privileged --cgroupns=host -p 8080:8080 coderunner
```

## Roadmap

- [ ] Add C++ and JS language support
- [ ] Migrate from in-memory storage to SQL database
- [ ] Implement user authentication
- [ ] Add problem/challenge system to frontend (ProblemController)
- [ ] Syntax error highlighting in editor
- [ ] Code sharing functionality
- [ ] Execution history
- [ ] Custom compilation flags
- [ ] Support for additional languages (Go, Rust, JavaScript)

## Technical Details

### Execution Safety
- **Timeouts**: 10-second hard limit on execution time
- **Output Limits**: Protection against output overflow
- **Containerization**: Isolated Docker containers for compiled languages
- **Multi-threading**: Separate threads for stdout/stderr to prevent deadlocks
- **Cleanup**: Automatic removal of temporary files and Docker containers

### Supported Languages

| Language | Execution Method | Status |
|----------|-----------------|--------|
| Java | eclipse-temurin:13-alpine | ✅ Supported |
| C | alpine:latest | ✅ Supported |
| C++ | alpine:latest | 🚧 Planned |
| Python | python:3.12-alpine | ✅ Supported |

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is open source and available under the [MIT License](LICENSE).

## Acknowledgments

- Built with Spring Boot and React
- Uses Docker for safe code execution
- Inspired by online code editors like LeetCode and HackerRank

---

**Note**: This project is under active development. Some features may be incomplete or subject to change.
