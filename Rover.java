import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Rover {
    int roverId;
    String landerIP;
    String fileName;
    DatagramSocket sendSocket,recSocket;

    public Rover(int roverId, String landerIP, String fileName, InetAddress localhost) {
        this.roverId = roverId;
        this.landerIP = landerIP;
        this.fileName = fileName;
        try {
            sendSocket = new DatagramSocket(63001, localhost);
            recSocket = new DatagramSocket(63002, localhost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startTransfers(){
        Thread fr = new Thread(new UdpMulticastClient(sendSocket,recSocket));
        fr.start();

        File f = new File(fileName);
        try {
            BufferedImage bfimage = ImageIO.read(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bfimage, "png", baos);
            baos.flush();
            byte[] img_in_bytes = baos.toByteArray();
            baos.close();
            Thread fs = new Thread(new UdpMulticastSender(landerIP, img_in_bytes, sendSocket, recSocket));
            fs.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
