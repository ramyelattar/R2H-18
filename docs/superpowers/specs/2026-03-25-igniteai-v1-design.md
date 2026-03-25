# IgniteAI V1.0 — Design Specification

## Overview

IgniteAI is a native Android couples intimacy app distributed via direct APK download. V1.0 ships with two tiers: **Spark (free)** and **Fire (paid, one-time $29 unlock)**. The app uses a curated content library with adaptive tone, hybrid audio (pre-recorded + on-device TTS), dual biometric consent, peer-to-peer couple sync, and a bold fire/ember visual identity. Fully private — zero cloud, zero servers, zero data collection.

**Target:** Married couples (marketed, not enforced)
**Platform:** Android (Kotlin + Jetpack Compose)
**Distribution:** Direct APK via website
**Monetization:** One-time unlock per level ($29 for Fire)
**Privacy model:** 100% on-device, no internet required except Stripe payment

---

## Architecture

```
┌─────────────────────────────────────┐
│           IgniteAI App              │
├─────────────────────────────────────┤
│  UI Layer (Jetpack Compose)         │
│  — Screens, navigation, themes      │
├─────────────────────────────────────┤
│  Feature Modules                    │
│  — Onboarding & Pairing            │
│  — Consent Engine                   │
│  — Content Engine                   │
│  — Anticipation Engine              │
│  — Fantasy Profiling                │
│  — Audio Engine (binaural + TTS)    │
│  — Haptic Engine                    │
│  — Pavlovian Conditioning           │
│  — Couple Sync                      │
│  — D/s Control Transfer             │
│  — Forbidden Vault                  │
│  — Biofeedback / Heart Rate (L2)   │
├─────────────────────────────────────┤
│  Core Layer                         │
│  — Local Database (Room + SQLCipher)│
│  — Preference Learning              │
│  — Payment (Stripe)                 │
│  — Security & Encryption            │
└─────────────────────────────────────┘
```

All data stays on-device. Couple sync uses Bluetooth LE / WiFi Direct (no internet). Only network call is Stripe for payment.

---

## Module Specifications

### 1. Consent & Safety System

**Dual Biometric Consent:**
- Both partners authenticate via fingerprint or face unlock before any session
- Uses Android BiometricPrompt API
- Consent is per-session, not one-time
- Consent records stored locally with timestamps

**Safeword System:**
- Floating emergency stop button always visible during active sessions
- Either partner can tap — instantly stops all audio, haptics, content
- Optional voice-activated safeword via on-device speech recognition
  - Couple sets a custom safeword during onboarding (default: "red")
  - Minimum 95% confidence threshold before triggering
  - If app audio is playing, mic sensitivity is boosted to compensate
  - English only for V1.0; multilingual in future updates
  - False positive = session pauses (safe default); couple resumes with one tap
- Post-safeword: cool-down screen with gentle messaging

**Session Boundaries:**
- Default time limit: 60 minutes (configurable: 15–180 min)
- Halfway check-in requiring both partners to confirm continuation
  - If one partner doesn't respond within 30 seconds, session auto-pauses
  - Waiting partner sees: "Waiting for [partner name] to check in..."
  - If no response within 2 minutes, session ends gracefully (safety-first)
- At 90% of time limit: gentle warning notification
- At 100%: session ends with cool-down screen
- If partners set different time limits, the shorter one applies

**Biometric Fallback:**
- If biometric sensor is unavailable (no sensor, too many failures, hardware error), fallback to 6-digit PIN
- PIN set during onboarding alongside biometric enrollment
- PIN fallback applies to all biometric gates: app unlock, session consent, vault access, role swap

### 2. Couple Pairing & Security

**Pairing Methods:**
- QR code scanning (in-person, most secure)
- 6-digit invite code (works remotely)

**Communication:**
- Bluetooth Low Energy for same-room sync (primary)
- WiFi Direct as fallback (auto-switch if BLE latency exceeds 500ms)
- All communication end-to-end encrypted
- No server, no relay — direct peer-to-peer only
- Target latency: <200ms for control transfer commands
- If both BLE and WiFi Direct fail for >5 seconds: session pauses with "Connection Lost" screen; auto-resumes on reconnect
- Reconnection timeout: 60 seconds; after that, session ends gracefully

