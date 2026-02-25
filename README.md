<p align="center">
  <h1 align="center">MonkeysLimit</h1>
  <p align="center">
    <strong>Your Personal Finance Companion — Track, Budget, Split & Analyze.</strong>
  </p>
  <p align="center">
    <img src="https://img.shields.io/badge/Version-1.2.3-blue?style=flat-square" alt="Version">
    <img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square" alt="Platform">
    <img src="https://img.shields.io/badge/Min%20SDK-29%20(Android%2010)-orange?style=flat-square" alt="Min SDK">
    <img src="https://img.shields.io/badge/Target%20SDK-36-red?style=flat-square" alt="Target SDK">
    <img src="https://img.shields.io/badge/Kotlin-2.2.21-purple?style=flat-square" alt="Kotlin">
  </p>
</p>

---

## 📖 About

**MonkeysLimit** is a modern Android application designed for personal finance management. It lets you track daily expenses, create smart budgets, split bills with friends using AI-powered receipt scanning, and visualize your spending patterns — all within a beautiful Material 3 interface.

Whether you're managing your household budget or splitting dinner bills with friends, MonkeysLimit provides intelligent tools to keep your finances under control.

---

## ✨ Features

### 🏠 Financial Dashboard
A centralized hub showing your complete financial overview at a glance:
- **Real-time balance** tracking with income vs. expense breakdown
- **Interactive charts** visualizing spending patterns across categories
- **Recent transactions** feed for quick review
- **Monthly summary** with trend indicators

### 📸 Smart Split (AI-Powered Bill Splitting)
The flagship feature — split bills effortlessly using your camera:
- **OCR Receipt Scanning** — Point your camera at any receipt and let ML Kit automatically extract line items and prices
- **Gallery Import** — Upload receipt images from your gallery
- **Manual Entry** — Add items manually when a receipt isn't available
- **Member Management** — Add participants and assign specific items to each person
- **Flexible Splitting** — Split by item, evenly, or proportionally (with tax, discounts & service charges calculated automatically)
- **Split History** — View and revisit past splits with full detail breakdowns
- **Result Sharing** — Share split results with participants

### 💰 Budget Management
Stay on top of your financial goals with powerful budgeting tools:
- **Custom Budgets** — Create budgets with flexible time periods (daily, weekly, monthly)
- **Category-Specific Limits** — Set spending caps per category (food, transport, entertainment, etc.)
- **Visual Progress Bars** — Track how much you've spent vs. your limit in real time
- **Budget Recommendations** — Get AI-generated budget plans based on your spending history
- **Alert Notifications** — Receive push notifications when you're approaching or exceeding limits

### 📊 Analytics & Insights
Make data-driven decisions with comprehensive spending analytics:
- **Interactive Charts** — Powered by Vico charting library with Material 3 styling
- **Spending Trends** — Track how your spending changes over time
- **Category Breakdown** — See exactly where your money goes
- **Period Comparison** — Compare spending across different time ranges

### 💳 Transaction Management
Full control over every financial activity:
- **Three Input Methods:**
  - 📷 **Camera Scan** — Scan receipts for automatic transaction entry
  - 🖼️ **Gallery Upload** — Import receipt images from your gallery
  - ✏️ **Manual Entry** — Enter transactions by hand
- **Smart Categorization** — Transactions are organized by customizable categories
- **Search & Filter** — Find any transaction quickly with powerful search
- **Detailed Records** — View complete transaction details with receipt images
- **Edit & Delete** — Full CRUD operations on all transactions

### 🔐 Authentication & Profile
Secure and personalized experience:
- **Firebase Authentication** — Email/password and Google Sign-In
- **Profile Completion Flow** — Guided onboarding for new users
- **Profile Management** — Edit display name, profile photo, and personal details
- **Image Preview** — Full-screen image viewer for profile and receipt photos
- **Cloud Sync** — Data synchronized across devices via Firebase Firestore

### 🔔 Smart Notifications
Stay informed without lifting a finger:
- **Daily Reminders** — Configurable daily financial check-in reminders
- **Budget Alerts** — Notifications when approaching budget limits
- **Boot Persistence** — Notifications survive device restarts via BootReceiver
- **Background Sync** — Powered by WorkManager for reliable delivery

