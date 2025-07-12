# LifeFix Android App

An AI-powered Android application built with modern Android development practices to help users solve life problems using OpenAI's GPT model.

## Features

- 🤖 **AI-Powered Problem Solving**: Get intelligent solutions to life problems using OpenAI's GPT model
- 📱 **Modern UI**: Built with Jetpack Compose and Material 3 design
- 💾 **Local Storage**: Room database for offline problem management
- 🔄 **Real-time Updates**: Live data updates with Kotlin Coroutines
- 🏗️ **Clean Architecture**: MVVM pattern with Repository pattern
- 🧪 **Comprehensive Testing**: Unit tests for ViewModels and Repository
- 🎯 **Dependency Injection**: Hilt for clean dependency management

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room with Kotlin Coroutines
- **Dependency Injection**: Hilt
- **Networking**: Retrofit with OkHttp
- **AI Integration**: OpenAI GPT API
- **Testing**: JUnit, MockK, Turbine

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog or later
- Android SDK 34
- Java 17
- OpenAI API key

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/gunainvestor/GenAI_life-problem-solver.git
   cd GenAI_life-problem-solver
   ```

2. **Configure API Key**
   
   **Option 1: Using gradle.properties (Recommended)**
   
   Copy the template file and add your API key:
   ```bash
   cp gradle.properties.template gradle.properties
   ```
   
   Edit `gradle.properties` and replace `your_openai_api_key_here` with your actual OpenAI API key:
   ```properties
   OPENAI_API_KEY=sk-your-actual-api-key-here
   ```
   
   **Option 2: Using Environment Variables**
   
   Set the environment variable:
   ```bash
   export OPENAI_API_KEY="sk-your-actual-api-key-here"
   ```

3. **Build and Run**
   ```bash
   ./gradlew build
   ```
   
   Open the project in Android Studio and run on your device or emulator.

### Security Notes

- ⚠️ **Never commit your API key to version control**
- The `gradle.properties` file is already added to `.gitignore`
- API keys are loaded securely at runtime
- The app will show a clear error if the API key is missing

## Project Structure

```
app/src/main/java/com/lifeproblemsolver/app/
├── data/
│   ├── dao/           # Room DAOs
│   ├── database/      # Room database configuration
│   ├── model/         # Data models and entities
│   ├── remote/        # API services and network models
│   └── repository/    # Repository implementations
├── di/                # Hilt dependency injection modules
├── ui/
│   ├── components/    # Reusable UI components
│   ├── navigation/    # Navigation configuration
│   ├── screens/       # Screen composables
│   ├── theme/         # App theme and styling
│   └── viewmodel/     # ViewModels
└── MainActivity.kt    # Main activity
```

## Key Features

### Problem Management
- Add new problems with title, description, category, and priority
- View all problems in a clean, organized list
- Filter problems by category, priority, or status
- Mark problems as resolved

### AI Integration
- Generate AI-powered solutions for any problem
- Solutions are generated using OpenAI's GPT model
- Real-time solution generation with loading states
- Error handling for API failures

### User Experience
- Material 3 design with adaptive theming
- Smooth navigation between screens
- Loading states and error handling
- Responsive design for different screen sizes

## Testing

Run the tests using:
```bash
./gradlew test
```

The project includes:
- Unit tests for ViewModels
- Repository tests with Room database
- Coroutine testing with Turbine
- MockK for mocking dependencies

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

If you encounter any issues:
1. Check that your API key is correctly configured
2. Ensure you have the latest Android Studio version
3. Verify your internet connection for API calls
4. Check the logs for detailed error messages

## Acknowledgments

- OpenAI for providing the GPT API
- Google for Jetpack Compose and Android development tools
- The Android developer community for best practices and libraries 