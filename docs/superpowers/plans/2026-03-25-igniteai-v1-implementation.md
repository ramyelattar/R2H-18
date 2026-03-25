# IgniteAI V1.0 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a native Android couples intimacy app with two tiers (Spark free + Fire $29), featuring dual biometric consent, peer-to-peer couple sync, curated adaptive content, hybrid audio, haptics, and psychological arousal mechanics — all fully on-device with zero cloud.

**Architecture:** Modular single-app architecture using Kotlin + Jetpack Compose. Feature modules communicate through a shared Room database and an event bus. All data encrypted on-device with AES-256/SQLCipher. Couple sync via Bluetooth LE / WiFi Direct (no server). Payment via Stripe Payment Links (only network call).

**Tech Stack:** Kotlin, Jetpack Compose + Material 3, Room + SQLCipher, ExoPlayer, Android TTS, BiometricPrompt, Bluetooth LE, WiFi Direct, Health Connect, Stripe, Gradle KTS, Min SDK 28.

**Spec:** `docs/superpowers/specs/2026-03-25-igniteai-v1-design.md`

---

## File Structure

```
IgniteAI/
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/igniteai/app/
│   │   │   │   ├── IgniteAIApp.kt                    # Application class, DI setup
│   │   │   │   ├── MainActivity.kt                    # Single activity, Compose host
│   │   │   │   │
│   │   │   │   ├── core/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── IgniteDatabase.kt          # Room database definition
│   │   │   │   │   │   ├── Converters.kt              # Type converters for Room
│   │   │   │   │   │   └── DatabaseProvider.kt         # Encrypted DB factory (SQLCipher)
│   │   │   │   │   ├── security/
│   │   │   │   │   │   ├── EncryptionManager.kt       # AES-256 encrypt/decrypt via Keystore
│   │   │   │   │   │   ├── BiometricAuthManager.kt    # BiometricPrompt + PIN fallback
│   │   │   │   │   │   └── PanicWipeManager.kt        # Data erasure logic
│   │   │   │   │   ├── sync/
│   │   │   │   │   │   ├── BleManager.kt              # Bluetooth LE connection + messaging
│   │   │   │   │   │   ├── WifiDirectManager.kt       # WiFi Direct fallback
│   │   │   │   │   │   ├── SyncProtocol.kt            # Message types + serialization
│   │   │   │   │   │   └── ConnectionManager.kt       # BLE/WiFi failover state machine
│   │   │   │   │   └── preferences/
│   │   │   │   │       └── AppPreferences.kt          # DataStore preferences (encrypted)
│   │   │   │   │
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── PairingData.kt             # Pairing keys + state
│   │   │   │   │   │   ├── CoupleProfile.kt           # Couple entity
│   │   │   │   │   │   ├── Partner.kt                 # Partner entity
│   │   │   │   │   │   ├── ContentItem.kt             # Dare/scenario/text content
│   │   │   │   │   │   ├── SessionRecord.kt           # Session + consent log
│   │   │   │   │   │   ├── FantasyProfile.kt          # Fantasy questionnaire answers
│   │   │   │   │   │   ├── EngagementRecord.kt        # Content engagement tracking
│   │   │   │   │   │   ├── VaultItem.kt               # Forbidden vault entries
│   │   │   │   │   │   ├── ScenarioNode.kt            # Branching scenario tree node
│   │   │   │   │   │   └── LicenseKey.kt              # Payment license
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── PairingDao.kt              # Pairing state + keys
│   │   │   │   │   │   ├── ContentDao.kt              # Content CRUD + queries
│   │   │   │   │   │   ├── SessionDao.kt              # Session + consent queries
│   │   │   │   │   │   ├── EngagementDao.kt           # Engagement tracking queries
│   │   │   │   │   │   ├── FantasyDao.kt              # Fantasy profile queries
│   │   │   │   │   │   ├── VaultDao.kt                # Vault CRUD
│   │   │   │   │   │   ├── ScenarioDao.kt             # Scenario tree queries
│   │   │   │   │   │   └── LicenseDao.kt              # License queries
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── ContentRepository.kt       # Content selection + adaptive algorithm
│   │   │   │   │       ├── SessionRepository.kt       # Session management
│   │   │   │   │       ├── PairingRepository.kt       # Couple pairing state
│   │   │   │   │       ├── FantasyRepository.kt       # Fantasy profiling logic
│   │   │   │   │       ├── VaultRepository.kt         # Vault operations
│   │   │   │   │       ├── ScenarioRepository.kt      # Scenario tree navigation
│   │   │   │   │       └── LicenseRepository.kt       # Payment/license management
│   │   │   │   │
│   │   │   │   ├── feature/
│   │   │   │   │   ├── onboarding/
│   │   │   │   │   │   ├── OnboardingViewModel.kt
│   │   │   │   │   │   ├── WelcomeScreen.kt
│   │   │   │   │   │   ├── PartnerSetupScreen.kt
│   │   │   │   │   │   ├── BiometricSetupScreen.kt
│   │   │   │   │   │   ├── PinSetupScreen.kt
│   │   │   │   │   │   ├── PairingScreen.kt           # QR + invite code
│   │   │   │   │   │   └── FantasyQuestionnaireScreen.kt
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   │   ├── HomeScreen.kt              # Main hub, daily dare, streak
│   │   │   │   │   │   └── DailyDareCard.kt
│   │   │   │   │   ├── session/
│   │   │   │   │   │   ├── SessionViewModel.kt
│   │   │   │   │   │   ├── SessionScreen.kt           # Active session container
│   │   │   │   │   │   ├── ConsentGateScreen.kt       # Dual biometric before session
│   │   │   │   │   │   ├── SafewordOverlay.kt         # Floating stop button
│   │   │   │   │   │   ├── CheckInDialog.kt           # Halfway check-in
│   │   │   │   │   │   ├── CoolDownScreen.kt          # Post-session/safeword
│   │   │   │   │   │   └── DenyDelayOverlay.kt        # Deny & delay pause effect
│   │   │   │   │   ├── content/
│   │   │   │   │   │   ├── ContentViewModel.kt
│   │   │   │   │   │   ├── DareScreen.kt
│   │   │   │   │   │   ├── TextMessageScreen.kt
│   │   │   │   │   │   └── ContentFeedbackBar.kt      # Favorite/skip/block controls
│   │   │   │   │   ├── anticipation/
│   │   │   │   │   │   ├── AnticipationViewModel.kt
│   │   │   │   │   │   ├── TeaseSequenceScreen.kt
│   │   │   │   │   │   └── CountdownLockScreen.kt
│   │   │   │   │   ├── audio/
│   │   │   │   │   │   ├── AudioViewModel.kt
│   │   │   │   │   │   ├── AudioPlayerScreen.kt
│   │   │   │   │   │   ├── AudioEngine.kt             # ExoPlayer + TTS orchestration
│   │   │   │   │   │   ├── BinauralMixer.kt           # Stereo channel mixing
│   │   │   │   │   │   ├── BreathPacer.kt             # Tap-based breath sync
│   │   │   │   │   │   └── SoundscapeLayer.kt         # Ambient audio layering
│   │   │   │   │   ├── haptic/
│   │   │   │   │   │   ├── HapticEngine.kt            # Vibration pattern player
│   │   │   │   │   │   └── HapticPatterns.kt          # Predefined patterns library
│   │   │   │   │   ├── pavlovian/
│   │   │   │   │   │   ├── PavlovianManager.kt        # Conditioning trigger logic
│   │   │   │   │   │   └── SignatureAssets.kt          # Signature sound + haptic defs
│   │   │   │   │   ├── vault/
│   │   │   │   │   │   ├── VaultViewModel.kt
│   │   │   │   │   │   ├── VaultScreen.kt
│   │   │   │   │   │   ├── VaultUnlockScreen.kt       # Dual biometric gate
│   │   │   │   │   │   └── VaultEncryption.kt         # Separate encryption layer
│   │   │   │   │   ├── control/                        # Level 2: D/s Control Transfer
│   │   │   │   │   │   ├── ControlViewModel.kt
│   │   │   │   │   │   ├── ControllerScreen.kt
│   │   │   │   │   │   └── ReceiverScreen.kt
│   │   │   │   │   ├── heartrate/                      # Level 2: Heart Rate
│   │   │   │   │   │   ├── HeartRateViewModel.kt
│   │   │   │   │   │   ├── HeartRateScreen.kt
│   │   │   │   │   │   └── HeartRateGlow.kt           # Pulsing glow composable
│   │   │   │   │   ├── scenario/                       # Level 2: Branching Scenarios
│   │   │   │   │   │   ├── ScenarioViewModel.kt
│   │   │   │   │   │   ├── ScenarioScreen.kt
│   │   │   │   │   │   └── BranchChoiceCard.kt
│   │   │   │   │   ├── challenge/                      # Level 2: Couple Challenges
│   │   │   │   │   │   ├── ChallengeViewModel.kt
│   │   │   │   │   │   ├── ChallengeScreen.kt
│   │   │   │   │   │   └── TimerComponent.kt
│   │   │   │   │   ├── payment/
│   │   │   │   │   │   ├── PaymentViewModel.kt
│   │   │   │   │   │   └── PaymentScreen.kt
│   │   │   │   │   └── settings/
│   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │       └── SettingsScreen.kt
│   │   │   │   │
│   │   │   │   └── ui/
│   │   │   │       ├── theme/
│   │   │   │       │   ├── Color.kt                   # Fire/ember color palette
│   │   │   │       │   ├── Type.kt                    # Typography
│   │   │   │       │   ├── Theme.kt                   # IgniteAI Material 3 theme
│   │   │   │       │   └── Shape.kt                   # Rounded shapes
│   │   │   │       ├── navigation/
│   │   │   │       │   └── NavGraph.kt                # All routes + navigation
│   │   │   │       └── components/
│   │   │   │           ├── IgniteButton.kt            # Styled buttons (ember glow)
│   │   │   │           ├── IgniteCard.kt              # Content cards
│   │   │   │           ├── EmberParticles.kt          # Fire particle animation
│   │   │   │           ├── PulsingGlow.kt             # Pulsing glow effect
│   │   │   │           └── StreakCounter.kt           # Heat streak display
│   │   │   │
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   └── themes.xml
│   │   │       ├── raw/                               # Pre-recorded audio + signature sound
│   │   │       └── drawable/                          # Icons, decoy icons
│   │   │
│   │   ├── test/java/com/igniteai/app/               # Unit tests
│   │   │   ├── data/repository/
│   │   │   │   ├── ContentRepositoryTest.kt
│   │   │   │   ├── SessionRepositoryTest.kt
│   │   │   │   ├── FantasyRepositoryTest.kt
│   │   │   │   └── LicenseRepositoryTest.kt
│   │   │   ├── feature/
│   │   │   │   ├── content/ContentViewModelTest.kt
│   │   │   │   ├── session/SessionViewModelTest.kt
│   │   │   │   └── pavlovian/PavlovianManagerTest.kt
│   │   │   └── core/
│   │   │       ├── security/EncryptionManagerTest.kt
│   │   │       └── sync/SyncProtocolTest.kt
│   │   │
│   │   └── androidTest/java/com/igniteai/app/        # Instrumented tests
│   │       ├── core/database/IgniteDatabaseTest.kt
│   │       ├── core/security/BiometricAuthTest.kt
│   │       └── feature/onboarding/OnboardingFlowTest.kt
│   │
├── build.gradle.kts                                   # Root build file
├── settings.gradle.kts                                # Project settings
├── gradle.properties                                  # Gradle config
├── content/                                           # Content library (JSON)
│   ├── dares.json                                     # 300+ dares
│   ├── texts.json                                     # 500+ text templates
│   ├── scenarios.json                                 # 50+ branching scenarios
│   ├── tts_scripts.json                               # 200+ TTS scripts
│   └── fantasy_questions.json                         # Fantasy questionnaire
└── docs/
    └── superpowers/
        ├── specs/2026-03-25-igniteai-v1-design.md
        └── plans/2026-03-25-igniteai-v1-implementation.md
```

