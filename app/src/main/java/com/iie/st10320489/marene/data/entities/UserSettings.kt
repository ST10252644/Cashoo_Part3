package com.iie.st10320489.marene.data.entities

data class UserSettings(
    val userSettingsId: Int = 0, // Unique ID (can still use this locally or set manually)
    val userId: Int = 0, // Foreign key referencing the User entity
    val payday: String = "", // String representing the user's payday
    val salary: Double = 0.0, // User's salary
    val minGoal: Double = 0.0, // User's minimum savings goal
    val maxGoal: Double = 0.0, // User's maximum savings goal
    var color: String = "", // A color preference (could be used for UI customization)
    var chinchilla: String = "" // An arbitrary string field, possibly a user preference or setting
) // (Android Knowledge, 2024)

//Reference List:
// Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
