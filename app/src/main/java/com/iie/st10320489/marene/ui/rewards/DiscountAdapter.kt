package com.iie.st10320489.marene.ui.rewards

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10320489.marene.R

class DiscountAdapter (private val discountList: List<Discount>
) : RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder>() { // (Code With Cal, 2025)

    class DiscountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val disLogo: ImageView = itemView.findViewById(R.id.discImg)
        val tvDiscName: TextView = itemView.findViewById(R.id.txtDiscName)
        val tvDiscountDetails: TextView = itemView.findViewById(R.id.tvDiscDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discount, parent, false)
        return DiscountViewHolder(view)
    } // (Code With Cal, 2025)

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        val discount = discountList[position]
        holder.tvDiscName.text = discount.disName
        holder.tvDiscountDetails.text = discount.details
        holder.disLogo.setImageResource(discount.disImageResId) // (Code With Cal, 2025)

        // Handle item click events to launch the detail activity
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("discount_image", discount.disImageResId)
            holder.itemView.findNavController().navigate(R.id.navigation_rewards_discountdetail, bundle)

        }
    }

    override fun getItemCount(): Int = discountList.size // (Code With Cal, 2025)
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