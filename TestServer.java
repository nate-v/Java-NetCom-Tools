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
    }

    public static void stop()   {
        nl.shutdown();
    }
}
