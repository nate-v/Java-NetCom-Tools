# **JAVA NETCOM TOOLS**

*Java NetCom Tools is a small bundle of classes that simplify the process of UDP networking across multiple machines in Java. It is an open-source program written entirely in Java. This project was written alongside my high school senior CS final project, feel free to use this library for your own programs, and as a case study.*

*Java NetCom Tools is created by an ameteur programmer who is still learning, so you may notice inefficiencies or bugs. Bug reports, issues, and suggestions are extremely helpful.*

## NETWORK BROADCASTER

### NetworkBroadcaster (constructor)
   ```
    NetworkBroadcaster <name> = new NetworkBroadcaster(<contents>)
    <contents> = 
        (String ip, int port)
     or (String ip, int port, DatagramSocket socket)
     or (InetAddress serverAddress, int port)
     or (InetAddress serverAddress, int port, DatagramSocket, socket)
   ```
> *Including a DatagramSocket can be useful for sharing sockets across classes, which makes sending data back to a class, like in a server-client system, easier.*

### broadcast (void)
   ```
    <name>.broadcast(byte[] <data>)
   ```
> *This method broadcasts the input data to the specified IP and port in the form of a UDP packet. Data must be in the form of a byte array.*

### close (void)
```
   <name>.close();
```
> *Just closes the socket safely, good practice to use when shutting down your client.*

### Other accessors:
```
   getAddress()
   getAddressString()
   getPort()
   getSocket()
```
> *The toString method returns the following string:*
```
   Casting to: [ip]:[port]
```

## NETWORK LISTENER

### NetworkListener (constructor)
   ```
    NetworkListener <name> = new NetworkListener(<contents>)
    <contents> = 
        (int port, int packet_size, DataReceiver receiver)
     or (DatagramSocket socket, int packet_size, DataReceiver receiver)
   ```

### start (void)
   ```
    <name>.start()
   ```
> *Starts the network listener.*

### shutdown (boolean)
```
    <name>.shutdown()
```
> *Shuts down the network listener.*

### restart (void)
```
   <name>.restart()
```
> *Restarts the network listener after being shutdown. Safe to use after update v1.2.4.*

### setReuseAddress (void)
```
   <name>.setReuseAddress(boolean b)
```
> *Changes the value of SO_REUSEADDR. When true, the socket is allowed to bind to ports that may already be in use or in TIME_WAIT. Does not actually change the state of socket address reuse during runtime, only prior to calling start(). See setReuseAddressRuntime() for directly changing this state during runtime. Not entirely necessary, but good practice to use if you will be restarting the NetworkListener a lot.*

### setReuseAddressRuntime (void)
```
   <name>.setReuseAddressRuntime(boolean b)
```
> *Changes the value of SO_REUSEADDR DURING RUNTIME. When true, the socket is allowed to bind to ports that may already be in use or in TIME_WAIT. Not entirely necessary, but good practice to use if you will be restarting the NetworkListener a lot.*

### setRestrictionSet (void)
```
   <name>.setRestrictionSet(Set<InetAddress> set)
```
> *Sets the set of restricted addresses.*

### SetRestrictionStringSet 
```
   <name.setRestrictionSet(Set<String> set)>
```
> *Identical to setRestrictionSet but accepts a set of Strings as a parameter*

### addRestricted (boolean)
```
   <name>.addRestricted(<contents>)
   <contents> = 
      String ip
   or InetAddress a
```
> *Adds an address into the set of restricted addresses. Returns true if added successfully, false if the set already contains that address.*

### removeRestricted (boolean)
```
   <name>.removeRestricted(<contents>)
   <contents> = 
      String ip
   or InetAddress a
```
> *Removes the specified address from the set of restricted address. Returns true if removed successfully, false if the set did not contain that address.*

### setRestriction (void)
```
   <name>.setRestriction(int restriction)

   Valid restrictions:
   -1: Disabled
   0:  Blacklist
   1:  Whitelist
```
> *Sets the type of restriction to be applied to the NetworkListener.*

### getRestriction (int)
```
   <name>.getRestriction()
```
> *Returns the type of restriction applied to the NetworkListener.*

### getRestrictionSet (Set)
```
   <name>.getRestrictionSet()
```
> *Returns the set of InetAddresses that are restricted.*

### getRestrictionStringSet (Set)
```
   <name>.getRestrictionStringSet()
```
> *Returns the set of InetAddresses in the form of Strings that are restricted.*

### isRestricted (boolean)
```
   <name>.isRestricted(<contents>)
   <contents> =
      String ip
   or InetAddress a
```
> *Returns true if the specified address is in the set of restricted addresses, false if otherwise.*

### restrictedAddressesSize (int)
```
   <name>.restrictedAddressesSize()
```
> *Returns the size of the set of restricted addresses.*

### Other Accessors:
```
   getPort()
   getPacketBufferSize()
   isActive()
   getSocket()
   getDataReceiver()
   getSocketReuseAddress()       // Returns the true state, cannot be used while listener is closed.
   getSocketReuseAddressFlag()   // Returns the flag state, for use while listener is closed.
```

> *The toString method returns the following string:*
```
[CLOSED/LISTENING] on port [port]. [DISABLED/WHITELISTING/BLACKLISTING] [# of restricted addresses] addresses.
```

# DATA RECEIVER

### onReceive (void)
*Must be modified via an abstract class.*
```
   Example:
    NetworkListener nl = new NetworkListener(serverport, 1024, new DataReceiver() {
            @Override
            public void onReceive(byte[] data, int offset, int length, InetAddress address, int port) {
                NetworkBroadcaster nb = new NetworkBroadcaster(address, port);

                String message = "recieved: " + new String(data);
                nb.broadcast(message.getBytes());
            }
        });
```
## TROUBLESHOOTING

### Common fixes:
```
     - Allow your JDK through the firewall.
     - Create a new inbound rule to allow specific
       ports through the firewall.
```
```
/**
 * author Nate Vailikit
 * created on 5-22-2026
 * github: https://github.com/nate-v
 * copyright 2026
 **/
 ```