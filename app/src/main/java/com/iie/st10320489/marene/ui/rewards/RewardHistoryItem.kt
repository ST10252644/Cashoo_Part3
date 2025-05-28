package com.iie.st10320489.marene.ui.rewards



data class RewardHistoryItem(

    val rewardId: String = "",
    val userId: String = "",
    val title: String = "",
    val imageResId: Int = 0,
    val location: String = "",
    val dateClaimed: String = "",
    val expiryTimestamp: Long = System.currentTimeMillis() + 60_000,
    val status: String = "Expired"

)
