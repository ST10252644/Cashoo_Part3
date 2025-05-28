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
    private lateinit var countdownTextView: TextView
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
        countdownTextView = rootView.findViewById(R.id.countdownTextView)
        titleTextView = rootView.findViewById(R.id.rewardTitleTextView)
        descriptionTextView = rootView.findViewById(R.id.rewardDescriptionTextView)


        // Get the data passed from the MainActivity2
        val imageResId = arguments?.getInt("IMAGE_RES_ID") ?: 0
        val title = arguments?.getString("TITLE") ?: "Weekly Reward"
        val location = arguments?.getString("LOCATION") ?: "Unknown"

        // Set the image on the new page
        imageClaimDetail.setImageResource(imageResId)
        titleTextView.text = "$title\n- $location"


        // Generate a unique voucher code
        val voucherCode = generateVoucherCode()
        voucherCodeTextView.text = voucherCode


        // Set static description
        descriptionTextView.text =
            "This voucher is valid for one small (350ml) regular smoothie or short (250ml) hot drink from Kauai. This offer is valid for 7 days only and is limited to one transaction per customer. This offer excludes all Kauai Protein and Superfood smoothies and Kauai Iced drinks. To redeem this voucher, simply open the voucher in the “Rewards” section in the app under “Active”, open the voucher and scan the QR code or read the wiCode beneath the QR code to the cashier, prior to making payment. If unredeemed, it will expire..."

        // Start 7-day countdown timer
        startCountdownTimer()

        // Setting the onClickListener for the buttons
        confirmButton.setOnClickListener {
            val claimedItem = RewardHistoryItem(
                title = arguments?.getString("TITLE") ?: "Unknown",
                imageResId = arguments?.getInt("IMAGE_RES_ID") ?: 0,
                location = arguments?.getString("LOCATION") ?: "Unknown",
                dateClaimed = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                expiryTimestamp = System.currentTimeMillis() + 60_000
            )

            RewardHistoryStore.claimedRewards.add(claimedItem) // If using singleton
            // or with ViewModel:
            // sharedViewModel.addReward(claimedItem)

            Toast.makeText(requireContext(), "Reward Claimed", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.navigation_rewards)
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

    private fun saveClaimToFirestore(title: String, description: String, location: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val reward = Reward(
            name = title,
            description = description,
            amount = 0.0,
            type = "claim",
            code = generateVoucherCode().takeLast(6).toIntOrNull() ?: 0
        )

        firestore.collection("rewards")
            .add(reward)
            .addOnSuccessListener { documentRef ->
                val rewardId = documentRef.id
                val uid = user.uid
                val claimRecord = mapOf(
                    "rewardId" to rewardId,
                    "userId" to uid,
                    "name" to reward.name,
                    "location" to location,
                    "description" to reward.description,
                    "dateClaimed" to FieldValue.serverTimestamp()
                )

                firestore.collection("rewards")
                    .document(rewardId)
                    .collection(uid)
                    .add(claimRecord)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Reward Claimed", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.navigation_rewards)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save claim details.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to create reward record.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateVoucherCode(): String {
        val uuid = UUID.randomUUID().toString()
        return uuid.filter { it.isDigit() }.take(11).padEnd(11, '0') // e.g., 53466663991
    }

    private fun startCountdownTimer() {
        val sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000 // 7 days in milliseconds

        countDownTimer = object : CountDownTimer(sevenDaysInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val days = millisUntilFinished / (1000 * 60 * 60 * 24)
                val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val seconds = (millisUntilFinished / 1000) % 60

                val formatted = String.format(
                    "Expires In %d:%02d:%02d:%02d",
                    days, hours, minutes, seconds
                )
                countdownTextView.text = formatted
            }

            override fun onFinish() {
                countdownTextView.text = "Expired"
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
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