/*
import java.net.*;
import java.util.List;
import java.util.ArrayList;

public class NetworkMulticaster {
    private List<InetAddress> ips;
    private List<Integer> ports;
    private List<DatagramSocket> sockets;
    private List<NetworkBroadcaster> broadcasters = new ArrayList<>();

    public void NetworkMulticasterHelper(List<InetAddress> ips, List<Integer> ports) {
        if (ips == null || ports == null || ips.size() == 0 || ports.size() == 0)
            throw new IllegalArgumentException("Parallel lists must not be null or size zero.");
        if (ips.size() != ports.size())
            throw new IllegalArgumentException("Parallel lists must be equal length.");
        this.ips = ips;
        this.ports = ports;
        for (int i = 0; i < ips.size(); i++) {
            broadcasters.add(new NetworkBroadcaster(ips.get(i), ports.get(i)));
        }
    }

    public void NetworkMulticasterHelper(List<InetAddress> ips, List<Integer> ports, List<DatagramSocket> sockets) {
        if (ips == null || ports == null || sockets == null || ips.size() == 0 || ports.size() == 0 || sockets.size() == 0)
            throw new IllegalArgumentException("Parallel lists must not be null or size zero.");
        if (ips.size() != ports.size() || ips.size() != sockets.size())
            throw new IllegalArgumentException("Parallel lists must be equal length.");
        this.ips = ips;
        this.ports = ports;
        this.sockets = sockets;
        for (int i = 0; i < ips.size(); i++) {
            broadcasters.add(new NetworkBroadcaster(ips.get(i), ports.get(i), sockets.get(i)));
        }
    }
}
*/