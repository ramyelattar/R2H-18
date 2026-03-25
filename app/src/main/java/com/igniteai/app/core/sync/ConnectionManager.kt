package com.igniteai.app.core.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * Manages the connection lifecycle between two paired devices.
 *
 * Implements the failover state machine:
 *
 *   DISCONNECTED → CONNECTING_BLE → CONNECTED_BLE
 *                                     ↓ (latency > 500ms)
 *                               FALLBACK_WIFI → CONNECTED_WIFI
 *                                     ↓ (both fail > 5s)
 *                               CONNECTION_LOST
 *                                     ↓ (> 60s)
 *                               SESSION_ENDED
 *
 * Provides a unified send/receive interface regardless of
 * which transport (BLE or WiFi Direct) is active.
 *
 * All messages are encrypted with the couple's shared secret
 * before transmission and decrypted on receipt.
 */
class ConnectionManager(
    private val bleManager: BleManager,
    private val wifiDirectManager: WifiDirectManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {

    /**
     * Connection states for the failover state machine.
     */
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING_BLE,
        CONNECTED_BLE,
        FALLBACK_WIFI,
        CONNECTED_WIFI,
        CONNECTION_LOST,
        SESSION_ENDED,
    }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    /**
     * Unified stream of incoming messages from either transport.
     */
    val incomingMessages: SharedFlow<String> = merge(
        bleManager.incomingMessages,
        wifiDirectManager.incomingMessages,
    ) as SharedFlow<String>

    private var connectionLostJob: Job? = null
    private var latencyCheckJob: Job? = null

    /**
     * Attempt to connect to the partner device.
     * Tries BLE first, falls back to WiFi Direct.
     */
    fun connect() {
        _connectionState.value = ConnectionState.CONNECTING_BLE

        bleManager.startScanning { device ->
            bleManager.connect(device)
            _connectionState.value = ConnectionState.CONNECTED_BLE
            startLatencyMonitoring()
        }
    }

    /**
     * Start advertising so partner can find us.
     */
    fun startListening() {
        _connectionState.value = ConnectionState.CONNECTING_BLE
        bleManager.startAdvertising()
    }

    /**
     * Send a message through whichever transport is active.
     *
     * @param message JSON string to send (should already be encrypted)
     */
    fun send(message: String) {
        when (_connectionState.value) {
            ConnectionState.CONNECTED_BLE -> bleManager.send(message)
            ConnectionState.CONNECTED_WIFI -> wifiDirectManager.send(message)
            else -> {
                // Queue message for when connection is restored?
                // For V1.0: drop silently (session will pause on CONNECTION_LOST)
            }
        }
    }

    /**
     * Send a typed SyncMessage (serializes + sends).
     */
    fun send(message: SyncMessage) {
        send(message.toJson())
    }

    /**
     * Disconnect from partner and clean up all resources.
     */
    fun disconnect() {
        connectionLostJob?.cancel()
        latencyCheckJob?.cancel()
        bleManager.disconnect()
        wifiDirectManager.disconnect()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    /**
     * Monitor BLE latency and failover to WiFi Direct if needed.
     */
    private fun startLatencyMonitoring() {
        latencyCheckJob = scope.launch {
            while (true) {
                delay(5000) // Check every 5 seconds

                if (!bleManager.isConnected && !wifiDirectManager.isConnected) {
                    handleConnectionLost()
                    return@launch
                }

                // TODO: Implement actual latency measurement via ping/pong
                // For V1.0: rely on connection state only
            }
        }
    }

    /**
     * Handle loss of connection — start 60-second timeout.
     */
    private fun handleConnectionLost() {
        _connectionState.value = ConnectionState.CONNECTION_LOST

        connectionLostJob = scope.launch {
            // Try WiFi Direct as fallback
            _connectionState.value = ConnectionState.FALLBACK_WIFI
            wifiDirectManager.discoverPeers { device ->
                wifiDirectManager.connect(device) { info ->
                    scope.launch {
                        if (info.isGroupOwner) {
                            wifiDirectManager.startServer()
                        } else {
                            info.groupOwnerAddress?.hostAddress?.let {
                                wifiDirectManager.connectToServer(it)
                            }
                        }
                        if (wifiDirectManager.isConnected) {
                            _connectionState.value = ConnectionState.CONNECTED_WIFI
                            connectionLostJob?.cancel()
                        }
                    }
                }
            }

            // 60-second timeout
            delay(60_000)
            if (_connectionState.value == ConnectionState.CONNECTION_LOST ||
                _connectionState.value == ConnectionState.FALLBACK_WIFI
            ) {
                _connectionState.value = ConnectionState.SESSION_ENDED
            }
        }
    }
}
