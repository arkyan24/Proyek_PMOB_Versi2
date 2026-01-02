package com.sushisasageyo

object MenuImages {
    fun get(id: String): Int {
        return when (id) {
            // --- NIGIRI (Format 00x) ---
            "menu_001" -> R.drawable.menu_001 // Nigiri Salmon
            "menu_002" -> R.drawable.menu_002 // Nigiri Tuna
            "menu_003" -> R.drawable.menu_003 // Nigiri Unagi
            "menu_004" -> R.drawable.menu_004 // Nigiri Shrimp
            "menu_005" -> R.drawable.menu_005 // Nigiri Uni
            "menu_006" -> R.drawable.menu_006 // Nigiri Squid
            "menu_007" -> R.drawable.menu_007 // Nigiri Tamago

            // --- SASHIMI (Format 10x) ---
            "menu_101" -> R.drawable.menu_101 // Salmon Sashimi
            "menu_102" -> R.drawable.menu_102 // Tuna Sashimi
            "menu_103" -> R.drawable.menu_103 // Uni Sashimi

            // Default jika gambar tidak ditemukan
            else -> R.drawable.menu_placeholder
        }
    }
}