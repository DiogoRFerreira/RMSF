package com.example.jose_trabalho.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.view.WindowManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import android.util.Log;
import android.app.Activity;
import java.lang.Thread;
import java.lang.Runnable;
import android.app.AlertDialog;
import android.os.Vibrator;
import android.content.Intent;


/**
 * Created by jose_trabalho on 16/04/2016.
 */
public class MyService extends Service{
    // Flag para sair do ciclo de background
    boolean flag = true;
    String Email;
    String ServerIP;
    public static String message;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    public final class ThreadClass implements Runnable
    {
        int service_id;

        ThreadClass(int service_id){
            this.service_id = service_id;
        }

        @Override
        public void run() {
            flag=true;

            //   Utilizado para criar a thread. De outra forma
            //   o servico entraria em conflicto com o uso simultaneo
            //   da aplicacao.
            synchronized (this) {
                while(flag){
                    // Inicar o socket
                    ClientJava client = new ClientJava(ServerIP,new IPandPORT().PHPServer_Port);
                    client.send_message("ANDROID NOTIFICATION "+ Email + "\n");
                     message = client.receive_message();
                    if(message != null) {
                        if (message.regionMatches(0, "ALARM", 0, 5)) {
                            Log.d("New ALARM at ", message);
                            UserArea.runOnUI(new Runnable() {
                                public void run() {
                                    Vibrator v = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    v.vibrate(3000);

                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
                                    mBuilder.setSmallIcon(R.raw.intruder_alarm_icon);
                                    mBuilder.setContentTitle("Motion detected!");
                                    String[] parts = message.split("_");

                                    Toast.makeText(getApplicationContext(), "Attention! ALARM at " + parts[1], Toast.LENGTH_SHORT).show();

                                    mBuilder.setContentText("At " + parts[1] + " by Sensor ID " + parts[2]);
                                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                                    PendingIntent resultPendingIntent =
                                            PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
                                            );
                                    mBuilder.setContentIntent(resultPendingIntent);
                                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    int notificationID = 2;
                                    // notificationID allows you to update the notification later on.
                                    mNotificationManager.notify(notificationID, mBuilder.build());
                                    MediaPlayer alarm_sound = new MediaPlayer();
                                    // O som do alarme foi colocado na pasta raw dos recursos
                                    alarm_sound = MediaPlayer.create(getApplicationContext(),R.raw.alarm_sound);
                                    alarm_sound.start();

                                }
                            });
                            //Toast.makeText(getApplicationContext(), "\" Attention required! New ALARM at \" + message", Toast.LENGTH_LONG).show();
                        } else if (message.regionMatches(0, "UPTODATE", 0, 8)) {
                            Log.d("No motion detected", "");
                            //Toast.makeText(getApplicationContext(), "\"No motion detected. Your home is safe!", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        UserArea.runOnUI(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Couldn't reach server. Closing...", Toast.LENGTH_SHORT).show();
                            }
                        });
                        flag=false;
                    }
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                }
                stopSelf(service_id);
            }
        }
    }

    // Executa assim que o servico e iniciado
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Email = intent.getStringExtra("Email");
        ServerIP = intent.getStringExtra("ServerIP");

        Toast.makeText(this, "Started client in BG. Will query " + ServerIP + " every 5 seconds for new alarms for " + Email, Toast.LENGTH_LONG).show();
        // Iniciar a thread com o servico de cliente
        Thread thread = new Thread(new ThreadClass(startId));
        thread.start();

        return START_STICKY;
    }
    // Executa assim que o servico e encerrado
    @Override
    public void onDestroy() { // Quando o sistema elimina o servico
        Toast.makeText(this, "Client stopped. You will not be notified anymore.", Toast.LENGTH_LONG).show();
        flag = false;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
