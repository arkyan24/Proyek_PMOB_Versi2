package com.sushisasageyo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sushisasageyo.CartUiItem
import com.sushisasageyo.MenuImages
import com.sushisasageyo.Utils
import com.sushisasageyo.databinding.ItemCartBinding

class CartAdapter(
    private val onPlus: (CartUiItem) -> Unit,
    private val onMinus: (CartUiItem) -> Unit
) : ListAdapter<CartUiItem, CartAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CartUiItem>() {
            override fun areItemsTheSame(oldItem: CartUiItem, newItem: CartUiItem) = oldItem.menuId == newItem.menuId
            override fun areContentsTheSame(oldItem: CartUiItem, newItem: CartUiItem) = oldItem == newItem
        }
    }

    inner class VH(val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.img.setImageResource(MenuImages.get(item.menuId))
        holder.b.tvName.text = item.name
        holder.b.tvPrice.text = Utils.rupiah(item.price)
        holder.b.tvQty.text = item.qty.toString()
        holder.b.btnPlus.setOnClickListener { onPlus(item) }
        holder.b.btnMinus.setOnClickListener { onMinus(item) }
    }
}
