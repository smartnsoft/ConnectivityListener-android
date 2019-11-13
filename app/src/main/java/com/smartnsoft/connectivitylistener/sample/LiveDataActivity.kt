package com.smartnsoft.connectivitylistener.sample

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.smartnsoft.connectivitylistener.library.ConnectivityInformation
import com.smartnsoft.connectivitylistener.livedata.LiveDataConnectivityListener
import kotlinx.android.synthetic.main.activity_main.*

class LiveDataActivity : AppCompatActivity(), View.OnClickListener {

    private val liveDataConnectivityListener: LiveDataConnectivityListener by lazy {
        LiveDataConnectivityListener(
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestButton?.setOnClickListener(this)

        liveDataConnectivityListener.observe(this, Observer<ConnectivityInformation> { connectionModel ->
            connectionModel?.also {
                setConnectivityText(currentConnectivityState, connectionModel)
            }
        })
    }

    override fun onClick(view: View?) {
        when (view) {
            requestButton -> setConnectivityText(requestedStatus, liveDataConnectivityListener.getConnectionInformation())
        }
    }

    private fun setConnectivityText(textView: TextView?, connectivityInformation: ConnectivityInformation) {
        val connectionStatus: CharSequence = when (connectivityInformation) {
            ConnectivityInformation.WIFI -> "WIFI CONNECTED"
            ConnectivityInformation.MOBILE -> "Mobile CONNECTED"
            else -> "NO internet"
        }.run {
            "$this : ${if (connectivityInformation.isConnected.not()) "not" else ""} connected"
        }

        val dataSaverStatus =
            "Background network calls are ${if (liveDataConnectivityListener.restrictBackgroundStatus.isRestricted) "DENIED" else "ALLOWED"}"

        textView?.text =
            "$connectionStatus\n$dataSaverStatus (${liveDataConnectivityListener.restrictBackgroundStatus})"
    }
}
