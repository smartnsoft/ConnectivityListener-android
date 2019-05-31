# Welcome to the ConnectivityListener project

ConnectivityListener is a simple library that notifies you about connectivity changes !

## What does it enable you to do ?

ConnectivityListener enable you to :

1. Get current connectivity's state
2. Listen for connectivity changes

## How does it work ?

We wanted to make ConnectivityListener as simple as possible to use, so we created 2 libraries in order to make you use the more suitable for your use case.

### The basic `ConnectivityListener`

This is the most basic implementation. It let the user responsible to `register` and `unregister` the listener

Steps are : 

```
implementation("com.smartnsoft:connectivitylistener:1.0-SNAPSHOT")
```

1.Register the Listener in an `active` LifeCycle :

```
connectivityListener = ConnectivityListener(this)
connectivityListener.register()
```

2.Set the listener :

```
connectivityListener.setListener(this)
```

3.Do something with the informations about connectivity :

```
override fun onConnectivityInformationChanged(connectivityInformation: ConnectivityInformation)
  {
    runOnUiThread {
      when (connectivityInformation)
      {
        ConnectivityInformation.Wifi  -> // WIFI CONNECTED
        ConnectivityInformation.Mobile -> // Mobile CONNECTED
        else                           -> // Mobile CONNECTED
      }
    }
  }

```

4.Unregister the listener when your app goes in an `inactive` Lifecycle :

```
connectivityListener.unregister();
```
    

In parallel, you can request the current connectivity information at any time :

```
when (connectivityListener.getConnectionInformation())
{
ConnectivityInformation.Wifi   -> // WIFI CONNECTED
ConnectivityInformation.Mobile -> // Mobile CONNECTED
else                           -> // Mobile CONNECTED
}
```


### The `LiveData ConnectivityListener` :

This library use the power of LiveData and the fact that they are LifeCycle aware in order to prevent the user from having to register and unregister the listener.

Steps are :

```
implementation("com.smartnsoft:livedataconnectivitylistener:1.0-SNAPSHOT")
```

1.Observe LiveData changes :

```
LiveDataConnectivityListener(this).observe(this, Observer<ConnectivityInformation> { connectionModel ->
    when (connectionModel)
    {
      ConnectivityInformation.Wifi   -> // WIFI CONNECTED
      ConnectivityInformation.Mobile -> // Mobile CONNECTED
      else                           -> // NO internet
    }
  })
```

In parallel, you can request the current connectivity information at any time :

```
when (connectivityListener.getConnectionInformation())
{
ConnectivityInformation.Wifi   -> // WIFI CONNECTED
ConnectivityInformation.Mobile -> // Mobile CONNECTED
else                           -> // Mobile CONNECTED
}
```


## Complete examples in simple activities :

Please refer to `app` module to see 2 implementations of the library corresponding to Basic and LiveData libraries.



