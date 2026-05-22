import java.util.Scanner;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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

        System.out.println("0) Send packet\n1) Packet statistics\n2) Exit");
        do {
            opt = input.nextInt();
            if (opt == 0) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                    ds.setSoTimeout(500);

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
            } else if (opt == 1) {
                double sttMs = 0;
                System.out.println("How many packets to send?");
                int n = input.nextInt();
                try {
                    for (int i = 1; i <= n; i++) {
                        long sendTime = System.nanoTime();
                        byte[] buffer = new byte[1024];
                        DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                        ds.setSoTimeout(500);
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
            }
        } while (opt != 2);
        input.close();
        TestServer.stop();
    }
}

/**
 * author Nate Vailikit
 * created on 5-22-2026
 * github: https://github.com/nate-v
 * copyright 2026
 **/