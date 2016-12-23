package com.nxt.demochatzalo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by NXT on 23/12/2016.
 */

public class SoundChatActivity extends AppCompatActivity {

    Button buttonGhiAm, buttonXong, buttonGui;

    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;


    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://10.9.1.39:3000/");
        } catch (URISyntaxException e) {

        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sound_chat);

        mSocket.connect();
        mSocket.on("server-gui-amthanh",onNewMessage_NhanAmThanh);

        buttonGhiAm = (Button) findViewById(R.id.buttonGhiAm);
        buttonXong = (Button) findViewById(R.id.buttonXong);
        buttonGui = (Button) findViewById(R.id.buttonGui);

        buttonGhiAm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start(view);
            }
        });
        buttonXong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop(view);
            }
        });
        buttonGui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                byte []amthanh=FileLocal_To_Byte(Environment.getExternalStorageDirectory().getAbsolutePath()+"/nxt.3gpp");

                mSocket.emit("client-gui-amthanh",amthanh);
            }
        });


    }


    private Emitter.Listener onNewMessage_NhanAmThanh = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    byte []amthanh;
                    try {
                        amthanh = (byte[]) data.get("noidung");
                        playMp3FromByte(amthanh);

                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };

    private void playMp3FromByte(byte[]mp3SoundByteArray){
        try {
            File tempMp3=File.createTempFile("chatsound","mp3",getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos=new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            MediaPlayer mediaPlayer=new MediaPlayer();
            FileInputStream fis=new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void start(View view) {
        try {

            if(ContextCompat.checkSelfPermission(SoundChatActivity.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
//
//                if(ActivityCompat.shouldShowRequestPermissionRationale(SoundChatActivity.this,Manifest.permission.RECORD_AUDIO)){
//
//                    Log.d("giaithich","giaithich");
//                    ActivityCompat.requestPermissions(SoundChatActivity.this,
//                            new String[]{Manifest.permission.RECORD_AUDIO},
//                            1);
//                }else{
//                    Log.d("khong giai thich","khong giai thich");

                ActivityCompat.requestPermissions(SoundChatActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);


                //   }


            }else{

                outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/nxt.3gpp";
                myRecorder = new MediaRecorder();
                myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                myRecorder.setOutputFile(outputFile);


                myRecorder.prepare();
                myRecorder.start();
                Toast.makeText(getApplicationContext(), "Start recording ...", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop(View view) {
        if(myRecorder!=null){
            try {
                myRecorder.stop();
                myRecorder.release();
                myRecorder = null;
                Toast.makeText(getApplicationContext(), "Stop recording ...", Toast.LENGTH_SHORT).show();
            }catch (IllegalStateException ex){
                ex.printStackTrace();
            }
            catch (RuntimeException ex){
                ex.printStackTrace();
            }finally {
                myRecorder.release();
                myRecorder = null;
            }

        }else{
            Log.d("ghiam","khong co gi");
        }

    }

    public byte[] FileLocal_To_Byte(String path){
        File file=new File(path);
        int size=(int)file.length();
        byte[] bytes=new byte[size];

        try {
            BufferedInputStream buf=new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes,0,bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
