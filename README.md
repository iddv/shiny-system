# Interactive Adventure Chat Frontend

A React-based frontend for an AI-powered text adventure game. This project provides a modern, interactive interface for text-based adventure gaming with real-time AI responses.

## 🌟 Features

- Modern React-based chat interface
- Real-time message updates
- Character stats tracking (health, gold, inventory)
- Theme selection
- Quick action buttons
- Dark mode styling
- Responsive design

## 🔧 Tech Stack

- React 18
- TypeScript
- Vite
- Tailwind CSS
- Lucide React (for icons)
- Axios (for API calls)

## 🚀 Getting Started

### Prerequisites

- Node.js (LTS version recommended)
- npm or yarn
- A compatible backend service running (separate repository)

### Installation

1. Clone the repository
```bash
git clone https://github.com/iddv/chat-app.git
cd chat-app
```

2. Install dependencies
```bash
npm install
# or
yarn install
```

3. Start the development server
```bash
npm run dev
# or
yarn dev
```

The application will be available at `http://localhost:5173`

## 🔌 Backend Integration

This frontend is designed to work with a Kotlin-based backend that uses Ollama for AI text generation. You'll need to have the backend service running locally or configure the API endpoint to point to your backend service.

The backend repository can be found at: [Backend Repository URL]

## 📁 Project Structure

```
chat-app/
├── src/
│   ├── App.tsx           # Main application component
│   ├── services/
│   │   └── api.ts        # API service layer
│   ├── assets/
│   ├── index.css
│   └── main.tsx
├── package.json
└── vite.config.ts
```

## ⚙️ Configuration

The application is configured to connect to a local backend by default. If you need to modify the backend URL or other settings, update the `api.ts` file in the services directory.

## 🛠️ Development

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build locally

### Making Changes

1. Create a new branch for your feature
2. Make your changes
3. Submit a pull request

## 🔜 Roadmap

- [ ] Add authentication
- [ ] Implement save/load game functionality
- [ ] Add sound effects and background music
- [ ] Enhance character customization
- [ ] Add multiplayer support
- [ ] Implement inventory management system

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.