### ⚙️ Settings
Customize the app to your preferences:
- **Notification Preferences** — Toggle different notification types
- **App Configuration** — Personalize your experience

---

## 🏗️ Architecture

MonkeysLimit follows **Clean Architecture** with the **MVVM** (Model-View-ViewModel) pattern, organized into three distinct layers:

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│         Jetpack Compose UI  +  Material 3               │
│    (Screens, Components, Navigation, Theme)             │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                          │
│              ViewModels (20 ViewModels)                  │
│        Business Logic  +  State Management              │
├─────────────────────────────────────────────────────────┤
│                     Data Layer                           │
│   Room DB (9 Entities, 9 DAOs)  │  Firebase Firestore   │
│   9 Repositories  │  Retrofit API  │  WorkManager       │
└─────────────────────────────────────────────────────────┘
```

### Data Layer
| Component | Count | Purpose |
|-----------|-------|---------|
| Entities | 10 | `User`, `Transactions`, `Categories`, `Budgets`, `SmartSplits`, `Members`, `Items`, `MemberItems`, `Notifications`, `TransactionType` |
| DAOs | 9 | Data Access Objects for all Room database operations |
| Repositories | 9 | `Users`, `Transactions`, `Categories`, `Budgets`, `SmartSplits`, `Members`, `Items`, `MemberItems`, `Notifications` |
| Workers | 3 | `DailyReminderWorker`, `BootReceiver`, `NotificationHelper` |
| Seeders | 11 | Development data seeders + `SeedCoordinator` + `DatabaseResetter` |
| Remote | 3 | `ApiConfig`, `ApiService`, API response models |

### Presentation Layer — Screens

| Module | Screens |
|--------|---------|
| **Dashboard** | Financial overview, balance, recent transactions |
| **Budget** | Budget list, add budget, budget recommendations |
| **Transaction** | Transaction hub, scan, gallery, manual entry, review, detail |
| **Smart Split** | Split hub, camera scan, gallery, manual, review, member select, results, history, detail |
| **Analytics** | Spending charts, trends, category breakdown |
| **Auth** | Login, sign-up, profile completion |
| **Profile** | Profile view, edit profile, image preview |
| **Settings** | App preferences and configuration |
| **Splash** | Animated app launch screen |

---

## 🛠️ Tech Stack

### Core Platform
| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.2.21 | Primary language |
| Android Gradle Plugin | 8.13.0 | Build tooling |
| JVM Toolchain | 21 | Java compilation target |
| Compile SDK | 36 | Latest Android API |
| Min SDK | 29 (Android 10) | Minimum supported version |

### UI & Design
| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose BOM | 2024.09.00 | Declarative UI framework |
| Material 3 | 1.4.0 | Design system |
| Material Icons Extended | 1.6.8 | Extended icon set |
| Compose Navigation | 2.9.5 | Screen routing & navigation |
| Coil Compose | 2.6.0 | Async image loading |
| Core SplashScreen | 1.0.1 | Native splash screen API |
| Vico | 2.3.6 | Material 3 charting library |

### Data & Persistence
| Library | Version | Purpose |
|---------|---------|---------|
| Room | 2.8.3 | Local SQLite database with type-safe queries |
| Firebase BOM | 33.7.0 | Firebase platform management |
| Firebase Auth | (BOM-managed) | User authentication |
| Firebase Firestore | (BOM-managed) | Cloud database & sync |
| Retrofit | 2.9.0 | REST API client |
| OkHttp | 4.11.0 | HTTP client with logging |
| Gson | (via Retrofit) | JSON serialization |

### Camera & Machine Learning
| Library | Version | Purpose |
|---------|---------|---------|
| CameraX | 1.4.1 | Camera integration (core, camera2, lifecycle, view) |
| ML Kit Text Recognition | 19.0.1 | OCR for receipt scanning |
| Compose Cropper | 0.4.0 | Image cropping, zoom, pan & rotate |

### Background & Utilities
| Library | Version | Purpose |
|---------|---------|---------|
| WorkManager | 2.11.1 | Reliable background task scheduling |
| Coroutines | (via Lifecycle KTX) | Asynchronous programming |
| Accompanist Permissions | 0.36.0 | Runtime permission handling |
| Google Credentials | 1.6.0-rc01 | Credential Manager for sign-in |
| Guava | 31.1 | Utility library for ListenableFuture |

### Testing
| Library | Version | Purpose |
|---------|---------|---------|
| JUnit | 4.13.2 | Unit testing framework |
| AndroidX JUnit | 1.1.5 | Android instrumented test runner |
| Espresso | 3.5.1 | UI interaction testing |
| Compose UI Test | (BOM-managed) | Compose-specific UI testing |

---

## 📁 Project Structure

```
app/src/main/java/com/menac1ngmonkeys/monkeyslimit/
│
├── 📂 data/                          # Data Layer
│   ├── local/
│   │   ├── AppDatabase.kt            # Room database definition
│   │   ├── dao/                      # 9 Data Access Objects
│   │   ├── entity/                   # 10 Database entities
│   │   └── seeders/                  # 11 Development data seeders
│   ├── remote/
│   │   ├── ApiConfig.kt              # Retrofit configuration
│   │   ├── ApiService.kt             # API endpoint definitions
│   │   └── response/                 # API response models
│   ├── repository/                   # 9 Repository implementations
│   ├── worker/
│   │   ├── BootReceiver.kt           # Restart notifications on boot
│   │   ├── DailyReminderWorker.kt    # Scheduled daily reminders
│   │   └── NotificationHelper.kt     # Notification channel & builder
│   └── AppContainer.kt               # Dependency injection container
│
├── 📂 ui/                            # Presentation Layer
│   ├── analytics/                    # Charts & spending insights
│   ├── auth/                         # Login, signup, profile completion
│   ├── budget/                       # Budget CRUD & recommendations
│   ├── components/                   # 14 Reusable UI components
│   ├── dashboard/                    # Main overview screen
│   ├── navigation/                   # NavGraph, bottom bar, top bar, FAB
│   ├── profile/                      # User profile management
│   ├── settings/                     # App preferences
│   ├── smartsplit/                   # Receipt scanner & bill splitting
│   ├── splash/                       # App launch screen
│   ├── state/                        # 16 UI state definitions
│   ├── theme/                        # Material 3 theme (color, type, shape)
│   ├── transaction/                  # Transaction recording & history
│   └── AppViewModelProvider.kt       # ViewModel factory
│
├── 📂 viewmodel/                     # 20 ViewModels for all features
├── 📂 utils/                         # Utility functions
│   ├── CurrencyFormatter.kt          # IDR currency formatting
│   ├── CurrencyVisualTransformation.kt
│   ├── DateConverters.kt             # Room type converters
│   ├── DateTimeConverters.kt
│   ├── BatteryUtils.kt               # Battery optimization checks
│   ├── NavUtils.kt                   # Navigation helpers
│   └── compactNumber.kt              # Large number formatting
│
├── MainActivity.kt                   # Single Activity entry point
└── MonkeyslimitApplication.kt        # Application class initialization

