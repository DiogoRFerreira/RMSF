package com.example.jose_trabalho.myapplication;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import android.view.View;
import android.widget.Toast;
import com.example.jose_trabalho.myapplication.ExpandableListAdapter;

// Thanks to Ravi Tamada for the tutorial

public class Records extends AppCompatActivity {
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    static String PANid;
    String ServerIP;
    int nrRecords = -1;
    int i;
    public String[] records;
    Thread TGetRecords =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ServerIP = extras.getString("ServerIP");
            PANid = extras.getString("PANid");
        }

        ThreadGetRecords tgetrecords = new ThreadGetRecords();
        Thread tgr = new Thread( tgetrecords );

        tgr.start();
        try {
            tgr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nrRecords =  tgetrecords.getNrRecords();

        //new GetNrRecordsTask().execute(PANid);
        //Toast.makeText(getApplicationContext(), "Record: " +  nrRecords, Toast.LENGTH_SHORT).show();
        String[] records = new String[nrRecords];
        records = Arrays.copyOf(tgetrecords.getRecords(), tgetrecords.getRecords().length);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData(records);
        Toast.makeText(getApplicationContext(), "Displaying a total of : " +  nrRecords + " records.", Toast.LENGTH_LONG).show();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

    }

    /*
     * Preparing the list data
     */
    private void prepareListData(String[] records) {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("PAN Identifier: " + PANid);

        // Adding child data
        List<String> PAN1 = new ArrayList<String>();
        PAN1.add("             Date                 Sensor ID");

        for( String r: records)
            PAN1.add(r);


        listDataChild.put(listDataHeader.get(0), PAN1); // Header, Child data
    }

    class ThreadGetRecords implements Runnable {
        private volatile int TnrRecords =-1;
        private volatile String[] Trecords;
        public void run() {
            ClientJava clientRegister = new ClientJava(ServerIP, new IPandPORT().PHPServer_Port);
            // strings[0] = PAN ID
            clientRegister.send_message("ANDROID RECORDS " + PANid + "\n");
            String message = clientRegister.receive_message();

            if (message != null) {
                if (message.regionMatches(0, "OK", 0, 2)) {
                    String[] parts = message.split("///");
                    String [] OKandNRrecords = parts[0].split(" ");
                    TnrRecords = Integer.parseInt(OKandNRrecords[1]);
                    Trecords = parts[1].split("//");
                }
            }


        }

        public int getNrRecords(){
            return TnrRecords;
        }

        public String[] getRecords(){
            return Trecords;
        }

    }

}
