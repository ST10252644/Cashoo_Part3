package com.iie.st10320489.marene.ui.rewards

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.iie.st10320489.marene.R
import java.util.UUID


class RewardsQrFragment : Fragment() {

    // Declare views for displaying reward info, QR code, and buttons
    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var qrImageView: ImageView
    private lateinit var confirmButton: Button
    private lateinit var voucherCodeTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment layout rewards_qr.xml
        val view = inflater.inflate(R.layout.rewards_qr, container, false)

        // Initialize views by finding them in the inflated layout
        imageView = view.findViewById(R.id.qrImage)
        titleTextView = view.findViewById(R.id.qrTitle)
        locationTextView = view.findViewById(R.id.qrLocation)
        dateTextView = view.findViewById(R.id.qrDate)
        qrImageView = view.findViewById(R.id.qrCodeImage)
        confirmButton = view.findViewById(R.id.qrConfirmButton)
        voucherCodeTextView = view.findViewById(R.id.qrVoucherCode)

        // Retrieve arguments passed to this fragment (e.g. from navigation)
        val args = arguments
        val imageResId = args?.getInt("imageResId") ?: 0
        val title = args?.getString("title") ?: ""
        val location = args?.getString("location") ?: ""
        val date = args?.getString("date") ?: ""

        // Set the image and text views with the received reward data
        imageView.setImageResource(imageResId)
        titleTextView.text = title
        locationTextView.text = "Location: $location"
        dateTextView.text = "Claimed: $date"

        // Generate a voucher code and display it in a TextView
        val code = generateVoucherCode()
        voucherCodeTextView.text = code

        // Generate a QR code bitmap from the voucher code and display it
        qrImageView.setImageBitmap(generateQRCodeBitmap(code))

        // Set up back button to navigate up in the navigation stack
        val backButton = view.findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Handle confirm button click
        confirmButton.setOnClickListener {
            Toast.makeText(requireContext(), "Reward confirmed", Toast.LENGTH_SHORT).show()

            // Attempt to find the matching reward in claimed rewards list by title and date
            val matchedItem = RewardHistoryStore.claimedRewards.find {
                it.title == title && it.dateClaimed == date
            }
            // If found, remove it from the claimed rewards list (history)
            matchedItem?.let {
                RewardHistoryStore.claimedRewards.remove(it)
            }

            // Create a new RewardHistoryItem with status "Used" and immediate expiry
            val confirmedItem = RewardHistoryItem(
                title = title,
                imageResId = imageResId,
                location = location, // (Viegen, 2022)
                dateClaimed = date,
                expiryTimestamp = 0L, // Set expiry to 0 to indicate immediate expiry
                status = "Used"
            )
            // Add this confirmed reward to expired rewards list
            RewardExpiredStore.expiredRewards.add(confirmedItem)
            // Navigate back up (exit this fragment)
            findNavController().navigateUp()
        }

        // Adjust padding of root view to respect system window insets (status bar, nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.qrmain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Return the inflated view to be displayed
        return view
    }

    // Generate a pseudo-random 11-digit voucher code by extracting digits from a UUID
    private fun generateVoucherCode(): String {
        val uuid = UUID.randomUUID().toString()
        // Filter only digits and take first 11 digits; pad with '0' if less than 11
        return uuid.filter { it.isDigit() }.take(11).padEnd(11, '0')
    }

    // Generate a simple QR code bitmap image displaying the voucher code as text
    private fun generateQRCodeBitmap(code: String): Bitmap {
        val width = 512
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        // (Viegen, 2022)
        // Prepare paint for drawing text
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 48f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }


        canvas.drawText("CODE:", width / 2f, height / 2f - 30, paint)
        // Draw the actual voucher code below label with larger text size
        paint.textSize = 60f
        canvas.drawText(code, width / 2f, height / 2f + 40, paint)

        // Return the generated bitmap
        return bitmap // (Viegen, 2022)
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
