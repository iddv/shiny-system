```markdown
# Interactive Adventure Game Backend

A Kotlin-based backend service built with Ktor and Ollama for AI-powered interactive text adventures. This service manages session handling, dynamic narrative generation (both standard and streaming via SSE), and even serves dynamic SVG placeholders for design purposes.

## 🌟 Features

- **Real-Time AI Text Generation:**  
  Leverages Ollama to generate immersive adventure narratives.
- **SSE Streaming:**  
  Provides Server-Sent Events (SSE) endpoints for live streaming of narrative updates.
- **Session Management:**  
  In-memory session handling for storing adventure configuration.
- **Adventure Configuration:**  
  Customize game settings including genre, tone, and additional details.
- **Dynamic Story Responses:**  
  Generates both full and chunked responses based on player actions.
- **SVG Placeholder Generation:**  
  Dynamically creates SVG images for use in frontend design.
- **Extensible Architecture:**  
  Easily integrates with React frontends and can be extended for features like combat, quests, and character progression.

## 🚀 Getting Started

### Prerequisites

1. **JDK 17 or Higher:**  
   Ensure Java is installed:
   ```bash
   java -version
   ```

2. **Gradle:**  
   Verify Gradle installation:
   ```bash
   gradle -v
   ```

3. **Ollama:**  
   Install and set up Ollama (visit [ollama.ai](https://ollama.ai/) for instructions).  
   Pull the required model (default is `deepseek-r1:14b`):
   ```bash
   ollama pull deepseek-r1:14b
   ```
   To use a different model, set the `OLLAMA_MODEL` environment variable:
   ```bash
   export OLLAMA_MODEL="mistral:latest"
   ```

### Installation & Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/iddv/shiny-system.git
   cd shiny-system
   ```

2. **Build the Project:**
   ```bash
   ./gradlew build
   ```

3. **Start the Server:**
   ```bash
   ./gradlew run
   ```
   The server will start on [http://localhost:8080](http://localhost:8080).

### Full Setup Instructions

- **Ensure Prerequisites:**  
  Install JDK 17+, Gradle, and Ollama as described above.

- **Configure Environment Variables (Optional):**  
  Change the default Ollama model if needed:
  ```bash
  export OLLAMA_MODEL="your-model-name"
  ```

- **Clone, Build, and Run:**  
  Follow the steps above to set up the project.

- **CORS Configuration:**  
  CORS is enabled for all hosts by default (adjust in `Application.kt` for production use).

## 🔌 API Endpoints

### 1. Root & Health Check

- **GET /**  
  Returns a greeting message.

- **GET /health**  
  Checks if the server is running.
  ```bash
  curl http://localhost:8080/health
  ```

### 2. Adventure Configuration

- **POST /set-adventure-settings**  
  Configure your adventure settings.
  ```http
  POST /set-adventure-settings
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
  _Note:_ The response includes an `X-Session-ID` header for session tracking.

- **GET /config**  
  Retrieves the current server configuration.
  ```bash
  curl http://localhost:8080/config
  ```

### 3. Starting an Adventure

- **POST /start-adventure**  
  Start a new adventure using the provided settings.
  ```http
  POST /start-adventure
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
  Returns the generated opening narrative.

- **GET /stream-adventure**  
  Begin a streaming adventure session via SSE.  
  _Note:_ Include the `X-Session-ID` header if available.
  ```bash
  curl -N http://localhost:8080/stream-adventure
  ```

### 4. Processing Player Actions

- **POST /action**  
  Process a player's action and receive a narrative response.
  ```http
  POST /action
  Content-Type: application/json

  {
      "action": "explore the dark forest"
  }
  ```

- **POST /stream-action**  
  Process a player's action with a streaming (SSE) narrative response.
  ```http
  POST /stream-action
  Content-Type: application/json

  {
      "action": "enter the ancient ruins"
  }
  ```

### 5. SVG Placeholder Generation

- **GET /placeholder/{width}/{height}**  
  Generates a dynamic SVG placeholder image.
  ```bash
  curl http://localhost:8080/placeholder/400/300
  ```
  Returns an SVG image with the specified dimensions.

## 📁 Project Structure

```
interactive-adventure-backend/
├── src/
│   └── main/
│       └── kotlin/
│           └── org/
│               └── shinysystem/
│                   ├── Application.kt
│                   ├── models/
│                   │   ├── AdventureSettings.kt
│                   │   ├── OllamaRequest.kt
│                   │   ├── OllamaResponse.kt
│                   │   └── ApiResponse.kt
│                   └── services/
│                       └── ApiService.kt
├── build.gradle.kts
└── gradle/
```

## 🛠️ Dependencies

- [Ktor](https://ktor.io/) – Asynchronous web framework for Kotlin
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization) – For JSON parsing and serialization
- [Ollama](https://ollama.ai/) – Local AI model service
- [Gradle](https://gradle.org/) – Build automation tool

## ⚙️ Configuration

### Server Configuration
- **Default Port:** 8080
- **Host:** 0.0.0.0
- **CORS:** Enabled for all hosts (modify for production)

### Ollama Configuration
- **Model:** Default is `deepseek-r1:14b` (override via `OLLAMA_MODEL`)
- **Endpoint:** `http://localhost:11434/api/generate`
- **Timeouts:**
   - Request Timeout: 5 minutes
   - Connection Timeout: 30 seconds
   - Socket Timeout: 5 minutes

## 🔜 Roadmap & Future Features

- [ ] **Persistent Storage:** Implement a database for session and game state persistence.
- [ ] **User Authentication:** Add secure user account management.
- [ ] **Multi-Session Support:** Enhance concurrent game session handling.
- [ ] **Enhanced AI Prompt Engineering:** Refine narrative prompts for richer storytelling.
- [ ] **Combat & Quest Systems:** Develop battle mechanics and quest tracking.
- [ ] **Character Progression:** Introduce leveling and skill management systems.
- [ ] **Improved Error Handling:** More robust error management for API and streaming responses.
- [ ] **Real-Time Analytics Dashboard (Cool Feature):** Monitor live game metrics and user interactions to help tweak adventure dynamics.

## 🤝 Contributing

1. Fork the repository.
2. Create a feature branch:
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. Commit your changes:
   ```bash
   git commit -m 'Add some amazing feature'
   ```
4. Push to the branch:
   ```bash
   git push origin feature/amazing-feature
   ```
5. Open a Pull Request.

## 🐛 Known Issues

- **Session Management:** Currently uses in-memory storage; consider moving to a persistent store for production.
- **Error Handling:** Limited error management for timeouts and unexpected API responses.
- **Scalability:** Future enhancements are needed for high concurrency and robust multi-session support.

## 🔗 Frontend Integration

This backend is designed to work seamlessly with our React-based frontend:
- **Frontend Repository:** [Interactive Adventure Frontend](https://github.com/yourusername/interactive-adventure-frontend)
- **Typical Setup:**
   - **Backend:** [http://localhost:8080](http://localhost:8080)
   - **Ollama Service:** [http://localhost:11434](http://localhost:11434)
   - **Frontend:** [http://localhost:5173](http://localhost:5173)

---

Happy adventuring!
```