---

## Chunk 1: Project Setup & Core Infrastructure

### Task 1: Initialize Android Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `app/build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/igniteai/app/IgniteAIApp.kt`
- Create: `app/src/main/java/com/igniteai/app/MainActivity.kt`

- [ ] **Step 1: Create root settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "IgniteAI"
include(":app")
```

- [ ] **Step 2: Create root build.gradle.kts**

```kotlin
// build.gradle.kts
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}
```

- [ ] **Step 3: Create gradle.properties**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: Create app/build.gradle.kts with all dependencies**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
    namespace = "com.igniteai.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.igniteai.app"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Room + SQLCipher
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")

    // DataStore (encrypted preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Biometrics
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    // Audio
    implementation("androidx.media3:media3-exoplayer:1.5.1")

    // Bluetooth & WiFi Direct
    implementation("androidx.core:core-ktx:1.15.0")

    // Health Connect (heart rate)
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Serialization (for sync protocol + content JSON)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // QR Code generation
    implementation("com.google.zxing:core:3.5.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.13")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

- [ ] **Step 5: Create AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Bluetooth LE -->
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

    <!-- WiFi Direct -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" />

    <!-- Audio recording (voice safeword) -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Notifications -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Vibration (haptics) -->
    <uses-permission android:name="android.permission.VIBRATE" />

    <!-- Internet (Stripe payment only) -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Health Connect (heart rate) -->
    <uses-permission android:name="android.permission.health.READ_HEART_RATE" />

    <!-- Keep screen on during sessions -->
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <application
        android:name=".IgniteAIApp"
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.IgniteAI">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <!-- Deep link for Stripe payment return -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="igniteai" android:host="payment" />
            </intent-filter>
        </activity>

        <!-- Health Connect intent filter -->
        <intent-filter>
            <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
        </intent-filter>
    </application>
</manifest>
```

- [ ] **Step 6: Create Application class**

```kotlin
// app/src/main/java/com/igniteai/app/IgniteAIApp.kt
package com.igniteai.app

import android.app.Application

class IgniteAIApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // DI and initialization will be added as modules are built
    }
}
```

- [ ] **Step 7: Create MainActivity with Compose host**

```kotlin
// app/src/main/java/com/igniteai/app/MainActivity.kt
package com.igniteai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.igniteai.app.ui.theme.IgniteAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IgniteAITheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // NavGraph will be added in Task 3
                }
            }
        }
    }
}
```

- [ ] **Step 8: Verify project builds**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git init
git add .
git commit -m "feat: initialize IgniteAI Android project with Kotlin + Compose"
```

---

### Task 2: Theme & UI Components

**Files:**
- Create: `app/src/main/java/com/igniteai/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/igniteai/app/ui/theme/Type.kt`
- Create: `app/src/main/java/com/igniteai/app/ui/theme/Shape.kt`
- Create: `app/src/main/java/com/igniteai/app/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/igniteai/app/ui/components/IgniteButton.kt`
- Create: `app/src/main/java/com/igniteai/app/ui/components/IgniteCard.kt`
- Create: `app/src/main/java/com/igniteai/app/ui/components/EmberParticles.kt`
- Create: `app/src/main/java/com/igniteai/app/ui/components/PulsingGlow.kt`
- Create: `app/src/main/java/com/igniteai/app/ui/components/StreakCounter.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: Create Color.kt — fire/ember color palette**

```kotlin
// Dark with fire/neon palette per spec
// Primary: deep ember orange-red
// Background: near-black (#0A0A0A)
// Accents: glowing red, orange, amber gradients
// Surface: dark charcoal (#1A1A1A)
// Error: bright red for safeword/stop
```

Define colors: `EmberOrange`, `FlameRed`, `MoltenGold`, `CharcoalDark`, `AbyssBlack`, `SafewordRed`, `CoolDownBlue`. Both light and dark color schemes (dark-only app, but Material 3 requires both).

- [ ] **Step 2: Create Type.kt — bold modern typography**

Use `fonts.google.com` — recommend **Inter** or **Outfit** for bold modern feel. Define `displayLarge` (splash), `headlineMedium` (section headers), `bodyLarge` (content text), `labelMedium` (buttons).

- [ ] **Step 3: Create Shape.kt and Theme.kt**

Rounded corners (12dp default, 24dp for cards). Theme wires Color + Type + Shape into Material 3 `MaterialTheme`.

- [ ] **Step 4: Create IgniteButton composable**

Styled button with ember glow effect (animated border gradient from orange to red). Takes `text`, `onClick`, `enabled`, `isEmergency` (red for safeword). Uses `animateFloatAsState` for glow pulse.

- [ ] **Step 5: Create IgniteCard composable**

Dark surface card with subtle ember border glow. Used for dares, content, settings. Takes `content` composable lambda.

- [ ] **Step 6: Create EmberParticles composable**

Canvas-based particle system. Spawns small orange/red circles that float upward and fade. Used as background decoration on key screens. Configurable `intensity` (1-10) and `particleCount`.

- [ ] **Step 7: Create PulsingGlow composable**

Animated radial gradient that pulses (expands/contracts). Used for heart rate visualization and interactive elements. Takes `color`, `pulseSpeed`, `size`.

- [ ] **Step 8: Create StreakCounter composable**

Displays current streak count with flame icon. Number animates up. Fire icon grows with streak level (1-5 intensity). Shows "🔥 x 3" style display.

- [ ] **Step 9: Create strings.xml and themes.xml**

```xml
<!-- strings.xml -->
<string name="app_name">IgniteAI</string>
<string name="app_name_decoy">Calculator Pro</string>
<!-- Add all user-facing strings -->
```

- [ ] **Step 10: Verify theme renders**

Create a temporary preview composable that shows all components. Run on emulator or `@Preview`.

- [ ] **Step 11: Commit**

```bash
git add app/src/main/java/com/igniteai/app/ui/ app/src/main/res/
git commit -m "feat: add IgniteAI fire/ember theme and core UI components"
```

---

### Task 3: Navigation Structure

**Files:**
- Create: `app/src/main/java/com/igniteai/app/ui/navigation/NavGraph.kt`
- Modify: `app/src/main/java/com/igniteai/app/MainActivity.kt`

- [ ] **Step 1: Define all routes as sealed class**

```kotlin
// Routes: Onboarding (welcome, partner setup, biometric setup, pin setup, pairing, fantasy questionnaire),
// Home, Session (consent gate, active session, cool down),
// Content (dare, text message), Audio player,
// Anticipation (tease sequence, countdown lock),
// Vault (unlock, vault main),
// Control (controller, receiver), HeartRate, Scenario, Challenge,
// Payment, Settings
```

- [ ] **Step 2: Build NavHost with all routes**

All routes defined but screens are placeholder `Text("Screen Name")` composables. Actual screens will be built in later tasks.

- [ ] **Step 3: Wire NavGraph into MainActivity**

Replace the empty `Surface` content with `NavGraph(navController)`.

- [ ] **Step 4: Verify navigation compiles and app launches**

