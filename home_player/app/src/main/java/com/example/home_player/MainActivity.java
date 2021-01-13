package com.example.home_player;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    private AudioManager _audioManager;

    String _receiveIp, _receivePort, _receiveData;

    private final Handler _handler = new Handler();
    private final Runnable _runnable = new Runnable() {
        @Override
        public void run() {
            ((TextView)findViewById(R.id.textView_ip)).setText(_receiveIp);
            ((TextView)findViewById(R.id.textView_port)).setText(_receivePort);
            ((TextView)findViewById(R.id.textView_data)).setText(_receiveData);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 오디오 매니저
        _audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        // UDP 수신
        ReceiveData receiver = new ReceiveData(5001);
        receiver.start();

        Button decreaseBtn = (Button)findViewById(R.id.button_decrease);
        decreaseBtn.setOnClickListener(new Button.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View v) {
                int curVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int minVolume = _audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
                if(minVolume > --curVolume)
                {
                    curVolume = minVolume;
                }
                _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, AudioManager.FLAG_PLAY_SOUND);
            }
        });
        Button increaseBtn = (Button)findViewById(R.id.button_increase);
        increaseBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int maxVolume = _audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if(maxVolume < ++curVolume)
                {
                    curVolume = maxVolume;
                }
                _audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, AudioManager.FLAG_PLAY_SOUND);
            }
        });
    }

    class  ReceiveData extends Thread {
        Handler handler = _handler;
        DatagramSocket socket;

        public ReceiveData(int port) {
            try {
                socket = new DatagramSocket(port);
            }catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                while (true) {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    InetAddress ipAddr = packet.getAddress();
                    _receiveIp = ipAddr.toString();

                    int port = packet.getPort();
                    _receivePort = String.valueOf(port);

                    _receiveData = new String(buf);
                    packet = new DatagramPacket(buf, buf.length, ipAddr, port) ;
                    socket.send(packet);
                    handler.post(_runnable);
                    sleep(20);
                }
            }catch (InterruptedException e) {
                _receiveData = e.toString();
                handler.post(_runnable);
            }catch (Exception e) {
                _receiveData = e.toString();
                handler.post(_runnable);
            }
        }
    }
}