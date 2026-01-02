package com.sushisasageyo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sushisasageyo.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentHomeBinding.bind(view)

        // Hot Menu: Nigiri & Sashimi
        b.cardNigiri.setOnClickListener {
            val args = Bundle().apply {
                putString("categoryId", "nigiri")
                putString("categoryName", "Nigiri Sushi")
            }
            findNavController().navigate(R.id.categoryFragment, args)
        }

        b.cardSashimi.setOnClickListener {
            val args = Bundle().apply {
                putString("categoryId", "sashimi")
                putString("categoryName", "Sashimi")
            }
            findNavController().navigate(R.id.categoryFragment, args)
        }
    }


    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}
