package com.iie.st10320489.marene.ui.rewards

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Reward
import java.util.UUID

// Adapter for displaying a list of expired or used rewards in a RecyclerView
class RewardExpiredAdapter (private val expiredList: MutableList<RewardHistoryItem>) :
    RecyclerView.Adapter<RewardExpiredAdapter.ExpiredViewHolder>()  {

    // ViewHolder class holds references to views for each item in the list
    inner class ExpiredViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.expiredImage) // Reward image
        val title: TextView = itemView.findViewById(R.id.expiredTitle) // Reward title
        val location: TextView = itemView.findViewById(R.id.expiredLocation) // Reward location
        val date: TextView = itemView.findViewById(R.id.expiredDate) // Date reward expired
        val status: TextView = itemView.findViewById(R.id.expiredStatus) // Status: "Used" or "Expired"
    }

    // Saves the expired or used reward to Firebase Firestore
    private fun saveExpiredClaimToFirestore(context: Context, item: RewardHistoryItem) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a Reward object to store in Firestore
        val reward = Reward(
            name = item.title,
            description = "Expired reward from ${item.location} claimed on ${item.dateClaimed}",
            amount = 0.0, // Amount is 0 for expired/used rewards
            type = "expired",
            // Generates a 6-digit numeric code (random)
            code = UUID.randomUUID().toString().takeLast(6).filter { it.isDigit() }
                .padEnd(6, '0').toIntOrNull() ?: 0 // (Viegen, 2022)
        )

        val firestore = FirebaseFirestore.getInstance()

        // Add the reward to the main rewards collection
        firestore.collection("rewards")
            .add(reward)
            .addOnSuccessListener { documentRef ->
                val rewardId = documentRef.id
                val uid = user.uid

                // Prepare a subdocument to track the user-specific claim
                val claimRecord = mapOf(
                    "rewardId" to rewardId,
                    "userId" to uid,
                    "name" to reward.name,
                    "location" to item.location,
                    "description" to reward.description,
                    "dateClaimed" to FieldValue.serverTimestamp()
                ) // (Viegen, 2022)

                // Save the claim under the user's subcollection inside the reward document
                firestore.collection("rewards")
                    .document(rewardId)
                    .collection(uid)
                    .add(claimRecord)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Expired/Used reward saved to Firebase.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to save expired/used reward details.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to create expired/used reward record.", Toast.LENGTH_SHORT).show()
            }
    } // (Viegen, 2022)

    // Inflate the layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpiredViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward_expired, parent, false)
        return ExpiredViewHolder(view)
    }

    // Bind the data to the views in the ViewHolder
    override fun onBindViewHolder(holder: ExpiredViewHolder, position: Int) {
        val item = expiredList[position]
        holder.image.setImageResource(item.imageResId)
        holder.title.text = item.title
        holder.location.text = "From: ${item.location}"
        holder.date.text = "Expired: ${item.dateClaimed}"
        holder.status.text = item.status
// Displays the item info

        // Save the expired reward to Firestore
        saveExpiredClaimToFirestore(holder.itemView.context, item)
    }


    // Return the total number of items
    override fun getItemCount(): Int = expiredList.size


    // Notify the adapter to refresh the entire list
    fun refreshData() {
        notifyDataSetChanged()
    } // (Viegen, 2022)


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