Run: `./gradlew assembleDebug && adb install app/build/outputs/apk/debug/app-debug.apk`
Expected: App launches showing welcome screen placeholder.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/igniteai/app/ui/navigation/ app/src/main/java/com/igniteai/app/MainActivity.kt
git commit -m "feat: add navigation graph with all screen routes"
```

---

### Task 4: Encrypted Database

**Files:**
- Create: `app/src/main/java/com/igniteai/app/core/database/IgniteDatabase.kt`
- Create: `app/src/main/java/com/igniteai/app/core/database/Converters.kt`
- Create: `app/src/main/java/com/igniteai/app/core/database/DatabaseProvider.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/CoupleProfile.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/Partner.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/ContentItem.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/SessionRecord.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/EngagementRecord.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/FantasyProfile.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/VaultItem.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/ScenarioNode.kt`
- Create: `app/src/main/java/com/igniteai/app/data/model/LicenseKey.kt`
- Test: `app/src/test/java/com/igniteai/app/core/database/ConvertersTest.kt`
- Test: `app/src/androidTest/java/com/igniteai/app/core/database/IgniteDatabaseTest.kt`

- [ ] **Step 1: Write unit test for type converters**

Test `Converters` class: `LocalDateTime` ↔ `Long`, `List<String>` ↔ `String` (JSON), enum ↔ `String`.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "*.ConvertersTest"`
Expected: FAIL — classes don't exist yet.

- [ ] **Step 3: Create all Room entity data classes**

Each entity as `@Entity` with:
- `CoupleProfile`: id, createdAt, pairingMethod, isActive
- `Partner`: id visibleName, isLocal (this device), coupleId (FK)
- `ContentItem`: id, type (DARE/SCENARIO/TEXT/AUDIO), tone (PLAYFUL/RAW/SENSUAL), intensity (1-10), level (SPARK/FIRE), duration, title, body, audioRef, tags
- `SessionRecord`: id, startedAt, endedAt, consentTimestamp, safewordTriggered, sessionType
- `EngagementRecord`: id, contentId (FK), action (COMPLETED/SKIPPED/FAVORITED/BLOCKED), timestamp, partnerId
- `FantasyProfile`: id, partnerId (FK), answers (JSON string), createdAt, updatedAt
- `VaultItem`: id, type (DARE/VOICE_NOTE/SAVED_CONTENT), content (encrypted), createdBy, createdAt
- `ScenarioNode`: id, scenarioId, parentNodeId, text, audioRef, hapticPattern, choices (JSON), isRoot, tone, intensity
- `LicenseKey`: id, level, key, purchasedAt, deviceId, stripeTransactionId

- [ ] **Step 4: Create Converters class**

Implement `@TypeConverter` methods for all custom types.

- [ ] **Step 5: Run converter test — should pass**

Run: `./gradlew test --tests "*.ConvertersTest"`
Expected: PASS

- [ ] **Step 6: Create IgniteDatabase (Room)**

`@Database` class with all entities and DAOs (empty DAOs for now — will be created per-feature).

- [ ] **Step 7: Create DatabaseProvider with SQLCipher encryption**

```kotlin
// Uses SQLCipher SupportFactory to create encrypted Room database
// Encryption key derived from Android Keystore
// Database file: "ignite_db.enc"
fun provideDatabase(context: Context, passphrase: ByteArray): IgniteDatabase {
    val factory = SupportFactory(passphrase)
    return Room.databaseBuilder(context, IgniteDatabase::class.java, "ignite_db.enc")
        .openHelperFactory(factory)
        .build()
}
```

- [ ] **Step 8: Write instrumented test for database creation**

Test that database opens with encryption, can insert/read a `CoupleProfile`, and fails without correct key.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/igniteai/app/core/database/ app/src/main/java/com/igniteai/app/data/model/ app/src/test/ app/src/androidTest/
git commit -m "feat: add encrypted Room database with all entity models"
```

---

### Task 5: Security Infrastructure

**Files:**
- Create: `app/src/main/java/com/igniteai/app/core/security/EncryptionManager.kt`
- Create: `app/src/main/java/com/igniteai/app/core/security/BiometricAuthManager.kt`
- Create: `app/src/main/java/com/igniteai/app/core/security/PanicWipeManager.kt`
- Create: `app/src/main/java/com/igniteai/app/core/preferences/AppPreferences.kt`
- Test: `app/src/test/java/com/igniteai/app/core/security/EncryptionManagerTest.kt`

- [ ] **Step 1: Write test for EncryptionManager**

Test: encrypt → decrypt roundtrip produces original data. Test: decrypt with wrong key fails. Test: different inputs produce different ciphertexts.

- [ ] **Step 2: Run test — should fail**

Run: `./gradlew test --tests "*.EncryptionManagerTest"`
Expected: FAIL

- [ ] **Step 3: Implement EncryptionManager**

```kotlin
// Uses Android Keystore to generate/store AES-256 key
// encrypt(plaintext: ByteArray): ByteArray — AES/GCM/NoPadding
// decrypt(ciphertext: ByteArray): ByteArray
// generateDatabaseKey(): ByteArray — for SQLCipher passphrase
// deleteAllKeys() — for panic wipe
```

- [ ] **Step 4: Run test — should pass**

Run: `./gradlew test --tests "*.EncryptionManagerTest"`
Expected: PASS

- [ ] **Step 5: Implement BiometricAuthManager**

```kotlin
// authenticate(activity, title, subtitle, onSuccess, onFailure, onFallbackPin)
// Uses BiometricPrompt with BIOMETRIC_STRONG
// If biometric unavailable → calls onFallbackPin
// Wraps the BiometricPrompt callback complexity into simple lambdas
```

- [ ] **Step 6: Implement PanicWipeManager**

```kotlin
// wipeAllData(context): Boolean
// 1. Delete Room database file
// 2. Clear DataStore preferences
// 3. Delete audio cache directory
// 4. Delete vault files
// 5. Remove encryption keys from Keystore
// 6. Return true if all deletions succeeded
```

- [ ] **Step 7: Implement AppPreferences**

```kotlin
// DataStore-based encrypted preferences
// Properties: pin (encrypted), decoyEnabled, decoyName,
//   notificationTime, tonePreference, streakCount, lastActiveDate,
//   pavlovianEnabled, conditioningIntensity, sessionTimeLimit
```

- [ ] **Step 8: Wire EncryptionManager to DatabaseProvider**

```kotlin
// In EncryptionManager, add: generateDatabaseKey(): ByteArray
// Derive a stable 32-byte key from Android Keystore for SQLCipher
// In DatabaseProvider, call EncryptionManager.generateDatabaseKey() to get passphrase
// Test: database opens successfully with derived key, data persists across app restarts
```

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/igniteai/app/core/security/ app/src/main/java/com/igniteai/app/core/preferences/ app/src/test/
git commit -m "feat: add encryption, biometric auth, panic wipe, and preferences"
```

---

### Task 6: Sync Protocol & Connection Manager

**Files:**
- Create: `app/src/main/java/com/igniteai/app/core/sync/SyncProtocol.kt`
- Create: `app/src/main/java/com/igniteai/app/core/sync/BleManager.kt`
- Create: `app/src/main/java/com/igniteai/app/core/sync/WifiDirectManager.kt`
- Create: `app/src/main/java/com/igniteai/app/core/sync/ConnectionManager.kt`
- Test: `app/src/test/java/com/igniteai/app/core/sync/SyncProtocolTest.kt`

- [ ] **Step 1: Write test for SyncProtocol message serialization**

Test: serialize/deserialize roundtrip for each message type: `PairingRequest`, `PairingResponse`, `ConsentConfirm`, `ContentSync`, `HapticTrigger`, `HeartRateUpdate`, `ControlCommand`, `SafewordTrigger`, `CheckInRequest`, `CheckInResponse`, `VaultUnlockRequest`.

- [ ] **Step 2: Run test — should fail**

Run: `./gradlew test --tests "*.SyncProtocolTest"`
Expected: FAIL

- [ ] **Step 3: Implement SyncProtocol**

```kotlin
// Sealed class hierarchy for all P2P message types
// Uses kotlinx.serialization for JSON encoding
// Each message has: type, timestamp, senderId, payload
// encrypt/decrypt wrapper using shared couple key
```

- [ ] **Step 4: Run test — should pass**

- [ ] **Step 5: Implement BleManager**

```kotlin
// startAdvertising() — makes this device discoverable
// startScanning() — finds partner device
// connect(deviceAddress) — establish GATT connection
// send(message: SyncMessage) — write to characteristic
// onMessageReceived: Flow<SyncMessage> — incoming messages
// disconnect()
// Uses BLE GATT with custom service UUID for IgniteAI
```

- [ ] **Step 6: Implement WifiDirectManager**

```kotlin
// Same interface as BleManager but over WiFi Direct
// discoverPeers() → connect() → send/receive via sockets
// Used as fallback when BLE latency > 500ms
```

- [ ] **Step 7: Implement ConnectionManager (failover state machine)**

```kotlin
// States: DISCONNECTED → CONNECTING_BLE → CONNECTED_BLE → FALLBACK_WIFI → CONNECTED_WIFI → CONNECTION_LOST
// Auto-switches BLE → WiFi if latency > 500ms
// Emits connectionState: StateFlow<ConnectionState>
// If both fail for >5s: emits CONNECTION_LOST
// If CONNECTION_LOST for >60s: emits SESSION_ENDED
// Provides unified send(message) and receive(): Flow<SyncMessage>
```

- [ ] **Step 8: Add Bluetooth & WiFi Direct runtime permission requests**

