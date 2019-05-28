package com.smartnsoft.connectivitylistener

import android.annotation.TargetApi
import android.arch.lifecycle.LiveData
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

open class ConnectivityListener(private val context: Context) : LiveData<ConnectivityInformation>()
{

  private val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  private var networkCallback: ConnectivityManager.NetworkCallback? = null

  private val networkBroadcastReceiver = object : BroadcastReceiver()
  {
    override fun onReceive(context: Context, intent: Intent)
    {
      if (intent.extras != null)
      {
        val activeNetwork = intent.extras?.get(ConnectivityManager.EXTRA_NETWORK_INFO) as NetworkInfo
        postValue(getNetworkType(activeNetwork))
      }
    }
  }

  override fun onActive()
  {
    super.onActive()
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
    {
      registerBroadcastReceiverUnderLolipop()
    }
    else
    {
      registerBroadcastListenerOnLollipopAndAbove()
      postValue(getNetworkType(connectivityManager.activeNetworkInfo))
    }
  }

  override fun onInactive()
  {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
    {
      unregisterBroadcastListenerLollipopAndAbove()
    }
    else
    {
      unregisterBroadcastReceiverUnderLolipop()
    }
    super.onInactive()
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
        postValue(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onUnavailable()
      {
        postValue(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onAvailable(network: Network?)
      {
        postValue(getNetworkType(connectivityManager.activeNetworkInfo))
      }

      override fun onLost(network: Network?)
      {
        postValue(getNetworkType(connectivityManager.activeNetworkInfo))
      }

    }

    connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private fun unregisterBroadcastListenerLollipopAndAbove()
  {
    connectivityManager.unregisterNetworkCallback(networkCallback)
  }

  private fun getNetworkType(activeNetworkInfo: NetworkInfo?): ConnectivityInformation
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