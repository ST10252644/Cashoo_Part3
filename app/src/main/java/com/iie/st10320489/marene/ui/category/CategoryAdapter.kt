package com.iie.st10320489.marene.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Category


// RecyclerView Adapter for displaying a list of Category items
class CategoryAdapter(//((Cal, 2023), (College, 2025)
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // ViewHolder class that holds references to the views within each item layout
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryButton: ImageButton = itemView.findViewById(R.id.categoryButton)
        val categoryTitle: TextView = itemView.findViewById(R.id.categoryTitle)
    }

    // Inflates the item layout and returns a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    // Binds data from the Category object to the views in the ViewHolder
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val resolvedColor = ContextCompat.getColor(holder.itemView.context, category.colour)

        holder.categoryTitle.text = category.name
        holder.categoryButton.setImageResource(category.icon)
        holder.categoryButton.background.setTint(resolvedColor)

        // Set a click listener to trigger the callback with the selected category
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    // Returns the total number of categories in the list
    override fun getItemCount() = categories.size

    // Updates the adapter's category list and refreshes the RecyclerView
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}
//(Firebase, 2023),(Technology, 2020),(GeeksforGeeks, 2024)


///Bibliography

//College, I. V., 2025. PROG7313 Module-Manual / Module-Outline. Pretoria: Varsity College Pretoria.
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
//Firebase, 2023. Ge=ng Started with Firebase on Android. [Online] Available at: hLps://youtu.be/jbHfJpoOzkl?si=rQ0hPeu_qKWpuAlm [Accessed 27 May 2025].
//Technology, S., 2020. 017 How to create MP Android Chart from Firebase RealKme Database. [Online]
//Available at: hLps://youtu.be/C0O9u0jd6nQ?si=c-H-xO4ISG2DWqQx [Accessed 22 May 2025].
//GeeksforGeeks, 2024. How to Create and Add Data to Firebase Firestore in Android. [Online] Available at: hLps://www.geeksforgeeks.org/create-and-add-data-to-firebase-firestore-in- android/
//[Accessed 23 May 2025].
