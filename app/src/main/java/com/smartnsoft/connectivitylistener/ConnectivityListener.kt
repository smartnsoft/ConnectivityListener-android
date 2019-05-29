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

interface OnConnectivityInformationChangedListener
{

  fun onConnectivityInformationChanged(connectivityInformation: ConnectivityInformation)
}

open class ConnectivityListener(private val context: Context)
{

  private val connectivityManager: ConnectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  private var networkCallback: ConnectivityManager.NetworkCallback? = null
  private var onConnectivityInformationChangedListener: OnConnectivityInformationChangedListener? = null

  private val networkBroadcastReceiver = object : BroadcastReceiver()
  {
    override fun onReceive(context: Context, intent: Intent)
    {
      if (intent.extras != null)
      {
        val activeNetwork = intent.extras?.get(ConnectivityManager.EXTRA_NETWORK_INFO) as NetworkInfo
        onConnectivityInformationChangedListener?.onConnectivityInformationChanged(getNetworkType(activeNetwork))
      }
    }
  }

  fun setListener(onConnectivityInformationChangedListener: OnConnectivityInformationChangedListener)
  {
    this.onConnectivityInformationChangedListener = onConnectivityInformationChangedListener
    this.onConnectivityInformationChangedListener?.onConnectivityInformationChanged(getNetworkType())
  }

  fun register()
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

  fun unregister()
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      unregisterBroadcastListenerLollipopAndAbove()
    }
    else
    {
      unregisterBroadcastReceiverUnderLolipop()
    }
    this.onConnectivityInformationChangedListener = null
  }

  fun getConnectionInformation(): ConnectivityInformation =
      getNetworkType(connectivityManager.activeNetworkInfo).run {
        onConnectivityInformationChangedListener?.onConnectivityInformationChanged(this)
        this
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
        onConnectivityInformationChangedListener?.onConnectivityInformationChanged(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onUnavailable()
      {
        onConnectivityInformationChangedListener?.onConnectivityInformationChanged(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onAvailable(network: Network?)
      {
        onConnectivityInformationChangedListener?.onConnectivityInformationChanged(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onLost(network: Network?)
      {
        onConnectivityInformationChangedListener?.onConnectivityInformationChanged(getNetworkType(connectivityManager.activeNetworkInfo))
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