```kotlin
// Create PermissionHelper utility:
// requestBluetoothPermissions(activity) — BLUETOOTH_CONNECT, BLUETOOTH_SCAN, BLUETOOTH_ADVERTISE
// requestWifiDirectPermissions(activity) — ACCESS_FINE_LOCATION, NEARBY_WIFI_DEVICES
// Uses ActivityResultContracts.RequestMultiplePermissions
// Called before any BLE scan or WiFi Direct discovery
```

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/igniteai/app/core/sync/ app/src/test/
git commit -m "feat: add BLE/WiFi Direct sync protocol with failover"
```

---

### Task 6b: Pairing State Management

**Files:**
- Create: `app/src/main/java/com/igniteai/app/data/model/PairingData.kt`
- Create: `app/src/main/java/com/igniteai/app/data/dao/PairingDao.kt`
- Create: `app/src/main/java/com/igniteai/app/data/repository/PairingRepository.kt`

- [ ] **Step 1: Create PairingData entity**

```kotlin
@Entity(tableName = "pairing_data")
data class PairingData(
    @PrimaryKey val id: String,
    val coupleId: String,
    val localPartnerId: String,
    val remotePartnerId: String,
    val sharedSecret: ByteArray,       // Derived during pairing for message encryption
    val remotePublicKey: ByteArray,    // Partner's public key
    val pairingMethod: String,         // "QR" or "INVITE_CODE"
    val pairedAt: Long,
    val isActive: Boolean
)
```

- [ ] **Step 2: Create PairingDao**

```kotlin
@Dao
interface PairingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storePairing(data: PairingData)

    @Query("SELECT * FROM pairing_data WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePairing(): PairingData?

    @Query("SELECT sharedSecret FROM pairing_data WHERE isActive = 1 LIMIT 1")
    suspend fun getSharedSecret(): ByteArray?

    @Query("UPDATE pairing_data SET isActive = 0 WHERE id = :id")
    suspend fun deactivatePairing(id: String)

    @Query("DELETE FROM pairing_data")
    suspend fun deleteAll()
}
```

- [ ] **Step 3: Implement PairingRepository**

```kotlin
// storePairingResult(coupleProfile, partnerKey, sharedSecret)
// getSharedEncryptionKey(): ByteArray — for SyncProtocol message encryption
// getPartnerInfo(): Partner?
// getActivePairing(): PairingData?
// unpair() — deactivates pairing, clears shared secret
// isPaired(): Boolean
// generateInviteCode(): String — 6-digit random code
// generateQrPayload(): String — JSON with partner info + public key
// performKeyExchange(remotePublicKey): ByteArray — ECDH key agreement
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/igniteai/app/data/model/PairingData.kt app/src/main/java/com/igniteai/app/data/dao/PairingDao.kt app/src/main/java/com/igniteai/app/data/repository/PairingRepository.kt
git commit -m "feat: add pairing state management with key exchange"
```

---

### Task 6c: Content Library Loading

**Files:**
- Modify: `app/src/main/java/com/igniteai/app/data/repository/ContentRepository.kt`
- Create: `app/src/main/assets/content/dares.json`
- Create: `app/src/main/assets/content/texts.json`

This task is placed here (before onboarding/home screen) so content is available when the app first launches.

- [ ] **Step 1: Create starter dares.json in assets**

30 dares (10 per tone: playful, raw, sensual) across intensity levels 1-5. Each dare: id, tone, intensity, level, duration, title, body, tags. Placed in `app/src/main/assets/content/`.

- [ ] **Step 2: Create starter texts.json in assets**

50 text templates (mixed tones). Each: id, tone, intensity, level, body, tags.

- [ ] **Step 3: Implement content loading in ContentRepository**

```kotlin
// loadContentLibrary(context: Context)
// Check preference flag: "content_loaded_v1"
// If not loaded:
//   Read dares.json from context.assets.open("content/dares.json")
//   Parse with kotlinx.serialization
//   Insert into Room via ContentDao.insertAll()
//   Repeat for texts.json
//   Set "content_loaded_v1" flag in preferences
// Called from IgniteAIApp.onCreate()
```

- [ ] **Step 4: Test content loads into database**

Run app → check Room has content items → verify count matches JSON.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/assets/content/ app/src/main/java/com/igniteai/app/data/repository/ContentRepository.kt
git commit -m "feat: add content library JSON loading on first launch"
```

---

## Chunk 2: Onboarding, Pairing & Consent

### Task 7: Onboarding Flow Screens

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/onboarding/OnboardingViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/onboarding/WelcomeScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/onboarding/PartnerSetupScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/onboarding/BiometricSetupScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/onboarding/PinSetupScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/onboarding/PairingScreen.kt`
- Modify: `app/src/main/java/com/igniteai/app/ui/navigation/NavGraph.kt`

- [ ] **Step 1: Create OnboardingViewModel**

```kotlin
// State: onboardingStep (WELCOME, PARTNER_SETUP, BIOMETRIC, PIN, PAIRING, FANTASY)
// Actions: setPartnerName, enrollBiometric, setPin, generateQrCode, generateInviteCode, acceptInviteCode
// Stores partner profile + couple profile to Room
// Generates encryption keypair for couple sync
```

- [ ] **Step 2: Create WelcomeScreen**

Fire-themed splash with app logo, ember particles background, tagline, and "Get Started" button. Full-screen immersive.

- [ ] **Step 3: Create PartnerSetupScreen**

Input field for partner display name. "This name is only visible to your partner." IgniteCard with IgniteButton.

- [ ] **Step 4: Create BiometricSetupScreen**

Explains why biometric is needed ("Both partners authenticate before every session — your privacy, your safety"). Triggers `BiometricAuthManager.authenticate()` to enroll. Shows success/fallback messaging.

- [ ] **Step 5: Create PinSetupScreen**

6-digit PIN entry with confirm step. "This PIN is your backup if biometric is unavailable." Stored via `AppPreferences` (encrypted).

- [ ] **Step 6: Create PairingScreen**

Two tabs:
- "Show QR Code" — generates QR from `SyncProtocol.PairingRequest` data, displays via ZXing
- "Enter Code" — 6-digit input field + "Join" button
- "Scan QR" — camera viewfinder to scan partner's QR

When pairing succeeds: exchange encryption keys, store `CoupleProfile`, navigate to fantasy questionnaire.

- [ ] **Step 7: Wire onboarding screens into NavGraph**

Add conditional start destination: if no `CoupleProfile` exists → onboarding, else → home (with biometric gate).

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/onboarding/ app/src/main/java/com/igniteai/app/ui/navigation/
git commit -m "feat: add complete onboarding flow with pairing"
```

---

### Task 8: Fantasy Questionnaire

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/onboarding/FantasyQuestionnaireScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/data/dao/FantasyDao.kt`
- Create: `app/src/main/java/com/igniteai/app/data/repository/FantasyRepository.kt`
- Create: `content/fantasy_questions.json`
- Test: `app/src/test/java/com/igniteai/app/data/repository/FantasyRepositoryTest.kt`

- [ ] **Step 1: Write test for FantasyRepository overlap matching**

```kotlin
// Test: two profiles with matching "dominant" interest → overlap includes "dominant"
// Test: two profiles with no overlap → empty overlap set
// Test: one partner's boundary blocks content even if other likes it
// Test: overlap score calculation is correct
```

- [ ] **Step 2: Run test — should fail**

- [ ] **Step 3: Create fantasy_questions.json**

```json
// 50-100 questions across categories:
// - dominance/submission preferences (scale 1-5)
// - scenario types (multiple choice: romantic, rough, playful, experimental)
// - intensity comfort level (1-10 scale)
// - specific interests (checkboxes: roleplay, toys, verbal, physical)
// - hard boundaries (checkboxes: things to never suggest)
// Each question has: id, category, text, type (scale/choice/checkbox), options
```

Start with 20 representative questions. Expand to 50+ in content pass.

- [ ] **Step 4: Create FantasyDao**

```kotlin
@Dao
interface FantasyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: FantasyProfile)

    @Query("SELECT * FROM fantasy_profile WHERE partnerId = :partnerId")
    suspend fun getProfile(partnerId: String): FantasyProfile?

    @Query("SELECT * FROM fantasy_profile WHERE partnerId IN (:partnerIds)")
    suspend fun getProfiles(partnerIds: List<String>): List<FantasyProfile>
}
```

- [ ] **Step 5: Implement FantasyRepository**

```kotlin
// saveAnswers(partnerId, answers: Map<String, Any>)
// getOverlap(partner1Id, partner2Id): FantasyOverlap
//   - Compares both profiles question by question
//   - Matching interests (both scored 3+ on same category) → overlap
//   - Either partner's hard boundary → excluded from all content
//   - Returns: sharedInterests, excludedTopics, intensityRange (min of both maxes)
// getContentTags(coupleId): List<String> — tags that match couple's overlap
```

- [ ] **Step 6: Run test — should pass**

- [ ] **Step 7: Create FantasyQuestionnaireScreen**

One question at a time (swipeable cards). Progress bar at top. Private — screen reminds user "Your answers are private and never shown to your partner." Completion saves to Room and navigates to Home.

- [ ] **Step 8: Commit**

```bash
git add content/fantasy_questions.json app/src/main/java/com/igniteai/app/feature/onboarding/FantasyQuestionnaireScreen.kt app/src/main/java/com/igniteai/app/data/dao/FantasyDao.kt app/src/main/java/com/igniteai/app/data/repository/FantasyRepository.kt app/src/test/
git commit -m "feat: add fantasy profiling questionnaire with overlap matching"
```

---

### Task 9: Consent & Session Management

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/session/SessionViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/session/ConsentGateScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/session/SessionScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/session/SafewordOverlay.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/session/CheckInDialog.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/session/CoolDownScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/session/DenyDelayOverlay.kt`
- Create: `app/src/main/java/com/igniteai/app/data/dao/SessionDao.kt`
- Create: `app/src/main/java/com/igniteai/app/data/repository/SessionRepository.kt`
- Test: `app/src/test/java/com/igniteai/app/data/repository/SessionRepositoryTest.kt`
- Test: `app/src/test/java/com/igniteai/app/feature/session/SessionViewModelTest.kt`

- [ ] **Step 1: Write test for SessionRepository**

```kotlin
// Test: session created with consent timestamps for both partners
// Test: session cannot start without both consents within 60s
// Test: safeword trigger sets safewordTriggered=true and endedAt
// Test: session time limit fires at correct time
// Test: shorter time limit of two partners is used
```

- [ ] **Step 2: Run test — should fail**

- [ ] **Step 3: Implement SessionDao and SessionRepository**

