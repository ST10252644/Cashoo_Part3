package com.iie.st10320489.marene.ui.rewards

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings.Global.putInt
import android.provider.Settings.Global.putString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Reward
import java.util.UUID



// Adapter to bind a list of RewardHistoryItem objects to a RecyclerView
class RewardHistoryAdapter (private val historyList: MutableList<RewardHistoryItem>) :
    RecyclerView.Adapter<RewardHistoryAdapter.HistoryViewHolder>() { // (Viegen, 2022)

    // ViewHolder class to hold references to item views for reuse
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.historyImage)
        val title: TextView = itemView.findViewById(R.id.historyTitle)
        val location: TextView = itemView.findViewById(R.id.historyLocation)
        val date: TextView = itemView.findViewById(R.id.historyDate)
        val status: TextView = itemView.findViewById(R.id.historyStatus)
        val countdown: TextView = itemView.findViewById(R.id.historyCountdown)
        var timer: CountDownTimer? = null // Timer to track countdown for expiry
    }

    // Saves the claim to Firebase Firestore
    private fun saveClaimToFirestore(context: Context, item: RewardHistoryItem) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create Reward object with randomized 6-digit numeric code
        val reward = Reward(
            name = item.title,
            description = "Claimed reward from ${item.location} on ${item.dateClaimed}",
            amount = 0.0,
            type = "claim",
            code = UUID.randomUUID().toString().takeLast(6).filter { it.isDigit() }
                .padEnd(6, '0').toIntOrNull() ?: 0
        )
// (Viegen, 2022)
        val firestore = FirebaseFirestore.getInstance()

        // Add reward to Firestore under "rewards" collection
        firestore.collection("rewards")
            .add(reward)
            .addOnSuccessListener { documentRef ->
                val rewardId = documentRef.id
                val uid = user.uid

                // Create user-specific claim record inside the reward document
                val claimRecord = mapOf(
                    "rewardId" to rewardId,
                    "userId" to uid,
                    "name" to reward.name,
                    "location" to item.location,
                    "description" to reward.description,
                    "dateClaimed" to FieldValue.serverTimestamp()
                )// (Viegen, 2022)

                firestore.collection("rewards")
                    .document(rewardId)
                    .collection(uid)
                    .add(claimRecord)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Reward claim saved.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to save claim details.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to create reward record.", Toast.LENGTH_SHORT).show()
            }
    }

    // Inflate item layout and return a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward_history, parent, false)
        return HistoryViewHolder(view)
    }

    // Bind each RewardHistoryItem to its ViewHolder
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.image.setImageResource(item.imageResId)
        holder.title.text = item.title
        holder.location.text = "Location: ${item.location}"
        holder.date.text = "Claimed: ${item.dateClaimed}"
        holder.status.text = "Status: Active"

        // Cancel existing timer to avoid overlap when recycled
        holder.timer?.cancel()

        // Compute time left before reward expires
        val timeRemaining = item.expiryTimestamp - System.currentTimeMillis()

        if (timeRemaining > 0) {
            // Start new countdown timer
            holder.timer = object : CountDownTimer(timeRemaining, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutes = (millisUntilFinished / 1000) / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    holder.countdown.text = String.format("%02d:%02d", minutes, seconds)
                }
                // (Viegen, 2022)
                override fun onFinish() {
                    holder.countdown.text = "00:00"
                    holder.status.text = "Expired"

                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos < historyList.size) {
                        val expiredItem = historyList[pos]

                        // Remove expired item from list and add it to RewardExpiredStore
                        (holder.itemView.context as? android.app.Activity)?.runOnUiThread {
                            historyList.removeAt(pos)
                            notifyItemRemoved(pos)
                            RewardExpiredStore.expiredRewards.add(expiredItem)
                        }
                    } // (Viegen, 2022)
                }
            }.start()
        } else {
            holder.countdown.text = "00:00"
            holder.status.text = "Expired"
        }

        // Handle item click: save to Firestore and navigate to QR page
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val bundle = Bundle().apply {
                putInt("imageResId", item.imageResId)
                putString("title", item.title)
                putString("location", item.location)
                putString("date", item.dateClaimed)
            }

            saveClaimToFirestore(context, item)

            val navController = Navigation.findNavController(holder.itemView)
            navController.navigate(R.id.navigation_rewards_qr, bundle)
        }
        // (Viegen, 2022)
    }



    // Cancel timer when a ViewHolder is recycled to prevent leaks
    override fun onViewRecycled(holder: HistoryViewHolder) {
        super.onViewRecycled(holder)
        holder.timer?.cancel()
    }


    // Return total number of items in history list
    override fun getItemCount(): Int = historyList.size
}

//Reference List

//Raikwar, A., 2023. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://developer.android.com/develop#core-areas
//[Accessed 28 April 2025].
//Cal, C. W., 2023. Room Database Android Studio Kotlin Example Tutorial. [Online] Available at: hRps://youtu.be/-LNg-K7SncM?si=y8cbMdvhhp48Pp9-
//[Accessed 27 April 2025].
//College, I. V., 2025. PROG7313 Module-Manual / Module-Outline. Pretoria: Varsity College Pretoria.
//Viegen, F. v., 2022. A PracKcal introducKon to Android Room-3 : EnKty, Dao and Database objects.. [Online]
//Available at: hRps://youtu.be/RstQg7f4Edk?si=8RoAGp-OKPpMNVdY
//[Accessed 28 April 2025].
//androidbyexample, 2024. EnKKes ,Dao and Database -Android By Example. [Online] Available at: hRps://androidbyexample.com/modules/movie-db/STEP-050_Repo.html [Accessed 25 April 2025].
//AndroidDevelopers, 2023. Layouts in Views. [Online]
//Available at: hRps://developer.android.com/developer/ui/views/layout/declaring-layout [Accessed 23 April 2025].
//Kay, R. M., 2022. IntroducKon To Development WithAndroid Studio: XML The Five Minute Language. [Online]
//Available at: hRps://youtu.be/94tm21PIBMs?si=BpJQ9meXr1_ynL2m
//[Accessed 15 April 2025].
//Angga Risky. 2017. Rewards UI Design to Android XML Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=fjXMx_iLkTY [Accessed on 10 April 2025]
//GeeksforGeeks. 2025. Android UI Layouts. [online]. Available at: https://www.geeksforgeeks.org/android-ui-layouts/ [Accessed on 10 April 2025]
//Muhammadumarch. 2023. Implementing Navigation in Your Android App with Android Navigation Component. [online]. Available at: https://medium.com/@muhammadumarch321/implementing-navigation-in-your-android-app-with-android-navigation-component-ff22a3d300a [Accessed on 11 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2022. RecyclerView in Android Studio using Kotlin | Source Code | 2024. [online]. Available at: https://www.youtube.com/watch?v=IYhmpUmeGOQ [Accessed on 12 April 2025]
//Android Developers. 2025. Add an Image composition. [online]. Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-add-images#2 [Accessed on 9 April 2025]
//StackOverflow. 2021. Trying to create a simple recyclerView in Kotlin, but the adapter is not applying properly. [online]. Available at: https://stackoverflow.com/questions/43012903/trying-to-create-a-simple-recyclerview-in-kotlin-but-the-adapter-is-not-applyin [Accessed on 10 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]

