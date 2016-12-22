package com.nxt.demochatzalo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText editTextUsername;
    Button buttonDangNhap;
    ListView listViewUsername;

    ArrayList<String> mangUsername;

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://10.9.1.39:3000");
        } catch (URISyntaxException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSocket.connect();

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        buttonDangNhap = (Button) findViewById(R.id.buttonDangNhap);


        buttonDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("client-gui-username", editTextUsername.getText().toString());
            }
        });

        mSocket.on("ketquaDangKyUn", onNewMessage_DangKyUsername);
        mSocket.on("server-gui-username", onNewMessage_DanhSachUsername);
//


    }


    private Emitter.Listener onNewMessage_DanhSachUsername = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray danhsach;
                    try {
                        danhsach = data.getJSONArray("danhsach");

                        listViewUsername = (ListView) findViewById(R.id.listViewUsername);
                        mangUsername = new ArrayList<>();
                        for(int i=0;i<danhsach.length();i++){
                            mangUsername.add(danhsach.get(i).toString());
                        }

                        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, mangUsername);
                        listViewUsername.setAdapter(adapter);
                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

    private Emitter.Listener onNewMessage_DangKyUsername = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String noidung;

                    try {
                        noidung = data.getString("noidung");
                        if (noidung.equals("true")) {
                            Toast.makeText(MainActivity.this, "Dang ky thanh cong", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Dang ky that bai", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };
}
