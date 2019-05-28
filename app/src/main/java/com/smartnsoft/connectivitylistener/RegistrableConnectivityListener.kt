package com.smartnsoft.connectivitylistener

import android.content.Context


/**
 * @author Thomas Ecalle
 * @since 2019.05.28
 */


open class RegistrableConnectivityListener(context: Context) : ConnectivityListener
{

  private val connectivityListener = BasicConnectivityListener(context)

  override fun getConnectionInformation(): ConnectivityInformation
  {
    return connectivityListener.getConnectionInformation()
  }

  fun register()
  {
    connectivityListener.startListening()
  }

  fun unRegister()
  {
    connectivityListener.stopListening()
  }

  fun setListener(connectivityChangesListener: ConnectivityChangesListener)
  {
    connectivityListener.setListener(connectivityChangesListener)
  }
}