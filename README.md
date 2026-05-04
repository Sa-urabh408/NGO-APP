<div align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="NGO App Logo" width="150" />
</div>

<h1 align="center">Unity Care Foundation - NGO App 🤝</h1>

<div align="center">
  <strong>An intuitive and purpose-driven Android application to facilitate charitable donations, connect volunteers, and manage NGO activities.</strong>
</div>

<br />

## 🌟 Features

### 👤 User Features:
*   **Secure Authentication**: Firebase-backed email and password login system.
*   **Seamless Donations**: Donate easily using the in-built **Razorpay Simulator**. Choose amounts via quick-chips or custom slider.
*   **Donation History**: Track your past donations, messages, and timestamps from your personalized dashboard.
*   **User Profile**: Manage your account details and view your contribution status.
*   **Developer Info**: Beautiful developer contact section featuring vectorized Social Links (GitHub & Instagram).

### 🛡️ Admin Features:
*   **Admin Dashboard**: A comprehensive control panel to manage the app ecosystem.
*   **Global Donation Ledger**: Real-time view of all donations made by every user, tracking overall grand totals, donor names, and transparent timestamps.
*   **Messages & Gallery**: Oversee incoming messages and upload community images to the Gallery.
*   **Events & Volunteers**: Oversee upcoming NGO events and track volunteer signups.

---

## 📸 Screenshots
*Screenshots will be added soon!*

---

## 🛠️ Tech Stack & Technologies Used
*   **Language**: Java / XML
*   **Frontend UI/UX**: Android Studio (Vanilla XML, CardViews, Custom Drawables)
*   **Backend & DB**: Firebase (Authentication & Realtime Database)
*   **Payments Simulator**: Custom local bypass simulating successful transaction flow based on Razorpay integration.

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   JDK 8+
*   A Firebase Project

### Installation Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YourUsername/NGOApp.git
   ```

2. **Setup Firebase:**
   * Go to the [Firebase Console](https://console.firebase.google.com/).
   * Create a new project and register an Android app (`com.example.ngoapp`).
   * Download the `google-services.json` file.
   * Place the `google-services.json` file inside the `app/` folder of this repository. **Note: This file is intentionally excluded from Git via `.gitignore` for security reasons.**
   * Enable **Authentication** (Email/Password) and **Realtime Database** in your Firebase console.

3. **Build & Run:**
   * Open the project in Android Studio.
   * Sync Project with Gradle Files.
   * Run the app on an Android Emulator or a Physical testing device.

---

## 🔒 Security Note
* The `google-services.json` file has been added to `.gitignore`. Never upload your production Firebase keys or Razorpay API keys to public source control!

---

<div align="center">
  <i>Developed with ❤️ for a better tomorrow.</i>
</div>