**Pairing Recovery & Device Management:**
- If one partner uninstalls/resets: other partner can trigger "Unpair" in Settings (requires biometric)
- Re-pairing to a new device: standard QR/invite flow; old pairing is invalidated automatically
- If pairing key is corrupted: app detects on launch and prompts re-pairing
- No data is synced between devices — each device holds its own copy. Re-pairing starts fresh.

**Data Security:**
- All local data encrypted with AES-256 via Android Keystore
- Biometric (or PIN fallback) required to open the app
- Decoy icon/name option (disguise as calculator/notes app)
- Panic wipe:
  - Triggered by triple-tap on the app logo on the lock screen
  - Confirmation step: "Erase all data? This cannot be undone." (5-second hold to confirm)
  - Deletes: Room database, audio cache, vault contents, preference files, encryption keys
  - Does NOT delete: Stripe transaction history (held by Stripe, not the app)
  - Post-wipe: app resets to fresh install state
  - Note: Android file deletion is not forensic-grade; for guaranteed erasure, recommend device factory reset

### 3. Content Engine

**Library Structure:**
- 300+ dares (flirty, spicy, intimate, intense)
- 50+ branching roleplay scenarios (Level 2)
- 500+ text templates (teasing, commanding, romantic)
- 20-30 pre-recorded audio clips + 200+ TTS scripts

**Adaptive Tone System:**
Content tagged with:
- Intensity level (1-10)
- Tone (playful / raw / sensual)
- Category (dare / scenario / text / audio)
- Duration (quick / medium / extended)

App tracks engagement, skips, and favorites to personalize content selection. Three tone modes:
- Playful & teasing (default for Level 1)
- Raw & dominant
- Sensual & intimate

**Adaptive Algorithm:**
- Engagement score = (favorites x 3) + (completions x 1) - (skips x 2)
- Content ranked by score; top 60% served regularly, bottom 20% rotated for exploration
- Minimum diversity rule: all 3 tones appear at least once per week regardless of preferences
- Each partner can set a personal tone preference in Settings; if preferences conflict, content suitable for both is prioritized
- Users can "block" specific content (separate from skip); blocked items never appear again
- Preferences recalculated weekly; manual reset available in Settings

Adapts based on couple preferences and current level.

**Daily Engine:**
- Daily Dare tailored to couple preferences
- Discreet push notification to both partners (default 8pm, customizable per partner)
- Either partner can snooze/skip daily dares; opt-out available in Settings
- Heat streak system:
  - Intensity increases by 1 point per consecutive day (base intensity + streak bonus)
  - Maximum streak bonus: +5 (caps at day 5, maintains after)
  - Streak resets if both partners miss a full 24-hour window
  - Individual skip does not break streak — only mutual inactivity does

### 4. Anticipation Engine

**Timed Tease Sequences:**
- Escalating messages throughout the day (mild morning → explicit evening)
- Configurable schedule and intensity curve
- Both partners receive coordinated but different messages

**Countdown Lock:**
- Scenarios revealed but locked behind configurable countdown (2-8 hours)
- Both partners can see what's coming but cannot access early
- Preview snippets increase desire during the wait

**Deny & Delay:**
- During sessions, deliberate pauses at peak moments
- Screen dims, audio fades, haptics freeze for 5-15 seconds
- Resumes at higher intensity
- Frequency and duration configurable

### 5. Fantasy Profiling

**Onboarding Questionnaire:**
- Each partner privately completes a detailed fantasy/preference survey
- Raw answers are never shown to the other partner
- Categories: dominance/submission, locations, scenarios, intensity, boundaries

**Overlap Matching:**
- Algorithm identifies shared desires between partners
- Content engine prioritizes overlapping fantasies
- Surfaces matched fantasies naturally through dares and scenarios
- Neither partner knows which specific answers matched

**Continuous Learning:**
- Tracks replayed, skipped, and favorited content
- Adjusts fantasy profile over time without re-surveying
- Expands boundaries gradually based on mutual engagement patterns

