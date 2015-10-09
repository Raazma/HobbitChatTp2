package raz.hobbitchat;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Chat_Activity extends AppCompatActivity {
    InetAddress adrMulticast = null;
    MulticastSocket soc = null;
    DatagramPacket paquet;
    String message = "TEST TEST TEST";
    String userName = "défault";
    int port = 6000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_);
        Intent intent = getIntent();
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiManager.MulticastLock lock = wifi.createMulticastLock("HobbitChat");
            lock.acquire();
        }

        ZeBigLecture lecture = new ZeBigLecture();
        lecture.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void sendMessage(View v)
    {
        message = userName  + ": " +  ((EditText) findViewById(R.id.theMessage)).getText().toString();
        Emeteur em = new Emeteur();
        Thread t = new Thread(em);
        t.start();

    }
    private boolean checkboxIsChecked()
    {
        return ((CheckBox)findViewById(R.id.checkBoxIp)).isChecked();
    }

    private class ZeBigLecture extends AsyncTask<Void, String, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                 adrMulticast = InetAddress.getByName("230.0.0.1");
                soc = new MulticastSocket(port);
                soc.joinGroup(adrMulticast);
            }
            catch (IOException e){}

        }

        @Override
        protected Void doInBackground(Void... args) {
            boolean end = false;
            byte tampon[] = new byte[1024];
            paquet = new DatagramPacket(tampon, 0, 1024);

            try {
                while (!end) {
                    soc.receive(paquet);
                    publishProgress(new String(paquet.getData(),
                            paquet.getOffset(), paquet.getLength()));
                }
            }
            catch (IOException e){}

            return null;
        }

        @Override
        protected void onProgressUpdate(String... messages) {
            if(!checkboxIsChecked())
            ((TextView)findViewById(R.id.message)).append(messages[0] + "\n");
            else
                ((TextView)findViewById(R.id.message)).append(messages[0].substring(0,messages[0].indexOf(":") )+" ("+ paquet.getAddress().toString().substring(1,paquet.getAddress().toString().length())+") " + messages[0].substring(messages[0].indexOf(":"),messages[0].length()) + "\n");


            // mise à jour de la barre de progression

        }

        @Override
        protected void onPostExecute(Void resultat) {
            try {
                soc.leaveGroup(adrMulticast);
            }
            catch (IOException e){}
        }
    }
    private class Emeteur implements Runnable
    {
       public Emeteur()
       {


       }

        @Override
        public void run() {
            byte tampon[] = message.getBytes();

            try {
                InetAddress adresse = InetAddress.getByName("230.0.0.1");
                MulticastSocket socEnvoie = new MulticastSocket();
                DatagramPacket paquet = new DatagramPacket(tampon, 0, tampon.length, adresse, port);
                socEnvoie.send(paquet);

            }
            catch (IOException e)
            {


            }

        }
    }
}
