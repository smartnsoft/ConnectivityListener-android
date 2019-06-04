package com.smartnsoft.connectivitylistener.sample

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.smartnsoft.connectivitylistener.library.ConnectivityInformation
import com.smartnsoft.livedataconnectivitylistener.LiveDataConnectivityListener
import kotlinx.android.synthetic.main.activity_main.*

class LiveDataActivity : AppCompatActivity(), View.OnClickListener
{

  private val liveDataConnectivityListener: LiveDataConnectivityListener by lazy {
    LiveDataConnectivityListener(
        this
    )
  }

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    requestButton?.setOnClickListener(this)

    liveDataConnectivityListener.observe(this, Observer<ConnectivityInformation> { connectionModel ->
      currentConnectivityState.text = when (connectionModel)
      {
        ConnectivityInformation.WIFI   -> "WIFI CONNECTED"
        ConnectivityInformation.MOBILE -> "Mobile CONNECTED"
        else                           -> "NO internet"
      }.run {
        "$this : ${if (connectionModel?.isConnected?.not() != false) "not" else ""} connected}"
      }
    })
  }

  override fun onClick(view: View?)
  {
    when (view)
    {
      requestButton -> requestedStatus?.text = when (liveDataConnectivityListener.getConnectionInformation())
      {
        ConnectivityInformation.WIFI   -> "WIFI CONNECTED"
        ConnectivityInformation.MOBILE -> "Mobile CONNECTED"
        else                           -> "NO internet"
      }
    }
  }
}
