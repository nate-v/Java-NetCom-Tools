import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;

public class NetworkListener implements Runnable {
    private final int port;
    private final int packet_size;
    private volatile boolean active = false;
    private final boolean socketProvided;
    private DatagramSocket ds;
    private final DataReceiver receiver;
    private DatagramPacket dp;
    private boolean SO_REUSEADDR = false;
    private Thread thread;

    private Set<InetAddress> address_list = new HashSet<>();
    private int restr_type = -1;// -1: disable --- 0: blacklist --- 1: whitelist

    /**
     * Constructor for the NetworkListener class.
     * 
     * @param port        Port to listen on.
     * @param packet_size Maximum packet size the listener can recieve.
     * @param receiver    DataReceiver object for communicating back to client
     *                    program.
     * @throws IllegalArgumentException If port is out of bounds (1-65535
     *                                  inclusive), packet_size is less than 1 byte,
     *                                  or the reciever is null.
     * @since v1.0.0
     */
    public NetworkListener(int port, int packet_size, DataReceiver receiver) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        if (packet_size <= 0) {
            throw new IllegalArgumentException("packet_size must be positive");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("receiver must not be null");
        }
        this.port = port;
        this.packet_size = packet_size;
        this.receiver = receiver;
        this.socketProvided = false;
    }

    /**
     * Constructor for the NetworkListener class. Requires a DatagramSocket to be provided, this socket must be bound to a port and connected to a destination address and port. This constructor is less flexible than its counterpart, and does not allow mutation of SO_REUSEADDR post-construction.
     * 
     * @param socket      DatagramSocket to listen through.
     * @param packet_size Maximum packet size the listener can recieve.
     * @param receiver    DataReceiver object for communicating back to client
     *                    program.
     * @throws IllegalArgumentException If port is out of bounds (1-65535
     *                                  inclusive), packet_size is less than 1 byte,
     *                                  or the reciever is null.
     * @since v1.0.0
     */
    public NetworkListener(DatagramSocket socket, int packet_size, DataReceiver receiver) {
        if (socket == null) {
            throw new IllegalArgumentException("socket must not be null");
        }
        if (packet_size <= 0) {
            throw new IllegalArgumentException("packet_size must be positive");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("receiver must not be null");
        }
        if(!socket.isBound()) {
            throw new IllegalArgumentException("Socket must be bound to a port.");
        }
        this.ds = socket;
        this.port = socket.getLocalPort();
        this.packet_size = packet_size;
        this.receiver = receiver;
        this.socketProvided = true;
    }
    // blacklist stuff

    /**
     * Initializes a set of addresses to have restrictions applied to, these
     * addresses can either be whitelisted or blacklisted.
     * 
     * @param set The set of InetAddresses which restrictions will be applied to.
     * @since v1.2.1
     */
    public void setRestrictionSet(Set<InetAddress> set) {
        if (set == null) {
            throw new IllegalArgumentException("Set cannot be null");
        }
        address_list = set;
    }

    /**
     * Initializes a set of addresses to have restrictions applied to, these
     * addresses can either be whitelisted or blacklisted.
     * <p>
     * Uses a {@code Set<String>} paramater, use {@code setRestrictionSet} with the
     * {@code Set<InetAddress>} parameter for much higher efficiency.
     * 
     * @param set The set of InetAddresses which restrictions will be applied to.
     * @since v1.2.1
     */
    public void setRestrictionStringSet(Set<String> set) {
        if (set == null) {
            throw new IllegalArgumentException("Set cannot be null");
        }
        address_list.clear();
        for (String ip : set) {
            address_list.add(resolveAddress(ip));
        }
    }

    /**
     * Adds an address to the set of restricted addresses.
     * 
     * @param a The address to be restricted.
     * @return {@code false} if the set already contains this address, and
     *         {@code true} if the address was succesfully added.
     * @throws IllegalArgumentException if a is null.
     * @since v1.2.1
     */
    public boolean addRestricted(InetAddress a) {
        if (a == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address_list.add(a))
            return true;
        return false;
    }

    /**
     * Adds an address to the set of restricted addresses.
     * <p>
     * Uses a {@code String} paramater, use the method with the {@code InetAddress}
     * parameter for slightly higher efficiency.
     * 
     * @param ip The address to be restricted.
     * @return {@code false} if the set already contains this address, and
     *         {@code true} if the address was succesfully added.
     * @throws IllegalArgumentException if a is null.
     * @since v1.2.1
     */
    public boolean addRestricted(String ip) {
        InetAddress a = resolveAddress(ip);
        if (a == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address_list.add(a))
            return true;
        return false;
    }

    /**
     * Removes an address from the set of restricted addresses.
     * <p>
     * Uses a {@code String} paramater, use the method with the {@code InetAddress}
     * parameter for slightly higher efficiency.
     * 
     * @param ip The address to be removed.
     * @return {@code false} if the set did not contain this address, and
     *         {@code true} if the address was succesfully removed.
     * @throws IllegalArgumentException if a is null.
     * @since v1.2.1
     */
    public boolean removeRestricted(String ip) {
        InetAddress a = resolveAddress(ip);
        if (a == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address_list.remove(a))
            return true;
        return false;
    }

    /**
     * Removes an address from the set of restricted addresses.
     * 
     * @param a The address to be removed.
     * @return {@code false} if the set did not contain this address, and
     *         {@code true} if the address was succesfully removed.
     * @throws IllegalArgumentException if a is null.
     * @since v1.2.1
     */
    public boolean removeRestricted(InetAddress a) {
        if (a == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address_list.remove(a))
            return true;
        return false;
    }

    /**
     * Changes the type of restriction applied to addresses in the restriction set.
     * 
     * @param restriction The restriction type. Valid restriction types are:
     *                    {@code -1} to disable restrictions.
     *                    {@code 0} to enable blacklist.
     *                    {@code 1} to enable whitelist.
     * @throws IllegalArgumentException if {@code restriction} is not -1, 0, or 1.
     * @since v1.2.1
     */
    public void setRestriction(int restriction) {
        if (restriction != -1 && restriction != 0 && restriction != 1) {
            throw new IllegalArgumentException(
                    "Invalid restriction type \nValid Arguments:\n-1: DISABLED\n0: BLACKLIST\n1: WHITELIST");
        }
        restr_type = restriction;
    }

    /**
     * 
     * @return The type of restriction applied. Valid restriction types are:
     *         {@code -1} to disable restrictions.
     *         {@code 0} to enable blacklist.
     *         {@code 1} to enable whitelist.
     * @since v1.2.1
     */
    public int getRestriction() {
        return restr_type;
    }

    /**
     * 
     * @return the set of addresses restricted.
     * @since v1.2.1
     */
    public Set<InetAddress> getRestrictionSet() {
        return Set.copyOf(address_list);
    }

    /**
     * This method is much slower than getRestrictionSet, which returns the set of
     * addresses as InetAddresses.
     * 
     * @return the set of addresses restricted in {@code String} form.
     * @since v1.2.1
     */
    public Set<String> getRestrictionStringSet() {
        Set<String> temp = new HashSet<>();
        for (InetAddress a : address_list) {
            temp.add(a.toString());
        }
        return temp;
    }

    /**
     * 
     * @param a The address to search for in the set of restricted addresses.
     * @return {@code true} if this address is contained in the set of restricted
     *         addresses, {@code false} if otherwise.
     * @since v1.2.1
     */
    public boolean isRestricted(InetAddress a) {
        if (a == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address_list.contains(a))
            return true;
        return false;
    }

    /**
     * 
     * @param a The address in {@code String} from to search for in the set of
     *          restricted addresses.
     * @return {@code true} if this address is contained in the set of restricted
     *         addresses, {@code false} if otherwise.
     * @since v1.2.1
     */
    public boolean isRestricted(String ip) {
        InetAddress a = resolveAddress(ip);
        if (a == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address_list.contains(a))
            return true;
        return false;
    }

    public int restrictedAddressesSize() {
        return address_list.size();
    }

    /**
     * Start method for NetworkListener. Starts the listening loop on a seperate
     * thread.
     * 
     * @since v1.2.3
     */
    public void start() {
        if (thread != null && thread.isAlive()) {
            return;
        }

        DatagramSocket ts = ds;

        try {
            if(socketProvided) {
                if(ts == null || ts.isClosed()) {
                    throw new IllegalStateException("Provided socket is closed or null, cannot start listener.");
                }
            } else {
                ts = new DatagramSocket(null);
                ts.setReuseAddress(SO_REUSEADDR);
                ts.bind(new InetSocketAddress(port));
            }
        } catch (SocketException e) {
            if(ts != null && !socketProvided) {
                ts.close();
            }
            ds = null;
            active = false;
            throw new IllegalStateException("Unable to bind socket to port " + port + ".\n" + e);
        }

        ds = ts; // assign socket to temporary socket after bind is succesful, avoids leaving socket open if bind fails.
        try {
            if(!socketProvided) {
                ds.setSoTimeout(250);
            }
        } catch (SocketException e) {
            if(!socketProvided) {
                ds.close();
                ds = null;
            }
            throw new IllegalStateException("Unable to set SO_TIMEOUT on socket.\n" + e);
        }

        active = true;
        thread = new Thread(this, "NetworkListener-" + port);
        thread.start();
    }

    /**
     * Implements the listener loop on a different thread.
     * Use <name>.start(), do not use this method.
     * 
     * @throws SocketException
     * @throws IOException
     * @since v1.0.0
     */
    @Override
    public void run() {
        try {
            if (!active)
                return;
            dp = new DatagramPacket(new byte[packet_size], packet_size);
            while (active) {
                dp.setLength(packet_size); // length check to avoid packet size issues after filtered packets call continue.
                try {
                    ds.receive(dp);
                } catch (SocketTimeoutException e) {
                    continue;
                }
                if (restr_type == 0) {
                    if (address_list.contains(dp.getAddress())) {
                        continue;
                    }
                } else if (restr_type == 1) {
                    if (!address_list.contains(dp.getAddress())) {
                        continue;
                    }
                }
                receiver.onReceive(dp.getData(), dp.getOffset(), dp.getLength(), dp.getAddress(), dp.getPort());
            }
        } catch (SocketException e) {
            if (active) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ds != null && !ds.isClosed() && !socketProvided) {
                ds.close();
            }
        }
    }

    /**
     * Safely shuts down the listening loop.
     * 
     * @since v1.0.0
     */
    public boolean shutdown() {
        active = false;
        if (ds != null && !socketProvided) {
            ds.close();
        }
        
        try {
            if (thread != null) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if(!socketProvided) {
            ds = null;
        }

        return true;
    }

    /**
     * Safely restarts the network listener if it was ever shut down.
     * <p>
     * Safe to use after update v1.2.4
     * 
     * @since v1.2.3
     */
    public void restart() {
        this.shutdown();
        this.start();
    }

    /**
     * Enables or disables the socket's ability to reuse addresses that are already
     * in use or in TIME_WAIT.
     * <p>
     * Only called while the listener is running.
     * 
     * @param b
     * @since v1.2.4b
     */
    public void setReuseAddressRuntime(boolean b) {
        if(!active) {
            throw new IllegalStateException("Cannot modify SO_REUSEADDR when listener is not active, use setReuseAddress(boolean b) instead.");
        }
        if (ds == null) {
            throw new NullPointerException("Cannot modify SO_REUSEADDR when socket is inactive, null.");
        }
        try {
            ds.setReuseAddress(b);
            SO_REUSEADDR = b;
        } catch (SocketException e) {
            throw new IllegalStateException("Unable to set SO_REUSEADDR on socket.\n" + e);
        }
    }

    /**
     * Enables or disables the socket's ability to reuse addresses that are already
     * in use or in TIME_WAIT.
     * <p>
     * Only called while the listener is not running, state will change when
     * listener restarts!
     * 
     * @param b
     * @since v1.2.1
     */
    public void setReuseAddress(boolean b) {
        if (!active) {// SO_REUSEADDR is a flag, so it should only be changed if the real state of the
                         // socket is changed!
            SO_REUSEADDR = b;
        }   else    {
            throw new IllegalStateException("Cannot modify SO_REUSEADDR when socket is active, use setReuseAddressRuntime(boolean b) instead.");
        }
    }

    /**
     * Converts String object to InetAddress object safely
     * Not meant for use outside of this class.
     * 
     * @param ip the String form of the ip address.
     * @throws IllegalArgumentException if the String is null, blank, or not in the
     *                                  valid IP address format.
     * @since v1.2.1
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
     * Accessor method for the listening port.
     * 
     * @return port.
     * @since v1.2.2
     */
    public int getPort() {
        return port;
    }

    /**
     * Accessor method for the packet buffer size.
     * 
     * @return packet size.
     * @since v1.2.2
     */
    public int getPacketBufferSize() {
        return packet_size;
    }

    /**
     * Accessor method for active boolean.
     * 
     * @return {@code true} if listening, {@code false} if closed.
     * @since v1.2.2
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Accessor method for the socket used by the NetworkListener.
     * 
     * @return socket.
     * @since v1.2.2
     */
    public DatagramSocket getSocket() {
        return ds;
    }

    /**
     * Accessor method for the dataReceiver object used to transfer data between
     * the client and NetworkListener.
     * 
     * @return receiver/
     * @since v1.2.2
     */
    public DataReceiver getDataReceiver() {
        return receiver;
    }

    /**
     * Accessor method for the SO_REUSEADDR state.
     * 
     * @return {@code true} if socket reuse address is enabled, {@code false} if
     *         otherwise OR the listener is closed.
     * @since v1.2.2
     */
    public boolean getSocketReuseAddress() {
        if (ds != null) {
            try {
                return ds.getReuseAddress();
            } catch (SocketException e) {
                throw new IllegalStateException("Unable to get SO_REUSEADDR state from socket.\n" + e);
            }
        }
        return false;
    }

    /**
     * Accessor method for the SO_REUSEADDR boolean.
     * <p>
     * For use when the listener is closed.
     * 
     * @return {@code true} if the socket reuse flag is set to true, {@code false} if
     *         otherwise.
     * @since v1.2.5
     */
    public boolean getSocketReuseAddressFlag() {
        return SO_REUSEADDR;
    }

    /**
     * @return A string containing details about the network listener on the format:
     *         {@code [CLOSED/LISTENING] on port [port]. [DISABLED/WHITELISTING/BLACKLISTING] [# of restricted addresses] addresses.}
     * @since v1.2.2;
     */
    public String toString() {
        String t = "[";
        if (!(thread != null && thread.isAlive())) {
            t += "CLOSED";
        } else {
            t += "LISTENING";
        }
        t += ("] on port [" + port + "]. ");
        if (restr_type == -1) {
            t += "No restrictions.";
        } else if (restr_type == 0) {
            t += "[BLACKLISTING] " + restrictedAddressesSize() + " addresses.";
        } else if (restr_type == 1) {
            t += "[WHITELISTING] " + restrictedAddressesSize() + " addresses.";
        }
        return t;
    }

}

/**
 * author Nate Vailikit
 * created on 5-22-2026
 * github: https://github.com/nate-v
 * copyright 2026
 **/