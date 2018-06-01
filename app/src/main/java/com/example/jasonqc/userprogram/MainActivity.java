package com.example.jasonqc.userprogram;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jasonqc.userprogram.utils.ZXingUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    ImageView qR_img;
    EditText sendTcpTxt;
    EditText ipEditText;
    EditText portEditText;
    /************************************************/
    ExecutorService exec = Executors.newCachedThreadPool();
    Socket clientSocket = null;
    Handler uiHandler = new Handler();

    /***********************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qR_img = findViewById(R.id.qR_img);
        sendTcpTxt = findViewById(R.id.sendTcpTxt);
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
    }

    public void sendTcpData(View view) {
        if (clientSocket == null) {
            Toast.makeText(this, "请先建立连接", Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(sendTcpTxt.getText())) {
                Toast.makeText(this, "请输入数据", Toast.LENGTH_SHORT).show();
            } else {
                exec.execute(() -> {
                    PrintWriter pw;
                    BufferedReader bufferedReader;
                    try {
                        pw = new PrintWriter(clientSocket.getOutputStream());
                        Log.d("tcpSendToHost", sendTcpTxt.getText().toString());
                        pw.print(sendTcpTxt.getText().toString());

                        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String message;
                        while ((message = bufferedReader.readLine()) != null) {
                            Log.d("message", message);
                            String finalMessage = message.trim();
                            uiHandler.post(() -> {
                                Bitmap bitmap = ZXingUtils.createQRImage(finalMessage, qR_img.getWidth(), qR_img.getHeight());
                                qR_img.setImageBitmap(bitmap);
                            });
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    public void disconnectToTcpHost(View view) {
        try {
            if (clientSocket == null)
                return;
            clientSocket.close();
            clientSocket = null;
            Log.d("disconnectToTcpHost", "disconnect");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectToTcpHost(View view) {
        if (TextUtils.isEmpty(ipEditText.getText()) || TextUtils.isEmpty(portEditText.getText())) {
            Toast.makeText(this, "请输入ip 和 port", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("connectToTcpHost", ipEditText.getText().toString() + "," + portEditText.getText().toString());
            exec.execute(() -> {
                try {
                    clientSocket = new Socket(ipEditText.getText().toString(), Integer.parseInt(portEditText.getText().toString()));
                    Log.d("connectToTcpHost", String.valueOf(clientSocket.isConnected()));
                    uiHandler.post(() -> {
                        if (clientSocket.isConnected())
                            Toast.makeText(getApplicationContext(), "TCP连接成功", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "TCP连接失败", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
