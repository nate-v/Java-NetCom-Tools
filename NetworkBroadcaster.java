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

    /**
     * Constructor for the NetworkBroadcaster class
     * 
     * @param ip   The ip address to send data packets to.
     * @param port The port to send data packets to.
     * @since v1.0.0
     */
    public NetworkBroadcaster(String ip, int port) {
        this(resolveAddress(ip), port, createSocket());
    }

    /**
     * Constructor for the NetworkBroadcaster class
     * 
     * @param ip     The ip address to send data packets to.
     * @param port   The port to send data packets to.
     * @param socket Shared socket to help sending and receiving.
     * @since v1.0.0
     */
    public NetworkBroadcaster(String ip, int port, DatagramSocket socket) {
        this(resolveAddress(ip), port, socket);
    }

    /**
     * Constructor for the NetworkBroadcaster class
     * 
     * @param ip   The ip address to send data packets to (in the form of an
     *             InetAddress object).
     * @param port The port to send data packets to.
     * @since v1.0.0
     */
    public NetworkBroadcaster(InetAddress serverAddress, int port) {
        this(serverAddress, port, createSocket());
    }

    /**
     * Constructor for the NetworkBroadcaster class
     * 
     * @param ip     The ip address to send data packets to (in the form of an
     *               InetAddress object).
     * @param port   The port to send data packets to.
     * @param socket Shared socket to help sending and receiving.
     * @since v1.0.0
     */
    public NetworkBroadcaster(InetAddress serverAddress, int port, DatagramSocket socket) {
        validateArguments(serverAddress, port, socket);
        this.serverAddress = serverAddress;
        this.port = port;
        this.socket = socket;
    }

    /**
     * Broadcasts a data packet to the address and port specified in the constructor
     * 
     * @param data The data packet in the form of a byte array to send.
     * @throws IllegalArgumentException if the data packet is null.
     * @throws IllegalStateException    if the packet fails to send.
     * @since v1.0.0
     */
    public void broadcast(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, port);
            socket.send(packet);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to send UDP packet", e);
        }
    }

    /**
     * Closes the socket safely
     * 
     * @since v1.0.0
     */
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * Converts String object to InetAddress object safely
     * Not meant for use outside of this class.
     * 
     * @param ip the String form of the ip address.
     * @throws IllegalArgumentException if the String is null, blank, or not in the
     *                                  valid IP address format.
     * @since v1.0.0
     */
    private static InetAddress resolveAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("ip must not be null or blank");
        }
        try {
            return InetAddress.getByName(ip);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to resolve host: " + ip, e);
        }
    }

    /**
     * Safely creates a new DatagramSocket object.
     * Not meant for use outside of this class.
     * 
     * @throws IllegalStateException if the socket failed to be created.
     * @since v1.0.0
     */
    private static DatagramSocket createSocket() {
        try {
            return new DatagramSocket();
        } catch (SocketException e) {
            throw new IllegalStateException("Unable to create DatagramSocket", e);
        }
    }

    /**
     * Centralizes error handling and argument validation for constructors.
     * Not meant for use outside of this class.
     * 
     * @param serverAddress
     * @param port
     * @param socket
     * @throws IllegalArgumentException if the serverAddress is null/invalid, or if
     *                                  the socket is null, or if the port is out of
     *                                  range.
     * @since v1.0.0
     */
    private static void validateArguments(InetAddress serverAddress, int port, DatagramSocket socket) {
        if (serverAddress == null) {
            throw new IllegalArgumentException("serverAddress must not be null");
        }
        if (socket == null) {
            throw new IllegalArgumentException("socket must not be null");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }
}

// © 2026 Nathan Vailikit. All rights reserved.