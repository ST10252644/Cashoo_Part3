package com.iie.st10320489.marene.ui.search

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.TransactionWithCategory
import com.iie.st10320489.marene.databinding.ItemTransactionBinding

class SearchTransactionAdapter(
    private var transactions: List<TransactionWithCategory>,
    private val onItemClick: (TransactionWithCategory) -> Unit
) : RecyclerView.Adapter<SearchTransactionAdapter.SearchTransactionViewHolder>() {


    inner class SearchTransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionWithCategory) {
            val transaction = item.transaction
            val category = item.category
            val context = binding.root.context

            // Set transaction name, method, and date
            binding.txtTransactionName.text = transaction.name
            binding.txtTransactionMethod.text = transaction.transactionMethod
            binding.txtTransactionDate.text = transaction.dateTime

            // Set amount text and color
            if (transaction.expense) {
                binding.txtTransactionAmount.text = String.format("-R%.2f", transaction.amount)
                binding.txtTransactionAmount.setTextColor(
                    ContextCompat.getColor(context, R.color.outcome)
                )
            } else {
                binding.txtTransactionAmount.text = String.format("+R%.2f", transaction.amount)
                binding.txtTransactionAmount.setTextColor(
                    ContextCompat.getColor(context, R.color.income)
                )
            }

            // Set icon or fallback
            if (category.icon != 0) {
                binding.imgCategoryIcon.setImageResource(category.icon)
            } else {
                binding.imgCategoryIcon.setImageResource(R.drawable.ic_default)
            }

            // Set background tint color safely
            val color = try {
                if (category.colour != 0)
                    ContextCompat.getColor(context, category.colour)
                else ContextCompat.getColor(context, R.color.black)
            } catch (e: Resources.NotFoundException) {
                Log.e("SearchTransactionAdapter", "Invalid color id: ${category.colour}", e)
                ContextCompat.getColor(context, R.color.black)
            }
            binding.imgCategoryIconBackground.background.setTint(color)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SearchTransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchTransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<TransactionWithCategory>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
} //(Firebase, 2023; Technology, 2020; GeeksforGeeks, 2024)
//Bibliography

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