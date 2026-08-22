import java.net.InetAddress;

public interface DataReceiver {
    /**
     * Used to transfer data between the NetworkListener and the client program.
     * 
     * @param data    The actual data packet that was receieved by the
     *                NetworkListener.
     * @param offset  Offset of the Datagram packet
     * @param length  Length of the Datagram packet.
     * @param address The address the data packet was sent from.
     * @param port    The port the data packet was sent from.
     * @since v1.1.0
     */
    void onReceive(byte[] data, int offset, int length, InetAddress address, int port);

    /**
     * Used to transfer data between the NetworkListener and the client program. Basic implementation with a default offset of 0, automatically matches the length of the provided data packet.
     * 
     * @param data    The actual data packet that was receieved by the
     *                NetworkListener.
     * @param address The address the data packet was sent from.
     * @param port    The port the data packet was sent from.
     * @since v1.0.0
     */
    default void onReceive(byte[] data, InetAddress address, int port) {
        onReceive(data, 0, data.length, address, port);
    }

        /**
     * Used to transfer data between the NetworkListener and the client program. Basic implementation that automatically matches the length of the provided data packet.
     * 
     * @param data    The actual data packet that was receieved by the
     *                NetworkListener.
     * @param address The address the data packet was sent from.
     * @param port    The port the data packet was sent from.
     * @since v1.0.0
     */
    default void onReceive(byte[] data, int offset, InetAddress address, int port) {
        onReceive(data, offset, data.length, address, port);
    }
}

/**
 * author Nate Vailikit
 * created on 5-22-2026
 * github: https://github.com/nate-v
 * copyright 2026
 **/