```kotlin
// SessionRepository:
// startSession(type, timeLimit): creates SessionRecord, starts timer
// recordConsent(partnerId): logs biometric consent with timestamp
// bothConsented(): Boolean — both partners consented within 60s
// triggerSafeword(): immediately ends session, records reason
// checkIn(): sends check-in to partner, waits for response (30s timeout)
// getTimeRemaining(): Flow<Duration>
// endSession(): records end time, navigates to cool-down
```

- [ ] **Step 4: Run test — should pass**

- [ ] **Step 5: Write test for SessionViewModel**

```kotlin
// Test: state transitions IDLE → CONSENT_GATE → ACTIVE → CHECK_IN → ACTIVE → COOL_DOWN
// Test: safeword from any state → COOL_DOWN within 500ms
// Test: deny&delay pauses session for configured duration then resumes
```

- [ ] **Step 6: Implement SessionViewModel**

```kotlin
// States: IDLE, CONSENT_GATE, WAITING_PARTNER_CONSENT, ACTIVE, CHECK_IN, DENY_DELAY, COOL_DOWN
// Manages session timer with check-in at 50%, warning at 90%, end at 100%
// Listens for safeword tap + voice recognition
// Coordinates deny & delay pauses
// Receives partner messages via ConnectionManager
```

- [ ] **Step 7: Create ConsentGateScreen**

Shows "Session requires both partners" message. Triggers biometric prompt for local partner. Shows waiting state for remote partner's consent (via BLE/WiFi). Both confirmed → transition to SessionScreen.

- [ ] **Step 8: Create SafewordOverlay**

Floating `Box` anchored bottom-center with large red stop button. Always composed on top of session content via `Modifier.zIndex`. `onClick` → `SessionViewModel.triggerSafeword()`. Animates with subtle pulse to remain visible but not distracting.

- [ ] **Step 9: Create CheckInDialog**

AlertDialog: "Still enjoying? Both partners confirm to continue." Two buttons: "Continue" and "End Session." 30-second auto-dismiss timer shown. If partner doesn't respond → "Waiting for [name]..." → 2-minute timeout → auto-end.

- [ ] **Step 10: Create CoolDownScreen**

Gentle blue/cool gradient (contrast to fire theme). Calm messaging: "Take a moment." Breathing animation. Option to return to Home or talk. Shows session summary (duration, content viewed).

- [ ] **Step 11: Create DenyDelayOverlay**

Full-screen overlay that dims content and pauses all audio/haptics. Countdown timer visible (5-15s). Resumes automatically. Subtle anticipation animation (pulsing ember).

- [ ] **Step 12: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/session/ app/src/main/java/com/igniteai/app/data/dao/SessionDao.kt app/src/main/java/com/igniteai/app/data/repository/SessionRepository.kt app/src/test/
git commit -m "feat: add consent gate, session management, safeword, and check-in system"
```

---

## Chunk 3: Content Engine & Daily Dares

### Task 10: Content Data & DAOs

**Files:**
- Create: `app/src/main/java/com/igniteai/app/data/dao/ContentDao.kt`
- Create: `app/src/main/java/com/igniteai/app/data/dao/EngagementDao.kt`
- Create: `content/dares.json`
- Create: `content/texts.json`

- [ ] **Step 1: Create ContentDao**

```kotlin
@Dao
interface ContentDao {
    @Query("SELECT * FROM content_item WHERE level = :level AND type = :type")
    suspend fun getByLevelAndType(level: String, type: String): List<ContentItem>

    @Query("SELECT * FROM content_item WHERE id = :id")
    suspend fun getById(id: String): ContentItem?

    @Query("SELECT * FROM content_item WHERE id NOT IN (:blockedIds) AND level = :level AND tone IN (:tones) AND intensity BETWEEN :minIntensity AND :maxIntensity ORDER BY RANDOM() LIMIT :limit")
    suspend fun getFiltered(blockedIds: List<String>, level: String, tones: List<String>, minIntensity: Int, maxIntensity: Int, limit: Int): List<ContentItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ContentItem>)
}
```

- [ ] **Step 2: Create EngagementDao**

```kotlin
@Dao
interface EngagementDao {
    @Insert
    suspend fun record(engagement: EngagementRecord)

    @Query("SELECT contentId FROM engagement_record WHERE action = 'BLOCKED'")
    suspend fun getBlockedContentIds(): List<String>

    @Query("SELECT contentId, SUM(CASE WHEN action = 'FAVORITED' THEN 3 WHEN action = 'COMPLETED' THEN 1 WHEN action = 'SKIPPED' THEN -2 ELSE 0 END) as score FROM engagement_record GROUP BY contentId ORDER BY score DESC")
    suspend fun getEngagementScores(): List<ContentScore>

    @Query("SELECT contentId FROM engagement_record WHERE action = 'COMPLETED' AND timestamp > :since ORDER BY timestamp DESC")
    suspend fun getRecentlyCompleted(since: Long): List<String>
}
```

- [ ] **Step 3: Create starter dares.json**

Begin with 30 dares (10 per tone: playful, raw, sensual) across intensity levels 1-5. Each dare has: id, tone, intensity, level, duration, title, body, tags. This is the seed content — expand to 300+ in a dedicated content pass.

- [ ] **Step 4: Create starter texts.json**

Begin with 50 text templates (mixed tones). Each has: id, tone, intensity, level, body, tags.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/igniteai/app/data/dao/ content/
git commit -m "feat: add content and engagement DAOs with starter content"
```

---

### Task 11: Content Repository & Adaptive Algorithm

**Files:**
- Create: `app/src/main/java/com/igniteai/app/data/repository/ContentRepository.kt`
- Test: `app/src/test/java/com/igniteai/app/data/repository/ContentRepositoryTest.kt`

- [ ] **Step 1: Write test for adaptive content selection**

```kotlin
// Test: getNextContent respects blocked items — blocked content never returned
// Test: getNextContent avoids yesterday's content (no same-day repeat)
// Test: engagement scores influence selection — high-scored content appears more
// Test: minimum diversity — all 3 tones appear within 7 days
// Test: intensity respects couple's fantasy overlap range
// Test: streak bonus increases intensity correctly (base + min(streak, 5))
```

- [ ] **Step 2: Run test — should fail**

- [ ] **Step 3: Implement ContentRepository**

```kotlin
class ContentRepository(
    private val contentDao: ContentDao,
    private val engagementDao: EngagementDao,
    private val fantasyRepository: FantasyRepository,
    private val preferences: AppPreferences
) {
    // loadContentLibrary(context) — parse JSON from assets, insert into Room on first launch
    // getNextDare(coupleId): ContentItem — adaptive selection per algorithm in spec
    // getNextText(coupleId): ContentItem
    // recordEngagement(contentId, action: COMPLETED/SKIPPED/FAVORITED/BLOCKED)
    // getDailyDare(coupleId): ContentItem — cached once per day
    // getStreakBonus(): Int — min(streakCount, 5)
    //
    // Adaptive algorithm:
    // 1. Get blocked IDs → exclude
    // 2. Get recently completed (last 24h) → exclude
    // 3. Get couple's fantasy overlap → filter by matching tags + intensity range
    // 4. Get engagement scores → rank
    // 5. Top 60% by score: 70% chance of selection
    // 6. Bottom 20%: 30% chance (exploration)
    // 7. Diversity check: if any tone missing from last 7 days, force it
    // 8. Apply streak bonus to intensity range
}
```

- [ ] **Step 4: Run test — should pass**

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/igniteai/app/data/repository/ContentRepository.kt app/src/test/
git commit -m "feat: add adaptive content selection algorithm"
```

---

### Task 12: Home Screen & Daily Dare

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/home/HomeViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/home/HomeScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/home/DailyDareCard.kt`

- [ ] **Step 1: Create HomeViewModel**

```kotlin
// State: dailyDare, streakCount, partnerName, isFireUnlocked, connectionStatus
// Actions: loadDailyDare, completeDare, skipDare, favoriteDare, startSession, navigateToSettings
// On init: checks biometric auth, loads daily dare, checks streak
```

- [ ] **Step 2: Create HomeScreen**

Main hub layout:
- Top: Partner names + connection status indicator (BLE/WiFi icon)
- Center: DailyDareCard (prominent)
- Below: StreakCounter
- Bottom: Action buttons — "Start Session", "Tease Sequence", "Vault" (if paired)
- FAB: Settings gear icon
- Background: subtle EmberParticles

- [ ] **Step 3: Create DailyDareCard**

IgniteCard containing:
- Dare title + body text
- Tone indicator (color-coded: playful=amber, raw=red, sensual=rose)
- Action row: ❤️ Favorite | ⏭️ Skip | 🚫 Block
- "Do It" button (records completion)
- Animates in with fade + slide

- [ ] **Step 4: Set up daily notification using WorkManager**

```kotlin
// Schedule daily notification at user's preferred time (default 8pm)
// Notification: discreet text "You have something waiting 🔥" (customizable)
// Tapping notification opens app → Home → DailyDare
// Uses PeriodicWorkRequest with flex window
```

- [ ] **Step 5: Verify home screen renders with mock data**

Run on emulator. Check: theme renders correctly, components display, navigation works.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/home/
git commit -m "feat: add home screen with daily dare and streak counter"
```

---

### Task 13: Content Screens & Feedback

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/content/ContentViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/content/DareScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/content/TextMessageScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/content/ContentFeedbackBar.kt`

- [ ] **Step 1: Create ContentViewModel**

```kotlin
// State: currentContent, nextContent (preloaded), contentHistory
// Actions: complete, skip, favorite, block, loadNext
// Coordinates with ContentRepository for adaptive selection
// Preloads next content item for instant transitions
```

- [ ] **Step 2: Create DareScreen**

Full-screen dare display within a session. Fire-themed card with dare text, intensity indicator (flame icons 1-5), timer (if timed dare). ContentFeedbackBar at bottom. Animates content transitions.

- [ ] **Step 3: Create TextMessageScreen**

