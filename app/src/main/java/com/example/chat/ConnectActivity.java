package com.example.chat;

import android.content.Intent;
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

public class ConnectActivity extends AppCompatActivity {

    public final String TAG = getClass().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // listener for connect-button
        findViewById(R.id.connect_button).setOnClickListener(view -> {
           new Thread(usernameClient).start();
        });

        ((EditText) findViewById(R.id.ip_input)).setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                MainActivity.hideKeyboard(this);
                findViewById(R.id.connect_button).callOnClick();
                return true;
            }
            return false;
        });
        findViewById(R.id.name_input).requestFocus();
    }

    private Runnable usernameClient = () -> {
        try {
            Socket s = new Socket(((EditText) findViewById(R.id.ip_input)).getText().toString(), 8082);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            out.write(((EditText) findViewById(R.id.name_input)).getText().toString());
            out.newLine();
            out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String text = in.readLine();
            Log.d(TAG, "Client received: " + text);
            if(!text.equals("ok")){
                runOnUiThread(() -> {
                    ((TextView) findViewById(R.id.error_view)).setText(text);
                });
                // stop execution if user exists!
                return;
            }
            out.close();
            in.close();
            s.close();
            // start chatActivity
            // TODO do I need a UI-Thread?
            runOnUiThread(() -> {
                // start message-activity
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(ChatActivity.IP_NAME, ((EditText) findViewById(R.id.ip_input)).getText().toString());
                intent.putExtra(ChatActivity.NAME_NAME, ((EditText) findViewById(R.id.name_input)).getText().toString());
                startActivity(intent);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


}