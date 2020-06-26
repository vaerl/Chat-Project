package com.example.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    public final String TAG = getClass().toString();

    public static String IP_NAME = "ip";
    private String ip;

    public static String NAME_NAME = "name";
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getIntent().hasExtra(IP_NAME) && getIntent().hasExtra(NAME_NAME)) {
            this.ip = getIntent().getStringExtra(IP_NAME);
            this.name = getIntent().getStringExtra(NAME_NAME);
            ((TextView) findViewById(R.id.display_name)).setText(name);
            ((TextView) findViewById(R.id.display_ip)).setText(ip);
        } else {
            super.onBackPressed();
        }

        findViewById(R.id.button_send).setOnClickListener(view -> {
            new Thread(messageClient).start();
        });

        ((EditText) findViewById(R.id.message_view)).setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                MainActivity.hideKeyboard(this);
                new Thread(messageClient).start();
                return true;
            }
            return false;
        });
    }

    private Runnable messageClient = () -> {
        try {
            Socket s = new Socket(ip, 8081);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            out.write(name + "|" + ((EditText) findViewById(R.id.message_view)).getText().toString());
            out.newLine();
            out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String text = in.readLine();
            Log.d(TAG, "Client received: " + text);
            ((TextView) findViewById(R.id.display_chat)).append(text + "\n");
            out.close();
            in.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
       runOnUiThread(() -> {
           MainActivity.hideKeyboard(this);
           ((EditText) findViewById(R.id.message_view)).setText("");
       });
    };

}