package com.example.jose_trabalho.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class UserArea extends AppCompatActivity {
    public static Handler UIHandler;
    String ServerIP;
    String Email;
    String PANid;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ServerIP = extras.getString("ServerIP");
            Email = extras.getString("Email");
            PANid = extras.getString("PANid");
        }

        //Login Fields
        final EditText etEmail = (EditText) findViewById(R.id.etEmail);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);



        String[] sActivities = new String[]{"Settings", "Records"};

        ArrayAdapter<String> adapterAct = new ArrayAdapter<String>(this, R.layout.listview, sActivities);
        ListView lvActivities = (ListView) findViewById(R.id.lvActivities);
        lvActivities.setAdapter(adapterAct);
        lvActivities.setOnItemClickListener(GoToActivities());

    }
    public AdapterView.OnItemClickListener GoToActivities(){
        return(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                switch (position) {
                    case 0:
                       intent = new Intent(UserArea.this, Settings.class);
                        intent.putExtra("ServerIP",ServerIP);
                        intent.putExtra("PANid",PANid);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(UserArea.this, Records.class);
                        intent.putExtra("ServerIP",ServerIP);
                        intent.putExtra("PANid",PANid);
                        startActivity(intent);
                        break;


                }
            }
        });
    }

    public void startService(View view)
    {
        Intent intent =  new Intent(this,MyService.class);
        intent.putExtra("Email", Email);
        intent.putExtra("ServerIP", ServerIP);
        startService(intent);
    }

    public void stopService(View view) {
        Intent intent =  new Intent(this,MyService.class);
        stopService(intent);
    }
}
