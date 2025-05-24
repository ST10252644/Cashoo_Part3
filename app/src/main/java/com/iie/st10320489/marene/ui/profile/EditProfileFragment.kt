package com.iie.st10320489.marene.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.iie.st10320489.marene.R

class EditProfileFragment : Fragment() { // (Code With Cal, 2025)

    // This method is called to create the view hierarchy of the fragment.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    } // (Code With Cal, 2025)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Finds the Button view with the ID 'updateButton' from the inflated layout.
        val updateButton: Button = view.findViewById(R.id.updateButton)

        // Sets an OnClickListener on the 'updateButton'. When clicked, a Toast message will be shown.
        updateButton.setOnClickListener {
            // Displays a Toast message indicating that the functionality will be available in Part 3.
            Toast.makeText(requireContext(), "Will be functional at Part 3", Toast.LENGTH_SHORT).show()
        }
    }
} // (Code With Cal, 2025)

//Reference List:
//Android Developers. 2025. Add an Image composition. [online]. Available at: https://developer.android.com/codelabs/basic-android-kotlin-compose-add-images#2 [Accessed on 9 April 2025]
//Code With Cal. 2025. Color Picker Android Studio Kotlin Custom Spinner Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=YsKjl8ZbM4g [Accessed on 9 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]
//Programming w/ Professor Sluiter. 2023. Learn Kotlin 08 how to use the if conditional statement. [online]. Available at: https://www.youtube.com/watch?v=usFfxlnTPHc [Accessed on 13 April 2025]
