package com.smartnsoft.livedataconnectivitylistener

import android.arch.lifecycle.LiveData
import android.content.Context
import com.smartnsoft.connectivitylistener.library.ConnectivityInformation
import com.smartnsoft.connectivitylistener.library.ConnectivityListener
import com.smartnsoft.connectivitylistener.library.OnConnectivityInformationChangedListener


/**
 * @author Thomas Ecalle
 * @since 2019.05.28
 */
open class LiveDataConnectivityListener(context: Context) : LiveData<ConnectivityInformation>()
{

  private val connectivityListener = ConnectivityListener(context)

  /**
   * Method used to request the current [ConnectivityInformation]
   *
   * @return[ConnectivityInformation] - the current [ConnectivityInformation]
   */
  fun getConnectionInformation(): ConnectivityInformation
  {
    return connectivityListener.getConnectionInformation()
  }

  override fun onActive()
  {
    super.onActive()
    connectivityListener.register()
    connectivityListener.setListener(object : OnConnectivityInformationChangedListener
    {
      override fun onConnectivityInformationChanged(connectivityInformation: ConnectivityInformation)
      {
        postValue(connectivityInformation)
      }
    })
  }

  override fun onInactive()
  {
    connectivityListener.unregister()
    super.onInactive()
  }
}