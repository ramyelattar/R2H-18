package com.igniteai.app.data.repository

import android.content.Context
import android.provider.Settings
import com.igniteai.app.data.dao.LicenseDao
import com.igniteai.app.data.model.LicenseKey
import java.util.UUID

/**
 * Manages level unlock licenses (Stripe Payment Links integration).
 *
 * Payment flow:
 * 1. User taps "Unlock Fire" → opens browser with Stripe Payment Link
 * 2. Stripe collects payment and redirects to igniteai://payment?session_id=X
 * 3. App deep link handler receives session_id
 * 4. LicenseRepository stores license locally
 *
 * All license data is stored in the encrypted local database.
 * The only network call in the entire app is the Stripe payment verification.
 */
class LicenseRepository(
    private val licenseDao: LicenseDao,
    private val context: Context,
) {

    companion object {
        const val LEVEL_SPARK = "SPARK"
        const val LEVEL_FIRE = "FIRE"

        // Stripe Payment Link base URL (to be configured with actual Stripe link)
        const val STRIPE_PAYMENT_LINK_BASE = "https://buy.stripe.com/YOUR_PAYMENT_LINK"
    }

    /**
     * Check if a level is unlocked. Spark is always unlocked.
     */
    suspend fun isLevelUnlocked(level: String): Boolean {
        if (level == LEVEL_SPARK) return true
        return licenseDao.isLevelUnlocked(level)
    }

    /**
     * Check if Fire tier is unlocked.
     */
    suspend fun isFireUnlocked(): Boolean {
        return isLevelUnlocked(LEVEL_FIRE)
    }

    /**
     * Store a license after successful payment.
     */
    suspend fun storeLicense(level: String, stripeTransactionId: String) {
        val license = LicenseKey(
            id = UUID.randomUUID().toString(),
            level = level,
            key = UUID.randomUUID().toString(),
            purchasedAt = System.currentTimeMillis(),
            deviceId = getDeviceId(),
            stripeTransactionId = stripeTransactionId,
        )
        licenseDao.insert(license)
    }

    /**
     * Get a consistent device ID for Stripe client_reference_id.
     */
    @Suppress("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: UUID.randomUUID().toString()
    }

    /**
     * Build the Stripe Payment Link URL with device tracking.
     */
    fun buildPaymentUrl(level: String): String {
        val deviceId = getDeviceId()
        return "$STRIPE_PAYMENT_LINK_BASE?client_reference_id=$deviceId&prefilled_email="
    }
}
