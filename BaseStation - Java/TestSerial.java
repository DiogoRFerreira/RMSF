
/*
 *	José Dias 	nr 75847
 *	Diogo Ferreira 	nr 76090
 *
 *	Professor: António Grilo
 *
 * Thanks to Phil Levis <pal@cs.berkeley.edu> for the TestSerial example.
 * 
 */

import java.io.IOException;
import net.tinyos.message.*;
import net.tinyos.packet.*;
import net.tinyos.util.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;

class Node{
	public int ID;
	public int activated;
}

 class Client_socket {
	
	private Socket socket;

	public Client_socket(String IP){
		try {
			int PORT = 1901;
			this.socket = new Socket(IP,PORT);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void send_message(String message){
        /*Sends public key to the home*/
		DataOutputStream os;
		try {
			os = new DataOutputStream(this.socket.getOutputStream());
			os.writeBytes(message);
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Can´t send the message");
			e.printStackTrace();
		}
	}
	public String receive_message(){
		String message = "";
		
		InputStreamReader inputStream;
		try {
			inputStream = new InputStreamReader(this.socket.getInputStream());
			BufferedReader input = new BufferedReader(inputStream);
			message = input.readLine();
			
			
			/*if(message.regionMatches(0, "UPTODATE", 0, 8)){
				System.out.println(message);
			}else{
			System.out.println("ALARM at " + message);
			}*/

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Can´t receive the message");
			e.printStackTrace();
		}
		return message;
	}

}

 class TCheckForModifications implements Runnable {
  public void run () {
      MoteIF moteIF;
      int flag = 0,j;
      String message;
      byte[] data = new byte[12];
      int counter = 0;
      moteIF = TestSerial.moteIF;
      System.out.println("PANid: " + TestSerial.PANid);
    
    Node[] nodes = new Node[2];
    nodes[0] = new Node(); nodes[1] = new Node();
    TestSerialMsg payload1 = new TestSerialMsg(data,0,data.length);
   
	// ---------------Request to PHP Server----------------------------------------
	System.out.println("Begining client...");
	Client_socket c2 = new Client_socket(TestSerial.ServerIP);
	c2.send_message("BASE REQSETS " + TestSerial.PANid +"\n");
	String message1;
	message1 = c2.receive_message();
	System.out.println("PHP Answer: "+ message1);
	String[] parts1 = message1.split(" ");
	// -----------------------------------------------------------------------------
	j=0;
	for (int i=5;i<parts1.length;i=i+2){
		nodes[j].activated = Integer.parseInt(parts1[i]);
		
			data[2] = (byte)0;
			data[3] = (byte)(j+1);
			data[4] = (byte)1;
			data[5] = (byte)(Integer.parseInt(parts1[2]));//Buzzer enabled
			data[6] = (byte)Integer.parseInt(parts1[i]);
			data[7] = (byte)(Integer.parseInt(parts1[3]));//Propagate Buzzer enabled
			data[8] = (byte)0;								//Buzzer Off
			data[11] = (byte)(Integer.parseInt(parts1[1]));//Shut Down
			payload1.set_counter(counter);
			try {
				moteIF.send(0, payload1);
				System.out.println(payload1);
				counter++;
		    }
		    catch (IOException exception) {
		      System.err.println("Exception thrown when sending packets. Exiting.");
		      System.err.println(exception);
		    }
			j++;
		
	}
	System.out.println();
    try {
      while (true) {
		// ---------------Request to PHP Server----------------------------------------
		Client_socket c1 = new Client_socket(TestSerial.ServerIP);
		c1.send_message("BASE REQSETS " + TestSerial.PANid +"\n");
		String message2;
		message2 = c1.receive_message();
		System.out.println("PHP Answer: "+ message2);
		    String[] parts = message2.split(" ");
    TestSerialMsg payload = new TestSerialMsg(data,0,data.length);
		// -----------------------------------------------------------------------------
		j=0;
		
		for (int i=5;i<parts.length;i=i+2){
				if(nodes[j].activated != Integer.parseInt(parts[i])){
					data[2] = (byte)0;
					data[3] = (byte)(j+1);
					data[4] = (byte)1;
					data[5] = (byte)(Integer.parseInt(parts[2]));//Buzzer enabled
					data[6] = (byte)Integer.parseInt(parts[i]);
					data[7] = (byte)(Integer.parseInt(parts[3]));//Propagate Buzzer enabled
					data[8] = (byte)0;								//Buzzer Off
					data[11] = (byte)0;								//Enable
					nodes[j].activated = Integer.parseInt(parts[i]);
					System.out.println("Sending packet.");
					payload.set_counter(counter);		
					moteIF.send(0, payload);
					System.out.println(payload);
					counter++;
					flag = 1;
				
			}j++;
		}	
		// Generic packet
		data[2] = (byte)0;
		data[3] = (byte)100;	//Node ID  
		data[4] = (byte)1;
		data[5] = (byte)(Integer.parseInt(parts[2]));	//Buzzer enabled
		data[6] = (byte)0;//Motion enabled
		data[7] = (byte)(Integer.parseInt(parts[3]));	//Propagate Buzzer enabled
		data[8] = (byte)0;//Buzzer Off
		data[11] = (byte)(Integer.parseInt(parts[1]));	//Enabled
		TestSerialMsg payload2 = new TestSerialMsg(data,0,data.length);	  
		System.out.println("Sending packet.");
		payload.set_counter(counter);		
		moteIF.send(0, payload2);
		System.out.println(payload2);
		counter++;
	      try {
	    	    Thread.sleep(3000);                 //1000 milliseconds is one second.
	    	} catch(InterruptedException ex) {
	    	    Thread.currentThread().interrupt();
	    	}
      }

    }
    catch (IOException exception) {
      System.err.println("Exception thrown when sending packets. Exiting.");
      System.err.println(exception);
    }
  }
}

class TNotifyOfAlarm implements Runnable, MessageListener {
     
  public MoteIF moteIF;
  public TNotifyOfAlarm(MoteIF moteIF){
	this.moteIF = moteIF;
	this.moteIF.registerListener(new TestSerialMsg(), this);
  }
  public void messageReceived(int to,Message message) {
		TestSerialMsg msg = (TestSerialMsg) message;
		byte[] data = new byte[12];
		data = msg.dataGet();
		System.out.println("============= Received packet sequence number " + msg.get_counter() + " " + TestSerial.PANid);
		System.out.println("Data rcv from Radio: " + msg + "\n" + Arrays.toString(data) + " .. " + data[3]);
		Client_socket c3 = new Client_socket(TestSerial.ServerIP);
		c3.send_message("BASE NOTIFICATION " + data[3] + " "+ TestSerial.PANid +"\n");
		String answer = c3.receive_message();
		System.out.println("PHP Answer: "+ answer);
  }
  
  public void run () {
     
	int counter = 0;
    System.out.println("PANid: " + TestSerial.PANid);

  }
  
}

public class TestSerial {

  public static MoteIF moteIF;
  public static int PANid;
  public static String ServerIP;
  public TestSerial(MoteIF moteIF) {
    this.moteIF = moteIF;

  }
    
  public void sendPackets() {
    int counter = 0;
    byte[] data = new byte[12];
    // Os 4 primeiros bytes sao para o counter e p/ node_id
    //Node_id
    data[2] = (byte)0;
    data[3] = (byte)0;
    
    data[4] = (byte)1;//Base Station
    data[8] = (byte)1;//Buzzer Off
    for(int i=5;i<11;i++){
		if(i!=8){
			data[i] = (byte)0; 	
		}
	}
    Scanner s1 = new Scanner(System.in);
    Scanner s2 = new Scanner(System.in);
    System.out.print("Server IP: ");
    ServerIP =  s1.nextLine();
    System.out.println();
    System.out.println("Server IP: " + ServerIP);
    System.out.print("PANid: ");
    PANid =  s1.nextInt();
    System.out.println();
    
	
	TCheckForModifications cfm = new TCheckForModifications();
	Thread tcfm = new Thread(cfm);
	tcfm.start();
	
	TNotifyOfAlarm cfa = new TNotifyOfAlarm(TestSerial.moteIF);
	Thread tcfa = new Thread(cfa);
	tcfa.start();
  }
  
  private static void usage() {
    System.err.println("usage: TestSerial [-comm <source>]");
  }
  
  public static void main(String[] args) throws Exception {
    String source = null;
    if (args.length == 2) {
      if (!args[0].equals("-comm")) {
	usage();
	System.exit(1);
      }
      source = args[1];
    }
    else if (args.length != 0) {
      usage();
      System.exit(1);
    }
    
    PhoenixSource phoenix;
    
    if (source == null) {
      phoenix = BuildSource.makePhoenix(PrintStreamMessenger.err);
    }
    else {
      phoenix = BuildSource.makePhoenix(source, PrintStreamMessenger.err);
    }

    MoteIF mif = new MoteIF(phoenix);
    TestSerial serial = new TestSerial(mif);
    serial.sendPackets();
  }


}
