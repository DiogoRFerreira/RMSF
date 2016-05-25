package com.example.jose_trabalho.myapplication;

/**
 * Created by jose_trabalho on 16/04/2016.
 */

import android.content.Intent;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
class IPandPORT {
    int PHPServer_Port = 1901;
    String PHPServer_IP = "192.168.0.102";
        }

public class ClientJava {
    private Socket socket;



    public ClientJava(String server, int port){
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
        String message = null;
        Object returned = new Object();

        InputStreamReader inputStream;
        try {
            inputStream = new InputStreamReader(this.socket.getInputStream());
            BufferedReader input = new BufferedReader(inputStream);
            message = input.readLine();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Can´t receive the message");
            e.printStackTrace();
        }
        return message;

    }
    public void Close() throws IOException {
        this.socket.close();

    }

}
