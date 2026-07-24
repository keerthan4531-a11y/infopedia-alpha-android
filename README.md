<div align="center">

# 🤖 Infopedia Alpha Android

<p align="center">
  <b>Next-Generation AI-Powered Wikipedia Client for Android</b>
</p>

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=for-the-badge&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![AI Powered](https://img.shields.io/badge/AI-Inixa%20Alpha-FF6F00.svg?style=for-the-badge&logo=openai)](https://github.com/keerthan4531-a11y/infopedia-alpha-android)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)](LICENSE)

<br/>

</div>

---

## 🌟 Overview

**Infopedia Alpha** reimagines the Android Wikipedia experience by embedding **Inixa AI**—an intelligent, context-aware research assistant directly into the browsing workflow. Powered by **Retrieval-Augmented Generation (RAG)**, Infopedia reads verified Wikipedia articles in real-time to answer complex queries with **factual accuracy, live token streaming, and inline citations**.

---

## ✨ Key Features

### 🧠 1. Live Wikipedia RAG Integration
* Automatically extracts and injects real-time Wikipedia article context into AI queries.
* Ensures answers are rooted in verified Wikipedia content to eliminate hallucinations.

### ⚡ 2. Real-Time Token Streaming
* Interactive typing and rendering experience powered by custom `StreamingTextRenderer`.
* Smooth Markdown parsing with inline code formatting, blockquotes, and lists.

### 🔗 3. Smart Verified Citations
* Inline citation tags (`[1]`, `[2]`, `[3]`) mapped directly to referenced Wikipedia sources.
* Tap on citations to jump instantly to the referenced article summary or full view.

### 🎯 4. Smart Follow-up Questions
* Automatically generates 3 relevant follow-up questions at the end of every AI interaction to spark deeper exploration.

### 🎛️ 5. Multi-Model Support
* Dynamic model selector sheet (`ModelSelectorSheet`) supporting top AI engines (Gemini, DeepSeek, Claude, Llama).

### 🎨 6. Modern Jetpack Compose UI
* Clean Material 3 design built with custom `WikipediaTheme`.
* Seamless dark mode & light mode support with fluid Compose micro-animations.

---

## 🛠️ Technology Stack

| Category | Technologies |
|---|---|
| **Language** | Kotlin 1.9+ |
| **UI Framework** | Jetpack Compose, Material 3 Design |
| **Asynchrony & State** | Kotlin Coroutines, StateFlow, SharedFlow |
| **Networking** | OkHttp 4, Retrofit, Server-Sent Events (SSE) |
| **Architecture** | MVVM / Clean Architecture |
| **Build System** | Gradle (Kotlin DSL) |

---

## 📁 Project Structure

```text
app/src/main/java/org/wikipedia/
├── ai/                         # Inixa AI Module
│   ├── AiApiClient.kt          # Streaming API Client & RAG Prompt Builder
│   ├── AiChatMessage.kt        # Chat Message Data Schema
│   ├── AiModel.kt              # LLM Model Definitions
│   ├── ChatBubble.kt           # Custom Compose Chat Bubbles & Markdown
│   ├── InixaAlphaScreen.kt     # Main AI Chat Screen UI
│   ├── InixaAlphaViewModel.kt  # ViewModel & State Management
│   ├── ModelSelectorSheet.kt   # Bottom Sheet for Model Switching
│   ├── StreamingTextRenderer.kt# High-Performance Text Streaming Renderer
│   ├── WikipediaContextBanner.kt# Live Wikipedia Context Badge UI
│   └── WikipediaContextProvider.kt# Article Summary Extractor for RAG
├── compose/                    # Design System & Compose Theme
└── ...
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio** Hedgehog (2023.1.1) or newer
* **JDK 17**
* **Android SDK** API 24+ (Android 7.0+)

### Building from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/keerthan4531-a11y/infopedia-alpha-android.git
   cd infopedia-alpha-android
   ```

2. **Open in Android Studio:**
   * Select `File -> Open` and navigate to the cloned project folder.

3. **Build & Run:**
   * Sync Project with Gradle Files.
   * Connect an emulator or physical device.
   * Press **Run `app`** (`Shift + F10`).

---

## 🤝 Contributing

Contributions are welcome! If you'd like to improve Infopedia Alpha or introduce new AI features:

1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📜 License

This project is licensed under the Apache License 2.0. See the [COPYING](COPYING) file for details.

<div align="center">
  <sub>Built with ❤️ for knowledge seekers everywhere.</sub>
</div>
