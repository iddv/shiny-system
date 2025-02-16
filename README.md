# Interactive Adventure Game Backend

A Kotlin-based backend service using Ktor and Ollama for AI-powered text adventure game generation. This service powers the interactive chat-based adventure game by managing game state and generating dynamic narrative responses.

## 🌟 Features

- Real-time AI text generation using Ollama
- RESTful API endpoints for game state management
- Session handling
- Character state tracking (health, gold, inventory)
- Dynamic story generation
- Event parsing for game mechanics
- Persistent conversation history

## 🚀 Getting Started

### Prerequisites

1. JDK 17 or higher
   ```bash
   # Check Java version
   java -version
   ```

2. Gradle
   ```bash
   # Check Gradle version
   gradle -v
   ```

3. Ollama
   ```bash
   # Install from https://ollama.ai/
   # Pull required model
   ollama pull deepseek-r1:14b
   ```

### Installation & Setup

1. Clone the repository
   ```bash
   git clone https://github.com/iddv/chat-app-backend.git
   cd chat-app-backend
   ```

2. Build the project
   ```bash
   ./gradlew build
   ```

3. Start the server
   ```bash
   ./gradlew run
   ```

The server will start on `http://localhost:8080`

### Verify Installation

1. Check if the server is running:
   ```bash
   curl http://localhost:8080
   ```
   Should return a greeting message

2. Check if Ollama is running:
   ```bash
   curl http://localhost:11434/api/generate
   ```
   Should receive a response (might be an error, but should connect)

## 🔌 API Endpoints

### Generate Adventure
```http
POST /generate-adventure
Content-Type: application/json

{
    "setting": "medieval fantasy",
    "genre": "fantasy",
    "playerCharacter": "adventurer",
    "theme": "heroic",
    "toneStyle": "classic fantasy",
    "additionalDetails": ""
}
```

### Process Player Action
```http
POST /ask
Content-Type: text/plain

"explore the cave"
```

### Update Character State
```http
PUT /game-state/{sessionId}/character
Content-Type: application/json

{
    "health": 100,
    "gold": 50,
    "inventory": ["sword", "health potion"]
}
```

## 📁 Project Structure

```
chat-app-backend/
├── src/
│   └── main/
│       └── kotlin/
│           └── org/
│               └── socialspaces/
│                   ├── Application.kt
│                   ├── models/
│                   │   ├── AdventureSettings.kt
│                   │   ├── CharacterState.kt
│                   │   └── ConversationState.kt
│                   └── services/
│                       └── OllamaService.kt
├── build.gradle.kts
└── gradle/
```

## 🛠️ Dependencies

- [Ktor](https://ktor.io/) - Kotlin async web framework
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization) - JSON parsing
- [Ollama](https://ollama.ai/) - Local AI model running
- [Gradle](https://gradle.org/) - Build automation

## ⚙️ Configuration

### Server Configuration
- Default port: 8080
- Host: 0.0.0.0
- CORS enabled for frontend development

### Ollama Configuration
- Model: deepseek-r1:14b
- Endpoint: http://localhost:11434
- Request timeout: 5 minutes
- Connection timeout: 30 seconds

## 🔗 Frontend Integration

This backend is designed to work with our React frontend:
- Frontend repository: https://github.com/iddv/chat-app
- Frontend typically runs on port 5173

Complete system requires:
1. This backend server (port 8080)
2. Ollama service (port 11434)
3. Frontend service (port 5173)

## 🔜 Roadmap

- [ ] Persistent storage for game states
- [ ] User authentication
- [ ] Multiple game sessions
- [ ] Enhanced AI prompt engineering
- [ ] Combat system
- [ ] Quest management
- [ ] Item and inventory system
- [ ] Character progression system

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 🐛 Known Issues

- Session management is currently using in-memory storage
- No persistent storage for game states
- Limited error handling for AI service timeouts

For the frontend implementation, visit the [frontend repository](https://github.com/iddv/chat-app)