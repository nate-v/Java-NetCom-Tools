/* ▄▄     ▄▄▄                 ▄▄▄             ▄▄                  
   ██▄   ██▀      █▄         █▀██  ██▀▀        ██              █▄ 
   ███▄  ██      ▄██▄          ██  ██       ▀▀ ██ ▀▀ ▄▄     ▀▀▄██▄
   ██ ▀█▄██ ▄▀▀█▄ ██ ▄█▀█▄     ██  ██ ▄▀▀█▄ ██ ██ ██ ██ ▄█▀ ██ ██ 
   ██   ▀██ ▄█▀██ ██ ██▄█▀     ██▄ ██ ▄█▀██ ██ ██ ██ ████   ██ ██ 
 ▀██▀    ██▄▀█▄██▄██▄▀█▄▄▄      ▀███▀▄▀█▄██▄██▄██▄██▄██ ▀█▄▄██▄██*/

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class NetworkListener extends Thread {
    private final int port;
    private final int packet_size;
    private volatile boolean active = true;
    private DatagramSocket ds;
    private final DataReceiver receiver;

    /*
        CONSTRUCTORS
            - socket is optional, if not provided a new socket is made.
            - socket may be used for sharing a socket across classes (recommended).
    */
    public NetworkListener(int port, int packet_size, DataReceiver receiver) {
        if(port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if(packet_size <= 0) {
            throw new IllegalArgumentException("packet_size must be positive");
        }
        if(receiver == null) {
            throw new IllegalArgumentException("receiver must not be null");
        }
        this.port = port;
        this.packet_size = packet_size;
        this.receiver = receiver;
    }

    public NetworkListener(DatagramSocket socket, int packet_size, DataReceiver receiver) {
        if(socket == null) {
            throw new IllegalArgumentException("socket must not be null");
        }
        if(packet_size <= 0) {
            throw new IllegalArgumentException("packet_size must be positive");
        }
        if(receiver == null) {
            throw new IllegalArgumentException("receiver must not be null");
        }
        this.ds = socket;
        this.port = socket.getLocalPort();
        this.packet_size = packet_size;
        this.receiver = receiver;
    }

    /*
        MAIN FUNCTIONALITY
    */
    @Override
    public void run() {
        try {
            if(ds == null) {
                ds = new DatagramSocket(port);
            }
            byte[] buffer = new byte[packet_size];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            while(active) {
                ds.receive(dp);
                byte[] data = Arrays.copyOf(dp.getData(), dp.getLength());
                receiver.onReceive(data, dp.getAddress(), dp.getPort());
            }
        } catch(SocketException e) {
            if(active) {
                e.printStackTrace();
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(ds != null && !ds.isClosed()) {
                ds.close();
            }
        }
    }

    /*
        CLOSURE AND UTILITIES
     */
    //Centralize logic for shutting down listeners.
    //Returns true if shutdown was successful, false if it was already inactive.
    public boolean shutdown() {
        if(ds != null) {
            ds.close();
            active = false;
            return true;
        }
        return false;
    }
}

// © 2026 Nathan Vailikit. All rights reserved.