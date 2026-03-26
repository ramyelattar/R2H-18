package com.igniteai.app.core.sync

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

/**
 * Manages Bluetooth Low Energy connections for couple sync.
 *
 * Uses a custom GATT service with a single read/write characteristic
 * for exchanging SyncMessages. One device acts as GATT server
 * (advertiser), the other as GATT client (scanner).
 *
 * Flow:
 * 1. Partner A starts advertising (GATT server)
 * 2. Partner B scans and connects (GATT client)
 * 3. Both can send/receive messages via the characteristic
 */
@SuppressLint("MissingPermission") // Permissions checked by ConnectionManager before calling
class BleManager(private val context: Context) {

    companion object {
        // Custom UUIDs for IgniteAI BLE service
        val SERVICE_UUID: UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567891")
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var gattServer: BluetoothGattServer? = null
    private var gattClient: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    private var _isConnected = false
    val isConnected: Boolean get() = _isConnected

    // ── Server (Advertiser) ─────────────────────────────────

    /**
     * Start advertising as a GATT server. Partner will find us via scan.
     */
    fun startAdvertising() {
        val advertiser = bluetoothAdapter?.bluetoothLeAdvertiser ?: return

        // Set up GATT server
        gattServer = bluetoothManager.openGattServer(context, serverCallback)
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or
                BluetoothGattCharacteristic.PROPERTY_WRITE or
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or
                BluetoothGattCharacteristic.PERMISSION_WRITE,
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)

        // Start advertising
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0) // Advertise indefinitely
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(false) // Privacy: don't broadcast device name
            .build()

        advertiser.startAdvertising(settings, data, advertiseCallback)
    }

    /**
     * Stop advertising and close GATT server.
     */
    fun stopAdvertising() {
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close()
        gattServer = null
    }

    // ── Client (Scanner) ────────────────────────────────────

    /**
     * Scan for a partner device advertising our service UUID.
     *
     * @param onDeviceFound Called when partner device is discovered
     */
    fun startScanning(onDeviceFound: (BluetoothDevice) -> Unit) {
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(listOf(filter), settings, object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                scanner.stopScan(this)
                onDeviceFound(result.device)
            }
        })
    }

    /**
     * Connect to a discovered partner device as GATT client.
     */
    fun connect(device: BluetoothDevice) {
        gattClient = device.connectGatt(context, false, clientCallback)
    }

    // ── Messaging ───────────────────────────────────────────

    /**
     * Send a message to the connected partner.
     *
     * @param message JSON string (already encrypted by ConnectionManager)
     */
    fun send(message: String) {
        val bytes = message.toByteArray(Charsets.UTF_8)

        // Send via GATT server if we're the advertiser
        gattServer?.let { server ->
            connectedDevice?.let { device ->
                val service = server.getService(SERVICE_UUID)
                val char = service?.getCharacteristic(CHARACTERISTIC_UUID)
                char?.value = bytes
                server.notifyCharacteristicChanged(device, char, false)
                return
            }
        }

        // Send via GATT client if we're the scanner
        gattClient?.let { client ->
            val service = client.getService(SERVICE_UUID)
            val char = service?.getCharacteristic(CHARACTERISTIC_UUID)
            char?.value = bytes
            client.writeCharacteristic(char)
        }
    }

    /**
     * Disconnect and clean up all BLE resources.
     */
    fun disconnect() {
        _isConnected = false
        connectedDevice = null
        gattClient?.close()
        gattClient = null
        gattServer?.close()
        gattServer = null
    }

    // ── Callbacks ───────────────────────────────────────────

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            // Advertising started successfully
        }

        override fun onStartFailure(errorCode: Int) {
            // Handle advertising failure
        }
    }

    private val serverCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
                _isConnected = true
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice = null
                _isConnected = false
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray,
        ) {
            val message = String(value, Charsets.UTF_8)
            _incomingMessages.tryEmit(message)
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
        }
    }

    private val clientCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _isConnected = true
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _isConnected = false
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            // Services discovered — ready to send/receive
            val service = gatt.getService(SERVICE_UUID)
            val char = service?.getCharacteristic(CHARACTERISTIC_UUID)
            if (char != null) {
                gatt.setCharacteristicNotification(char, true)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            val message = String(characteristic.value, Charsets.UTF_8)
            _incomingMessages.tryEmit(message)
        }
    }
}
