package com.sushisasageyo.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.sushisasageyo.Order
import com.sushisasageyo.Utils
import com.sushisasageyo.databinding.ItemOrderBinding

class OrderAdapter(private val onClick: (Order) -> Unit) : ListAdapter<Order, OrderAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
        }
    }

    inner class VH(val b: ItemOrderBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = getItem(position)
        val b = holder.b

        // 1. CLICK LISTENER: Memungkinkan klik pada seluruh area kartu
        b.root.setOnClickListener { onClick(order) }

        // 2. SET ID ORDER (6 Karakter Terakhir) & TOTAL
        b.tvOrderId.text = "Order #${order.id.takeLast(6).uppercase()}"
        b.tvTotal.text = Utils.rupiah(order.total)

        // 3. LOGIKA STATUS & WARNA BADGE DINAMIS
        // Warna teks dipaksa Putih untuk semua kondisi
        b.tvStatus.setTextColor(Color.WHITE)

        when (order.status) {
            "MENUNGGU PEMBAYARAN" -> {
                b.tvStatus.text = "Menunggu Pembayaran"
                // Badge Kuning Solid
                b.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFC107"))
            }
            "PAID" -> {
                b.tvStatus.text = "Sudah Dibayar"
                // Badge Hijau Solid
                b.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            }
            else -> {
                b.tvStatus.text = order.status
                // Default abu-abu transparan untuk status lainnya
                b.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#33FFFFFF"))
            }
        }

        // 4. PASANG TANGGAL & WAKTU (TIMESTAMP)
        b.tvTimestamp.text = Utils.formatTimestamp(order.timestamp)

        // 5. NESTED LIST: Menampilkan daftar item di dalam kartu order
        val lines = order.items.values.toList()
        val lineAdapter = OrderLineAdapter()
        b.rvLines.adapter = lineAdapter
        lineAdapter.submitList(lines)
    }
}