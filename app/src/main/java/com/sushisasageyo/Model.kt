package com.sushisasageyo

/**
 * Representasi kategori sushi (Nigiri, Sashimi, dll)
 */
data class Category(
    val id: String = "",
    val name: String = ""
)

/**
 * Detail item menu sushi
 */
data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Long = 0L
)

/**
 * Data item di dalam keranjang (disimpan di Firebase)
 */
data class CartItem(
    val menuId: String = "",
    val categoryId: String = "",
    val qty: Int = 0
)

/**
 * Data profil pengguna
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val photoUri: String = "",
    val points: Long = 0L, // TAMBAHAN: Untuk menyimpan poin loyalitas
    val createdAt: Long = 0L
)

/**
 * Detail item di dalam sebuah pesanan yang sudah selesai
 */
data class OrderLine(
    val menuId: String = "",
    val name: String = "",
    val price: Long = 0L,
    val qty: Int = 0
)

/**
 * Data transaksi pesanan
 */
data class Order(
    val id: String = "",
    val timestamp: Long = 0L, // PERBAIKAN: Nama diubah agar sinkron dengan Utils
    val status: String = "PAID", // PAID | WAITING_DELIVERY
    val total: Long = 0L,
    val items: Map<String, OrderLine> = emptyMap()
)

/**
 * Model pembantu untuk menggabungkan data keranjang dan detail menu di UI
 */
data class CartUiItem(
    val menuId: String = "",
    val categoryId: String = "",
    val name: String = "",
    val price: Long = 0L,
    val qty: Int = 0
)