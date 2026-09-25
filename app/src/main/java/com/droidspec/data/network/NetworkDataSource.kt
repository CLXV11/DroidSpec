package com.droidspec.data.network

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.nfc.NfcManager
import android.hardware.usb.UsbManager
import com.droidspec.domain.model.NetworkInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkDataSource(context: Context) {
    private val app = context.applicationContext

    private val connectivityManager =
        app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager =
        app.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val bluetoothManager =
        app.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val nfcManager =
        app.getSystemService(Context.NFC_SERVICE) as? NfcManager
    private val usbManager =
        app.getSystemService(Context.USB_SERVICE) as? UsbManager
    private val packageManager = app.packageManager

    fun snapshot(net: Network? = null): NetworkInfo {
        val caps = net?.let { runCatching { connectivityManager.getNetworkCapabilities(it) }.getOrNull() }
        val transportKey = when {
            caps == null -> null
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "vpn"
            else -> "other"
        }
        return NetworkInfo(
            connected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
            transportKey = transportKey,
            linkDownstreamKbps = caps?.linkDownstreamBandwidthKbps,
            metered = caps?.let { !it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) },
            vpnActive = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN),
            wifiLinkSpeedMbps = runCatching { wifiManager?.connectionInfo?.linkSpeed }
                .getOrNull()?.takeIf { it > 0 },
            bluetoothSupported =
                packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            bluetoothEnabled = runCatching { bluetoothManager?.adapter?.isEnabled }.getOrNull(),
            nfcSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_NFC),
            nfcEnabled = runCatching { nfcManager?.defaultAdapter?.isEnabled }.getOrNull(),
            gpsSupported =
                packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS),
            usbSupported =
                packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST) ||
                    packageManager.hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY),
            usbDeviceCount = runCatching { usbManager?.deviceList?.size }.getOrNull()
        )
    }

    fun observe(): Flow<NetworkInfo> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(snapshot(network))
            }

            override fun onLost(network: Network) {
                trySend(snapshot(null))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(snapshot(network))
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        trySend(snapshot(null))
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
