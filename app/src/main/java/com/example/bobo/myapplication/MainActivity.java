package com.example.bobo.myapplication;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.slider.LightnessSlider;
import com.flask.colorpicker.slider.OnValueChangedListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnColorSelectedListener,OnValueChangedListener {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public String DeviceAdress="";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ColorPickerView colorPicker=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorPicker=(ColorPickerView)findViewById(R.id.color_picker_view);
        colorPicker.addOnColorSelectedListener(this);
        LightnessSlider lightnessSlider=(LightnessSlider)findViewById(R.id.v_lightness_slider);
        lightnessSlider.setOnValueChangedListener(this);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

    }
    @Override
    public void onResume() {
        super.onResume();
        try {
            checkBTState();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            DeviceAdress=prefs.getString(getString(R.string.savedAdress),"");
        }
        catch(Exception e) {
            Toast.makeText(getBaseContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
        if(DeviceAdress=="")
        {
            Intent intent =new Intent(this,DeviceList.class);
            startActivity(intent);
            return;
        }
        Connect(DeviceAdress);

    }


    public void onColorSelected(int selectedColor) {
        String input=Integer.toHexString(selectedColor);
        sendData(GetRgbString(input));
    }
    private  String GetRgbString(String colorStr) {
        String r=Integer.valueOf( colorStr.substring( 2, 4 ), 16 ).toString();
        String g=Integer.valueOf( colorStr.substring( 4, 6 ), 16 ).toString();
        String b=Integer.valueOf( colorStr.substring( 6, 8 ), 16 ).toString() ;
        return r+","+g+","+b+"\n";
    }
    private void checkBTState()
    {
        // Check device has Bluetooth and that it is turned on
        BluetoothAdapter mBtAdapter= BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(mBtAdapter==null) {
            Toast.makeText(getBaseContext(), "Urządzenie nie obsługuje Bluetooth ", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mBtAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (Exception e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Błąd połączenia - sprawdź czy kuźwa masz w kontakcie.", Toast.LENGTH_SHORT).show();
            Connect(DeviceAdress);
        }
    }

    @Override
    public void onValueChanged(float v) {
        if(colorPicker!=null)
        {
            String input=Integer.toHexString(colorPicker.getSelectedColor());
            sendData(GetRgbString(input));
        }
    }
    private void Connect(String address)
    {
        if(btSocket!=null && btSocket.isConnected())
        {
            try {
                btSocket.close();
            }
            catch(Exception e)
            {
                Toast.makeText(getBaseContext(), "ERROR - Nie można zamknąć połączenia", Toast.LENGTH_SHORT).show();
            }
        }
        BluetoothDevice device=null;
        try
        {
            device = btAdapter.getRemoteDevice(DeviceAdress);
        }
        catch(Exception e)
        {
            Toast.makeText(getBaseContext(), "ERROR - Zły adres urządzenia", Toast.LENGTH_SHORT).show();
            Intent intent =new Intent(this,DeviceList.class);
            startActivity(intent);
            return;
        }
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - Nie mozna utworzyć socketu", Toast.LENGTH_SHORT).show();
        }

        // Establish the connection.
        if(!btSocket.isConnected()) {
            try {
                btSocket=(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);;
            } catch (Exception e) {
                try {
                    btSocket.close();        //If IO exception occurs attempt to close socket
                } catch (IOException e2) {
                    Toast.makeText(getBaseContext(), "ERROR - Nie można zamknąć podczas reconnectu", Toast.LENGTH_SHORT).show();
                }
            }
        }
        // Create a data stream so we can talk to the device
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - Nie mnmożna utworzyć outstream", Toast.LENGTH_SHORT).show();
        }
    }
}
