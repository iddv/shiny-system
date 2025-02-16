# Interactive Text Adventure Engine

A modern text adventure game engine built with React and Kotlin, leveraging AI for dynamic storytelling.

## 🌟 Features

### Current Implementation
- Real-time AI-powered narrative generation using Ollama (deepseek-r1:14b model)
- Dynamic game state management
- Character stats tracking (health, gold, inventory)
- Theme-based adventure generation
- Interactive chat interface with styled messages
- Quick action buttons for common commands
- Session persistence

### Technical Stack
- Frontend: React with TypeScript
- Backend: Kotlin with Ktor
- AI: Ollama with deepseek-r1:14b model
- State Management: Custom implementation with session tracking
- API Communication: Axios

## 🚀 Getting Started

### Prerequisites
- Node.js and npm/yarn
- Kotlin development environment
- Ollama installed with deepseek-r1:14b model
- JDK 8 or higher

### Installation

1. Clone the repository
```bash
git clone [your-repo-url]
cd [your-repo-name]
```

2. Install frontend dependencies
```bash
cd frontend
npm install
# or
yarn install
```

3. Start the Ktor backend server
```bash
./gradlew run
```

4. Start Ollama with the required model
```bash
ollama run deepseek-r1:14b
```

5. Start the frontend development server
```bash
npm run dev
# or
yarn dev
```

## 🔧 Work in Progress

### Immediate Todos
1. Fix API service integration
    - Correct import paths
    - Implement proper error handling
    - Add request/response typing

2. Backend Enhancements
    - Implement proper session management
    - Add character state persistence
    - Enhance event parsing from AI responses
    - Add proper error responses

3. Frontend Improvements
    - Add loading states
    - Implement proper error handling
    - Add character state updates
    - Enhance UI/UX for game events

### Future Features
- Save/Load game functionality
- Multiple character classes
- Quest system
- Inventory management system
- Combat mechanics
- Sound effects and background music
- Custom theme creation
- Multiple AI model support

## 🏗️ Project Structure

```
project/
├── frontend/
│   ├── src/
│   │   ├── App.tsx
│   │   ├── services/
│   │   │   └── api.ts
│   │   └── assets/
│   └── package.json
└── server/
    └── src/
        └── main/
            └── kotlin/
                └── org/
                    └── socialspaces/
                        └── Application.kt
```

## 🤝 Contributing

The project is currently in development. Contributions, ideas, and feedback are welcome!