Displays text templates styled as "message bubbles" — as if the app is texting the couple. Scrollable conversation-style layout. Each message appears with typing animation delay.

- [ ] **Step 4: Create ContentFeedbackBar**

Reusable bottom bar: ❤️ Favorite | ⏭️ Next | 🚫 Block. Block shows confirmation dialog: "Hide this forever? You can undo in Settings." Each action calls `ContentRepository.recordEngagement()`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/content/
git commit -m "feat: add dare and text content screens with feedback system"
```

---

## Chunk 4: Audio, Haptics & Pavlovian System

### Task 14: Audio Engine

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/audio/AudioEngine.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/audio/BinauralMixer.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/audio/BreathPacer.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/audio/SoundscapeLayer.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/audio/AudioViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/audio/AudioPlayerScreen.kt`

- [ ] **Step 1: Implement AudioEngine**

```kotlin
// Central audio orchestrator
// playPrerecorded(resId: Int) — ExoPlayer for pre-recorded clips from res/raw
// speakTts(text: String, voice: VoiceConfig) — Android TTS with gender/speed/pitch
// stopAll() — immediate stop (for safeword)
// setVolume(layer: AudioLayer, volume: Float) — per-layer volume control
// AudioLayer: VOICE, SOUNDSCAPE, SIGNATURE_SOUND
// Manages ExoPlayer lifecycle (create/release with session)
```

- [ ] **Step 2: Implement BinauralMixer**

```kotlin
// Takes two audio sources (e.g., left=commands, right=praise)
// Mixes into stereo output: source1 → left channel, source2 → right channel
// Checks headphone connection before enabling
// Falls back to mono mix if no headphones detected
// Uses AudioTrack for low-level stereo control
```

- [ ] **Step 3: Implement BreathPacer**

```kotlin
// Tap-based breath synchronization
// onTap() — records tap timestamp, calculates rhythm (inhale/exhale cycle)
// getCurrentPace(): BreathPace (inhaleDuration, exhaleDuration)
// If no taps for 30s → fallback to default (4s in, 6s out)
// Provides pace as Flow<BreathPace> for audio sync
// Audio engine adjusts TTS speaking rate and pauses to match breath pace
```

- [ ] **Step 4: Implement SoundscapeLayer**

```kotlin
// Ambient background audio player
// Layers: HEARTBEAT, BREATHING, AMBIENT
// Each layer has independent volume and looping
// play(layer, resId, volume) — starts looping audio
// crossfade(fromLayer, toLayer, durationMs) — smooth transition
// stopAll() — fade out and release
```

- [ ] **Step 5: Create AudioPlayerScreen**

Session sub-screen for audio content. Shows:
- Waveform visualization (animated bars)
- Voice selector (male/female toggle)
- Volume sliders per layer
- Breath pacer tap zone (large circular button: "Tap to breathe")
- Binaural indicator (headphones icon, green if connected)

- [ ] **Step 6: Verify audio plays on emulator**

Test TTS output, pre-recorded playback, and volume controls.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/audio/
git commit -m "feat: add audio engine with TTS, binaural mixing, breath pacer, and soundscapes"
```

---

### Task 15: Haptic Engine

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/haptic/HapticEngine.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/haptic/HapticPatterns.kt`

- [ ] **Step 1: Implement HapticPatterns**

```kotlin
// Predefined vibration patterns as VibrationEffect compositions
// GENTLE_PULSE — slow 200ms on, 300ms off (sensual tone)
// SHARP_BURST — 50ms intense burst (commands)
// SLOW_WAVE — escalating 100→500ms with ramp (anticipation)
// HEARTBEAT — thud-thud-pause rhythm
// SIGNATURE — unique pattern for Pavlovian conditioning (distinct, recognizable)
// EMERGENCY_STOP — triple sharp buzz (safeword acknowledgment)
// Each pattern has: name, VibrationEffect, intensity (1-10), audioSyncPoint
```

- [ ] **Step 2: Implement HapticEngine**

```kotlin
// play(pattern: HapticPattern, intensity: Float = 1.0)
// playSequence(patterns: List<Pair<HapticPattern, Long>>) — timed sequence
// syncWithAudio(audioTimestamp: Long, pattern: HapticPattern) — trigger at audio cue
// stopAll() — for safeword
// setIntensity(level: Float) — global intensity multiplier (0.0-1.0)
// triggerRemote(partnerId: String, pattern: HapticPattern) — send via ConnectionManager
// Uses Vibrator service with VibrationEffect API (API 26+)
// Checks device capability: hasAmplitudeControl()
```

- [ ] **Step 3: Test haptic patterns on real device**

Haptics don't work on emulator. If real device available, verify each pattern. Otherwise, verify API calls are correct via unit test with mocked Vibrator.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/haptic/
git commit -m "feat: add haptic engine with predefined patterns and remote trigger"
```

---

### Task 16: Pavlovian Conditioning System

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/pavlovian/PavlovianManager.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/pavlovian/SignatureAssets.kt`
- Test: `app/src/test/java/com/igniteai/app/feature/pavlovian/PavlovianManagerTest.kt`

- [ ] **Step 1: Write test for PavlovianManager**

```kotlin
// Test: trigger fires both sound and haptic when enabled
// Test: trigger does nothing when disabled in settings
// Test: intensity setting controls frequency (subtle=20% of moments, moderate=50%, intense=80%)
// Test: remote trigger sends via ConnectionManager
// Test: conditioning moment is recorded for tracking
```

- [ ] **Step 2: Run test — should fail**

- [ ] **Step 3: Implement SignatureAssets**

```kotlin
// SIGNATURE_SOUND_RES = R.raw.ignite_chime — unique recognizable chime
// SIGNATURE_HAPTIC = HapticPatterns.SIGNATURE
// Note: actual audio file (ignite_chime.ogg) will be added in audio content pass
// For now, use a placeholder tone generated programmatically via AudioTrack
```

- [ ] **Step 4: Implement PavlovianManager**

```kotlin
// triggerConditioningMoment() — called by ContentEngine at arousing moments
//   Checks if enabled, rolls against intensity setting probability
//   Plays signature sound + signature haptic simultaneously
//   Records trigger timestamp for analytics
// sendTriggerToPartner() — remote conditioning trigger via Bluetooth
// onRemoteTrigger() — received from partner, plays locally
// isEnabled: Boolean — from AppPreferences
// intensity: ConditioningIntensity (SUBTLE/MODERATE/INTENSE)
```

- [ ] **Step 5: Run test — should pass**

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/pavlovian/ app/src/test/
git commit -m "feat: add Pavlovian conditioning system with signature sound and haptic"
```

---

## Chunk 5: Anticipation Engine & Tease Sequences

### Task 17: Anticipation Engine

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/anticipation/AnticipationViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/anticipation/TeaseSequenceScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/anticipation/CountdownLockScreen.kt`

- [ ] **Step 1: Create AnticipationViewModel**

```kotlin
// Timed Tease Sequences:
// scheduleTeaseSequence(startTime, endTime, messageCount, intensityCurve)
// Uses WorkManager to schedule messages at calculated intervals
// Morning messages: intensity 1-3, evening: 7-10
// Both partners get different but coordinated messages
//
// Countdown Lock:
// lockContent(contentId, unlockTime) — stores lock in Room
// getLockedContent(): Flow<List<LockedContent>> — items with countdown
// isUnlocked(contentId): Boolean
// getTimeRemaining(contentId): Flow<Duration>
```

- [ ] **Step 2: Create TeaseSequenceScreen**

Shows current tease sequence status:
- Timeline visualization (dots on a line, filled = delivered, empty = upcoming)
- Latest message displayed prominently
- "Next tease in: X hours" countdown
- Partner's reaction indicator (if they've opened the message)
- Configure button → schedule picker

- [ ] **Step 3: Create CountdownLockScreen**

Shows locked content with:
- Blurred preview of what's coming
- Large countdown timer (animated digits)
- "Unlocks in: 2h 34m"
- Ember particle animation intensifies as time gets closer
- When unlocked: dramatic reveal animation → navigate to content

- [ ] **Step 4: Wire Deny & Delay into SessionViewModel**

```kotlin
// In SessionViewModel, add deny & delay logic:
// scheduleDenyDelay(afterMinutes: Int, durationSeconds: Int)
// Random deny&delay triggers during session based on settings
// When triggered: emit DENY_DELAY state → DenyDelayOverlay shows → timer → resume
// Configurable: frequency (never/rare/moderate/frequent) and pause duration (5/10/15s)
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/anticipation/
git commit -m "feat: add anticipation engine with tease sequences and countdown locks"
```

---

## Chunk 6: Level 2 Features

### Task 18: Payment & License Management

**Files:**
- Create: `app/src/main/java/com/igniteai/app/data/dao/LicenseDao.kt`
- Create: `app/src/main/java/com/igniteai/app/data/repository/LicenseRepository.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/payment/PaymentViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/payment/PaymentScreen.kt`
- Test: `app/src/test/java/com/igniteai/app/data/repository/LicenseRepositoryTest.kt`

- [ ] **Step 1: Write test for LicenseRepository**

```kotlin
// Test: isLevelUnlocked("FIRE") returns false initially
// Test: after storeLicense with valid key → isLevelUnlocked returns true
// Test: license persists across repository recreation (database-backed)
// Test: getDeviceId returns consistent ID
```

- [ ] **Step 2: Run test — should fail**

- [ ] **Step 3: Implement LicenseDao and LicenseRepository**

```kotlin
// LicenseRepository:
// isLevelUnlocked(level: String): Boolean
// storeLicense(level, key, stripeTransactionId)
// getDeviceId(): String — derived from Android Settings.Secure.ANDROID_ID
// buildPaymentUrl(level): String — constructs Stripe Payment Link URL with
//   client_reference_id=deviceId and success_url=igniteai://payment?session_id={CHECKOUT_SESSION_ID}
// verifyPayment(sessionId: String): Boolean — calls Stripe API to verify (only network call)
// Retry logic: if verification fails, retry every 30s for 5 min
```

