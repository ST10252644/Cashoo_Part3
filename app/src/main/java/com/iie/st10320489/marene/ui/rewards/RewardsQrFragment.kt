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
        val view = inflater.inflate(R.layout.rewards_qr, container, false)

        imageView = view.findViewById(R.id.qrImage)
        titleTextView = view.findViewById(R.id.qrTitle)
        locationTextView = view.findViewById(R.id.qrLocation)
        dateTextView = view.findViewById(R.id.qrDate)
        qrImageView = view.findViewById(R.id.qrCodeImage)
        confirmButton = view.findViewById(R.id.qrConfirmButton)
        voucherCodeTextView = view.findViewById(R.id.qrVoucherCode)

        // Get args from bundle
        val args = arguments
        val imageResId = args?.getInt("imageResId") ?: 0
        val title = args?.getString("title") ?: ""
        val location = args?.getString("location") ?: ""
        val date = args?.getString("date") ?: ""

        imageView.setImageResource(imageResId)
        titleTextView.text = title
        locationTextView.text = "Location: $location"
        dateTextView.text = "Claimed: $date"

        val code = generateVoucherCode()
        voucherCodeTextView.text = code
        qrImageView.setImageBitmap(generateQRCodeBitmap(code))

        confirmButton.setOnClickListener {
            Toast.makeText(requireContext(), "Reward confirmed", Toast.LENGTH_SHORT).show()

            // Remove from RewardHistory (if exists)
            val matchedItem = RewardHistoryStore.claimedRewards.find {
                it.title == title && it.dateClaimed == date
            }
            matchedItem?.let {
                RewardHistoryStore.claimedRewards.remove(it)
            }

            val confirmedItem = RewardHistoryItem(
                title = title,
                imageResId = imageResId,
                location = location,
                dateClaimed = date,
                expiryTimestamp = 0L, // immediate expiry
                status = "Used" // ✅ Updated status
            )
            RewardExpiredStore.expiredRewards.add(confirmedItem)
            findNavController().navigateUp()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.qrmain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return view
    }

    private fun generateVoucherCode(): String {
        val uuid = UUID.randomUUID().toString()
        return uuid.filter { it.isDigit() }.take(11).padEnd(11, '0')
    }

    private fun generateQRCodeBitmap(code: String): Bitmap {
        val width = 512
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 48f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("CODE:", width / 2f, height / 2f - 30, paint)
        paint.textSize = 60f
        canvas.drawText(code, width / 2f, height / 2f + 40, paint)

        return bitmap
    }
}

