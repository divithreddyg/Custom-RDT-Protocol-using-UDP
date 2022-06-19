import java.net.InetAddress;

public class CustomProtocol {
    String senderIP;
    InetAddress destIP;
    int senderPort,destPort;
    long seq;
    byte[] data;


    public CustomProtocol(String senderIP, int senderPort, InetAddress destIP, int destPort, int seq, byte[] currentData) {
        this.senderIP = senderIP;
        this.senderPort = senderPort;
        this.destIP = destIP;
        this.destPort = destPort;
        this.seq = seq;
        this.data = currentData;
    }

    public byte[] getByteData(){
        byte[] packetByteArray;
        if (data != null) {
            packetByteArray = new byte[10 + data.length];
        }else {
            packetByteArray = new byte[10];
        }
        int start = 0;
        String[] ip = senderIP.split("\\.");
        for (String s : ip){//0-3
            packetByteArray[start++] = (byte) Integer.parseInt(s);
        }
        packetByteArray[start++] = (byte) (seq >> 24);//4
        packetByteArray[start++] = (byte) (seq >> 16);//5
        packetByteArray[start++] = (byte) (seq >> 8);//6
        packetByteArray[start++] = (byte) (seq);//7

        packetByteArray[start++] = (byte) (0);//8
        if (data!=null) {
            for (byte datum : data) {
                packetByteArray[start++] = datum;
            }
        }
        return packetByteArray;
    }
}