- [ ] **Step 4: Run test — should pass**

- [ ] **Step 5: Create PaymentScreen**

Shows Fire tier features with marketing copy:
- Feature list with fire icons
- Price: $29 one-time
- "Unlock Fire" IgniteButton → opens browser with Stripe Payment Link
- On return from browser (deep link): shows "Verifying payment..." with loading animation
- Success: celebration animation → "Fire Unlocked!" → navigate to Home
- Failure: "Payment Pending" with transaction ID and support contact
- Retry button

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/igniteai/app/data/dao/LicenseDao.kt app/src/main/java/com/igniteai/app/data/repository/LicenseRepository.kt app/src/main/java/com/igniteai/app/feature/payment/ app/src/test/
git commit -m "feat: add Stripe payment flow and license management"
```

---

### Task 19: Branching Roleplay Scenarios

**Files:**
- Create: `app/src/main/java/com/igniteai/app/data/dao/ScenarioDao.kt`
- Create: `app/src/main/java/com/igniteai/app/data/repository/ScenarioRepository.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/scenario/ScenarioViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/scenario/ScenarioScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/scenario/BranchChoiceCard.kt`
- Create: `content/scenarios.json`

- [ ] **Step 1: Create ScenarioDao**

```kotlin
@Dao
interface ScenarioDao {
    @Query("SELECT * FROM scenario_node WHERE scenarioId = :scenarioId AND isRoot = 1")
    suspend fun getRootNode(scenarioId: String): ScenarioNode?

    @Query("SELECT * FROM scenario_node WHERE id = :nodeId")
    suspend fun getNode(nodeId: String): ScenarioNode?

    @Query("SELECT * FROM scenario_node WHERE parentNodeId = :parentId")
    suspend fun getChildren(parentId: String): List<ScenarioNode>

    @Query("SELECT DISTINCT scenarioId FROM scenario_node WHERE isRoot = 1")
    suspend fun getAllScenarioIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<ScenarioNode>)
}
```

- [ ] **Step 2: Create ScenarioRepository**

```kotlin
// getAvailableScenarios(coupleId): List<ScenarioSummary> — filtered by fantasy overlap
// startScenario(scenarioId): ScenarioNode — returns root node
// chooseOption(nodeId, choiceIndex): ScenarioNode — returns next node
// getCurrentNode(): ScenarioNode
// isComplete(nodeId): Boolean — leaf node
// getAudioForNode(node): audio reference if exists
// getHapticForNode(node): haptic pattern if exists
```

- [ ] **Step 3: Create starter scenarios.json**

5 scenarios to start (expand to 50+ in content pass). Each scenario is a tree:
```json
{
  "id": "scenario_001",
  "title": "The Hotel Room",
  "tone": "playful",
  "intensity": 4,
  "nodes": [
    {"id": "n1", "isRoot": true, "text": "You check into a hotel room...", "choices": ["Open the champagne", "Push them onto the bed", "Blindfold them"]},
    {"id": "n2", "parentId": "n1", "choiceIndex": 0, "text": "The champagne fizzes...", "choices": [...]},
    ...
  ]
}
```

- [ ] **Step 4: Create ScenarioScreen**

Full-screen narrative display:
- Story text with typewriter animation
- At decision points: 2-3 BranchChoiceCards appear from bottom
- Partner sync: decision points alternate between partners (Partner A chooses odd nodes, Partner B chooses even)
- Audio and haptics trigger per node
- Tone/intensity label in corner
- Progress indicator (depth in tree)

- [ ] **Step 5: Create BranchChoiceCard**

Tappable IgniteCard with choice text. Subtle ember glow on hover/press. When chosen: card expands, others fade, transition to next node.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/igniteai/app/data/dao/ScenarioDao.kt app/src/main/java/com/igniteai/app/data/repository/ScenarioRepository.kt app/src/main/java/com/igniteai/app/feature/scenario/ content/scenarios.json
git commit -m "feat: add branching roleplay scenarios with synchronized partner decisions"
```

---

### Task 20: D/s Control Transfer

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/control/ControlViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/control/ControllerScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/control/ReceiverScreen.kt`

- [ ] **Step 1: Create ControlViewModel**

```kotlin
// State: role (CONTROLLER/RECEIVER/NONE), partnerRole, isSwapPending
// Controller actions:
//   triggerHaptic(pattern) → sends via ConnectionManager to receiver
//   triggerAudio(audioRef) → sends command to play on receiver
//   sendCommand(text) → displays text on receiver screen
//   setReceiverScreen(mode: INSTRUCTIONS/COUNTDOWN/DARKNESS/SURPRISE)
//   blackoutReceiver() → receiver screen goes dark
// Receiver: listens for commands, displays accordingly
// Role swap: requestSwap() → both confirm biometric → roles flip
```

- [ ] **Step 2: Create ControllerScreen**

Dashboard layout for the controller:
- Grid of haptic trigger buttons (each pattern has an icon + name)
- Audio trigger dropdown
- Text command input field + send button
- Receiver screen mode selector (4 options)
- "Swap Roles" button at bottom
- Live connection status indicator
- Partner's heart rate (if available, from Level 2 heart rate feature)

- [ ] **Step 3: Create ReceiverScreen**

Minimal screen controlled entirely by partner:
- INSTRUCTIONS mode: large centered text from controller
- COUNTDOWN mode: dramatic countdown timer
- DARKNESS mode: pure black screen (with safeword button still visible!)
- SURPRISE mode: waiting animation → reveals content when controller triggers
- SafewordOverlay always present on top

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/control/
git commit -m "feat: add D/s control transfer with controller and receiver modes"
```

---

### Task 21: Heart Rate Visualization

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/heartrate/HeartRateViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/heartrate/HeartRateScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/heartrate/HeartRateGlow.kt`

- [ ] **Step 1: Create HeartRateViewModel**

```kotlin
// Uses Health Connect API to read heart rate from Wear OS / fitness band
// localHeartRate: StateFlow<Int?> — null if no sensor
// partnerHeartRate: StateFlow<Int?> — received via ConnectionManager
// Sends local heart rate to partner every 2 seconds
// Triggers haptic on partner's phone when spike detected (>20% increase in 5s)
// isAvailable(): Boolean — Health Connect installed and permissions granted
```

- [ ] **Step 2: Create HeartRateGlow composable**

```kotlin
// Animated radial gradient that pulses with heart rate
// Input: heartRate: Int, baseColor: Color
// Pulse speed = heartRate BPM (60bpm = 1 pulse/sec)
// Color interpolation: 60-80bpm → warm orange, 80-100 → orange-red, 100-120 → deep red, 120+ → bright red with glow
// Size oscillates with each beat
// Uses Canvas + animateFloatAsState
```

- [ ] **Step 3: Create HeartRateScreen**

Side-by-side display:
- Left: "You" label + HeartRateGlow + BPM number
- Right: "Partner" label + HeartRateGlow + BPM number
- Center: sync indicator (both glows pulse together when BPMs are close)
- Bottom: "Feeling it" text when spike detected
- If no smartwatch: shows "Connect a smartwatch to see heart rate" with setup link

- [ ] **Step 4: Integrate heart rate into SessionScreen**

Add optional HeartRateGlow overlay to session screen (small, corner position). Tapping expands to full HeartRateScreen.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/heartrate/
git commit -m "feat: add heart rate visualization with pulsing glow and partner sync"
```

---

### Task 22: Couple Challenges

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/challenge/ChallengeViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/challenge/ChallengeScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/challenge/TimerComponent.kt`

- [ ] **Step 1: Create ChallengeViewModel**

```kotlin
// State: challenge (current), role (Partner A/B instructions differ), timer, score
// Challenges loaded from content library (new content type: CHALLENGE)
// startChallenge(challengeId) → syncs start with partner via ConnectionManager
// completeStep() → records and syncs
// getPartnerProgress() → from sync messages
// timerTick: StateFlow<Int> — countdown seconds
// Scoring: points for speed, bonus for coordination
```

- [ ] **Step 2: Create TimerComponent**

Circular countdown timer composable. Ring that depletes clockwise. Number in center. Color shifts from green → amber → red as time runs out. Pulses at 10-second warning.

- [ ] **Step 3: Create ChallengeScreen**

Split layout:
- Top: Challenge name + TimerComponent
- Center: Partner-specific instructions (different text per partner, synced)
- Bottom: "Done" button when step complete
- Score overlay after completion
- Connection status (challenges require live sync)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/challenge/
git commit -m "feat: add synchronized couple challenges with timer and scoring"
```

---

### Task 23: Forbidden Vault

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/vault/VaultViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/vault/VaultScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/vault/VaultUnlockScreen.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/vault/VaultEncryption.kt`
- Create: `app/src/main/java/com/igniteai/app/data/dao/VaultDao.kt`
- Create: `app/src/main/java/com/igniteai/app/data/repository/VaultRepository.kt`

- [ ] **Step 1: Implement VaultEncryption**

```kotlin
// Separate encryption layer from main app encryption
// Uses its own AES-256 key stored in Android Keystore (different alias)
// encrypt(content: String): ByteArray
// decrypt(ciphertext: ByteArray): String
// deleteKey() — for panic wipe
```

- [ ] **Step 2: Create VaultDao and VaultRepository**

```kotlin
// VaultDao: CRUD for VaultItem (id, type, encryptedContent, createdBy, createdAt)
// VaultRepository:
//   addDare(text: String, partnerId: String) — custom dare, encrypted
//   addVoiceNote(audioBytes: ByteArray, partnerId) — voice recording, encrypted
//   saveContent(contentItem: ContentItem) — save from session
//   getAll(): Flow<List<VaultItem>> — decrypted
//   delete(id: String)
```

- [ ] **Step 3: Create VaultUnlockScreen**

Dramatic dual-unlock UI:
- Dark screen with lock icon
- "Both partners must authenticate"
- Partner A authenticates → first lock opens (animation)
- Within 60 seconds, Partner B authenticates → second lock opens
- If timeout: "Authentication expired. Try again."
- On success: vault opens with dramatic animation (doors opening, ember burst)

- [ ] **Step 4: Create VaultScreen**

Grid of vault items:
- Custom dares (text cards)
- Voice notes (play button + waveform)
- Saved content (standard content cards)
- "Add" FAB → bottom sheet with options (Write dare, Record voice note)
- Voice recording: simple press-and-hold to record, release to save
- Items sorted by date, most recent first

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/vault/ app/src/main/java/com/igniteai/app/data/dao/VaultDao.kt app/src/main/java/com/igniteai/app/data/repository/VaultRepository.kt
git commit -m "feat: add forbidden vault with dual-unlock and separate encryption"
```

