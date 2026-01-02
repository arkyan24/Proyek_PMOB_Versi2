package com.sushisasageyo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.database.ValueEventListener
import com.sushisasageyo.databinding.ActivityMainBinding
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private lateinit var navController: NavController
    private val repo = FirebaseRepository()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var user: User? = User(id = "guest_user", name = "Sobat Sushi", email = "guest@sushi.com")
    private var cartListener: ValueEventListener? = null
    private var cartUiCache: List<CartUiItem> = emptyList()
    private var isCartNotEmpty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = navHost.navController
        b.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateCartBarVisibility(destination.id)
        }

        b.cartBarRoot.root.setOnClickListener {
            b.bottomNav.selectedItemId = R.id.nav_order
        }

        // Mulai memantau keranjang untuk guest_user
        startCartObserver("guest_user")
    }

    private fun startCartObserver(userId: String) {
        cartListener = repo.listenCart(userId) { cartItems ->
            val totalQty = cartItems.sumOf { it.qty }
            isCartNotEmpty = totalQty > 0

            updateCartBarVisibility(navController.currentDestination?.id ?: -1)
            b.cartBarRoot.tvCartCount.text = "$totalQty items"

            // FIX: Jika items kosong, reset cache secara instan agar bisa order lagi
            if (cartItems.isEmpty()) {
                cartUiCache = emptyList()
                b.cartBarRoot.tvCartTotal.text = Utils.rupiah(0)
                return@listenCart
            }

            scope.launch {
                val joined = withContext(Dispatchers.IO) {
                    cartItems.mapNotNull { c ->
                        val m = repo.getMenu(c.categoryId, c.menuId) ?: return@mapNotNull null
                        CartUiItem(c.menuId, c.categoryId, m.name, m.price, c.qty)
                    }
                }
                cartUiCache = joined
                b.cartBarRoot.tvCartTotal.text = Utils.rupiah(joined.sumOf { it.price * it.qty })
            }
        }
    }

    // Fungsi untuk memaksa pembersihan cache dari Fragment
    fun refreshCartCache() {
        cartUiCache = emptyList()
        isCartNotEmpty = false
        updateCartBarVisibility(navController.currentDestination?.id ?: -1)
    }

    private fun updateCartBarVisibility(currentDestinationId: Int) {
        val isOrderPage = currentDestinationId == R.id.nav_order || currentDestinationId == R.id.orderFragment
        if (isCartNotEmpty && !isOrderPage) {
            b.cartBarRoot.root.visibility = View.VISIBLE
            b.cartBarRoot.root.animate().translationY(0f).setDuration(300).start()
        } else {
            b.cartBarRoot.root.animate().translationY(b.cartBarRoot.root.height.toFloat() + 200f)
                .setDuration(300).withEndAction { b.cartBarRoot.root.visibility = View.GONE }.start()
        }
    }

    fun getRepo() = repo
    fun getUser() = user
    fun getCartUiCache() = cartUiCache

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}