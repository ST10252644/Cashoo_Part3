package com.iie.st10320489.marene.ui.transaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.databinding.ItemTransactionBinding
import com.iie.st10320489.marene.data.entities.TransactionWithCategory

class TransactionAdapter(
    private var transactions: List<TransactionWithCategory>, // List of transactions with their associated categories
    private val onItemClick: (TransactionWithCategory) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() { // (Android Developers, 2025)

    // ViewHolder class to manage individual transaction item views
    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Binds data from a TransactionWithCategory object to the UI components
        fun bind(item: TransactionWithCategory) {
            val transaction = item.transaction
            val category = item.category

            // Set the transaction name, method, and date in the respective TextViews
            binding.txtTransactionName.text = transaction.name
            binding.txtTransactionMethod.text = transaction.transactionMethod
            binding.txtTransactionDate.text = transaction.dateTime

            // Set amount text and color depending on whether it's income or expense
            setTransactionAmount(transaction)

            // Set the category icon and background tint color
            binding.imgCategoryIcon.setImageResource(category.icon)
            binding.imgCategoryIconBackground.background.setTint(
                ContextCompat.getColor(binding.root.context, category.colour)
            )

            // Set click listener for the entire transaction item view
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        // Sets the transaction amount text and color based on whether it's income or expense
        private fun setTransactionAmount(transaction: com.iie.st10320489.marene.data.entities.Transaction) {
            val context = binding.root.context
            if (transaction.expense) {
                binding.txtTransactionAmount.text = String.format("-R%.2f", transaction.amount)
                binding.txtTransactionAmount.setTextColor(
                    ContextCompat.getColor(context, R.color.outcome) // Red color for expenses
                )
            } else {
                binding.txtTransactionAmount.text = String.format("+R%.2f", transaction.amount)
                binding.txtTransactionAmount.setTextColor(
                    ContextCompat.getColor(context, R.color.income) // Green color for income
                )
            }
        }
    } // (Android Developers, 2025)

    // Called when the RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    // Called to bind data to a ViewHolder at a specific position
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int = transactions.size

    // Updates the adapter's list of transactions and refreshes the view
    fun updateTransactions(newTransactions: List<TransactionWithCategory>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
} // (Android Developers, 2025)

//Reference List:
// Android Developers. 2025. Accessing data using Room DAOs. [online]. Available at: https://developer.android.com/training/data-storage/room/accessing-data [Accessed on 15 April 2025]
//Android Developers. 2025. Fragment lifecycle. [online]. Available at: https://developer.android.com/guide/fragments/lifecycle [Accessed on 12 April 2025]
//Android Knowledge. 2024. ViewModel in Android Studio using Kotlin | Android Knowledge. [video online]. Available at: https://www.youtube.com/watch?v=v32hSKtlH9A [Accessed on 11 April 2025]
//Code With Cal. 2025. Room Database Android Studio Kotlin Example Tutorial. [video online]. Available at: https://www.youtube.com/watch?v=-LNg-K7SncM [Accessed on 12 April 2025]