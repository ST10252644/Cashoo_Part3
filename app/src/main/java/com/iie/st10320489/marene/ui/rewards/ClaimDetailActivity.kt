package com.iie.st10320489.marene.ui.rewards


import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Reward
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ClaimDetailActivity : Fragment() { // (Code With Cal, 2025)

    private lateinit var imageClaimDetail: ImageView
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button
    private lateinit var voucherCodeTextView: TextView

    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView

    private var countDownTimer: CountDownTimer? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView: View = inflater.inflate(R.layout.activity_claim_detail, container, false)

        // Bind the views
        imageClaimDetail = rootView.findViewById(R.id.imageClaimDetail)
        confirmButton = rootView.findViewById(R.id.btnConfirm)
        cancelButton = rootView.findViewById(R.id.btnCancel)

        voucherCodeTextView = rootView.findViewById(R.id.voucherCodeTextView)

        titleTextView = rootView.findViewById(R.id.rewardTitleTextView)
        descriptionTextView = rootView.findViewById(R.id.rewardDescriptionTextView)


        // Get the data passed from the RewardsFragment
        val imageResId = arguments?.getInt("IMAGE_RES_ID") ?: 0
        val title = arguments?.getString("TITLE") ?: "Weekly Reward"
        val location = arguments?.getString("LOCATION") ?: "Unknown"
        val amount = arguments?.getDouble("AMOUNT") ?: 0.0

        // Set the image on the new page
        imageClaimDetail.setImageResource(imageResId)
        titleTextView.text = "$title\n- $location"


        // Generates a unique voucher code
        val voucherCode = generateVoucherCode()
        voucherCodeTextView.text = voucherCode


        // Sets a static description
        descriptionTextView.text =
            "Cost: $amount cashoos\nLocation: $location \n\n This voucher is valid for a reward of choice from the a location. This offer is valid for 7 days. To redeem this voucher, simply open the voucher in the “Rewards” section in the app under “Active”, open the voucher and scan the QR code or read the wiCode beneath the QR code to the cashier, prior to making payment. If unredeemed, it will expire..."



        // Setting the onClickListener for the buttons
        confirmButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // (Viegen, 2022)
            }

            val uid = user.uid
            val userRef = firestore.collection("users").document(uid)
            userRef.get().addOnSuccessListener { doc ->
                val currentBalance = doc.getDouble("cashoos") ?: 0.0
                if (currentBalance < amount) {
                    Toast.makeText(requireContext(), "Not enough Cashoos, Please have more to proceed", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.navigation_rewards)
                    return@addOnSuccessListener // (Viegen, 2022)
                }

                // Deducts the reward amount from the user's balance
                val newBalance = currentBalance - amount
                userRef.update("cashoos", newBalance)

                // Create reward document with ID
                val rewardDocRef = firestore.collection("rewards").document()
                val rewardId = rewardDocRef.id

// Create a Reward object to store in Firestore
                val reward = Reward(
                    rewardId = rewardId.hashCode(),
                    name = title,
                    description = "Claimed reward: $title at $location",
                    amount = amount,
                    type = "claim",
                    code = generateVoucherCode().takeLast(6).toIntOrNull() ?: 0
                )

// Create a RewardHistoryItem to track this reward in local store
                val historyItem = RewardHistoryItem(
                    imageResId = imageResId,
                    title = title,
                    location = location,
                    dateClaimed = Date().toString(),
                    expiryTimestamp = System.currentTimeMillis() + (60 * 1000),
                    status = "Expired"
                ) // (Viegen, 2022)

                // Call the static save function from RewardsFragment
                RewardsFragment.saveClaimedRewardToFirestore(
                    requireContext(),
                    rewardId,
                    reward,
                    historyItem
                ) { success ->
                    if (success) {
                        RewardHistoryStore.claimedRewards.add(historyItem)
                        Toast.makeText(requireContext(), "Reward Claimed", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.navigation_rewards)
                    } else {
                        Toast.makeText(requireContext(), "Failed to save reward", Toast.LENGTH_SHORT).show()
                    } // (Viegen, 2022)
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to check balance", Toast.LENGTH_SHORT).show()
            }
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


    // Utility function to generate a numeric voucher code
    private fun generateVoucherCode(): String {
        val uuid = UUID.randomUUID().toString()
        return uuid.filter { it.isDigit() }.take(11).padEnd(11, '0') // e.g., 53466663991
    }


    // Cancel any running timers when view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
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