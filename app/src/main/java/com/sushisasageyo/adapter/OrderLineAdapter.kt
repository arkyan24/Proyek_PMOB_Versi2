package com.sushisasageyo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.sushisasageyo.OrderLine
import com.sushisasageyo.Utils
import com.sushisasageyo.databinding.ItemOrderLineBinding

class OrderLineAdapter : ListAdapter<OrderLine, OrderLineAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<OrderLine>() {
            override fun areItemsTheSame(oldItem: OrderLine, newItem: OrderLine) = oldItem.menuId == newItem.menuId
            override fun areContentsTheSame(oldItem: OrderLine, newItem: OrderLine) = oldItem == newItem
        }
    }

    inner class VH(val b: ItemOrderLineBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemOrderLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvName.text = item.name
        holder.b.tvQty.text = "x${item.qty}"
        holder.b.tvPrice.text = Utils.rupiah(item.price)
    }
}
