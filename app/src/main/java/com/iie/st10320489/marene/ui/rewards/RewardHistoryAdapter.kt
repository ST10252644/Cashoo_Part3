package com.iie.st10320489.marene.ui.rewards

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings.Global.putInt
import android.provider.Settings.Global.putString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.iie.st10320489.marene.R
import com.iie.st10320489.marene.data.entities.Reward
import java.util.UUID



class RewardHistoryAdapter (private val historyList: MutableList<RewardHistoryItem>) :
    RecyclerView.Adapter<RewardHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.historyImage)
        val title: TextView = itemView.findViewById(R.id.historyTitle)
        val location: TextView = itemView.findViewById(R.id.historyLocation)
        val date: TextView = itemView.findViewById(R.id.historyDate)
        val status: TextView = itemView.findViewById(R.id.historyStatus)
        val countdown: TextView = itemView.findViewById(R.id.historyCountdown)
        var timer: CountDownTimer? = null
    }

    private fun saveClaimToFirestore(context: Context, item: RewardHistoryItem) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }
        val reward = Reward(
            name = item.title,
            description = "Claimed reward from ${item.location} on ${item.dateClaimed}",
            amount = 0.0,
            type = "claim",
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
                        Toast.makeText(context, "Reward claim saved.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to save claim details.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to create reward record.", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {


        val item = historyList[position]
        holder.image.setImageResource(item.imageResId)
        holder.title.text = item.title
        holder.location.text = "Location: ${item.location}"
        holder.date.text = "Claimed: ${item.dateClaimed}"
        holder.status.text = "Status: Active"

        // Cancel previous timer if any
        holder.timer?.cancel()

        // Calculate remaining time
        val timeRemaining = item.expiryTimestamp - System.currentTimeMillis()

        if (timeRemaining > 0) {
            holder.timer = object : CountDownTimer(timeRemaining, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutes = (millisUntilFinished / 1000) / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    holder.countdown.text = String.format("%02d:%02d", minutes, seconds)
                }

                override fun onFinish() {
                    holder.countdown.text = "00:00"
                    holder.status.text = "Expired"

                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos < historyList.size) {
                        val expiredItem = historyList[pos]

                        // Post to main thread to ensure safe UI update
                        (holder.itemView.context as? android.app.Activity)?.runOnUiThread {
                            historyList.removeAt(pos)
                            notifyItemRemoved(pos)
                            RewardExpiredStore.expiredRewards.add(expiredItem)
                        }
                    }
                }
            }.start()
        } else {
            // Already expired (for example, app reopened)
            holder.countdown.text = "00:00"
            holder.status.text = "Expired"
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val bundle = Bundle().apply {
                putInt("imageResId", item.imageResId)
                putString("title", item.title)
                putString("location", item.location)
                putString("date", item.dateClaimed)
            }
            saveClaimToFirestore(context, item)

            val navController = Navigation.findNavController(holder.itemView)
            navController.navigate(R.id.navigation_rewards_qr, bundle)
        }
    }



    override fun onViewRecycled(holder: HistoryViewHolder) {
        super.onViewRecycled(holder)
        holder.timer?.cancel() // Prevent memory leaks
    }

    override fun getItemCount(): Int = historyList.size
}

