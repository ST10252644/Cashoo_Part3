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

class RewardExpiredFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RewardExpiredAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.reward_expired, container, false)
        recyclerView = view.findViewById(R.id.expiredRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = RewardExpiredAdapter(RewardExpiredStore.expiredRewards)
        recyclerView.adapter = adapter

        val expButton: TextView = view.findViewById(R.id.actButton)
        expButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_rewards_history)

        }

        view.findViewById<Button>(R.id.expiredBack).setOnClickListener {
            findNavController().navigate(R.id.navigation_rewards)
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.expiredmain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        adapter.refreshData()
    }

}
