package com.sushisasageyo.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sushisasageyo.MenuImages
import com.sushisasageyo.MenuItem
import com.sushisasageyo.R
import com.sushisasageyo.Utils
import com.sushisasageyo.databinding.ItemMenuBinding

class MenuAdapter(
    private val onClick: (MenuItem) -> Unit
) : ListAdapter<MenuItem, MenuAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<MenuItem>() {
            override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem) = oldItem == newItem
        }
    }

    inner class VH(val b: ItemMenuBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(menu: MenuItem) {
            // Pastikan ID ini sama dengan yang ada di XML Anda
            b.tvMenuName.text = menu.name
            b.tvMenuDesc.text = menu.description
            b.tvMenuPrice.text = Utils.rupiah(menu.price)

            // Memasang gambar sushi
            b.imgMenu.setImageResource(MenuImages.get(menu.id))

            // Logika label berdasarkan ID: menu_00x untuk Nigiri, menu_10x untuk Sashimi
            if (menu.id.startsWith("menu_00")) {
                b.tvCategoryLabel.text = "NIGIRI"
                b.tvCategoryLabel.setTextColor(Color.parseColor("#4CAF50")) // Hijau
            } else if (menu.id.startsWith("menu_10")) {
                b.tvCategoryLabel.text = "SASHIMI"
                b.tvCategoryLabel.setTextColor(Color.parseColor("#FF5252")) // Merah
            }

            b.root.setOnClickListener { onClick(menu) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}