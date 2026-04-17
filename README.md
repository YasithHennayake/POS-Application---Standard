This is a POS Sale Dummy Application built with Kotlin, MVVM, Hilt, Room, StateFlow, Material Design 3. Create a professional README.md for the GitHub repository.

Generate a README.md with:

1. PROJECT TITLE: POS Sale Dummy Application

2. BRIEF DESCRIPTION (2-3 lines):
    - What it is: Android POS terminal simulation app
    - Purpose: Assessment project demonstrating MVVM architecture, clean code, and Material Design 3
    - Clarify: No real POS hardware, SDK, or payment gateway integration

3. SCREENSHOTS SECTION:
    - Placeholder for 3 screenshots: Sale Screen, Processing State, Transaction Result
    - Use markdown image syntax with placeholder paths

4. FEATURES:
    - Sale and Refund transaction types
    - Cents-based right-to-left amount entry (real POS terminal pattern)
    - Simulated transaction processing with random approval/decline
    - Transaction history stored locally via Room DB
    - State-driven UI with processing, success, and failure states
    - User cancellation during processing
    - Material Design 3 blue and white theme
    - Forced light theme regardless of device settings

5. ARCHITECTURE:
    - MVVM with clean separation of concerns
    - Brief layer explanation: UI (Fragments) → ViewModel (StateFlow) → Repository → Room DB
    - Mention Hilt for DI
    - Mention coroutines for async simulation

6. TECH STACK (as a clean list):
    - Kotlin
    - MVVM Architecture
    - Hilt (Dependency Injection)
    - Room Database (Local Storage)
    - Kotlin Coroutines + StateFlow
    - Material Design 3
    - ViewBinding

7. PROJECT STRUCTURE:
    - Show the package structure: core/, data/, di/, repository/, requestmodel/, responsemodel/, ui/, utils/, viewmodels/
    - One line description per package

8. TRANSACTION FLOW:
    - Enter Amount → Select Type → CHARGE → Processing (2-3s delay) → Result (Approved/Declined)
    - 70% approval, 30% decline rate
    - Transaction ID format: TXN + timestamp + random digits

9. SETUP:
    - Clone repo
    - Open in Android Studio
    - Build and run (no API keys or backend needed)
    - Min SDK and target SDK info

10. BUILT FOR:
    - Assessment project evaluating MVVM understanding, architecture thinking, and code quality

Save as: README.md in the project root directory.