---

## Chunk 7: Settings, Polish & Distribution

### Task 24: Settings Screen

**Files:**
- Create: `app/src/main/java/com/igniteai/app/feature/settings/SettingsViewModel.kt`
- Create: `app/src/main/java/com/igniteai/app/feature/settings/SettingsScreen.kt`

- [ ] **Step 1: Create SettingsViewModel**

```kotlin
// All settings backed by AppPreferences (encrypted DataStore)
// Sections:
// Profile: partner name, re-take fantasy questionnaire
// Session: default time limit (slider 15-180), deny&delay frequency, deny&delay duration
// Notifications: daily dare time picker, enable/disable, notification text
// Audio: voice gender, TTS speed/pitch, soundscape volume, headphones-only toggle
// Haptics: intensity slider, enable/disable per type
// Pavlovian: enable/disable sound, enable/disable haptic, intensity (subtle/moderate/intense)
// Tone: preferred tone selector (playful/raw/sensual/adaptive)
// Privacy: decoy icon toggle + name selector, change PIN, panic wipe button
// Pairing: connection status, unpair button (requires biometric), device info
// Payment: unlock status, support contact
// Content: blocked content list (unblock option), reset preferences
```

- [ ] **Step 2: Create SettingsScreen**

Scrollable list of settings sections using Material 3 components:
- Switches for toggles
- Sliders for intensity/volume/time
- Dropdown selectors
- Time picker for notifications
- Dangerous actions (unpair, panic wipe) require biometric confirmation
- Fire-themed section headers

- [ ] **Step 3: Implement decoy icon switching**

```kotlin
// Uses activity-alias in AndroidManifest.xml
// Define aliases: .MainActivityDefault (fire icon), .MainActivityCalc (calculator icon), .MainActivityNotes (notes icon)
// Enable/disable aliases via PackageManager.setComponentEnabledSetting
// When switching: disable current alias, enable new one
// App name changes via alias label
```

Add to AndroidManifest.xml:
```xml
<activity-alias android:name=".MainActivityCalc"
    android:targetActivity=".MainActivity"
    android:label="@string/app_name_decoy"
    android:icon="@mipmap/ic_calculator"
    android:enabled="false" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity-alias>
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/settings/ app/src/main/AndroidManifest.xml
git commit -m "feat: add settings screen with all configuration options and decoy icon"
```

---

### Task 25: App Lock & Auth Gate

**Files:**
- Modify: `app/src/main/java/com/igniteai/app/MainActivity.kt`
- Modify: `app/src/main/java/com/igniteai/app/ui/navigation/NavGraph.kt`

- [ ] **Step 1: Add biometric gate on app launch**

```kotlin
// In MainActivity or NavGraph:
// On app open → check if couple profile exists
//   No → onboarding flow
//   Yes → biometric/PIN prompt
//     Success → Home
//     Failure → "Authentication Required" screen with retry
// Uses BiometricAuthManager
```

- [ ] **Step 2: Handle app backgrounding**

```kotlin
// When app goes to background (onStop) and returns (onStart):
// If away > 30 seconds → require re-authentication
// If away < 30 seconds → resume without auth
// Uses ProcessLifecycleOwner to detect app lifecycle
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/igniteai/app/MainActivity.kt app/src/main/java/com/igniteai/app/ui/navigation/
git commit -m "feat: add biometric app lock with background timeout"
```

---

### Task 26: (Moved to Task 6c — Content Library Loading)

---

### Task 27: Phone Call & Notification Handling

**Files:**
- Modify: `app/src/main/java/com/igniteai/app/feature/session/SessionViewModel.kt`

- [ ] **Step 1: Add phone call detection**

```kotlin
// Register TelephonyCallback (API 31+) or PhoneStateListener (API 28-30)
// On RINGING/OFFHOOK → pause session (emit SESSION_PAUSED state)
// On IDLE (call ended) → show "Session Paused" overlay with "Resume" button
// Audio stops, haptics stop, timer pauses
```

- [ ] **Step 2: Add DND mode for notifications during session**

```kotlin
// On session start: request NotificationManager DND access
// Set interruption filter to PRIORITY only (allows calls but suppresses other notifications)
// On session end: restore previous interruption filter
// If DND permission not granted: skip (notifications come through normally)
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/session/
git commit -m "feat: add phone call pause and notification muting during sessions"
```

---

### Task 28: Voice Safeword

**Files:**
- Modify: `app/src/main/java/com/igniteai/app/feature/session/SessionViewModel.kt`

- [ ] **Step 1: Implement voice safeword recognition**

```kotlin
// Uses Android SpeechRecognizer (on-device)
// Starts listening when session begins (if voice safeword enabled in settings)
// Custom safeword set during onboarding (default: "red")
// Continuous recognition in background during session
// On recognition with confidence >= 0.95:
//   If recognized word matches safeword → triggerSafeword()
//   Else → ignore
// If app audio playing: boost mic gain (system-level, best-effort)
// Language: RecognizerIntent.EXTRA_LANGUAGE = "en-US"
```

- [ ] **Step 2: Add safeword configuration to onboarding and settings**

During onboarding: "Choose a safeword" screen with default "red" and custom input.
In settings: ability to change safeword, test it, enable/disable.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/igniteai/app/feature/session/ app/src/main/java/com/igniteai/app/feature/settings/
git commit -m "feat: add voice-activated safeword with on-device speech recognition"
```

---

### Task 29: Integration Testing & End-to-End Flow

**Files:**
- Create: `app/src/androidTest/java/com/igniteai/app/feature/onboarding/OnboardingFlowTest.kt`

- [ ] **Step 1: Write E2E test for critical flow**

```kotlin
// Test 1: Fresh install → onboarding → partner setup → biometric → PIN → pairing screen displays QR
// Test 2: Home screen loads daily dare after onboarding complete
// Test 3: Session flow: consent gate → session starts → safeword → cool down
// Test 4: Content feedback: favorite → appears in engagement records
// Test 5: Settings: change notification time → preference persists
```

- [ ] **Step 2: Run instrumented tests**

Run: `./gradlew connectedAndroidTest`
Expected: All pass (may need emulator or real device).

- [ ] **Step 3: Commit**

```bash
git add app/src/androidTest/
git commit -m "test: add end-to-end integration tests for critical flows"
```

---

### Task 30: APK Build & Signing

- [ ] **Step 1: Generate signing key**

```bash
keytool -genkey -v -keystore igniteai-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias igniteai
```

Store keystore securely. Add to `.gitignore`.

- [ ] **Step 2: Configure release signing in build.gradle.kts**

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../igniteai-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "igniteai"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

- [ ] **Step 3: Build release APK**

Run: `./gradlew assembleRelease`
Expected: APK at `app/build/outputs/apk/release/app-release.apk`

- [ ] **Step 4: Verify APK size**

Run: `ls -lh app/build/outputs/apk/release/app-release.apk`
Expected: Under 250MB (likely ~30-50MB without audio assets, ~150-200MB with).

- [ ] **Step 5: Install and smoke test on device**

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

Verify: app installs, onboarding works, theme renders, biometric prompts.

- [ ] **Step 6: Commit**

```bash
git add app/build.gradle.kts .gitignore
git commit -m "feat: add release APK signing configuration"
```

---

## Summary

| Chunk | Tasks | What It Delivers |
|-------|-------|------------------|
| 1: Core Infrastructure | 1-6, 6b, 6c | Buildable project, theme, navigation, encrypted DB, security, sync protocol, pairing state, content loading |
| 2: Onboarding & Consent | 7-9 | Complete onboarding, pairing, fantasy profiling, consent/safety system |
| 3: Content Engine | 10-13 | Adaptive content, daily dares, streaks, content screens |
| 4: Audio & Haptics | 14-16 | Full audio engine, haptic patterns, Pavlovian conditioning |
| 5: Anticipation | 17 | Tease sequences, countdown locks, deny & delay |
| 6: Level 2 Features | 18-23 | Payment, scenarios, D/s control, heart rate, challenges, vault |
| 7: Polish & Distribution | 24-25, 27-30 | Settings, app lock, voice safeword, E2E tests, APK |

**Total: 32 tasks across 7 chunks** (including 6b and 6c).

Each chunk produces a working, testable increment. Chunks 1-3 give you a complete Level 1 (Spark) app. Chunks 4-5 add the psychological arousal features. Chunk 6 adds all Level 2 (Fire) features. Chunk 7 polishes and prepares for distribution.

**Content creation (non-code) runs in parallel:** While development progresses through Chunks 1-3, content writing should begin — dares, scenarios, text templates, fantasy questions, TTS scripts. Audio recording (voice actors) should be commissioned during Chunk 4.

**Testing note:** Chunks 1-3 are fully testable on emulator. Chunks 4+ require a real Android device for haptics, audio, and Bluetooth testing. Level 2 sync features (D/s control, challenges) require two paired devices.
