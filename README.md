# ZQore Mutual Fund App - Android

![App Logo Placeholder](https://via.placeholder.com/150)

## Table of Contents
- [About](#about)
- [Features](#features)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Setup & Installation](#setup--installation)
- [Usage](#usage)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## About

This is the Android mobile application for the ZQore Mutual Fund platform. It allows users to view their mutual fund holdings, track portfolio performance, and access detailed information about individual funds. This application is part of a larger ecosystem that includes iOS and Spring Boot microservices.

## Features

Currently implemented features:
- **Dashboard Overview:** Displays a comprehensive summary of the user's mutual fund portfolio, including total invested value, current value, and overall gain/loss.
- **Fund Holdings List:** Presents a scrollable list of individual mutual fund holdings with their current value, units, and real-time gain/loss.
- **Dynamic UI States:** Handles and displays different UI states including:
    - **Loading Indicator:** Shows a progress bar while data is being fetched.
    - **Error Messages:** Displays user-friendly error messages if data fetching fails.
    - **Empty State:** Informs the user when there are no holdings to display.
- **Data Refresh Mechanisms:**
    - **Pull-to-Refresh:** Allows users to refresh holdings by pulling down on the list.
    - **Floating Action Button (FAB) Refresh:** Provides an explicit button for refreshing data.
- **Fund Detail View:** Provides detailed information for individual mutual fund holdings (ISIN, Current Value, Units, NAVs, Last Updated).
- **Bottom Navigation:** Seamless navigation between Dashboard, Portfolio (placeholder), Transactions (placeholder), and Menu (placeholder).

Planned features include:
- User Authentication (Login/Logout)
- Real-time NAV updates
- Transaction history
- Portfolio performance charts
- Investment/Redemption functionalities

## Architecture

The application is built following modern Android development best practices.
- **UI:** Traditional XML layouts.
- **Navigation:** Android Jetpack Navigation Component for managing in-app navigation.
- **Data Handling:** Designed to interact with RESTful APIs from backend microservices, utilizing a `Results` sealed class for robust state management (Success, Error, Loading) of asynchronous operations.
- **MVVM (Model-View-ViewModel):** For clean separation of concerns and testability.

## Technologies Used

- **Kotlin:** Primary programming language.
- **Android Jetpack:**
    - **Navigation Component:** For app navigation.
    - **ViewModel:** For UI-related data handling and business logic.
    - **LiveData:** For observable data holders, enabling reactive UI updates.
    - **View Binding:** For safe and convenient interaction with UI components.
    - **ListAdapter & DiffUtil:** For efficient updates of RecyclerView lists.
- **Material Design 3:** For modern UI components.
- **Retrofit / Ktor Client:** (Placeholder for future network calls)
- **Coroutines:** For asynchronous programming and managing long-running tasks.
- **Git:** Version control.
- **GitHub:** Repository hosting (zeroqoremf organization).

## Setup & Installation

To get a local copy up and running, follow these simple steps.

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/zeroqoremf/ZQoreMutualFundApp-Android.git](https://github.com/zeroqoremf/ZQoreMutualFundApp-Android.git)
    ```
2.  **Open in Android Studio:**
    * Launch Android Studio.
    * Select `Open an existing Android Studio project`.
    * Navigate to the cloned `ZQoreMutualFundApp-Android` directory and select it.
3.  **Sync Gradle:** Allow Android Studio to sync Gradle dependencies.
4.  **Run on Emulator/Device:**
    * Select a virtual device (emulator) or connect a physical Android device.
    * Click the `Run 'app'` button (green play icon) in the toolbar.

## Usage

* Upon launching the app, you will be presented with a Dashboard displaying a summary of mutual fund holdings and a list of individual holdings.
* Observe the loading indicator during data fetches, and potential error/empty messages if data is unavailable.
* Refresh the data using the pull-to-refresh gesture or by tapping the floating action button.
* Tap on any fund holding to view its detailed information on the Fund Detail screen.
* Use the bottom navigation bar to switch between Dashboard, Portfolio, Transactions, and Menu. (Note: Portfolio, Transactions, and Menu currently display placeholder content).

## Roadmap

* Implement secure Login/User Authentication.
* Integrate with backend microservices for real-time data.
* Develop full functionality for Portfolio, Transactions, and Menu sections.
* Add data persistence.
* Implement unit and integration tests.

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information. (You can add a LICENSE file later).

## Contact

Ashish K - ashishk@zeroqore.com

Project Link: [https://github.com/zeroqoremf/ZQoreMutualFundApp-Android](https://github.com/zeroqoremf/ZQoreMutualFundApp-Android)
