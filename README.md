# Life Problem Solver

A modern Android application built with Jetpack Compose that helps users manage and solve life problems with AI-powered suggestions.

## Features

### Core Functionality
- **Problem Management**: Add, edit, and delete life problems
- **AI-Powered Solutions**: Generate intelligent solutions using AI
- **Problem Categories**: Organize problems by categories (Work, Personal, Health, etc.)
- **Priority Levels**: Set priority levels (Low, Medium, High, Urgent)
- **Status Tracking**: Mark problems as resolved or unresolved
- **History View**: View all problems with filtering options

### Modern UI/UX
- **Material Design 3**: Beautiful, modern interface
- **Jetpack Compose**: Declarative UI with reactive programming
- **Dark/Light Theme**: Automatic theme switching
- **Responsive Design**: Works on all screen sizes
- **Smooth Animations**: Fluid transitions and interactions

### Technical Features
- **Room Database**: Local data persistence
- **Kotlin Flow**: Reactive data streams
- **MVVM Architecture**: Clean separation of concerns
- **Hilt Dependency Injection**: Modern dependency management
- **Unit Testing**: Comprehensive test coverage
- **Coroutines**: Asynchronous programming

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- Kotlin 1.9.10+
- Java 17+

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device/emulator

## Architecture

The app follows Clean Architecture with MVVM pattern:
- **UI Layer**: Jetpack Compose + ViewModels
- **Domain Layer**: Repository pattern
- **Data Layer**: Room Database + AI Service

## Testing

Run tests with:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## License

MIT License 