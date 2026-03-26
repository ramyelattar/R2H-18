package com.igniteai.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Payment license for a premium level.
 *
 * Stored locally after Stripe payment verification.
 * Tied to this device — each device needs its own purchase.
 */
@Entity(tableName = "license_key")
data class LicenseKey(
    @PrimaryKey val id: String,
    val level: String,              // "FIRE", (future: "INFERNO", "ETERNAL")
    val key: String,                // License key from Stripe
    val purchasedAt: Long,
    val deviceId: String,           // Android device ID this license is tied to
    val stripeTransactionId: String,
)
