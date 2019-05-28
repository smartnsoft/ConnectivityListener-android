package com.smartnsoft.connectivitylistener

import android.arch.lifecycle.LiveData
import android.content.Context


/**
 * @author Thomas Ecalle
 * @since 2019.05.28
 */
open class LiveDataConnectivityListener(context: Context) : LiveData<ConnectivityInformation>(), ConnectivityChangesListener, ConnectivityListener
{

  private val connectivityListener = BasicConnectivityListener(context)

  override fun onConnectivityNotification(connectivityInformation: ConnectivityInformation)
  {
    postValue(connectivityInformation)
  }

  override fun getConnectionInformation(): ConnectivityInformation
  {
    return connectivityListener.getConnectionInformation()
  }

  override fun onActive()
  {
    super.onActive()
    connectivityListener.startListening()
    connectivityListener.setListener(this)
  }

  override fun onInactive()
  {
    connectivityListener.stopListening()
    super.onInactive()
  }
}