package com.sushisasageyo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.database.ValueEventListener
import com.sushisasageyo.databinding.FragmentAccountBinding

class AccountFragment : Fragment(R.layout.fragment_account) {

    private var _b: FragmentAccountBinding? = null
    private val b get() = _b!!

    private lateinit var repo: FirebaseRepository
    private lateinit var userId: String
    private var userListener: ValueEventListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentAccountBinding.bind(view)

        val mainActivity = (requireActivity() as MainActivity)
        repo = mainActivity.getRepo()
        userId = mainActivity.getUser()?.id ?: return

        // MENGAKTIFKAN LISTENER REAL-TIME
        userListener = repo.listenUser(userId) { updatedUser ->
            // Bagian ini otomatis jalan saat database berubah (misal: poin bertambah)
            b.tvName.text = "guest_user"
            b.tvEmail.text = "guest@sushisasageyo.com"
            b.imgProfile.setImageResource(R.drawable.ic_profile)

        }

        b.apply {
            // Memanggil Popup Edit Profile
            btnEdit.setOnClickListener {
                val dialog = EditProfileDialogFragment()
                dialog.show(childFragmentManager, "EditProfilePopup")
            }

        }
    }

    override fun onDestroyView() {
        // PENTING: Lepas listener agar tidak boros baterai dan internet
        userListener?.let { repo.removeUserListener(userId, it) }
        _b = null
        super.onDestroyView()
    }
}