package com.sushisasageyo

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.ValueEventListener
import com.sushisasageyo.adapter.CartAdapter
import com.sushisasageyo.adapter.OrderAdapter
import com.sushisasageyo.databinding.FragmentOrderBinding
import kotlinx.coroutines.*

class OrderFragment : Fragment(R.layout.fragment_order) {

    private var _b: FragmentOrderBinding? = null
    private val b get() = _b!!
    private lateinit var repo: FirebaseRepository
    private lateinit var user: User

    private var cartListener: ValueEventListener? = null
    private var ordersListener: ValueEventListener? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val cartAdapter = CartAdapter(
        onPlus = { item -> updateQty(item, item.qty + 1) },
        onMinus = { item -> updateQty(item, item.qty - 1) }
    )

    private val orderAdapter = OrderAdapter { order ->
        if (order.status == "MENUNGGU PEMBAYARAN") {
            showReceiptDialog(order.items.values.map {
                CartUiItem(it.menuId, "", it.name, it.price, it.qty)
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentOrderBinding.bind(view)

        val mainActivity = (requireActivity() as MainActivity)
        repo = mainActivity.getRepo()

        val mainUser = mainActivity.getUser()
        user = mainUser ?: User("guest_user", "Guest User", "guest@sushisasageyo.com")

        b.rvCart.adapter = cartAdapter
        b.rvOrders.adapter = orderAdapter

        cartListener = repo.listenCart(user.id) { _ ->
            scope.launch {
                delay(100)
                refreshCartUi()
            }
        }

        ordersListener = repo.listenOrders(user.id) { list ->
            orderAdapter.submitList(list)
            val totalSpent = list.sumOf { it.total }
            b.tvPoints.text = "Points: ${Utils.pointsFromTotal(totalSpent)}"
        }

        b.btnCheckout.setOnClickListener {
            val cartUi = mainActivity.getCartUiCache()
            if (cartUi.isEmpty()) {
                Toast.makeText(requireContext(), "Keranjang kosong.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPaymentMethodDialog(cartUi)
        }
    }

    private fun showPaymentMethodDialog(cartUi: List<CartUiItem>) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_payment_method, null)
        dialog.setContentView(view)

        val rbGoPay = view.findViewById<RadioButton>(R.id.rbGoPay)
        val rbBNI = view.findViewById<RadioButton>(R.id.rbBNI)
        val rbTunai = view.findViewById<RadioButton>(R.id.rbTunai)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirmPayment)

        val radioButtons = listOf(rbGoPay, rbBNI, rbTunai)
        val selectMethod = { selected: RadioButton ->
            radioButtons.forEach { it.isChecked = (it == selected) }
        }

        view.findViewById<View>(R.id.btnGoPay).setOnClickListener { selectMethod(rbGoPay) }
        rbGoPay.setOnClickListener { selectMethod(rbGoPay) }
        view.findViewById<View>(R.id.btnBNI).setOnClickListener { selectMethod(rbBNI) }
        rbBNI.setOnClickListener { selectMethod(rbBNI) }
        view.findViewById<View>(R.id.btnTunai).setOnClickListener { selectMethod(rbTunai) }
        rbTunai.setOnClickListener { selectMethod(rbTunai) }

        btnConfirm.setOnClickListener {
            val method = when {
                rbGoPay.isChecked -> "GoPay"
                rbBNI.isChecked -> "BNI m-Banking"
                rbTunai.isChecked -> "Tunai"
                else -> null
            }

            if (method != null) {
                dialog.dismiss()
                performCheckout(cartUi, method)
            } else {
                Toast.makeText(context, "Pilih metode pembayaran!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun performCheckout(cartUi: List<CartUiItem>, method: String) {
        val status = if (method == "Tunai") "MENUNGGU PEMBAYARAN" else "PAID"
        val mainActivity = (requireActivity() as MainActivity)

        scope.launch {
            b.btnCheckout.isEnabled = false
            val orderId = withContext(Dispatchers.IO) { repo.createOrderFromCart(user.id, cartUi, status) }

            if (orderId.isNotBlank()) {
                // SINKRONISASI MANUAL CACHE
                mainActivity.refreshCartCache()

                delay(800)
                refreshCartUi()

                Toast.makeText(requireContext(), "Pesanan berhasil diproses!", Toast.LENGTH_SHORT).show()

                if (method == "Tunai") {
                    showReceiptDialog(cartUi)
                }
            }
            b.btnCheckout.isEnabled = true
        }
    }

    private fun showReceiptDialog(items: List<CartUiItem>) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_receipt, null)
        dialog.setContentView(view)

        val container = view.findViewById<LinearLayout>(R.id.containerItems)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalReceipt)
        var total = 0L

        items.forEach { item ->
            val itemView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
            itemView.findViewById<TextView>(android.R.id.text1).apply {
                text = "${item.name} (x${item.qty})"
                setTextColor(Color.WHITE)
            }
            itemView.findViewById<TextView>(android.R.id.text2).apply {
                text = Utils.rupiah(item.price * item.qty)
                setTextColor(Color.LTGRAY)
            }
            container.addView(itemView)
            total += item.price * item.qty
        }

        tvTotal.text = Utils.rupiah(total)
        view.findViewById<View>(R.id.btnCloseReceipt).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun refreshCartUi() {
        val cartUi = (requireActivity() as MainActivity).getCartUiCache()
        cartAdapter.submitList(cartUi)

        if (cartUi.isEmpty()) {
            b.layoutEmptyCart.visibility = View.VISIBLE
            b.cardCart.visibility = View.GONE
        } else {
            b.layoutEmptyCart.visibility = View.GONE
            b.cardCart.visibility = View.VISIBLE
            val total = cartUi.sumOf { it.price * it.qty }
            b.tvCartTotal.text = Utils.rupiah(total)
        }
    }

    private fun updateQty(item: CartUiItem, newQty: Int) {
        scope.launch {
            withContext(Dispatchers.IO) { repo.setCartQty(user.id, item.categoryId, item.menuId, newQty) }
            delay(100)
            refreshCartUi()
        }
    }

    override fun onDestroyView() {
        cartListener?.let { repo.removeCartListener(user.id, it) }
        ordersListener?.let { repo.removeOrdersListener(user.id, it) }
        scope.cancel()
        _b = null
        super.onDestroyView()
    }
}