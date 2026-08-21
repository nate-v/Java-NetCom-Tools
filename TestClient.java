import java.util.Scanner;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestClient {
    private static NetworkBroadcaster nb;
    private static DatagramSocket ds;
    private static InetAddress serverAddress;

    public static void main(String[] arg) {
        TestServer.main(null);
        System.out.println("Server started.\n");

        try {

            // Remove this to change the server address.
            //    \/
            serverAddress = InetAddress.getLocalHost();
            //    /\

            ds = new DatagramSocket();
            nb = new NetworkBroadcaster(serverAddress, 1234, ds);
        } catch (Exception e) {
            System.out.println("/!\\ NetworkBroadcaster not initialized /!\\");
        }
        Scanner input = new Scanner(System.in);
        int opt;

        System.out.println("0) Send packet\n1) Packet statistics\n2) Run functionality checks\n3) Run lifecycle checks\n4) Exit");
        do {
            opt = input.nextInt();
            if (opt == 0) { // send single packet
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                    ds.setSoTimeout(500); // default timeout: 500ms

                    long sendTime = System.nanoTime();
                    nb.broadcast("hi".getBytes());
                    ds.receive(dp);

                    long rttNs = System.nanoTime() - sendTime;
                    double rttMs = rttNs / 1_000_000.0;

                    String rece = (String.format("%.3f ms", rttMs));
                    System.out.println("\n┌-------------------> [ Server ]" +
                            "\n|                              |" +
                            "\n[ Client ] <------" + rece + "-----┘");
                } catch (Exception e) {
                    System.out.println("Did not recieve a response packet.\n" + e);
                }
            } else if (opt == 1) { // send n packets and calculate test data
                double sttMs = 0;
                System.out.println("How many packets to send?");
                int n = input.nextInt();
                try {
                    for (int i = 1; i <= n; i++) {
                        long sendTime = System.nanoTime();
                        byte[] buffer = new byte[1024];
                        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                        ds.setSoTimeout(500); // default timeout: 500ms
                        nb.broadcast("hi".getBytes());
                        ds.receive(dp);

                        long rttNs = System.nanoTime() - sendTime;
                        double rttMs = rttNs / 1_000_000.0;
                        sttMs += rttMs;

                        System.out.println("[" + i + "] " + rttMs);
                    }
                } catch (Exception e) {
                    System.out.println("Did not recieve a response packet.\n" + e);
                }
                double nd = (double)n;
                String o = (String.format("%.3f ms", (sttMs / nd)));
                System.out.println("\n[ Complete ]\n Avg response time: " + o + "\n");
            } else if (opt == 2) {
                runFunctionalityChecks();
            } else if (opt == 3) {
                TestServer.runLifecycleChecks();
            }
        } while (opt != 4); //end
        input.close();
        TestServer.stop();
        if (nb != null) {
            nb.close();
        } else if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }

    private static void runFunctionalityChecks() {
        try {
            nb.broadcast("address probe".getBytes(StandardCharsets.UTF_8));
            ds.setSoTimeout(500);
            ds.receive(new DatagramPacket(new byte[1024], 1024));
            InetAddress clientAddress = TestServer.getLastClientAddress();

            byte[] filteredData = "filtered".getBytes(StandardCharsets.UTF_8);
            TestServer.blockAddress(clientAddress);
            nb.broadcast(filteredData);
            ds.setSoTimeout(100);
            try {
                ds.receive(new DatagramPacket(new byte[1024], 1024));
                System.out.println("Filtering check: FAILED (filtered packet received)");
            } catch (java.net.SocketTimeoutException expected) {
                System.out.println("Filtering check: passed");
            } finally {
                TestServer.unblockAddress(clientAddress);
            }

            byte[] largeData = new byte[1024];
            Arrays.fill(largeData, (byte) 'x');
            nb.broadcast(largeData);
            ds.setSoTimeout(500);
            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
            ds.receive(response);
            System.out.println("Packet length check: "
                    + (response.getLength() == largeData.length ? "passed" : "FAILED"));
        } catch (Exception e) {
            System.out.println("Functionality checks failed: " + e);
        }
    }
}

/**
 * author Nate Vailikit
 * created on 5-22-2026
 * github: https://github.com/nate-v
 * copyright 2026
 **/