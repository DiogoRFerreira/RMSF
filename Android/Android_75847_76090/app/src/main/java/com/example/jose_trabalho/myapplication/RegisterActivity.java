package com.example.jose_trabalho.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ContentHandler;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    public String ServerIP;

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Clique no botao de Registo
            String message, Userfeedback;
            // Pedido à base de dados
            ClientJava clientRegister = new ClientJava(ServerIP, new IPandPORT().PHPServer_Port);
            clientRegister.send_message("ANDROID REGISTER " + strings[0] + " " + strings[1] + " " + strings[2] + " " + strings[3]+"\n");
            message = clientRegister.receive_message();
            try {
                clientRegister.Close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //message = "NOK PASSWD";
            //Se a comunicacao c/base de dados for bem sucedida
            if(message!=null) {
                if (message.regionMatches(0, "OK", 0, 2)) {
                    return Userfeedback = "Successfully registered!";
                } else if (message.regionMatches(0, "NOK PANID", 0, 9)) {
                    return Userfeedback = "PAN Identifier does not exist.";
                } else if (message.regionMatches(0, "NOK PANSK", 0, 9)) {
                    return Userfeedback = "Serial Key does not match the inserted PAN.";
                }
            }
            return "";
        }
        protected void onPostExecute(String UserFeedback) {

            Toast.makeText(getApplicationContext(), UserFeedback, Toast.LENGTH_SHORT).show();
            if (UserFeedback.equals("Successfully registered!")){
                Intent MainActivityIntent = new Intent(RegisterActivity.this, MainActivity.class);
                RegisterActivity.this.startActivity(MainActivityIntent);
            }

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ServerIP = extras.getString("ServerIP");
        }

        Toast.makeText(getApplicationContext(), ServerIP, Toast.LENGTH_LONG).show();

        //Administrator Fields
        final EditText etName = (EditText) findViewById(R.id.etName);
        final EditText etEmail = (EditText) findViewById(R.id.etEmail);
        //PAN Fields
        final EditText etPANid = (EditText) findViewById(R.id.etPANid);
        final EditText etPANsk = (EditText) findViewById(R.id.etPANsk);
        //Text filed para indicacoes de erro
        final TextView tvPANidError = (TextView) findViewById(R.id.tvPANidError);
        final TextView tvPANskError = (TextView) findViewById(R.id.tvPANskError);
        //Button Field
        final Button bRegisterAdmin = (Button) findViewById(R.id.bRegisterAdmin);

        //Clique no botao de Registo
        bRegisterAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask().execute(etName.getText().toString(), etEmail.getText().toString(), etPANid.getText().toString(),etPANsk.getText().toString());
            }

        });

        etPANid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPANidError.setVisibility(View.INVISIBLE); // Esconder aviso
            }
        });

        etPANsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvPANskError.setVisibility(View.INVISIBLE); // Esconder aviso
            }
        });


    }


}
