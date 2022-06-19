import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

public class UdpMulticastSender implements Runnable{
   String landerIP;
   byte[] bytes;
   String senderIP;
   int senderPort;
   InetAddress destIP;
   int destPort = 63002;
   DatagramSocket socket;
   static long mem = 0;
   static Integer[] received = new Integer[10];
   boolean isRec = false;
   static boolean isLastSent = false;
   static Integer diff =0,rec=0;
   static boolean close = false;
   HashMap<Integer,byte[]> sent = new HashMap<>();

   public UdpMulticastSender(String landerIP, byte[] bytes,DatagramSocket sendSocket, DatagramSocket recSocket) throws UnknownHostException {
      this.landerIP = landerIP;
      this.bytes = bytes;
      InetAddress localhost = InetAddress.getLocalHost();
      this.senderIP = (localhost.getHostAddress()).trim();
      this.senderPort = 63001;
      this.socket = sendSocket;
      Thread rec = new Thread(new UdpMulticastSender(true, sendSocket, landerIP));
      rec.start();
   }

   public UdpMulticastSender(boolean isRec, DatagramSocket sendSocket, String landerIP) {
      this.isRec = isRec;
      this.socket = sendSocket;
      this.landerIP = landerIP;
   }

   @Override
   public synchronized void run() {
      if (!isRec) {
         try {
            sendFile();
         } catch (Exception ex) {
            ex.printStackTrace();
         }
      }else {
         while (!close) {
            receiveAcks();
         }
      }
   }

   private void receiveAcks() {
      byte[] buffer = new byte[1024+9];

      DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
      try {
         this.socket.setSoTimeout(60000);
         this.socket.receive(packet);
      } catch (IOException e) {
         close = true;
         this.socket.close();
         System.out.println("No transmissions in last one minute, Closing the connection!");
         return;
      }

      byte[] data = packet.getData();
      String recu = packet.getAddress().toString().substring(1);
      String msg=new String(packet.getData(),packet.getOffset(),packet.getLength());

      if (recu.equals(landerIP)) {
         if(msg.equals("end")) {
            System.out.println("Image successfully sent!");
            close = true;
            this.socket.close();
            return;
         }else {
            int cost = Integer.parseInt(Integer.toHexString((data[4]) & 0xFF) +
                    Integer.toHexString((data[5]) & 0xFF) + Integer.toHexString(
                    (data[6]) & 0xFF) +
                    Integer.toHexString(
                            (data[7]) & 0xFF), 16);
            received[cost] = cost;
            rec++;
         }

      }
   }

   private void sendFile() throws InterruptedException {
      System.out.println("Started sending file to " + landerIP);
      try {
         destIP = InetAddress.getByName(this.landerIP);
      } catch (UnknownHostException e) {
         e.printStackTrace();
      }

      int packetSize = 1024;
      int seq = 0;
      int expecting = 0;
      for (int i = 0; i< bytes.length;i= i+packetSize){

         byte[] currentData = new byte[packetSize];
         if ( i + packetSize >= bytes.length) {
            isLastSent = true;
            for ( int j = 0; j< bytes.length - i ;j++ ){
               currentData[j] = bytes[i+j];
            }
         }else {
            for ( int j = 0; j< packetSize ;j++ ){
               currentData[j] = bytes[i+j];
            }
         }
         CustomProtocol cp = new CustomProtocol(senderIP,senderPort,destIP,destPort,seq,currentData);
         byte[] finalPacket = cp.getByteData();
         DatagramPacket packet = new DatagramPacket(finalPacket, finalPacket.length, destIP, destPort);
         try {
            this.socket.send(packet);
         } catch (IOException e) {
            e.printStackTrace();
         }
         sent.put(seq,finalPacket);
         seq++;
         diff++;
         mem++;
         if (mem == 10) {
            seq = expecting;
            Instant start = Instant.now();
            while (!Arrays.asList(received).contains(expecting)){
               Instant current = Instant.now();
               Duration duration = Duration.between(start, current);

               if (duration.getSeconds() > 10) {
                  finalPacket = sent.get(expecting);
                  finalPacket[9] = 1;
                  packet = new DatagramPacket(finalPacket, finalPacket.length, destIP, destPort);
                  try {
                     System.out.println("retransmitted  "+expecting);
                     this.socket.send(packet);
                  } catch (IOException ioException) {
                     close = true;
                     this.socket.close();
                     return;
                  }
                  start = Instant.now();
               }


            }
            received[expecting] = null;
            mem --;
            expecting++;
         }

         if (expecting == 10){
            expecting = 0;
         }
      }

      if (isLastSent){
         while (diff-rec!=0) {
            Thread.sleep(10);
         }
         try {
            destIP = InetAddress.getByName(this.landerIP);
         } catch (UnknownHostException e) {
            e.printStackTrace();
         }
         byte[] buf = null;
         String inp = "end";
         buf = inp.getBytes();
         DatagramPacket DpSend = new DatagramPacket(buf, buf.length, destIP, destPort);
         try {
            this.socket.send(DpSend);
         } catch (IOException e) {
            e.printStackTrace();
         }
         Instant start = Instant.now();
         while (!close){
            Instant current = Instant.now();
            Duration duration = Duration.between(start, current);

            if (duration.getSeconds() > 10) {
               try {
                  System.out.println("retransmitted  end");
                  this.socket.send(DpSend);
               } catch (IOException ioException) {
                  ioException.printStackTrace();
               }
               start = Instant.now();
            }
         }
         this.socket.close();
      }

   }

}

