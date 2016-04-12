package rmsf;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class client_socket {
	
	private Socket socket;
	
	public client_socket(String server,int port){
		try {
			this.socket = new Socket(server,port);
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
	public void receive_message(){
		String message;
		
		InputStreamReader inputStream;
		try {
			inputStream = new InputStreamReader(this.socket.getInputStream());
			BufferedReader input = new BufferedReader(inputStream);
			message = input.readLine();
			System.out.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Can´t receive the message");
			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		while(true){
			client_socket test1 = new client_socket("194.210.228.207",1906);
			test1.send_message("JAVA\n");
			test1.receive_message();
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