### 6. Audio Engine

**Pre-recorded Audio:**
- 20-30 high-impact clips from professional voice actors (male + female)
- Signature phrases, key scenario moments, onboarding
- Compressed storage (~50-100MB)

**On-device TTS:**
- Android TextToSpeech API for V1.0
- Future upgrade path: Piper TTS for neural voice quality
- Voice gender selection, speed/pitch adjustment

**Binaural Whisper Tracks:**
- Stereo audio with different content per ear (headphones required)
- One ear: commands. Other ear: praise/moans
- Creates immersive "surrounded" sensation

**Breath-paced Audio:**
- User taps screen to set their breathing tempo (tap on inhale, tap on exhale)
- Audio voice syncs to the tapped rhythm — breathes with the user
- Creates subconscious physiological entrainment
- Fallback: if no taps detected, audio uses a default slow breathing pace (4 seconds in, 6 seconds out)
- Note: mic-based breath detection deferred to Level 2+ (technically unreliable on phone mics)

**Layered Soundscapes:**
- Ambient layer under voice: heartbeats, skin sounds, breathing
- Triggers subconscious arousal responses
- Volume and mix adjustable

### 7. Haptic Engine

- Rich haptic patterns via Android VibrationEffect API
- Synced to audio content (whisper = slow pulse, command = sharp burst)
- Signature haptic pattern for Pavlovian conditioning
- Intensity levels configurable per partner
- Custom patterns for different content types

### 8. Pavlovian Conditioning System

**Signature Sound:**
- Unique app chime played during every arousing moment
- Over weeks of use, the sound alone triggers anticipation/arousal
- Partners can send the sound to each other via the app during the day
- Sound is distinct and not used anywhere else in the app

**Signature Haptic:**
- Specific vibration pattern paired with arousal moments
- Same conditioning principle — vibration alone triggers response over time
- Can be triggered remotely by partner

**Design Principle:**
- Conditioning builds gradually over 2-4 weeks of regular use
- The longer the couple uses the app, the more powerful these triggers become
- Creates strong positive habit formation and deep engagement over time
- Users can disable signature sound/haptic independently in Settings
- Conditioning frequency is adjustable (subtle / moderate / intense)

### 9. Heart Rate Visualization (Level 2)

**Data Source:**
- Wear OS smartwatch or fitness band via Health Connect API
- Optional — all features work without smartwatch

**Visualization:**
- Partner's heart rate shown as pulsing glow on screen
- Color shifts: warm orange → deep red as heart rate climbs
- Heart rate spike triggers haptic burst on partner's phone

**Mutual Display:**
- Both heart rates shown side by side during sessions
- Visual feedback loop: seeing partner's arousal increases own arousal
- Synced climbing rates create shared intensity

### 10. D/s Control Transfer (Level 2)

**Controller Mode:**
- One partner's phone becomes the remote control
- Can trigger audio, haptics, and commands on receiver's phone
- Chooses what receiver sees, hears, and feels
- Real-time control via Bluetooth/WiFi Direct

**Receiver Mode:**
- Screen shows only what controller allows
- Can be: instructions, countdown, darkness, or surprise content
- No ability to advance or change content — controller decides

**Role Swap:**
- One-tap role reversal
- Both partners must biometrically confirm the swap
- Roles can switch multiple times per session

### 11. Forbidden Vault

**Access:**
- Requires biometric authentication from both partners within 60 seconds of each other
- Works locally (both phones in BLE/WiFi Direct range) — not available remotely
- If biometric fails, PIN fallback applies
- Dual-unlock creates ritual feeling — a deliberate act of vulnerability

**Content:**
- Saved intense content from sessions
- Custom dares written by/for each other
- Private voice notes between partners
- Escalates over time based on what couple adds

**Security:**
- Separate encryption layer from main app data
- Included in panic wipe

### 12. Branching Roleplay Scenarios (Level 2)

**Structure:**
- Pre-written scenarios with 3-5 decision points each
- Couple chooses direction at each branch
- Branches tagged by tone and intensity — path adapts to choices
- 50+ scenarios across: romantic, dominant, submissive, exploratory

