/* ▄▄     ▄▄▄                 ▄▄▄             ▄▄                  
   ██▄   ██▀      █▄         █▀██  ██▀▀        ██              █▄ 
   ███▄  ██      ▄██▄          ██  ██       ▀▀ ██ ▀▀ ▄▄     ▀▀▄██▄
   ██ ▀█▄██ ▄▀▀█▄ ██ ▄█▀█▄     ██  ██ ▄▀▀█▄ ██ ██ ██ ██ ▄█▀ ██ ██ 
   ██   ▀██ ▄█▀██ ██ ██▄█▀     ██▄ ██ ▄█▀██ ██ ██ ██ ████   ██ ██ 
 ▀██▀    ██▄▀█▄██▄██▄▀█▄▄▄      ▀███▀▄▀█▄██▄██▄██▄██▄██ ▀█▄▄██▄██*/

import java.net.InetAddress;

/**
 * Used to transfer data between the NetworkListener and the client program.
 * 
 * @param data    The actual data packet that was receieved by the
 *                NetworkListener.
 * @param offset  Offset of the Datagram packet
 * @param length  Length of the Datagram packet.
 * @param address The address the data packet was sent from.
 * @param port    The port the data packet was sent from.
 *                <p>
 *                v1.1.0 Added parameters for Datagram packet offset and length
 *                for major NetworkListener.run() optimizations.
 * @since v1.0.0
 */
public interface DataReceiver {
    void onReceive(byte[] data, int offset, int length, InetAddress address, int port);
}

// © 2026 Nathan Vailikit. All rights reserved.