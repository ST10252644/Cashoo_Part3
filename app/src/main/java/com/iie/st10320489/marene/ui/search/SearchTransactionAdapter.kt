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
}