**Synchronized Play:**
- Both partners see the scenario on their devices
- Decision points can alternate between partners
- Audio and haptics sync to story beats

### 13. Synchronized Couple Challenges (Level 2)

**Format:**
- Real-time challenges requiring both partners
- Coordinated but different instructions per partner
- Timer, scoring, playful competition elements
- Synced via Bluetooth — both phones show coordinated state

**Examples:**
- Timed dares with role assignments
- Competitive challenges with rewards
- Cooperative scenarios requiring coordination

---

## UI & Visual Identity

**Theme:** Dark with fire/neon — bold, intense, unapologetic
- Dark backgrounds (near-black)
- Glowing red, orange, and ember gradient accents
- Animated flame/pulse effects
- Fire particle system for transitions
- Pulsing ember glow on interactive elements

**Typography:** Modern, bold, clean sans-serif
**Icons:** Custom fire-themed icon set
**Animations:** Flame flickers, ember particles, heat shimmer effects

---

## Payment & Unlocking

- Stripe Payment Links for V1.0
- "Unlock Fire" button → Stripe checkout in browser → returns to app with deep link containing session token
- App verifies payment via Stripe API callback → stores license key locally
- If internet drops after payment but before verification: app retries every 30 seconds for up to 5 minutes
- "Payment Pending" screen shown during retry; after 5 min, shows "Contact support with transaction ID: [ID]"
- License key stored locally, tied to device
- Each device requires its own purchase (no cross-device sync)
- Stripe fee: ~2.9% + $0.30 per transaction (~$27.86 net per $29 sale)
- Support contact displayed in Settings for payment issues

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| Database | Room (encrypted with SQLCipher) |
| Audio | ExoPlayer + Android TTS |
| Haptics | Android VibrationEffect API |
| Biometrics | BiometricPrompt API |
| Couple Sync | Bluetooth LE + WiFi Direct |
| Heart Rate | Health Connect API |
| Payments | Stripe Payment Links |
| Security | Android Keystore + AES-256 |
| Build | Gradle + Kotlin DSL |
| Min SDK | API 28 (Android 9.0) |

---

## Out of Scope for V1.0

- VR integration (Level 3+)
- Haptic suit support (Level 3+)
- Generative AI / on-device LLM
- iOS version
- Server/backend infrastructure
- User accounts or cloud sync
- Video content
- Levels 3-6

---

## Content Requirements (Non-Code)

These must be created alongside development:
- 300+ written dares across 4 intensity tiers and 3 tones
- 50+ branching scenario scripts with 3-5 decision points each
- 500+ text message templates across 3 tones
- 200+ TTS audio scripts
- Fantasy profiling questionnaire (50-100 questions)
- 20-30 pre-recorded audio clips (requires voice actor hiring)
- Binaural audio tracks (requires audio engineering)
- Layered ambient soundscapes

---

## Edge Cases & Error Handling

**Phone call / notification during session:**
- During active sessions, notifications are muted but not blocked
- Incoming call: session auto-pauses, shows "Session Paused" overlay; resumes when call ends

**Audio without headphones:**
- On session start, check headphone connection if binaural content is queued
- No headphones: disable binaural tracks, fallback to mono audio; show optional "Headphones recommended" tip

**One partner loses device:**
- Other partner triggers "Unpair" in Settings (requires biometric/PIN)
- Old device's encryption keys are invalidated
- New device pairs via standard QR/invite flow

**Content offensive to one partner:**
- "Block" action (distinct from skip) permanently removes content item from rotation
- Blocks are personal and private — not shared with partner
- Fantasy profile re-survey available anytime in Settings

---

## Success Criteria

- App installs and runs fully offline after initial APK download
- Both partners can pair, authenticate, and sync within 2 minutes
- Safeword stops all activity within 500ms
- Daily dare notification arrives consistently
- Stripe payment completes and unlocks Level 2 reliably
- Same exact dare/scenario never appears two days in a row; full library rotation within 14 days
- App size under 250MB (including audio assets)
- Battery drain under 5% per hour of active use
