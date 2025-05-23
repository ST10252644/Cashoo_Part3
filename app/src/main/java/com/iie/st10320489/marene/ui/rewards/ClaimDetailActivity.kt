package com.iie.st10320489.marene.ui.rewards


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iie.st10320489.marene.R

class ClaimDetailActivity : Fragment() { // (Code With Cal, 2025)

    private lateinit var imageClaimDetail: ImageView
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView: View = inflater.inflate(R.layout.activity_claim_detail, container, false)

        // Bind the views
        imageClaimDetail = rootView.findViewById(R.id.imageClaimDetail)
        confirmButton = rootView.findViewById(R.id.btnConfirm)
        cancelButton = rootView.findViewById(R.id.btnCancel)

        // Get the data passed from the MainActivity2
        val imageResId = arguments?.getInt("IMAGE_RES_ID") ?: 0

        // Set the image on the new page
        imageClaimDetail.setImageResource(imageResId)

        // Setting the onClickListener for the buttons
        confirmButton.setOnClickListener {
            Toast.makeText(requireContext(), "Reward Claimed", Toast.LENGTH_SHORT).show()
        }
        // (Code With Cal, 2025)

        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            // Close the fragment page
        }


        ViewCompat.setOnApplyWindowInsetsListener(rootView.findViewById(R.id.mainClaim)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return rootView
    }
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