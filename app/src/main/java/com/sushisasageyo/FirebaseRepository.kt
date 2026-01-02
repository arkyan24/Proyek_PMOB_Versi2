package com.sushisasageyo

import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    // Inisialisasi Database
    private val db = FirebaseDatabase.getInstance(
        "https://projectpmob-94757-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    private fun ref(path: String): DatabaseReference = db.getReference(path)

    // --- 1. MANAJEMEN USER ---
    fun listenUser(userId: String, onUpdate: (User) -> Unit): ValueEventListener {
        val r = ref("users/$userId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let { onUpdate(it) }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        r.addValueEventListener(listener)
        return listener
    }

    fun removeUserListener(userId: String, listener: ValueEventListener) {
        ref("users/$userId").removeEventListener(listener)
    }

    suspend fun getUserById(userId: String): User? {
        val snap = ref("users/$userId").get().await()
        return snap.getValue(User::class.java)
    }

    suspend fun registerUser(name: String, email: String): User {
        val userId = ref("users").push().key ?: "guest_user"
        val user = User(id = userId, name = name, email = email, points = 0L, createdAt = System.currentTimeMillis())
        ref("users/$userId").setValue(user).await()
        return user
    }

    suspend fun updateUser(userId: String, newName: String, photoUri: String) {
        ref("users/$userId/name").setValue(newName.trim()).await()
        ref("users/$userId/photoUri").setValue(photoUri).await()
    }

    // --- 2. MANAJEMEN MENU & KATEGORI ---
    fun listenCategories(onUpdate: (List<Category>) -> Unit): ValueEventListener {
        val r = ref("categories")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
                onUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        r.addValueEventListener(listener)
        return listener
    }

    fun listenMenus(categoryId: String, onUpdate: (List<MenuItem>) -> Unit): ValueEventListener {
        val r = ref("menus/$categoryId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(MenuItem::class.java) }
                onUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        r.addValueEventListener(listener)
        return listener
    }

    fun removeMenusListener(categoryId: String, listener: ValueEventListener) {
        ref("menus/$categoryId").removeEventListener(listener)
    }

    suspend fun getMenu(categoryId: String, menuId: String): MenuItem? {
        val snap = ref("menus/$categoryId/$menuId").get().await()
        return snap.getValue(MenuItem::class.java)
    }

    // --- 3. MANAJEMEN KERANJANG (CART) ---
    fun listenCart(userId: String, onUpdate: (List<CartItem>) -> Unit): ValueEventListener {
        val r = ref("carts/$userId/items")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                onUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        r.addValueEventListener(listener)
        return listener
    }

    fun removeCartListener(userId: String, listener: ValueEventListener) {
        ref("carts/$userId/items").removeEventListener(listener)
    }

    suspend fun addToCart(userId: String, categoryId: String, menuId: String) {
        val itemRef = ref("carts/$userId/items/$menuId")
        val existing = itemRef.get().await().getValue(CartItem::class.java)
        val nextQty = (existing?.qty ?: 0) + 1
        itemRef.setValue(CartItem(menuId = menuId, categoryId = categoryId, qty = nextQty)).await()
    }

    suspend fun setCartQty(userId: String, categoryId: String, menuId: String, qty: Int) {
        val itemRef = ref("carts/$userId/items/$menuId")
        if (qty <= 0) {
            itemRef.removeValue().await()
        } else {
            itemRef.setValue(CartItem(menuId = menuId, categoryId = categoryId, qty = qty)).await()
        }
    }

    // Fungsi pembersihan: Menghapus node items agar listener mendeteksi perubahan ke kosong
    suspend fun clearCart(userId: String) {
        ref("carts/$userId/items").removeValue().await()
    }

    // --- 4. MANAJEMEN PESANAN (ORDER) ---
    fun listenOrders(userId: String, onUpdate: (List<Order>) -> Unit): ValueEventListener {
        val r = ref("orders/$userId")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                    .sortedByDescending { it.timestamp }
                onUpdate(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        r.addValueEventListener(listener)
        return listener
    }

    fun removeOrdersListener(userId: String, listener: ValueEventListener) {
        ref("orders/$userId").removeEventListener(listener)
    }

    suspend fun createOrderFromCart(userId: String, cartUi: List<CartUiItem>, status: String): String {
        if (cartUi.isEmpty()) return ""

        val orderId = ref("orders/$userId").push().key ?: return ""
        val lines = linkedMapOf<String, OrderLine>()
        var total = 0L

        cartUi.forEach { c ->
            val line = OrderLine(menuId = c.menuId, name = c.name, price = c.price, qty = c.qty)
            lines[c.menuId] = line
            total += c.price * c.qty
        }

        val order = Order(
            id = orderId,
            timestamp = System.currentTimeMillis(),
            status = status, // PAID atau MENUNGGU PEMBAYARAN
            total = total,
            items = lines
        )

        // 1. Simpan Pesanan ke Firebase
        ref("orders/$userId/$orderId").setValue(order).await()

        // 2. Update Poin User (1 poin tiap Rp 1.000)
        val earnedPoints = (total / 1000)
        val userRef = ref("users/$userId")
        val currentPoints = userRef.get().await().child("points").getValue(Long::class.java) ?: 0L
        userRef.child("points").setValue(currentPoints + earnedPoints).await()

        // 3. BERSIHKAN KERANJANG agar tidak tersangkut data lama
        clearCart(userId)

        return orderId
    }
}