import java.net.*; 
import java.io.*; 
import java.util.*; 
import java.net.InetAddress; 



public class Main {
	public static void main(String[] args) throws UnknownHostException {
		InetAddress localhost = InetAddress.getLocalHost();

		System.out.println("Client started" + args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}
		if (args.length == 3) {
			int roverId = Integer.parseInt(args[0]);
			System.out.println("I'm Rover " + roverId);
			String landerIP = args[1];
			String fileName = args[2];
			Rover rover = new Rover(roverId, landerIP, fileName,localhost);
			rover.startTransfers();
		}else if(args.length == 2) {
			int landerId = Integer.parseInt(args[0]);
			System.out.println("I'm lander " + landerId);
			String landerIP = args[1];
			Lander lander = new Lander(landerId, landerIP, localhost);
			lander.startReceiving();
		}
	}
}