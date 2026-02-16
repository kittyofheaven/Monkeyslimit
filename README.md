# MonkeysLimit

A comprehensive Android application for personal finance management, expense tracking, budget planning, and intelligent bill splitting. MonkeysLimit empowers users to maintain financial control through intuitive tools and smart automation.

## Overview

MonkeysLimit is designed for individuals and groups who want to simplify their financial lives. The app combines traditional expense tracking with innovative features like AI-powered receipt scanning and collaborative bill splitting, all wrapped in a modern Material 3 interface.

## Key Features

### Financial Dashboard
Centralized view of your financial health with real-time balance tracking, income vs. expense analytics, and interactive charts showing spending patterns across different categories.

### Smart Split (Receipt Scanner)
Advanced OCR-powered receipt scanner that automatically extracts line items and prices from physical receipts. Assign items to specific members, split bills evenly or proportionally, and automatically calculate individual shares including taxes, discounts, and service charges.

### Budget Management
Create flexible budgets with customizable time periods and category-specific limits. Visual progress indicators and notifications help you stay on track with your financial goals.

### Transaction Management
Comprehensive transaction logging system with categorization, search, and filtering capabilities. Track every financial activity with detailed records and visual summaries.

### Analytics & Insights
Visual spending analytics with interactive charts powered by Vico. Track trends, identify spending patterns, and make data-driven financial decisions.

### Secure Authentication
Firebase Authentication with Google Sign-In support ensures secure access and seamless data synchronization across devices. Complete profile management with customizable preferences.

### Smart Notifications
WorkManager-powered background sync and intelligent notifications for budget alerts, payment reminders, and financial milestones.

## Technical Architecture

### Platform
- **Target SDK:** Android 36 (API 36)
- **Minimum SDK:** Android 10 (API 29)
- **Language:** Kotlin with JVM 21
- **Build System:** Gradle with Kotlin DSL

### Architecture Pattern
Clean Architecture with MVVM (Model-View-ViewModel) pattern:
- **Data Layer:** Repository pattern, Room database, Firebase Firestore
- **Domain Layer:** ViewModels for business logic and state management  
- **Presentation Layer:** Jetpack Compose with Material 3

### Core Technologies

**UI Framework**
- Jetpack Compose (BOM 2024.09.00)
- Material 3 with extended icon set
- Compose Navigation for routing
- Coil for image loading

**Data & Persistence**
- Room 2.8.3 for local SQLite storage
- Firebase Firestore for cloud sync
- Firebase Authentication for user management

**Camera & ML**
- CameraX 1.4.1 for camera functionality
- ML Kit for OCR text recognition
- Compose Cropper for image manipulation

**Networking**
- Retrofit 2.9.0 with Gson converter
- OkHttp 4.11.0 with logging interceptor

**Charts & Visualization**
- Vico 2.3.6 for Material 3 charts

**Background Processing**
- WorkManager 2.11.1 for reliable background tasks
- Coroutines for asynchronous operations

**Utilities**
- Accompanist Permissions for runtime permission handling
- Core SplashScreen API for app launch experience

## Project Structure

```
app/src/main/java/com/menac1ngmonkeys/monkeyslimit/
├── data/
│   ├── local/          # Room database, DAOs, and entities
│   ├── remote/         # Firebase API services
│   ├── repository/     # Repository implementations
│   └── worker/         # WorkManager background tasks
├── ui/
│   ├── analytics/      # Spending charts and insights
│   ├── auth/           # Login, signup, profile completion
│   ├── budget/         # Budget creation and tracking
│   ├── components/     # Reusable UI components
│   ├── dashboard/      # Main overview screen
│   ├── navigation/     # Navigation setup and components
│   ├── profile/        # User profile management
│   ├── settings/       # App settings
│   ├── smartsplit/     # Receipt scanner and bill splitting
│   ├── splash/         # Splash screen
│   ├── state/          # UI state definitions
│   ├── theme/          # Material 3 theme configuration
│   └── transaction/    # Transaction history and management
├── viewmodel/          # ViewModels for each feature
├── utils/              # Utility functions and extensions
├── MainActivity.kt     # Main activity entry point
└── MonkeyslimitApplication.kt  # Application class

```

## Getting Started

### Prerequisites
- **Android Studio:** Ladybug or newer
- **JDK:** Version 21
- **Android Device/Emulator:** API 29 or higher
- **Firebase Project:** For authentication and cloud sync

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/monkeyslimit.git
   cd monkeyslimit
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app with package name: `com.menac1ngmonkeys.monkeyslimit`
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Firebase Authentication and Firestore in your Firebase project

3. **Configure signing (for release builds)**
   - Create a `local.properties` file in the project root
   - Add your keystore configuration:
     ```properties
     RELEASE_STORE_FILE=/path/to/your/keystore.jks
     RELEASE_STORE_PASSWORD=your_store_password
     RELEASE_KEY_ALIAS=your_key_alias
     RELEASE_KEY_PASSWORD=your_key_password
     ```

4. **Build and run**
   - Open the project in Android Studio
   - Sync Gradle files
   - Run the app on your device or emulator

### First Launch

On first launch, the app will:
1. Request necessary permissions (Camera, Notifications)
2. Initialize local database with sample categories
3. Present the authentication screen for login or signup

## Permissions

The app requires the following permissions:
- **CAMERA:** For receipt scanning functionality
- **POST_NOTIFICATIONS:** For budget alerts and reminders
- **RECEIVE_BOOT_COMPLETED:** For persistent background sync
- **REQUEST_IGNORE_BATTERY_OPTIMIZATIONS:** For reliable WorkManager tasks

## Development

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Keep functions small and focused
- Document complex logic with comments

### Testing
- Unit tests: JUnit 4
- Instrumented tests: AndroidX Test with Espresso
- UI tests: Compose UI Testing

### Database Seeding
The app includes seeders for development data:
- Categories seeder for expense/income types
- Budgets seeder for sample budgets
- See `data/local/seeders/` for implementation

## License

Developed by **Menac1ng Monkeys** 🐒

---

**Version:** 1.0  
**Package:** com.menac1ngmonkeys.monkeyslimit
