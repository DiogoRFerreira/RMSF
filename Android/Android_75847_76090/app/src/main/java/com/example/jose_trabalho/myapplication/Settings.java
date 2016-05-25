package com.example.jose_trabalho.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Settings extends AppCompatActivity {



    class Sensor {
        String ID = "";
        boolean enabled;

    }

    // Array of booleans to store toggle button status
    public boolean Buzzer;
    public boolean Propagation;
    public boolean AlarmSysEnabled;
    String PANid;
    String ServerIP;
    Switch swBuzzer;
    Switch swPropagation;
    int sensorsLen;
    View selectedListItem;
    Sensor[] sensors = new Sensor[10];


    public void toggleBuzzer(View view) {
        Buzzer = !Buzzer;
        new ModifyTask().execute(PANid, "BUZZER", String.valueOf(Buzzer),"GenSettings");
    }

    public void toggleEnableAlarm(View view) {
        AlarmSysEnabled = !AlarmSysEnabled;
        new ModifyTask().execute(PANid, "ENABLE", String.valueOf(AlarmSysEnabled), "GenSettings");
    }

    public void togglePropagation(View view) {
        Propagation = !Propagation;
        new ModifyTask().execute(PANid, "PROPAGATION", String.valueOf(Propagation), "GenSettings");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toast.makeText(getApplicationContext(), "Loading current settings..", Toast.LENGTH_SHORT).show();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ServerIP = extras.getString("ServerIP");
            PANid = extras.getString("PANid");
        }
        for (int i = 0; i < sensors.length; i++) {
            sensors[i] = new Sensor();
        }
        ToggleButton swBuzzer = (ToggleButton) findViewById(R.id.swBuzzer);
        ToggleButton swPropagation = (ToggleButton) findViewById(R.id.swPropagation);
        ToggleButton swAlarmEnable = (ToggleButton) findViewById(R.id.swEnableAlarm);
        //Verificar no servidor e na BD quais as configuracoes actuais
        try {
            String str_result= new GetSettingsTask().execute(PANid).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < sensors.length; i++) {

            if ((sensors[i].ID.compareTo("")) == 0) {
                sensorsLen = (i);
                break;
            }
        }
        //Passa-las para a interface gráfica
        swAlarmEnable.setChecked(AlarmSysEnabled);
        swBuzzer.setChecked(Buzzer);
        swPropagation.setChecked(Propagation);
        String[] sSensors = new String[sensorsLen];
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sSensors);
        final ListView lvSensors = (ListView) findViewById(R.id.lvSensors);
        lvSensors.setAdapter(adapter);


        for (int i = 0; i < sensorsLen; i++) {
            sSensors[i] = "ID:  " + sensors[i].ID;
        }

        lvSensors.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                sensors[position].enabled = !sensors[position].enabled;
                String msg;
                if(sensors[position].enabled) msg = "Sensor ENABLED";
                else msg = "Sensor DISABLED";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                if (sensors[position].enabled == true) {
                    adapterView.getChildAt(position).setBackgroundColor(Color.parseColor("#c7dcb2"));
                } else {
                    adapterView.getChildAt(position).setBackgroundColor(Color.WHITE);
                }
                new ModifyTask().execute(PANid, String.valueOf(sensors[position].enabled ? 1 : 0),String.valueOf(sensors[position].ID), "Sensor");

            }
        });
        adapter.notifyDataSetChanged();
        lvSensors.setAdapter(adapter);
        lvSensors.post(new Runnable() {

            @Override
            public void run() {
                int total = lvSensors.getAdapter().getCount();
                for(int i=0; i<total; i++){
                    lvSensors.setSelected(true);
                    if(sensors[i].enabled==true) {
                        lvSensors.getChildAt(i).setBackgroundColor(Color.parseColor("#c7dcb2"));
                    }else{
                        lvSensors.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
                    }
                }
            }
        });

    }

    private class ModifyTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Clique no botao de Registo
            String message;
            // Pedido à base de dados
            ClientJava clientRegister = new ClientJava(ServerIP, new IPandPORT().PHPServer_Port);
            switch (strings[3]) {
                case "GenSettings":
                    clientRegister.send_message("ANDROID MODIFY " + strings[0] + " " + strings[1] + " " + (Boolean.parseBoolean(strings[2]) ? 1 : 0) + "\n");
                    break;
                case "Sensor":
                    clientRegister.send_message("ANDROID MODIFY " + strings[0] + " SENSOR " + strings[1] +" " + strings[2] + "\n");
                    break;
            }
            message = clientRegister.receive_message();
            Log.d("Message rcv: ", message);
            try {
                clientRegister.Close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //message = "NOK PASSWD";
            //Se a comunicacao c/base de dados for bem sucedida
            if (message != null) {
                if (message.regionMatches(0, "OK", 0, 2)) {
                    return strings[0] + " modified";
                } else return "Error ocurred";
            }
            return "Failed communication with server";
        }

        protected void onPostExecute(String UserFeedback) {
            //Toast.makeText(getApplicationContext(), UserFeedback, Toast.LENGTH_SHORT).show();
        }
    }


    private class GetSettingsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String message;
            // Pedido à base de dados
            ClientJava clientRegister = new ClientJava(ServerIP, new IPandPORT().PHPServer_Port);
            // strings[0] = PAN ID
            clientRegister.send_message("ANDROID RETRIEVE " + strings[0] + "\n");
            message = clientRegister.receive_message();
            try {
                clientRegister.Close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Se a comunicacao c/base de dados for bem sucedida
            if (message != null) {
                if (message.regionMatches(0, "OK", 0, 2)) {
                    String[] parts = message.split(" ");
                    AlarmSysEnabled = Integer.parseInt(parts[1]) != 0;
                    Buzzer = Integer.parseInt(parts[2]) != 0;
                    Propagation = Integer.parseInt(parts[3]) != 0;
                    int j = 0;
                    for (int i = 4; i < parts.length-1; i+=2) {
                        sensors[j].ID = parts[i];
                        sensors[j].enabled = Integer.parseInt(parts[i + 1]) != 0;
                        j++;
                    }
                }
            }
            return "";

        }
        protected void onPostExecute(String UserFeedback) {

        }
    }


}
