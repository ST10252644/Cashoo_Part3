package com.iie.st10320489.marene.ui.rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.iie.st10320489.marene.R

class DiscountActivity : Fragment() { // (Code With Cal, 2025)

    private lateinit var chipGroup: ChipGroup
    private lateinit var recyclerViewClothing: RecyclerView
    private lateinit var recyclerViewFood: RecyclerView
    private lateinit var recyclerViewAccessories: RecyclerView
    private lateinit var recyclerViewShoes: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView: View = inflater.inflate(R.layout.activity_discount, container, false)

// (Code With Cal, 2025)
        // Initialize RecyclerViews
        recyclerViewClothing = rootView.findViewById(R.id.recyclerViewClothing)
        recyclerViewFood = rootView.findViewById(R.id.recyclerViewFood)
        recyclerViewAccessories = rootView.findViewById(R.id.recyclerViewAccessories)
        recyclerViewShoes = rootView.findViewById(R.id.recyclerViewShoes)
        chipGroup = rootView.findViewById(R.id.chipGroup)

// (Code With Cal, 2025)
        // Set LayoutManagers
        recyclerViewClothing.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewFood.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewAccessories.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewShoes.layoutManager = LinearLayoutManager(requireContext())

        // Setup hard coded data
        val clothingDiscounts = listOf(
            Discount("H&M Clothing", "20% off all jeans", R.drawable.zara),
            Discount("Factorie", "Buy 2 shirts for the price of 1 shirt", R.drawable.factorie),
            Discount("Zara", "5 % OFF of all clothes", R.drawable.zara)
        )
        val foodDiscounts = listOf(
            Discount("Starbucks Coffee", "50 % OFF of Coffee", R.drawable.starbuck),
            Discount("Energy Snacks", "Buy 1 and get 1 free on energy bars", R.drawable.bar),
            Discount("Kauai Freebies", "80 % OFF of any Kauai item", R.drawable.kauai)
        )
        val accessoriesDiscounts = listOf(
            Discount("Ray-Bans", "15% off sunglasses", R.drawable.ray),
            Discount("Beanies", "25% off beanies", R.drawable.beanie),
            Discount("Chains and Rings", "15% off sunglasses", R.drawable.rings)
        )
        val shoesDiscounts = listOf(
            Discount("Nike Shoes", "10% off sneakers", R.drawable.jordan4),
            Discount("Shoes Wipers", "Save 60% off on shoe wipers", R.drawable.wipers),
            Discount(
                "SportScene",
                "15% of on shoe related items at the shop",
                R.drawable.sportscene
            ),
            Discount("Sneakers", "20% off on all sneakers in Sandton Mall", R.drawable.allsneaks)
        )
// (Code With Cal, 2025)

        // Setup adapters
        recyclerViewClothing.adapter = DiscountAdapter(clothingDiscounts)
        recyclerViewFood.adapter = DiscountAdapter(foodDiscounts)
        recyclerViewAccessories.adapter = DiscountAdapter(accessoriesDiscounts)
        recyclerViewShoes.adapter = DiscountAdapter(shoesDiscounts)


        // Chip click handling
        rootView.findViewById<Chip>(R.id.chipAll).setOnClickListener { showOnly("all") }
        rootView.findViewById<Chip>(R.id.chipCloths).setOnClickListener { showOnly("clothing") }
        rootView.findViewById<Chip>(R.id.chipFood).setOnClickListener { showOnly("food") }
        rootView.findViewById<Chip>(R.id.chipAccess).setOnClickListener { showOnly("accessories") }
        rootView.findViewById<Chip>(R.id.chipShoes).setOnClickListener { showOnly("shoes") } // (Code With Cal, 2025)


        val backm2 = rootView.findViewById<ImageView>(R.id.back2)
        backm2.setOnClickListener {
            //findNavController().navigate(R.id.navigation_rewards_two)
        }
        // (Code With Cal, 2025)


        ViewCompat.setOnApplyWindowInsetsListener(rootView.findViewById(R.id.disc)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return rootView
    }

    private fun showOnly(category: String) {
        recyclerViewClothing.visibility =
            if (category == "clothing" || category == "all") View.VISIBLE else View.GONE
        recyclerViewFood.visibility =
            if (category == "food" || category == "all") View.VISIBLE else View.GONE
        recyclerViewAccessories.visibility =
            if (category == "accessories" || category == "all") View.VISIBLE else View.GONE
        recyclerViewShoes.visibility =
            if (category == "shoes" || category == "all") View.VISIBLE else View.GONE
    }
} // (Code With Cal, 2025)

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