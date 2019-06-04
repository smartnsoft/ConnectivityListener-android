package com.smartnsoft.connectivitylistener.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.smartnsoft.connectivitylistener.library.ConnectivityInformation
import com.smartnsoft.connectivitylistener.library.ConnectivityListener
import com.smartnsoft.connectivitylistener.library.OnConnectivityInformationChangedListener
import kotlinx.android.synthetic.main.activity_main.*

class RegistrableActivity : AppCompatActivity(), View.OnClickListener,
    OnConnectivityInformationChangedListener {

    private val registrableConnectivityListener: ConnectivityListener by lazy {
        ConnectivityListener(
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestButton?.setOnClickListener(this)

        registrableConnectivityListener.register()
        registrableConnectivityListener.setListener(this)
    }

    override fun onConnectivityInformationChanged(connectivityInformation: ConnectivityInformation) {
        runOnUiThread {
            setConnectivityText(currentConnectivityState, connectivityInformation)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            requestButton -> {
                setConnectivityText(requestedStatus, registrableConnectivityListener.getConnectionInformation())
            }
        }
    }

    override fun onDestroy() {
        registrableConnectivityListener.unregister()
        super.onDestroy()
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
            "Background network calls are ${if (registrableConnectivityListener.restrictBackgroundStatus.isRestricted) "DENIED" else "ALLOWED"}"

        textView?.text =
            "$connectionStatus\n$dataSaverStatus (${registrableConnectivityListener.restrictBackgroundStatus})"
    }
}
