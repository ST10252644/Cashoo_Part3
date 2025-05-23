package com.iie.st10320489.marene.ui.rewards

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10320489.marene.R

// Adapter class for a RecyclerView that displays a list of RewardItem objects
class RewardsAdapter (private val rewardList: List<RewardItem>) :
    RecyclerView.Adapter<RewardsAdapter.RewardViewHolder>() { // (Code With Cal, 2025)


    // ViewHolder class that holds the views for each reward item in the RecyclerView
    inner class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageReward: ImageView = itemView.findViewById(R.id.ImageReward)
        val textTitle: TextView = itemView.findViewById(R.id.textRewardTitle)
        val textPoints: TextView = itemView.findViewById(R.id.textRewardPoints)
    }
    // Called when RecyclerView needs a new ViewHolder of the given type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward, parent, false)
        return RewardViewHolder(view)
    }

    // Called by RecyclerView to display data at the specified position
    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewardList[position]
        holder.imageReward.setImageResource(reward.imageResId)
        holder.textTitle.text = reward.title
        holder.textPoints.text = reward.points
    }

    override fun getItemCount(): Int = rewardList.size
    // (Code With Cal, 2025)
}

//Reference List:
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