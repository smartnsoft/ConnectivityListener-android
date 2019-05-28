package com.smartnsoft.connectivitylistener

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener
{

  private val connectivityListener: ConnectivityListener by lazy { ConnectivityListener(this) }

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    requestButton?.setOnClickListener(this)

    connectivityListener.observe(this, Observer<ConnectivityInformation> { connectionModel ->
      currentConnectivityState.text = when (connectionModel)
      {
        ConnectivityInformation.Wifi   -> "WIFI CONNECTED"
        ConnectivityInformation.Mobile -> "Mobile CONNECTED"
        else                           -> "NO internet"
      }
    })
  }

  override fun onClick(view: View?)
  {
    when (view)
    {
      requestButton -> requestedStatus?.text = when (connectivityListener.getConnectionInformation())
      {
        ConnectivityInformation.Wifi   -> "WIFI CONNECTED"
        ConnectivityInformation.Mobile -> "Mobile CONNECTED"
        else                           -> "NO internet"
      }
    }
  }
}
