package com.smartnsoft.connectivitylistener

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class RegistrableActivity : AppCompatActivity(), View.OnClickListener, ConnectivityChangesListener
{

  private val registrableConnectivityListener: RegistrableConnectivityListener by lazy { RegistrableConnectivityListener(this) }

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    requestButton?.setOnClickListener(this)

    registrableConnectivityListener.register()
    registrableConnectivityListener.setListener(this)
  }

  override fun onConnectivityNotification(connectivityInformation: ConnectivityInformation)
  {
    runOnUiThread {
      currentConnectivityState.text = when (connectivityInformation)
      {
        ConnectivityInformation.Wifi   -> "WIFI CONNECTED"
        ConnectivityInformation.Mobile -> "Mobile CONNECTED"
        else                           -> "NO internet"
      }
    }
  }

  override fun onClick(view: View?)
  {
    when (view)
    {
      requestButton -> requestedStatus?.text = when (registrableConnectivityListener.getConnectionInformation())
      {
        ConnectivityInformation.Wifi   -> "WIFI CONNECTED"
        ConnectivityInformation.Mobile -> "Mobile CONNECTED"
        else                           -> "NO internet"
      }
    }
  }

  override fun onDestroy()
  {
    registrableConnectivityListener.unRegister()
    super.onDestroy()
  }
}
