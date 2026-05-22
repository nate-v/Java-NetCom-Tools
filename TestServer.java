import java.net.InetAddress;

public class TestServer {
    private static NetworkListener nl;
    private static NetworkBroadcaster nb;

    public static void main(String[] args) {
        nl = new NetworkListener(1234, 1024, new DataReceiver() {
            @Override
            public void onReceive(byte[] data, int offset, int length, InetAddress address, int port) {
                nb = new NetworkBroadcaster(address, port);
                nb.broadcast("hi".getBytes());
            }
        });
        nl.start();
        // RESTRICTION TESTING
        nl.addRestricted("178.10.52.67");
        nl.setRestriction(0);
        try {
            nl.addRestricted(InetAddress.getByName("192.40.20.1"));
            System.out.println(nl.isRestricted(InetAddress.getByName("192.40.20.1")));
            System.out.println(nl.isRestricted(InetAddress.getByName("197.40.22.1")));
        } catch (Exception e) {
            // do nothing
        }
        System.out.println(nl);
        nl.setReuseAddress(false);
        nl.shutdown();
        System.out.println(nl);
        nl.restart();
        System.out.println(nl);
    }

    public static void stop() {
        nl.shutdown();
    }
}

/**
 * author Nate Vailikit
 * created on 5-22-2026
 * github: https://github.com/nate-v
 * copyright 2026
 **/