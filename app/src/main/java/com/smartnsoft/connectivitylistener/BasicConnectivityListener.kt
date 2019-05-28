package com.smartnsoft.connectivitylistener

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.os.Build


/**
 * @author Thomas Ecalle
 * @since 2019.05.28
 */

enum class ConnectivityInformation(private val isConnected: Boolean)
{

  Wifi(true),
  Mobile(true),
  Other(true),
  Disconnected(false)
}

interface ConnectivityChangesListener
{

  fun onConnectivityNotification(connectivityInformation: ConnectivityInformation)
}

interface ConnectivityListener
{
  fun getConnectionInformation(): ConnectivityInformation
}

class BasicConnectivityListener(private val context: Context)
{

  private val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  private var networkCallback: ConnectivityManager.NetworkCallback? = null
  private var connectivityChangesListener: ConnectivityChangesListener? = null

  private val networkBroadcastReceiver = object : BroadcastReceiver()
  {
    override fun onReceive(context: Context, intent: Intent)
    {
      if (intent.extras != null)
      {
        val activeNetwork = intent.extras?.get(ConnectivityManager.EXTRA_NETWORK_INFO) as NetworkInfo
        connectivityChangesListener?.onConnectivityNotification(getNetworkType(activeNetwork))
      }
    }
  }

  fun setListener(connectivityChangesListener: ConnectivityChangesListener)
  {
    this.connectivityChangesListener = connectivityChangesListener
    this.connectivityChangesListener?.onConnectivityNotification(getNetworkType())
  }

  fun startListening()
  {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
    {
      registerBroadcastReceiverUnderLolipop()
    }
    else
    {
      registerBroadcastListenerOnLollipopAndAbove()
    }
  }

  fun stopListening() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      unregisterBroadcastListenerLollipopAndAbove()
    }
    else
    {
      unregisterBroadcastReceiverUnderLolipop()
    }
  }

  fun getConnectionInformation(): ConnectivityInformation
  {
    return getNetworkType(connectivityManager.activeNetworkInfo)
  }

  private fun registerBroadcastReceiverUnderLolipop()
  {
    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    context.registerReceiver(networkBroadcastReceiver, filter)
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  }

  private fun unregisterBroadcastReceiverUnderLolipop()
  {
    context.unregisterReceiver(networkBroadcastReceiver)
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private fun registerBroadcastListenerOnLollipopAndAbove()
  {
    val builder = NetworkRequest.Builder()
    networkCallback = object : ConnectivityManager.NetworkCallback()
    {

      override fun onCapabilitiesChanged(network: Network?, networkCapabilities: NetworkCapabilities?)
      {
        connectivityChangesListener?.onConnectivityNotification(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onUnavailable()
      {
        connectivityChangesListener?.onConnectivityNotification(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onAvailable(network: Network?)
      {
        connectivityChangesListener?.onConnectivityNotification(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onLost(network: Network?)
      {
        connectivityChangesListener?.onConnectivityNotification(getNetworkType(connectivityManager.activeNetworkInfo))
      }

    }

    connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private fun unregisterBroadcastListenerLollipopAndAbove()
  {
    connectivityManager.unregisterNetworkCallback(networkCallback)
  }

  private fun getNetworkType(activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo): ConnectivityInformation
  {
    return if (activeNetworkInfo == null || !activeNetworkInfo.isConnected)
    {
      ConnectivityInformation.Disconnected
    }
    else
    {
      when (activeNetworkInfo.type)
      {
        ConnectivityManager.TYPE_WIFI   -> ConnectivityInformation.Wifi
        ConnectivityManager.TYPE_MOBILE -> ConnectivityInformation.Mobile
        else                            -> ConnectivityInformation.Other
      }
    }

  }
}