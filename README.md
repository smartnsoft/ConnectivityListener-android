# Welcome to the ConnectivityListener project

ConnectivityListener is a simple library that notifies you about connectivity changes !

## What does it enable you to do ?

ConnectivityListener enable you to :

1. Get current connectivity's state
2. Listen for connectivity changes

## How does it work ?

We wanted to make ConnectivityListener as simple as possible to use, so we wrapped it into a LiveData to prevent you from managing any LifeCycle.

1. In order to get the current connectivity's state :

``` 
ConnectivityListener(context).isConnected()
```

2. In order to listen for connectivity changes :

``` 
ConnectivityListener(context).observe(this, Observer<ConnectivityInformations> { connectionModel ->
      when (connectionModel?.isConnected)
      {
        true -> // Do something when network is available
        else -> // Do something when network is no longer available :(
      }
    })
```

## Complete example in simple activity :

```
class MainActivity : AppCompatActivity(), View.OnClickListener
{

  private val connectivityListener: ConnectivityListener by lazy { ConnectivityListener(this) }

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    requestButton?.setOnClickListener(this)

    connectivityListener.observe(this, Observer<ConnectivityInformations> { connectionModel ->
      currentConnectivityState.text = when (connectionModel?.isConnected)
      {
        true -> getString(R.string.status_connected)
        else -> getString(R.string.status_disconnected)
      }
    })
  }

  override fun onClick(view: View?)
  {
    when (view)
    {
      requestButton -> requestedStatus?.text = when (connectivityListener.isConnected())
      {
        true -> getString(R.string.status_connected)
        else -> getString(R.string.status_disconnected)
      }
    }
  }
}
```

