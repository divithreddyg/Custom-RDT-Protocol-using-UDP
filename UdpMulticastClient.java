import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// Code originally from:
//https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
//
// edited by Sam Fryer.

public class UdpMulticastClient implements Runnable {
    DatagramSocket receiverSocket = null;
    DatagramSocket senderSocket = null;
    InetAddress rec;
    String myIp, recu;
    HashMap<Integer, byte[]> hhh = new HashMap<>();
    static boolean close = false;

    public UdpMulticastClient(DatagramSocket sendSocket, DatagramSocket recvSocket) {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            myIp = localhost.getHostAddress().trim();
            this.receiverSocket = recvSocket;
            this.senderSocket = sendSocket;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {
        while (!close) {
            try {
                receivePackets();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void receivePackets() throws IOException {
        byte[] buffer = new byte[1024+9];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
        try {
            this.receiverSocket.receive(packet);
            this.receiverSocket.setSoTimeout(60000);
        } catch (IOException e) {
            close = true;
            this.receiverSocket.close();
            System.out.println("No transmissions in last one minute, closing the connection!");
            return;
        }
        byte[] data = packet.getData();
        recu = packet.getAddress().toString().substring(1);
        if (hhh.isEmpty()) {
            System.out.println("Started receiving an image from " + recu);
        }
        try {
            rec = InetAddress.getByName(recu);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String msg=new String(packet.getData(),packet.getOffset(),packet.getLength());
        if ("end".equals(msg)){
            ByteArrayOutputStream pp = new ByteArrayOutputStream();
            ArrayList<Integer> sortedKeys
                    = new ArrayList<Integer>(hhh.keySet());
            Collections.sort(sortedKeys);
            for (Integer k : sortedKeys){
                pp.write(hhh.get(k));

            }
            BufferedImage final_img = ImageIO.read(new ByteArrayInputStream(pp.toByteArray()));
            if (final_img!=null) {
                File output_file = new File("image.png");
                ImageIO.write(final_img, "png", output_file);
                sendEnd();
                System.out.println("Received an image from "+recu +" and stored it as image.png");
                close = true;
                this.receiverSocket.close();
            }
        }else if (!recu.equals(myIp)) {
            parsePacket(data);
        }
    }

    private void sendEnd() {
        try{
            byte buf[] = null;
            String inp = "end";
            buf = inp.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, rec, 63001);
            this.receiverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void parsePacket(byte[] data) {
        int cost = Integer.parseInt(Integer.toHexString((data[4]) & 0xFF)+
                Integer.toHexString(
                        (data[5]) & 0xFF)
                +Integer.toHexString(
                (data[6]) & 0xFF)+
                Integer.toHexString(
                        (data[7]) & 0xFF),16 );
        if (!recu.equals(myIp)) {
            sendAck(cost);
            Integer ke = cost;
            if (data[8] == 0) {
                while (hhh.containsKey(ke)) {
                    ke = ke + 10;
                }
            }
            byte[] po = new byte[data.length - 9];

            for (int i = 0; i < data.length - 9; i++) {
                po[i] = data[i + 9];
            }
            hhh.put(ke, po);
        }
    }

    private synchronized void sendAck(int cost) {
        try{
            int senderPort = 63001;
            CustomProtocol cp =
                    new CustomProtocol(myIp,senderPort,rec,63001,cost,null);
            byte[] finalPacket = cp.getByteData();
            DatagramPacket packet = new DatagramPacket(finalPacket, finalPacket.length, rec, 63001);

            this.receiverSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
