package com.iie.st10320489.marene.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.iie.st10320489.marene.R

class ChinchillaActivity : AppCompatActivity(){    // (Android Knowledge, 2024)

    private lateinit var chinchillaImage: ImageView
    private var selectedColor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chinchilla)  // Set the layout for the activity

        // Handle back button click - finishes the current activity
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }    // (Android Knowledge, 2024)

        chinchillaImage = findViewById(R.id.chinchillaImage)  // Reference to the chinchilla image view

        // Set image and color when white button is clicked
        findViewById<View>(R.id.whiteButton).setOnClickListener {
            selectedColor = "white"
            chinchillaImage.setImageResource(R.drawable.white_nohat)
        }

        // Set image and color when beige button is clicked
        findViewById<View>(R.id.beigeButton).setOnClickListener {
            selectedColor = "beige"
            chinchillaImage.setImageResource(R.drawable.beige_nohat)
        }

        // Set image and color when violet button is clicked
        findViewById<View>(R.id.violetButton).setOnClickListener {
            selectedColor = "voilet"
            chinchillaImage.setImageResource(R.drawable.voilet_nohat)
        }

        // Set image and color when brown button is clicked
        findViewById<View>(R.id.brownButton).setOnClickListener {
            selectedColor = "brown"
            chinchillaImage.setImageResource(R.drawable.brown_nohat)
        }

        // Set image and color when black button is clicked
        findViewById<View>(R.id.blackButton).setOnClickListener {
            selectedColor = "black"
            chinchillaImage.setImageResource(R.drawable.black_nohat)
        }

        // Set image and color when grey button is clicked
        findViewById<View>(R.id.greyButton).setOnClickListener {
            selectedColor = "grey"
            chinchillaImage.setImageResource(R.drawable.grey_nohat)
        }

        // Handle next button click - start the next activity and pass the selected color
        findViewById<Button>(R.id.nextButton).setOnClickListener {
            val intent = Intent(this, ChinchillaHatActivity::class.java)
            intent.putExtra("selectedColor", selectedColor)  // Pass color to next activity
            startActivity(intent)
            finish()
        }
    }
    // (Android Knowledge, 2024)
}

//Reference List:
//Android Developers. 2025. Add an Image composition. [online]. Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-add-images#2 [Accessed on 9 April 2025]
//Code With Cal. 2025. Color Picker Android Studio Kotlin Custom Spinner Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=YsKjl8ZbM4g [Accessed on 9 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Programming w/ Professor Sluiter. 2023. Learn Kotlin 08 how to use the if conditional statement. [online]. Available at: https://www.youtube.com/watch?v=usFfxlnTPHc [Accessed on 13 April 2025]
//GeeksforGeeks. 2025. Android UI Layouts. [online]. Available at: https://www.geeksforgeeks.org/android-ui-layouts/ [Accessed on 10 April 2025]
//Muhammadumarch. 2023. Implementing Navigation in Your Android App with Android Navigation Component. [online]. Available at: https://medium.com/@muhammadumarch321/implementing-navigation-in-your-android-app-with-android-navigation-component-ff22a3d300a [Accessed on 11 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]
