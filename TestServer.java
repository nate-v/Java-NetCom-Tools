import java.net.InetAddress;
import java.util.Arrays;

public class TestServer {
    private static NetworkListener nl;
    private static NetworkBroadcaster nb;
    private static volatile InetAddress lastClientAddress;

    public static void main(String[] args) {
        nl = new NetworkListener(1234, 1024, new DataReceiver() {
            @Override
            public void onReceive(byte[] data, int offset, int length, InetAddress address, int port) {
                lastClientAddress = address;
                nb = new NetworkBroadcaster(address, port);
                nb.broadcast(Arrays.copyOfRange(data, offset, offset + length));
                nb.close();
            }
        });
        nl.setReuseAddress(true);
        System.out.println(nl.getSocketReuseAddressFlag());
        nl.start();
        nl.addRestricted("178.10.52.67");
        nl.setRestriction(0);
        try {
            nl.addRestricted(InetAddress.getByName("192.40.20.1"));
        } catch (Exception e) {
            // do nothing
        }
        System.out.println(nl);
        System.out.println(nl.getSocketReuseAddress());
    }

    // courtesy of claude
    public static void runLifecycleChecks() {
        try {
            nl.setReuseAddressRuntime(true);
            boolean reuseEnabled = nl.getSocketReuseAddress();

            nl.setReuseAddressRuntime(false);
            boolean reuseDisabled = !nl.getSocketReuseAddress();

            nl.shutdown();
            boolean shutdownPassed = !nl.isActive();

            nl.restart();
            boolean restartPassed = nl.isActive();

            System.out.println("SO_REUSEADDR enable check: " + (reuseEnabled ? "passed" : "FAILED"));
            System.out.println("SO_REUSEADDR disable check: " + (reuseDisabled ? "passed" : "FAILED"));
            System.out.println("Shutdown check: " + (shutdownPassed ? "passed" : "FAILED"));
            System.out.println("Restart check: " + (restartPassed ? "passed" : "FAILED"));
        } catch (Exception e) {
            System.out.println("Lifecycle checks failed: " + e);
        }
    }

    public static void blockAddress(InetAddress address) {
        nl.addRestricted(address);
    }

    public static void unblockAddress(InetAddress address) {
        nl.removeRestricted(address);
    }

    public static InetAddress getLastClientAddress() {
        return lastClientAddress;
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