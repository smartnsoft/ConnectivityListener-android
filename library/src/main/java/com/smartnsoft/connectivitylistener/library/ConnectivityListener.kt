package com.smartnsoft.connectivitylistener.library

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.os.Build
import com.smartnsoft.connectivitylistener.library.ConnectivityInformation.*


/**
 * @author Thomas Ecalle
 * @since 2019.05.28
 */

/**
 * An enum that defines the type of network the device is connected to and if it represents a "connected" state
 *
 *
 * [Wifi] - A Wifi network
 *
 * [Mobile] - A Mobile network
 *
 * [Other] - Other kind of networks
 *
 * @param [isConnected] Does the network have an internet connection or not
 */
enum class ConnectivityInformation(private val isConnected: Boolean) {
    Wifi(true),
    Mobile(true),
    Other(true),
    Disconnected(false)
}

/**
 * An interface that is called whenever the network's type change
 */
interface OnConnectivityInformationChangedListener {

    /**
     * Function called whenever the network's change
     *
     * @param [connectivityInformation] - the new [ConnectivityInformation] available
     */
    fun onConnectivityInformationChanged(connectivityInformation: ConnectivityInformation)
}

/**
 * The class that is reponsible for listening to network's changes
 *
 * After instanciating it with a context, the developer can register to its changes and listent for networks changes.
 *
 * We also can request the network [ConnectivityInformation] a any time
 *
 * @param [context] - A Valid context (your Zctivity or Fragment, for example)
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
open class ConnectivityListener(private val context: Context) {

    val isBackgroundDownloadAllowedByUser: Boolean
        get() {
            return when (currentDataSaverMode) {
                // Background data usage is blocked for this app. Wherever possible,
                // the app should also use less data in the foreground.
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED -> return false
                // The app is whitelisted. Wherever possible,
                // the app should use less data in the foreground and background.
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED,
                    // Data Saver is disabled. Since the device is connected to an
                    // unmetered network, the app should use less data wherever possible.
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED -> return true
                else -> true
            }
        }

    val currentDataSaverMode: Int
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val connMgr = this.connectivityManager
                // Checks if the device is on a metered network
                if (connMgr.isActiveNetworkMetered) {
                    // Checks user’s Data Saver settings.
                    return connMgr.restrictBackgroundStatus
                }
            }
            // The device is not running Android N or not on a metered network.
            // Use data as required to perform syncs, downloads, and updates.
            return 1
        }

    protected val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var onConnectivityInformationChangedListener: OnConnectivityInformationChangedListener? = null

    private val networkBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.extras != null) {
                val activeNetwork = intent.extras?.get(ConnectivityManager.EXTRA_NETWORK_INFO) as NetworkInfo
                onConnectivityInformationChangedListener?.onConnectivityInformationChanged(getNetworkType(activeNetwork))
            }
        }
    }

    /**
     * Method used to start listening to the network changes by setting a [OnConnectivityInformationChangedListener]
     *
     * The current [ConnectivityInformation] is directly sent when setting the listener
     *
     * @param[onConnectivityInformationChangedListener] - the interface that will be called at each network change
     */
    fun setListener(onConnectivityInformationChangedListener: OnConnectivityInformationChangedListener) {
        this.onConnectivityInformationChangedListener = onConnectivityInformationChangedListener
        this.onConnectivityInformationChangedListener?.onConnectivityInformationChanged(getNetworkType())
    }

    /**
     * Method used to register the Listener
     *
     * Pay attention to call this method in an "Active" LifeCycle state like in onCreate of an activity
     *
     * Note that you have to specifically set the [OnConnectivityInformationChangedListener] with the method [setListener] in order tp have callbacks on network changes
     */
    fun register() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            registerBroadcastReceiverUnderLolipop()
        } else {
            registerBroadcastListenerOnLollipopAndAbove()
        }
    }

    /**
     * Method used to unregister the listener
     *
     * Pay attention to unregister the listen when you lose the context (in the onDestroy method for example)
     */
    fun unregister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unregisterBroadcastListenerLollipopAndAbove()
        } else {
            unregisterBroadcastReceiverUnderLolipop()
        }
        this.onConnectivityInformationChangedListener = null
    }

    /**
     * Method used to request the current connection information
     *
     * @return[ConnectivityInformation] - The current [ConnectivityInformation]
     */
    fun getConnectionInformation(): ConnectivityInformation {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var capabilities: NetworkCapabilities? = null
            connectivityManager.allNetworks?.forEach { network ->
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities == null && networkCapabilities.isConnected()) {
                    capabilities = networkCapabilities
                }
            }
            getNetworkType(connectivityManager.activeNetworkInfo, capabilities).run {
                onConnectivityInformationChangedListener?.onConnectivityInformationChanged(this)
                this
            }
        } else {
            getNetworkType(connectivityManager.activeNetworkInfo).run {
                onConnectivityInformationChangedListener?.onConnectivityInformationChanged(this)
                this
            }
        }
    }

    private fun registerBroadcastReceiverUnderLolipop() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkBroadcastReceiver, filter)
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun unregisterBroadcastReceiverUnderLolipop() {
        context.unregisterReceiver(networkBroadcastReceiver)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun registerBroadcastListenerOnLollipopAndAbove() {
        val builder = NetworkRequest.Builder()
        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onCapabilitiesChanged(network: Network?, networkCapabilities: NetworkCapabilities?) {
                onConnectivityInformationChangedListener?.onConnectivityInformationChanged(
                    getNetworkType(
                        connectivityManager.activeNetworkInfo,
                        networkCapabilities
                    )
                )
            }

            override fun onUnavailable() {
//                onConnectivityInformationChangedListener?.onConnectivityInformationChanged(
//                    getNetworkType(
//                        connectivityManager.activeNetworkInfo
//                    )
//                )
            }

            override fun onAvailable(network: Network?) {
                onConnectivityInformationChangedListener?.onConnectivityInformationChanged(
                    getNetworkType(
                        connectivityManager.activeNetworkInfo,
                        network?.let { network ->
                            return@let connectivityManager.getNetworkCapabilities(network)
                        }
                    )
                )
            }

            override fun onLost(network: Network?) {
                connectivityManager.getNetworkCapabilities(network)
                onConnectivityInformationChangedListener?.onConnectivityInformationChanged(
                    getNetworkType(
                        connectivityManager.activeNetworkInfo,
                        network?.let { network ->
                            return@let connectivityManager.getNetworkCapabilities(network)
                        }
                    )
                )
            }

        }

        connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun unregisterBroadcastListenerLollipopAndAbove() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun getNetworkType(
        activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
    ): ConnectivityInformation {
        return if (activeNetworkInfo == null || !activeNetworkInfo.isConnected) {
            ConnectivityInformation.Disconnected
        } else {
            when (activeNetworkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> ConnectivityInformation.Wifi
                ConnectivityManager.TYPE_MOBILE -> ConnectivityInformation.Mobile
                else -> ConnectivityInformation.Other
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun NetworkCapabilities.isConnected(): Boolean {
        return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || hasCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND))
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getNetworkType(
        activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        , networkCapabilities: NetworkCapabilities?
    ): ConnectivityInformation {
        val isConnected = networkCapabilities?.isConnected() ?: false

        return if (isConnected.not()) {
            ConnectivityInformation.Disconnected
        } else {
            when (activeNetworkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> ConnectivityInformation.Wifi
                ConnectivityManager.TYPE_MOBILE -> ConnectivityInformation.Mobile
                else -> ConnectivityInformation.Other
            }
        }

    }
}