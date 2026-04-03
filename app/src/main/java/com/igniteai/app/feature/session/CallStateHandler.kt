package com.igniteai.app.feature.session

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.TelephonyManager

/**
 * Monitors phone call state to auto-pause sessions.
 *
 * When a call comes in (ringing or off-hook), fires onCallActive.
 * When the call ends (idle), fires onCallEnded.
 * Register in Activity/Service lifecycle, unregister on destroy.
 */
class CallStateHandler(
    private val onCallActive: () -> Unit,
    private val onCallEnded: () -> Unit,
) {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING,
                TelephonyManager.EXTRA_STATE_OFFHOOK -> onCallActive()
                TelephonyManager.EXTRA_STATE_IDLE -> onCallEnded()
            }
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        context.registerReceiver(receiver, filter)
    }

    fun unregister(context: Context) {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: IllegalArgumentException) {
            // Already unregistered
        }
    }
}
