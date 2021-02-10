package com.example.seniordesignappv1;

//import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;


import androidx.annotation.RequiresApi;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class BLEControlPage extends Activity {
    private final static String TAG = BLEControlPage.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    //public static final boolean EXTRAS_DEVICE_SCAN_TOGGLE = true;


    private TextView mConnectionState;
    private TextView mDeviceAddressView;
    //private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;



    //my message button
    private Button mSendButton;
    //my RSSI button
    private Button mRSSIbutton;
    //my force Disconnect button
    private Button mDisconnectButton;
    private Button mTimeButton;
    //public int rssi_val;
    //my RSSI value display textview
    //private TextView mRSSIdisplay;
    //my average RSSI value display textview
    //private TextView mAveRSSIdisplay;
    //my sample number textview
    private TextView mSampleNumDisplay;
    private TextView mRSSIdisplay;
    private TextView mAveRSSIdisplay;
    private TextView mTimeDisplay;

    private Date mDate;

    private Instant mInstant;


    //sampling period (in ms)
    int sampling_period = 5;
    // my sample counter int
    public int g_sample_count = 0;
    // the number of RSSI samples for the the average
    public final static int SAMPLE_NUM = 20;
    //array of RSSI measurements
    int[] g_rssi_array = new int[SAMPLE_NUM];
    //the current rssi sample value
    int rssi = 0;
    //the average rssi sample value
    float rssi_average = 0;



    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";




    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(TAG, "You made it to the mBLuetoothLeService initialization");
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            Log.e(TAG, "You made it to the connection call");
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "You made it to the onServiceDisconnected call");
            mBluetoothLeService = null;

            /********************************************************
             *
             * After the devices disconnect, return to BLEScanPage
             *
             ********************************************************/

            //startActivity(new Intent(BLEControlPage.this, BLEScanPage.class));

            //finish();

        }
    };



    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                doorDisconnect(true);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);

                        }
                        mBluetoothLeService.writeCharacteristic(1);
                        return true;
                    }
                    return false;
                }
            };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b_l_e_control_page);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);


        Log.e(TAG, "Value stored in mDeviceAddress: " + mDeviceAddress);

        //connecting to BLE
        //mBluetoothLeService.connect(mDeviceAddress);

        // Sets up UI references.

        /**************************
         *
         * UNCOMMENT NEXT LINE
         * DISPLAY ADDRESS
         *
         **************************/
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDeviceAddressView = (TextView) findViewById(R.id.device_address);
        //mDataField = (TextView) findViewById(R.id.data_value);

        //my force disconnect button
        mDisconnectButton = (Button) findViewById(R.id.force_disconnect_button);

        mTimeButton = findViewById(R.id.time_button);
        mTimeDisplay = findViewById(R.id.time_textview);

        //timer display in GUI
        //mSampleNumDisplay = (TextView) findViewById(R.id.sample_count_display);
        mSampleNumDisplay = (TextView) findViewById(R.id.sample_count_display);
        mRSSIdisplay = (TextView) findViewById(R.id.rssi_value_display);
        mAveRSSIdisplay = (TextView) findViewById(R.id.ave_rssi_value_display);

        //setting up array to initially have all -100
        for(int i = 0; i < SAMPLE_NUM; i++)
        {
            g_rssi_array[i] = -125;
        }




        //initialize Timer/TimerTask
        Timer mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                //g_sample_count++;
                if(mBluetoothLeService == null)
                {
                    Log.e(TAG, "mBluetoothLeService is null");
                    //mBluetoothLeService.disconnect();
                    //mBluetoothLeService.connect(mDeviceAddress);
                }
                else {
                    if(!mBluetoothLeService.checkGATTnull()) {
                        g_sample_count++;
                        rssi = mBluetoothLeService.getRSSIsample();
                        rssi_average = mBluetoothLeService.updateAverageRSSI(g_sample_count, g_rssi_array, SAMPLE_NUM, rssi);
                        mBluetoothLeService.sendOpenDoorMessage(rssi_average);
                    }
                    else
                    {
                        Log.e(TAG, "mGATT is null");
                    }
                }
                //mAveRSSIdisplay.setText(String.valueOf(rssi_average));
                mAveRSSIdisplay.setText(String.format("%.2f", rssi_average));
                mRSSIdisplay.setText(String.valueOf(rssi));
                mSampleNumDisplay.setText(String.valueOf(g_sample_count));
            }
        };
        //starting the timer and choosing when to perform task
        mTimer.schedule(mTimerTask, 500, sampling_period);
        //the first RSSI reading is always zero. So I'm going to call it once below and not
        //catch the first reading.
        //int dummy = mBluetoothLeService.readRSSI();










        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "The disconnect Button Works");
                doorDisconnect(false);
            }
        });


        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //Log.w(TAG, "The disconnect Button Works");
                mInstant = Instant.now();
                mTimeDisplay.setText(formatTime(mInstant));
                Log.w(TAG, "Inside Timestamp Display. mInstant: " + mInstant.toString());
            }
        });

        //getActionBar().setTitle(mDeviceName);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        if(gattServiceIntent == null) {
            Log.e(TAG, "The mServiceConnection is null");
        }
        else
        {
            Log.e(TAG, "The mServiceConnection is NOT null");
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //mBluetoothLeService.writeCharacteristic(1);
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
                mDeviceAddressView.setText(mDeviceAddress);
            }
        });

    }

    private void displayData(String data) {
        if (data != null) {
            //mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    /*********************************
     *
     * MY Door Disconnect Function
     *
     *********************************/


    private void doorDisconnect(boolean continueScanning)
    {

        //the disconnect() function
        //mBluetoothLeService.disconnect();

        Log.e(TAG, "You made it to doorDisconnect Function");

        /*********************************
         *
         * maybe make gatt null?
         *
         * ---> mGattCharacteristics = null;
         * ---> mGattServicesList = null;
         *
         *********************************/

        mBluetoothLeService.close();

        final Intent intent = new Intent(this, BLEScanPage.class);
        intent.putExtra("EXTRAS_DEVICE_SCAN_TOGGLE",continueScanning);

        intent.putExtra("EXTRAS_DEVICE_DUMMY","A new Secret Message");


        //finish();

        startActivity(intent);
    }

    private String formatTime(Instant i)
    {

        String instantStr = i.toString();
        String[] separatedStr = instantStr.split("T");

        Log.e(TAG, "Separated String: " + separatedStr[0] + " " + separatedStr[1]);

        return  separatedStr[0] + " " + separatedStr[1];
    }

}