package com.example.cindy.ece1160_new;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter bluetoothAdapter;
    private static BluetoothSerialService mSerialService = null;
    private boolean CONNECTED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Toggle button to turn Bluetooth on and off
        Button unlockButton = (Button) findViewById(R.id.unlockbutton);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean multi = loadPrefs("phone_unlock", false); //phone_unlock multi_auth
//                final class workerThread implements Runnable {
//
//                    public workerThread() {
//                    }
//
//                    public void run()
//                    {
                if(multi) {
                    if(CONNECTED) {
                        String msg = "1";
                        mSerialService.write(msg.getBytes());
                        Toast.makeText(getApplicationContext(), "Key Phrase Sent.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        BTconnect();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Multifactor authentication disabled.", Toast.LENGTH_SHORT).show();
                }
//                    }
//                };
//                (new Thread(new workerThread())).start();
            }
        });

        //Click on password hint there will be a pop-up message
        Button passwordHintBtn = (Button) findViewById(R.id.passwordHintBtn);
        passwordHintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean test = loadPrefs("allow_hints", false);

                if(test){
                    Toast.makeText(getApplicationContext(), "Hint: straight and down will turn this lock upside down ", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Hints not enabled.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void BTconnect() {
        //Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
//            FragmentActivity activity = getActivity();
//            Toast.makeText(activity, "Bluetooth is not available on this device", Toast.LENGTH_LONG).show();
//            activity.finish();
            Log.d(TAG,"Does not have BT capabilities");
            //Device does not support Bluetooth
        }
        BluetoothSocket socket = null;
        String Sarah_MAC = "f4:cb:52:72:d0:55";
        final String Rpi_MAC = "B8:27:EB:33:7A:A1"; //"b8:27:eb:cc:85:5e"

        //final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//
        if(!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            //bluetoothAdapter.enable();
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                Toast.makeText(getApplicationContext(), "Searching for device...", Toast.LENGTH_SHORT).show();
                //BluetoothDevice device  = null;
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                //deviceHardwareAddress.equals(Rpi_MAC)
                if(deviceName.equals("raspberrypi")) {
                    Toast.makeText(getApplicationContext(), "Verified MAC Address: "+ deviceHardwareAddress, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Found device.", Toast.LENGTH_SHORT).show();
                    try {
//                        if ( mAllowInsecureConnections ) {
                            Method method;

                            method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
                            socket = (BluetoothSocket) method.invoke(device, 1);
                        Toast.makeText(getApplicationContext(), "Created socket.", Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            socket = device.createRfcommSocketToServiceRecord( SerialPortServiceClass_UUID );
//                        }
                    } catch (Exception e) {
                        Log.e(TAG, "create() failed", e);
                    }
//                    try {
//                        // Get a BluetoothSocket to connect with the given BluetoothDevice.
//                        // MY_UUID is the app's UUID string, also used in the server code.
//                        //socket = device.createInsecureRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
//                        socket = device.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
//                        Toast.makeText(getApplicationContext(), "Create socket.", Toast.LENGTH_SHORT).show();
//                    } catch (IOException e) {
//                        Log.e(TAG, "Socket's create() method failed", e);
//                    }
                    try {
                        // Connect to the remote device through the socket. This call blocks
                        // until it succeeds or throws an exception.

                        socket.connect();
                        Toast.makeText(getApplicationContext(), "Connected to socket.", Toast.LENGTH_SHORT).show();
                    } catch (IOException connectException) {
                        // Unable to connect; close the socket and return.
                        Toast.makeText(getApplicationContext(), "Unable to connect socket." + connectException.getMessage(), Toast.LENGTH_SHORT).show();
                        try {
                            socket.close();
                        } catch (IOException closeException) {
                            Toast.makeText(getApplicationContext(), "Could not close the client socket.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Could not close the client socket", closeException);
                        }
                        return;
                    }
                    try {
                        String msg = "1";
                        OutputStream mmOutputStream = socket.getOutputStream();
                        mmOutputStream.write(msg.getBytes());
                        Toast.makeText(getApplicationContext(), "Unlocking...", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                android.app.FragmentManager fragmentManager = getFragmentManager();
                SignInDialogFragment fire = new SignInDialogFragment();
                fire.show(fragmentManager, "dialog");
//                Intent myIntent = new Intent(MainActivity.this, Main2Activity.class);
//                startActivity(myIntent);
            case R.id.secure_connect_scan:
                final String Rpi_MAC = "B8:27:EB:33:7A:A1"; //Todds Rpi
                //String Rpi_MAC = "B8:27:EB:94:41:B4"; //Sarah Rpi
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                    //bluetoothAdapter.enable();
                }
                else{
                    if(! CONNECTED) {
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(Rpi_MAC);
                        mSerialService = new BluetoothSerialService(getBaseContext());
                        mSerialService.start();
                        mSerialService.connect(device);
                        Toast.makeText(getApplicationContext(), "Connected to box.", Toast.LENGTH_SHORT).show();
                        CONNECTED = true;
                    }
                }

        }

        return super.onOptionsItemSelected(item);
    }


    //get prefs
    private Boolean loadPrefs(String key, Boolean value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean data = sharedPreferences.getBoolean(key, value);
        return data;
    }

    public static class SignInDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater li = LayoutInflater.from(getActivity());
            View prompt = li.inflate(R.layout.dialog_signin, null);
            builder.setView(prompt);

            builder.setPositiveButton("Enter",  new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent myIntent = new Intent(getActivity(), Main2Activity.class);
                            startActivity(myIntent);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

}

