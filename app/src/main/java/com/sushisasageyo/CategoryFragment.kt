package com.sushisasageyo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.ValueEventListener
import com.sushisasageyo.adapter.MenuAdapter
import com.sushisasageyo.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment(R.layout.fragment_category) {

    private var _b: FragmentCategoryBinding? = null
    private val b get() = _b!!
    private lateinit var repo: FirebaseRepository

    private var categoryId: String = ""
    private var categoryName: String = ""
    private var menusListener: ValueEventListener? = null

    private val adapter = MenuAdapter(
        onClick = { menu ->
            val action = CategoryFragmentDirections.actionCategoryToMenuDetail(categoryId, menu.id, categoryName)
            findNavController().navigate(action)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentCategoryBinding.bind(view)
        repo = (requireActivity() as MainActivity).getRepo()

        val args = CategoryFragmentArgs.fromBundle(requireArguments())
        categoryId = args.categoryId
        categoryName = args.categoryName

        // 1. SET JUDUL TOOLBAR & TEKS DI ATAS GAMBAR
        b.toolbar.title = categoryName
        b.tvCategoryTitle.text = categoryName // Menghubungkan ID tvCategoryTitle dari XML
        b.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // 2. LOGIKA PASANG GAMBAR SAMPUL BERDASARKAN ID KATEGORI
        when (categoryId) {
            "nigiri" -> b.ivCategoryCover.setImageResource(R.drawable.card_nigiri)
            "sashimi" -> b.ivCategoryCover.setImageResource(R.drawable.card_sashimi)
            else -> b.ivCategoryCover.setImageResource(R.drawable.banner_home)
        }

        b.rvMenus.adapter = adapter

        // 3. LISTEN DATA MENU DARI FIREBASE
        menusListener = repo.listenMenus(categoryId) { list ->
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        // MELEPASKAN LISTENER SAAT HALAMAN DITUTUP
        menusListener?.let { repo.removeMenusListener(categoryId, it) }
        _b = null
        super.onDestroyView()
    }


}