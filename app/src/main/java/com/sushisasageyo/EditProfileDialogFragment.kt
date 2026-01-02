package com.sushisasageyo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.sushisasageyo.databinding.FragmentEditProfileBinding // Ganti sesuai nama file XML popup Anda
import kotlinx.coroutines.*

class EditProfileDialogFragment : DialogFragment() {

    private var _b: FragmentEditProfileBinding? = null
    private val b get() = _b!!

    private lateinit var repo: FirebaseRepository
    private lateinit var user: User

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var selectedUri: Uri? = null

    // Launcher Image Picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            b.imgProfile.setImageURI(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentEditProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi Data
        val mainActivity = (requireActivity() as MainActivity)
        repo = mainActivity.getRepo()
        user = mainActivity.getUser() ?: return dismiss()

        // 2. Tampilkan Data Awal
        b.etName.setText(user.name)
        b.imgProfile.setImageResource(R.drawable.ic_profile)

        // 3. Logika Tombol
        b.btnPickPhoto.setOnClickListener { pickImage.launch("image/*") }

        b.btnCancel.setOnClickListener { dismiss() } // Menutup popup

        b.btnSave.setOnClickListener {
            val newName = b.etName.text.toString().trim()
            val photoUri = selectedUri?.toString() ?: user.photoUri

            if (newName.isBlank()) {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            scope.launch {
                b.btnSave.isEnabled = false
                b.btnSave.text = "..."

                try {
                    withContext(Dispatchers.IO) {
                        repo.updateUser(user.id, newName, photoUri)
                    }
                    Toast.makeText(requireContext(), "Profile diperbarui.", Toast.LENGTH_SHORT).show()
                    dismiss() // Tutup popup setelah sukses
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    b.btnSave.isEnabled = true
                    b.btnSave.text = "Simpan"
                }
            }
        }
    }

    // Mengatur agar background dialog transparan (agar rounded corner kartu terlihat)
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _b = null
    }
}