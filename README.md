# ☀️ Surya Shakti Solar Monitor

**Surya Shakti** is a futuristic, AI-powered solar energy monitoring application built for modern sustainable living. It provides intelligent insights, real-time weather-aware tracking, and a high-tech dashboard to help users maximize their energy independence.

---

## ✨ Key Features

### 🌌 Futuristic UI/UX
- **Glassmorphism Design**: High-contrast, sleek interface with vertical gradients and translucent elements.
- **Dynamic Autonomy Ring**: A glowing visual representation of your energy independence score.
- **Modern Navigation**: Seamless transitions between Home, Analytics, and Profile sections.

### 🤖 AI-Powered Intelligence (Gemini AI)
- **Context-Aware Advisor**: Get hyper-dynamic energy-saving tips that change based on your location, usage history, and current weather.
- **Time-Aware Logic**: The AI distinguishes between day and night to provide relevant advice (e.g., battery prep at night vs. load-shifting during peak sun).
- **Unique Insights**: Every refresh brings new strategies using a randomized entropy engine.

### 🌤️ Real-Time Environmental Sync
- **Auto-Weather Detection**: Automatically fetches weather conditions for your specific plant location.
- **Night Safeguard**: Intelligent logic ensures accurate reporting (no "Sunny" reports at night!).
- **Location Lookup**: Dynamic city suggestions for adding new solar plants using Google/Gemini APIs.

### 📊 Advanced Analytics
- **Visual Breakdown**: Interactive Bar Charts for weekly trends and Pie Charts for your Energy Mix (Exported vs. Self-Used).
- **Economic Tracking**: Configure grid unit rates and export credits to see your real-world savings in ₹ (INR).
- **Lifetime Metrics**: Keep track of cumulative generation and consumption across all your solar plants.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Iguana (or newer)
- Kotlin 1.9+
- A Google Gemini API Key

### Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/SuryaShakti-SolarMonitor.git
   ```
2. **Set up API Key**:
   - Create a `local.properties` file in the root directory (if not present).
   - Add your Gemini API key:
     ```properties
     GEMINI_API_KEY=your_actual_key_here
     ```
3. **Build and Run**:
   - Sync Gradle and run the project on an emulator or physical device.

---

## 🛠️ Tech Stack
- **UI**: Jetpack Compose (Modern Declarative UI)
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Dagger Hilt
- **Database**: Room (Local persistence for energy logs)
- **Networking**: Retrofit & OkHttp
- **AI Engine**: Google Gemini 1.5 Flash API
- **Charts**: MPAndroidChart
- **Navigation**: Compose Navigation

---

## 📸 Screenshots
*(Coming Soon - Add your screenshots here!)*

---

## 📜 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Empowering your home with the strength of the Sun. ⚡**
