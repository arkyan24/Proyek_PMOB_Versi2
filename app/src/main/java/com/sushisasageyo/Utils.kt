package com.sushisasageyo

import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Locale
import java.util.Date

object Utils {

    /**
     * Mengubah angka (Long) menjadi format mata uang Rupiah.
     * Contoh: 30000 -> Rp 30.000
     */
    fun rupiah(value: Long): String {
        // Menggunakan "id" untuk standar Locale Indonesia yang lebih modern
        val nf = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return nf.format(value).replace(",00", "").replace("Rp", "Rp ")
    }

    /**
     * Menghitung poin yang didapat dari total belanja.
     * Logika: Setiap kelipatan Rp 1.000 mendapatkan 1 poin.
     */
    fun pointsFromTotal(totalSpent: Long): Long {
        return totalSpent / 1000L
    }

    /**
     * Mengubah Long timestamp menjadi format tanggal dan waktu yang mudah dibaca.
     * Contoh: 1704153600000 -> 02 Jan 2026, 12:47
     */
    fun formatTimestamp(timestamp: Long): String {
        // Format: dd (tanggal), MMM (nama bulan singkat), yyyy (tahun), HH:mm (jam:menit)
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val date = Date(timestamp)
        return sdf.format(date)
    }
}