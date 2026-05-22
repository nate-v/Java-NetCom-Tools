import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;

public class NetworkListener extends Thread {
    private final int port;
    private final int packet_size;
    private volatile boolean active = true;
    private DatagramSocket ds;
    private final DataReceiver receiver;
    private DatagramPacket dp;
    private boolean SO_REUSEADDR = false;

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
    }

    /**
     * Constructor for the NetworkListener class.
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
        this.ds = socket;
        this.port = socket.getLocalPort();
        this.packet_size = packet_size;
        this.receiver = receiver;
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
        return address_list;
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
     * Starts the listener loop on a different thread.
     * Use <name>.start(), do not use this method.
     * 
     * After using {@code <name>.shutdown()}, you cannot restart the network
     * listener.
     * 
     * @throws SocketException
     * @throws IOException
     * @throws IllegalThreadStateException if you attempt to call <name>.start()
     *                                     after the NetworkListener was shut down
     *                                     or is already listening.
     * @since v1.0.0
     */
    @Override
    public void run() {
        try {
            if (ds == null) {
                ds = new DatagramSocket(null);
                ds.setReuseAddress(SO_REUSEADDR);
                ds.bind(new InetSocketAddress(port));
            }
            if (!active)
                return;
            dp = new DatagramPacket(new byte[packet_size], packet_size);
            while (active) {
                ds.receive(dp);
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
                dp.setLength(packet_size);
            }
        } catch (SocketException e) {
            if (active) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ds != null && !ds.isClosed()) {
                ds.close();
            }
        }
    }

    /**
     * Shuts down the listening loop.
     * <p>
     * After calling {@code <name>.shutdown()}, you cannot restart the network
     * listener.
     * 
     * @since v1.0.0
     */
    public boolean shutdown() {
        active = false;
        if (ds != null) {
            ds.close();
            return true;
        }
        return false;
    }

    /**
     * Restarts the network listener if it was ever shut down.
     * <p>
     * Generally not best practice, only use if absolutely necessary.
     * 
     * @see {@code <name>.shutdown()} After calling this method, you cannot
     *      run {@code <name>.start()} or else it will throw an
     *      IllegalThreadStateException. You must call this method to restart the
     *      NetworkListener.
     * @throws InterruptedException
     * @since 1.2.2
     */
    public void restart() {
        if (active) {
            return;
        }
        active = true;
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
        ds = null;
        try {// Thread has to sleep for 100ms to allow time for the OS to release the port.
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Thread t = new Thread(this, "NetworkListener-" + port);
        t.start();
    }

    public void setReuseAddress(boolean b) {
        SO_REUSEADDR = b;
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
     * Accessore method for the SO_REUSEADDR boolean.
     * 
     * @return {@code true} if socket reuse address is enable, {@code false} if
     *         otherwise.
     * @since v1.2.2
     */
    public boolean socketReuseAddress() {
        return SO_REUSEADDR;
    }

    /**
     * @return A string containing details about the network listener on the format:
     *         {@code [CLOSED/LISTENING] on port [port]. [DISABLED/WHITELISTING/BLACKLISTING] [# of restricted addresses] addresses.}
     * @since v1.2.2;
     */
    public String toString() {
        String t = "[";
        if (!active) {
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