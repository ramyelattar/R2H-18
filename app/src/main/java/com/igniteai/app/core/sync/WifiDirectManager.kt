package com.igniteai.app.core.sync

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * WiFi Direct fallback for couple sync.
 *
 * Used when BLE latency exceeds 500ms. WiFi Direct provides
 * higher throughput (~250 Mbps) and lower latency (~10-50ms)
 * but takes longer to establish a connection.
 *
 * Communication uses TCP sockets over the WiFi Direct group.
 * One device becomes the group owner (server socket),
 * the other connects as a client.
 */
@SuppressLint("MissingPermission")
class WifiDirectManager(private val context: Context) {

    companion object {
        private const val PORT = 8765
    }

    private val wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = wifiP2pManager.initialize(context, context.mainLooper, null)

    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    private var _isConnected = false
    val isConnected: Boolean get() = _isConnected

    /**
     * Discover nearby WiFi Direct peers.
     */
    fun discoverPeers(onPeerFound: (WifiP2pDevice) -> Unit) {
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                wifiP2pManager.requestPeers(channel) { peerList ->
                    peerList.deviceList.firstOrNull()?.let { onPeerFound(it) }
                }
            }

            override fun onFailure(reason: Int) {
                // Discovery failed
            }
        })
    }

    /**
     * Connect to a discovered peer.
     */
    fun connect(device: WifiP2pDevice, onConnected: (WifiP2pInfo) -> Unit) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                wifiP2pManager.requestConnectionInfo(channel) { info ->
                    onConnected(info)
                }
            }

            override fun onFailure(reason: Int) {
                // Connection failed
            }
        })
    }

    /**
     * Start listening for connections as group owner (server).
     */
    suspend fun startServer() = withContext(Dispatchers.IO) {
        try {
            serverSocket = ServerSocket(PORT)
            val socket = serverSocket?.accept() ?: return@withContext
            clientSocket = socket
            writer = PrintWriter(socket.getOutputStream(), true)
            _isConnected = true

            // Listen for incoming messages
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (_isConnected) {
                val line = reader.readLine() ?: break
                _incomingMessages.tryEmit(line)
            }
        } catch (e: Exception) {
            _isConnected = false
        }
    }

    /**
     * Connect to the group owner as a client.
     */
    suspend fun connectToServer(hostAddress: String) = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(hostAddress, PORT), 5000)
            clientSocket = socket
            writer = PrintWriter(socket.getOutputStream(), true)
            _isConnected = true

            // Listen for incoming messages
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (_isConnected) {
                val line = reader.readLine() ?: break
                _incomingMessages.tryEmit(line)
            }
        } catch (e: Exception) {
            _isConnected = false
        }
    }

    /**
     * Send a message to the connected partner.
     */
    fun send(message: String) {
        writer?.println(message)
    }

    /**
     * Disconnect and clean up.
     */
    fun disconnect() {
        _isConnected = false
        writer?.close()
        clientSocket?.close()
        serverSocket?.close()
        wifiP2pManager.removeGroup(channel, null)
    }
}
