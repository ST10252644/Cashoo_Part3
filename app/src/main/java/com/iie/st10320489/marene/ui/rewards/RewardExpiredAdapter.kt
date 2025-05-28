package com.iie.st10320489.marene.ui.rewards

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Reward
import java.util.UUID

class RewardExpiredAdapter (private val expiredList: MutableList<RewardHistoryItem>) :
    RecyclerView.Adapter<RewardExpiredAdapter.ExpiredViewHolder>()  {

    inner class ExpiredViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.expiredImage)
        val title: TextView = itemView.findViewById(R.id.expiredTitle)
        val location: TextView = itemView.findViewById(R.id.expiredLocation)
        val date: TextView = itemView.findViewById(R.id.expiredDate)
        val status: TextView = itemView.findViewById(R.id.expiredStatus)

    }

    private fun saveExpiredClaimToFirestore(context: Context, item: RewardHistoryItem) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }
        val reward = Reward(
            name = item.title,
            description = "Expired reward from ${item.location} claimed on ${item.dateClaimed}",
            amount = 0.0,
            type = "expired",
            code = UUID.randomUUID().toString().takeLast(6).filter { it.isDigit() }
                .padEnd(6, '0').toIntOrNull() ?: 0
        )

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("rewards")
            .add(reward)
            .addOnSuccessListener { documentRef ->
                val rewardId = documentRef.id
                val uid = user.uid
                val claimRecord = mapOf(
                    "rewardId" to rewardId,
                    "userId" to uid,
                    "name" to reward.name,
                    "location" to item.location,
                    "description" to reward.description,
                    "dateClaimed" to FieldValue.serverTimestamp()
                )

                firestore.collection("rewards")
                    .document(rewardId)
                    .collection(uid)
                    .add(claimRecord)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Expired/Used reward saved to Firebase.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to save expired/used reward details.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to create expired/used reward record.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpiredViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward_expired, parent, false)
        return ExpiredViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpiredViewHolder, position: Int) {
        val item = expiredList[position]
        holder.image.setImageResource(item.imageResId)
        holder.title.text = item.title
        holder.location.text = "From: ${item.location}"
        holder.date.text = "Expired: ${item.dateClaimed}"
        holder.status.text = item.status // dynamic status: "Used" or "Expired"

        saveExpiredClaimToFirestore(holder.itemView.context, item)
    }

    override fun getItemCount(): Int = expiredList.size
    fun refreshData() {
        notifyDataSetChanged()
    }

}
