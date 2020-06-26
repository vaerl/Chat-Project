package com.example.chat;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static java.nio.ByteBuffer.allocate;

public class SeverActivity extends AppCompatActivity {

    public final String TAG = getClass().toString();
    private ArrayList<String> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sever);
        new Thread(messagesServer).start();
        new Thread(usersServer).start();
    }

    private Runnable messagesServer = () -> {
        try {
            // set everything up
            ServerSocket server = new ServerSocket(8081);
            String ip = InetAddress.getByAddress(
                    allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE)).getConnectionInfo().getIpAddress()).array())
                    .getHostAddress();
            runOnUiThread(() -> {
                Log.i(TAG, "messagesServer: IP-Address: " + ip + ", listening on port 8081.");
                TextView tv = findViewById(R.id.ip_view);
                tv.setText("IP: " + ip);
            });

            // accept messages as long as the app is running
            while (true) {
                // accept new message
                Socket s = server.accept();
                Log.d(TAG, "messagesServer: Server accepted new message.");
                // read message
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                final String text = in.readLine();
                Log.d(TAG, "messagesServer: Server received text: " + text);
                String user = text.split("\\|")[0];
                Log.d(TAG, "messagesServer: Server received user: " + user);
                String content = text.split("\\|")[1];
                Log.d(TAG, "messagesServer: Server received content: " + content);
                String message;
                print(users, "messagesServer");
                if(users.contains(user)){
                    message = user + " says: " + content;
                    Log.d(TAG, "messagesServer: User is known, sending message: " + text);
                } else {
                    Log.d(TAG, "messagesServer: User is unkown, refusing to send message.");
                    continue;
                }

                // update UI
                runOnUiThread(() -> {
                    TextView tv = findViewById(R.id.messages_view);
                    tv.append(message + "\n");
                });
                // distribute message
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                out.write(message);
                out.newLine();
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    private Runnable usersServer = () -> {
        try {
            // set everything up
            ServerSocket server = new ServerSocket(8082);

            // accept messages as long as the app is running
            while (true) {
                // accept new message
                Socket s = server.accept();
                Log.d(TAG, "usersServer: Server accepted new user.");
                // read message
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                final String user = in.readLine();
                Log.d(TAG, "usersServer: Server received user:" + user);

                // react to new user
                String message;
                if (users.contains(user)) {
                    message = "User " + user + " already exists, choose a different name!";
                    Log.d(TAG, "usersServer: user " + user + " already exists.");
                } else {
                    users.add(user);
                    message = "ok";
                    Log.d(TAG, "usersServer: Server added user:" + user);
                }
                print(users, "usersServer");

                // distribute message
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                out.write(message);
                out.newLine();
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    private void print(List<?> list, String thread){
        Log.d(TAG, "UsersList from thread: " + thread);
        for (Object user:list) {
            Log.d(TAG, "User: " + user);
        }
    }
}