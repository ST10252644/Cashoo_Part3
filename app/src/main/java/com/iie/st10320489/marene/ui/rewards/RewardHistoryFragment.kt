package com.iie.st10320489.marene.ui.rewards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10320489.marene.R

class RewardHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RewardHistoryAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.reward_history, container, false)
        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = RewardHistoryAdapter(RewardHistoryStore.claimedRewards)
        recyclerView.adapter = adapter


        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.historymain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Back button
        val backButton: Button = view.findViewById(R.id.butback)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_rewards)
        }

        // Expired button: Navigate to RewardExpiredFragment
        val expButton: TextView = view.findViewById(R.id.expButton)
        expButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_rewards_expired)

        }

        return view
    }
}