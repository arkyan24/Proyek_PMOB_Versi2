package com.sushisasageyo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sushisasageyo.databinding.FragmentMenuDetailBinding
import kotlinx.coroutines.*

class MenuDetailFragment : BottomSheetDialogFragment() {

    private var _b: FragmentMenuDetailBinding? = null
    private val b get() = _b!!
    private lateinit var repo: FirebaseRepository
    private lateinit var user: User
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var categoryId = ""
    private var categoryName = ""
    private var menuId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentMenuDetailBinding.inflate(inflater, container, false)
        return b.root
    }

    // --- LOGIKA AGAR TIDAK FULL SCREEN & BISA DI-SWIPE ---
    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        // Mencari view internal bottom sheet dari library Material
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)

            // 1. Agar tinggi mengikuti isi konten (wrap_content), bukan paksa full screen
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            // 2. Memastikan bisa ditutup dengan swipe ke bawah
            behavior.isHideable = true

            // 3. Mencegah agar tidak "nyangkut" di tengah saat di-swipe
            behavior.skipCollapsed = true

            // 4. Mengatur agar lebar tetap match_parent (opsional)
            it.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }
    // ---------------------------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = (requireActivity() as MainActivity).getRepo()
        val mainUser = (requireActivity() as MainActivity).getUser()
        if (mainUser != null) user = mainUser

        val args = MenuDetailFragmentArgs.fromBundle(requireArguments())
        categoryId = args.categoryId
        menuId = args.menuId
        categoryName = args.categoryName

        b.imgMenu.setImageResource(MenuImages.get(menuId))

        scope.launch {
            val menu = withContext(Dispatchers.IO) { repo.getMenu(categoryId, menuId) }
            if (menu == null) {
                Toast.makeText(requireContext(), "Menu tidak ditemukan.", Toast.LENGTH_SHORT).show()
                dismiss()
                return@launch
            }

            b.tvName.text = menu.name
            b.tvDesc.text = menu.description
            b.tvPrice.text = Utils.rupiah(menu.price)

            b.btnAdd.setOnClickListener {
                scope.launch {
                    withContext(Dispatchers.IO) { repo.addToCart(user.id, categoryId, menuId) }
                    Toast.makeText(requireContext(), "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        scope.cancel()
        _b = null
        super.onDestroyView()
    }
}