/* ▄▄     ▄▄▄                 ▄▄▄             ▄▄                  
   ██▄   ██▀      █▄         █▀██  ██▀▀        ██              █▄ 
   ███▄  ██      ▄██▄          ██  ██       ▀▀ ██ ▀▀ ▄▄     ▀▀▄██▄
   ██ ▀█▄██ ▄▀▀█▄ ██ ▄█▀█▄     ██  ██ ▄▀▀█▄ ██ ██ ██ ██ ▄█▀ ██ ██ 
   ██   ▀██ ▄█▀██ ██ ██▄█▀     ██▄ ██ ▄█▀██ ██ ██ ██ ████   ██ ██ 
 ▀██▀    ██▄▀█▄██▄██▄▀█▄▄▄      ▀███▀▄▀█▄██▄██▄██▄██▄██ ▀█▄▄██▄██*/

import java.io.IOException;
import java.net.*;

public class NetworkBroadcaster {
    private final int port;
    private final InetAddress serverAddress;
    private final DatagramSocket socket;

    /*
        CONSTRUCTORS
            - socket is optional, if not provided a new socket is made.
            - socket may be used for sharing a socket across classes.
    
    */
    public NetworkBroadcaster(String ip, int port) {
        this(resolveAddress(ip), port, createSocket());
    }

    public NetworkBroadcaster(String ip, int port, DatagramSocket socket) {
        this(resolveAddress(ip), port, socket);
    }

    public NetworkBroadcaster(InetAddress serverAddress, int port) {
        this(serverAddress, port, createSocket());
    }

    public NetworkBroadcaster(InetAddress serverAddress, int port, DatagramSocket socket) {
        validateArguments(serverAddress, port, socket);
        this.serverAddress = serverAddress;
        this.port = port;
        this.socket = socket;
    }

    /*
        MAIN FUNCTIONALITY
    */
    public void broadcast(byte[] data) {
        if(data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, port);
            socket.send(packet);
        } catch(IOException e) {
            throw new IllegalStateException("Failed to send UDP packet", e);
        }
    }

    /*
        CLOSURE AND UTILITIES
    */
    public void close() {
        if(socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    //centralize error handling for address creation.
    private static InetAddress resolveAddress(String ip) {
        if(ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("ip must not be null or blank");
        }
        try {
            return InetAddress.getByName(ip);
        } catch(IOException e) {
            throw new IllegalArgumentException("Unable to resolve host: " + ip, e);
        }
    }

    //Keeps constructors clean, centralizes socket creation and error handling.
    private static DatagramSocket createSocket() {
        try {
            return new DatagramSocket();
        } catch(SocketException e) {
            throw new IllegalStateException("Unable to create DatagramSocket", e);
        }
    }

    //centralize error handling for constructors
    private static void validateArguments(InetAddress serverAddress, int port, DatagramSocket socket) {
        if(serverAddress == null) {
            throw new IllegalArgumentException("serverAddress must not be null");
        }
        if(socket == null) {
            throw new IllegalArgumentException("socket must not be null");
        }
        if(port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }
}

// © 2026 Nathan Vailikit. All rights reserved.