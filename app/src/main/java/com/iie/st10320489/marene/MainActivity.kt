package com.iie.st10320489.marene

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import android.util.TypedValue
import android.widget.ProgressBar
import com.google.firebase.FirebaseApp
import com.iie.st10320489.marene.ui.subcategory.SubCategoryDialogFragment
import com.iie.st10320489.marene.ui.subcategory.SubcategoryFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {//((Cal, 2023), (College, 2025)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)


        //change color to transparent
        // Change status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        // Make it transparent (Optional)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }


        val navController: NavController = findNavController(R.id.nav_host_fragment_activity_main)

        val shouldNavigateToHome = intent.getBooleanExtra("navigateToHome", false)
        if (shouldNavigateToHome) {
            navController.navigate(R.id.navigation_home)
        }

        val bottomNavView: BottomNavigationView = findViewById(R.id.nav_view)
        val topNav = findViewById<androidx.appcompat.widget.Toolbar>(R.id.top_nav)
        val backButton = findViewById<ImageButton>(R.id.topNavBackButton)
        val title = findViewById<TextView>(R.id.topNavTitle)
        val rightButton = findViewById<ImageButton>(R.id.topNavRightButton)


        bottomNavView.setupWithNavController(navController)


        // Listen for navigation changes
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.navigation_home -> {
                    topNav.visibility = View.GONE
                }
                R.id.settingsFragment -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.VISIBLE
                    title.text = "Settings"
                    rightButton.visibility = View.GONE
                }
                R.id.editProfileFragment -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.VISIBLE
                    title.text = "Edit Profile"
                    rightButton.visibility = View.GONE
                }
                R.id.navigation_analysis -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.GONE
                    title.text = "Analysis"
                    rightButton.visibility = View.GONE
                    rightButton.setImageResource(R.drawable.ic_search)
                }
                R.id.navigation_add -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.GONE
                    title.text = "Add Entry"
                    rightButton.visibility = View.GONE
                    rightButton.setImageResource(R.drawable.ic_search)
                }
                R.id.navigation_category -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.GONE
                    title.text = "Categories"
                    rightButton.visibility = View.GONE
                    rightButton.setImageResource(R.drawable.ic_search)
                }
                R.id.transactionFragment -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.VISIBLE // Show back button
                    title.text = arguments?.getString("categoryName") ?: "Transactions"
                    rightButton.visibility = View.VISIBLE
                    rightButton.setImageResource(R.drawable.ic_search)
                }
                R.id.filterFragment -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.VISIBLE
                    title.text = arguments?.getString("categoryName") ?: "Filter"
                    rightButton.visibility = View.GONE
                }

                R.id.subcategoryFragment -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.VISIBLE
                    title.text = arguments?.getString("categoryName") ?: "Subcategories"
                    rightButton.visibility = View.VISIBLE
                    rightButton.setImageResource(R.drawable.ic_add)

                    // Set onClick listener for the "add" button in SubcategoryFragment
                    rightButton.setOnClickListener {
                        if (navController.currentDestination?.id == R.id.subcategoryFragment) {
                            // Launch the dialog to add a subcategory
                            showAddSubcategoryDialog()
                        }
                    }
                }

                R.id.transactionDetailsFragment -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.VISIBLE
                    rightButton.visibility = View.GONE
                    rightButton.setImageResource(R.drawable.ic_edit)
                }

                else -> {
                    topNav.visibility = View.VISIBLE
                    backButton.visibility = View.GONE
                    title.text = "Rewards"
                    rightButton.visibility = View.GONE
                }
            }
        }

        // Back button functionality
        backButton.setOnClickListener {
            navController.navigateUp()
        }

    }

    // Show the "Add Subcategory" dialog, but now in the fragment context
    private fun showAddSubcategoryDialog() {
        val dialogFragment = SubCategoryDialogFragment()
        dialogFragment.show(supportFragmentManager, "AddSubcategoryDialog")
    }//((Cal, 2023), (College, 2025)
}

//Bibliography
//AndroidDevelopers, 2024. Save data in a local database using Room. [Online] Available at: hRps://developer.android.com/training/data-storage/room [Accessed 27 April 2025].
//AndroidDevelopers, 2024. Write asynchronous DAO queries. [Online]
//Available at: hRps://developer.android.com/training/data-storage/room/async- queries?authuser=2
//[Accessed 26 April 2025].
//Raikwar, A., 2024. Ge=ng Started with Room Database in Android. [Online]
//Available at: hRps://amitraikwar.medium.com/ge[ng-started-with-room-database-in- android-fa1ca23ce21e
//[Accessed 27 April 2025].
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
//Team, G. D. T., 2024. Add repository and Manual DI. [Online]
//Available at: hRps://developer.android.com/codelabs/basic-android-kotlin-compose-add- repository#0
//[Accessed 22 April 2025].
//Coder, O., 2022. Implament Pie Chart in Android Studio Using Kotlin. [Online] Available at: hRps://youtu.be/TUJHcU0FOkA?si=jk90LRSO1_eyMyIG
//[Accessed 24 April 2025].
//Coder, E. O., 2024. hot to create bar chart | MP Android Chart | Android Studio 2024. [Online]
//Available at: hRps://youtu.be/WdsmQ3Zyn84?si=jz2AtkIRsNEUwNbX
//[Accessed 23 April 2025].