app/src/androidTest/                   # Instrumented UI Tests
└── ui/
    ├── analytics/                     # Analytics screen tests
    ├── auth/                          # Login & signup tests
    ├── budget/                        # Budget screen tests (4 files)
    ├── dashboard/                     # Dashboard tests
    ├── profile/                       # Profile screen tests (3 files)
    ├── settings/                      # Settings tests
    ├── smartsplit/                    # Smart split tests (6 files)
    ├── splash/                        # Splash screen tests
    └── transaction/                   # Transaction tests (4 files)
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Details |
|-------------|---------|
| **Android Studio** | Ladybug (2024.2) or newer |
| **JDK** | Version 21+ |
| **Device / Emulator** | Android 10 (API 29) or higher |
| **Firebase Project** | For authentication & cloud sync |

### Step-by-Step Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/kittyofheaven/Monkeyslimit.git
cd Monkeyslimit
```

#### 2. Set Up Firebase
1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project (or use an existing one).
2. Click **Add App** → **Android** and enter the package name:
   ```
   com.menac1ngmonkeys.monkeyslimit
   ```
3. Download the generated `google-services.json` file.
4. Place `google-services.json` in the `app/` directory of the project.
5. In the Firebase Console, enable the following services:
   - **Authentication** → Enable Email/Password and Google sign-in providers
   - **Firestore Database** → Create a database in production or test mode

#### 3. Configure Signing (Release Builds Only)
If you want to create a signed release APK, add the following to `local.properties` in the project root:
```properties
RELEASE_STORE_FILE=/path/to/your/keystore.jks
RELEASE_STORE_PASSWORD=your_store_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password
```
> **Note:** This step is optional for debug builds. The app runs without signing configuration in debug mode.

#### 4. Build & Run
1. Open the project in **Android Studio**.
2. Wait for Gradle to sync all dependencies (this may take a few minutes on first run).
3. Select your target device or emulator from the toolbar.
4. Click **▶ Run** or press `Shift + F10`.

### First Launch Behavior
When the app launches for the first time, it will:
1. Display an animated splash screen
2. Request necessary permissions (**Camera** for receipt scanning, **Notifications** for alerts)
3. Initialize the local Room database with default categories
4. Present the **Login screen** — sign in with your Google account or create a new account

---

## 📱 Permissions

| Permission | Purpose | Required? |
|------------|---------|-----------|
| `CAMERA` | Capture receipt photos for OCR scanning | Yes (for Smart Split & Scan Transaction) |
| `POST_NOTIFICATIONS` | Budget alerts and daily reminders | Recommended |
| `RECEIVE_BOOT_COMPLETED` | Restart notification scheduler after device reboot | Automatic |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Ensure WorkManager tasks run reliably | Optional |

---

## 🧪 Testing

MonkeysLimit includes a comprehensive UI test suite covering **9 screen categories** with instrumented tests:

```bash
# Run all instrumented tests
./gradlew connectedAndroidTest

