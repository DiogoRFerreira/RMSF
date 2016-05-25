package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Timestamp;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client_socket {
	
	private Socket socket;
	
	public Client_socket(String server,int port){
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
