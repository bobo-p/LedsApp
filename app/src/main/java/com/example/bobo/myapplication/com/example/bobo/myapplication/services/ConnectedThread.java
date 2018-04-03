package com.example.bobo.myapplication.com.example.bobo.myapplication.services;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            //Create I/O streams for connection
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }


    //write method
    public void write(String input) throws IOException {
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes

            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream

    }
}
