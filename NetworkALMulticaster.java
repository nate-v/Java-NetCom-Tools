import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.*;
import java.io.IOException;

/**
 * NetworkALMulticaster, or Network Application-Layer Multicaster, is a fan-out
 * style simplification of multicasting that does not require group IP's. True
 * multicasting is more efficient, and will come in a later version of Java
 * NetCom Tools.
 * <p> Shared sockets will be closed upen fail or calling the {@code close()} method.
 * 
 * @since v1.3.0
 */
public class NetworkALMulticaster {
    private final List<InetAddress> address;
    private final List<Integer> port;
    private final List<DatagramSocket> socket;

    /**
     * Constructor for the NetworkALMulticaster class.
     * <p>
     * NetworkALMulticaster does not support the use of address input in the type
     * String due to limitations on generic types and overloading constructors in
     * Java.
     * 
     * @param address the list containing InetAddresses by index.
     * @param port    the list containing ports by index.
     * @param socket  the list containing sockets by index.
     * @throws IllegalArgumentException if paramaters are null, contain null
     *                                  elements, or are not of equal size.
     * @since v1.3.0
     */
    public NetworkALMulticaster(List<InetAddress> address, List<Integer> port, List<DatagramSocket> socket) {
        if (address == null || port == null || socket == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        if (address.size() != port.size() || address.size() != socket.size()) {
            throw new IllegalArgumentException("List parameters must be of equal length.");
        }
        for (int i = 0; i < address.size(); i++) {
            validateArguments(address.get(i), port.get(i), socket.get(i));
        }
        this.address = List.copyOf(address);
        this.port = List.copyOf(port);
        this.socket = List.copyOf(socket);
    }

    /**
     * Constructor for the NetwokMulticaster class.
     * <p>
     * Creates its own socket.
     * <p>
     * NetworkALMulticaster does not support the use of address input in the type
     * String due to limitations on generic types and overloading constructors in
     * Java.
     * 
     * @param address the list containing InetAddresses by index.
     * @param port    the list containing ports by index.
     * @throws IllegalArgumentException if paramaters are null, contain null
     *                                  elements, or are not of equal size.
     * @since v1.3.0
     */
    public NetworkALMulticaster(List<InetAddress> address, List<Integer> port) {
        this(address, port, createSockets(address, port));
    }

    /**
     * Constructor for the NetwokMulticaster class.
     * <p>
     * Requires pre-binded sockets that already point to a destination.
     * 
     * @param socket the list containing sockets.
     * @throws IllegalArgumentException if the parameter is null, contains null
     *                                  elements, or contains unbound elements.
     * @since v1.3.0
     */
    public NetworkALMulticaster(List<DatagramSocket> socket) {
        if (socket == null) {
            throw new IllegalArgumentException("socket must not be null");
        }
        List<InetAddress> address = new ArrayList<>(socket.size());
        List<Integer> port = new ArrayList<>(socket.size());

        for (int i = 0; i < socket.size(); i++) {
            if (socket.get(i) == null) {
                throw new IllegalArgumentException("Socket must not be null.");
            }
            if (!socket.get(i).isConnected() || socket.get(i).isClosed()) {
                throw new IllegalArgumentException("Socket must be connected to a destination address and port.");
            }
            address.add(socket.get(i).getInetAddress());
            port.add(socket.get(i).getPort());
        }
        this(address, port, socket);
    }

    /**
     * Multicasts a datapacket to all listed addresses and ports.
     * 
     * @param data The data packet in the fom of a byte array.
     * @throws IllegalArgumentException if {@code data} is null.
     * @throws IllegalStateException    if a packet fails to send, does not mean
     *                                  that all packets failed to send.
     * @since v1.3.0
     */
    public void ALMulticast(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must not be null");
        }
        try {
            for (int i = 0; i < socket.size(); i++) {
                socket.get(i).send(new DatagramPacket(data, data.length, address.get(i), port.get(i)));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to send UDP packet.", e);
        }
    }

    /**
     * Broadcasts a data packet containing a string to all listed addresses and
     * ports. Uses the charset defined as a parameter to encode the string.
     * 
     * @param str     the string message to be sent.
     * @param charset the charset to be used to encode the string.
     * @throws IllegalArgumentException if {@code str} or {@code charset} is null.
     * @since v1.3.0
     */
    public void ALMulticast(String str, Charset charset) {
        if (str == null || charset == null) {
            throw new IllegalArgumentException("String or Charset must not be null");
        }
        ALMulticast(str.getBytes(charset));
    }

    /**
     * Broadcasts a data packet containing a string to all listed addresses and
     * ports. Uses UTF_8 by default to encode the string.
     * 
     * @param str the string message to be sent.
     * @throws IllegalArgumentException if {@code str} is null.
     * @since v1.3.0
     */
    public void ALMulticast(String str) {
        if (str == null) {
            throw new IllegalArgumentException("String must not be null");
        }
        ALMulticast(str, StandardCharsets.UTF_8);
    }

    /**
     * Centralizes error handling and argument validation for constructors.
     * Not meant for use outside of this class.
     * 
     * @param address
     * @param port
     * @param socket
     * @throws IllegalArgumentException if the address is null/invalid, or if
     *                                  the socket is null, or if the port is out of
     *                                  range.
     * @since v1.3.0
     */
    private static void validateArguments(InetAddress address, int port, DatagramSocket socket) {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        if (socket == null) {
            throw new IllegalArgumentException("socket must not be null");
        }
        if (socket.isClosed()) {
            throw new IllegalArgumentException("socket must be open");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }

    /**
     * Safely creates a new DatagramSocket object.
     * <p>
     * Not meant for use outside of this class.
     * 
     * @throws IllegalStateException if the socket failed to be created.
     * @since v1.3.0
     */
    private static DatagramSocket createSocket() {
        try {
            return new DatagramSocket();
        } catch (SocketException e) {
            throw new IllegalStateException("Unable to create DatagramSocket", e);
        }
    }

    /**
     * Safely creates DatagramSocket list.
     * <p>
     * Not meant for use outside of this class.
     * 
     * @param address
     * @param port
     * @return List of datagram sockets.
     * @since v1.3.0
     */
    private static List<DatagramSocket> createSockets(List<InetAddress> address, List<Integer> port) {
        if (address == null || port == null) {
            throw new IllegalArgumentException("Address and port lists must not be null.");
        }
        if (address.size() != port.size()) {
            throw new IllegalArgumentException("Address and port lists must be of equal length.");
        }

        for (int i = 0; i < address.size(); i++) {
            if (address.get(i) == null) {
                throw new IllegalArgumentException("Address must not be null");
            }
            if (port.get(i) == null || port.get(i) < 1 || port.get(i) > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535, and non null");
            }
        }

        List<DatagramSocket> sockets = new ArrayList<>(address.size());
        try {
            for (int i = 0; i < address.size(); i++) {
                sockets.add(createSocket());
            }
            return sockets;
        } catch (RuntimeException e) {
            closeSockets(sockets);
            throw e;
        }
    }

    /**
     * Closes all sockets. Caller provided sockets will also be closed.
     * Not meant for use outside this class.
     * 
     * @since v1.3.0
     */
    private static void closeSockets(List<DatagramSocket> sockets) {
        for (DatagramSocket socket : sockets) {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    /**
     * Closes all sockets. Caller provided sockets will also be closed.
     * 
     * @since v1.3.0
     */
    public void close() {
        closeSockets(socket);
    }
}