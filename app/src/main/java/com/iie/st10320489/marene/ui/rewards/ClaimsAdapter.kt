package com.iie.st10320489.marene.ui.rewards

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10320489.marene.R

// Adapter for displaying claimable rewards in a RecyclerView
class ClaimsAdapter(private var items: List<ClaimItem>) : RecyclerView.Adapter<ClaimsAdapter.ViewHolder>() {
    // (Viegen, 2022)
    // ViewHolder class holds references to views for each item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageClaim: ImageView = itemView.findViewById(R.id.ClaimImage)
        val claimTitle: TextView = itemView.findViewById(R.id.ClaimRewardTitle)
        val claimPoints: TextView = itemView.findViewById(R.id.ClaimRewardPoints)
        val claimButton: Button = itemView.findViewById(R.id.ClaimButton) // (Viegen, 2022)

        init {
            // Handle click event on the claim button
            claimButton.setOnClickListener {
                val claim = items[adapterPosition] // Get the clicked item based on adapter position

                // Create a bundle to pass reward data to the next fragment
                val bundle = Bundle().apply {
                    putInt("IMAGE_RES_ID", claim.clmImageResId)
                    putString("TITLE", claim.clmTitle)
                    putDouble("AMOUNT", claim.clmAmount.toDouble())
                    putString("LOCATION", claim.location)
                } // Adds all the item info

                // Navigate to the Claim Detail screen with the bundle
                itemView.findNavController().navigate(R.id.navigation_rewards_claimdetail, bundle)
            }
        }
    }

    // Inflate the layout for each reward item and return the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewClaim = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward_claim, parent, false)
        return ViewHolder(viewClaim)
    }

    // Bind reward data to each view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val claim = items[position]
        holder.imageClaim.setImageResource(claim.clmImageResId)
        holder.claimTitle.text = claim.clmTitle
        holder.claimPoints.text = "${claim.clmAmount} pts"
    } // sets all the item info

    // Returns the total number of reward items
    override fun getItemCount(): Int = items.size

    // Update the adapter's data and refresh the RecyclerView
    fun updateList(newItems: List<ClaimItem>) {
        items = newItems
        notifyDataSetChanged() // Notifys about data has changes
    }
} // (Viegen, 2022)


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