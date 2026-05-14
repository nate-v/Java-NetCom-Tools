# **JAVA NETCOM TOOLS**

*Java NetCom Tools is a small bundle of classes that simplify the process of UDP networking across multiple machines in Java. It is an open-source program written entirely in Java.*

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

## NETWORK LISTENER

### NetworkListener (constructor)
   ```
    NetworkListener <name> = new NetworkListener(<contents>)
    <contents> = 
        (int port, int packet_size, DataReceiver receiver)
     or (DatagramSocket socket, int packet_size, DataReceiver receiver)
   ```

## start (void)
   ```
    <name>.start()
   ```
> *Starts the network listener.*

### shutdown (boolean)
```
    <name>.shutdown()
```
> *Shuts down the network listener.*

# DATA RECEIVER

### onReceive (void)
*Must be modified via an abstract class.*
```
   Example:
    NetworkListener nl = new NetworkListener(serverport, 1024, new DataReceiver() {
            @Override
            public void onReceive(byte[] data, InetAddress address, int port) {
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

© 2026 Nathan Vailikit. All rights reserved.