# Run tests for a specific screen
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.menac1ngmonkeys.monkeyslimit.ui.auth.LoginScreenTest
```

| Test Category | Files | Coverage |
|---------------|-------|----------|
| Auth | 3 | Login, Sign Up, Profile Completion |
| Dashboard | 1 | Main overview screen |
| Budget | 4 | Budget list, add, detail, recommendations |
| Transaction | 4 | Transaction hub, scan, manual, review |
| Smart Split | 6 | Split hub, review, members, results, history, detail |
| Analytics | 1 | Charts and insights |
| Profile | 3 | Profile view, edit, image preview |
| Settings | 1 | Preferences |
| Splash | 1 | Launch screen |

### Testing Tools
- **JUnit 4** — Unit test framework
- **Espresso** — Android UI interaction testing
- **Compose UI Test** — Compose-specific test rules and matchers (`createComposeRule`)

---

## 🧑‍💻 Development

### Code Style Guidelines
- Follow official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful, descriptive variable and function names
- Keep functions focused and single-purpose
- Document complex business logic with inline comments
- Use `sealed class` for navigation routes and UI states

### Database Seeding (Development)
The app includes a complete set of seeders in `data/local/seeders/` for populating the database with sample data during development:

| Seeder | Purpose |
|--------|---------|
| `CategoriesSeeder` | Default income/expense categories |
| `TransactionsSeeder` | Sample transactions |
| `BudgetsSeeder` | Sample budgets |
| `MembersSeeder` | Sample split members |
| `ItemsSeeder` | Sample receipt items |
| `MemberItemsSeeder` | Sample member-item assignments |
| `SmartSplitsSeeder` | Sample split sessions |
| `NotificationsSeeder` | Sample notifications |
| `SeedCoordinator` | Orchestrates seeding order |
| `DatabaseResetter` | Wipes and re-seeds the database |

---

## 📄 License

Developed with ❤️ by **Menac1ng Monkeys** 🐒

---

<p align="center">
  <strong>Version:</strong> 1.2.3 &nbsp;|&nbsp;
  <strong>Build:</strong> 10 &nbsp;|&nbsp;
  <strong>Package:</strong> <code>com.menac1ngmonkeys.monkeyslimit</code>
</p>
