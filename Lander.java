import java.net.DatagramSocket;
import java.net.InetAddress;

public class Lander {
    int landerID;
    String landerIP;
    DatagramSocket sendSocket,recSocket;

    public Lander(int id, String ip, InetAddress localhost){
        landerID = id;
        landerIP = ip;
        try {
            sendSocket = new DatagramSocket(63001, localhost);
            recSocket = new DatagramSocket(63002, localhost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startReceiving() {
        Thread receiver = new Thread(new UdpMulticastClient(sendSocket, recSocket));
        receiver.start();
    }
}
