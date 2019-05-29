package com.smartnsoft.connectivitylistener

import android.arch.lifecycle.LiveData
import android.content.Context


/**
 * @author Thomas Ecalle
 * @since 2019.05.28
 */
open class LiveDataConnectivityListener(context: Context) : LiveData<ConnectivityInformation>() {

    private val connectivityListener = ConnectivityListener(context)

    fun getConnectionInformation(): ConnectivityInformation {
        return connectivityListener.getConnectionInformation()
    }

    override fun onActive() {
        super.onActive()
        connectivityListener.register()
        connectivityListener.setListener(object : OnConnectivityInformationChangedListener {
            override fun onConnectivityInformationChanged(connectivityInformation: ConnectivityInformation) {
                postValue(connectivityInformation)
            }
        })
    }

    override fun onInactive() {
        connectivityListener.unregister()
        super.onInactive()
    }
}