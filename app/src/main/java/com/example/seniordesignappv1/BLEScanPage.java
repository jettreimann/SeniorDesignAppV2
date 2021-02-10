package com.example.seniordesignappv1;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class BLEScanPage extends Activity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    //my flag for when the door is found
    private boolean doorFound;
    //the uuid of the door device
    private String doorServiceUUID = "19B10000-E8F2-537E-4F6C-D104768A1214";
    private String doorAddress = "FA:2E:0E:38:8B:F5";

    //These get passed between activities
    public static final boolean EXTRAS_DEVICE_SCAN_TOGGLE = true;
    public static final String EXTRAS_DEVICE_DUMMY = "Hi I am a Dummy";

    public String dummy;

    //declare XML objects
    TextView connectionStatus;
    TextView connectionState;
    TextView mainMessage;

    private Button mScanToggleButton;


    private static final int REQUEST_ENABLE_BT = 1;
    // Resets scanning after 40 seconds.
    private static final long SCAN_PERIOD = 40000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b_l_e_scan_page);
        //getActionBar().setTitle(R.string.title_devices);
        mHandler = new Handler();

        //initially set doorfound to false
        doorFound = false;

        //XML objects
        connectionStatus = findViewById(R.id.connection_status_message);
        connectionState = findViewById(R.id.scan_bool);
        mainMessage = findViewById(R.id.ble_scanning_intro_message);
        mScanToggleButton = findViewById(R.id.scan_toggle_button);



        mScanToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "The mScanToggleButton Button Works");

                //mScanToggleButton.setText(getString(R.string.disable_scan_message));
                scanLeDevice(!mScanning);

            }
        });



        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }

        connectionState.setText(String.valueOf(mScanning));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                doorReScan();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();


        final Intent intent = getIntent();

        mScanning = intent.getExtras().getBoolean("EXTRAS_DEVICE_SCAN_TOGGLE");

        dummy = intent.getExtras().getString("EXTRAS_DEVICE_DUMMY");

        Log.e(TAG, "The Value of Dummy: " + dummy);

        connectionState.setText(String.valueOf(mScanning));


        if(mScanning)
        {
            mainMessage.setText(R.string.ble_scanning_message);

            mScanToggleButton.setText(getString(R.string.disable_scan_message));
            connectionStatus.setText(getString(R.string.scanning));
            mainMessage.setText(R.string.ble_scanning_message);
            connectionStatus.setTextColor(getResources().getColor(R.color.error_green));
        }
        else
        {
            mainMessage.setText(R.string.ble_not_scanning_message);

            mScanToggleButton.setText(getString(R.string.enable_scan_message));
            connectionStatus.setText(getString(R.string.not_scanning));
            mainMessage.setText(R.string.ble_not_scanning_message);
            connectionStatus.setTextColor(getResources().getColor(R.color.error_red));
        }

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Log.e(TAG, "The Value of mScanning during onResume(): " + mScanning);

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        //setListAdapter(mLeDeviceListAdapter);









        //scanLeDevice(mScanning);


        Timer freshScanTimer = new Timer();
        TimerTask freshScanTimerTask = new TimerTask() {
            @Override
            public void run() {
                if(mBluetoothAdapter != null)
                {
                    if(mScanning) {
                        scanLeDevice(false);
                        scanLeDevice(true);
                    }
                    else
                    {
                        Log.e(TAG, "mScanning is false");
                    }
                }
                else
                {
                    Log.e(TAG, "mBluetoothAdapter is null");
                }
            }
        };
        //starting the timer and choosing when to perform task
        freshScanTimer.schedule(freshScanTimerTask, SCAN_PERIOD, SCAN_PERIOD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    /*
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }

     */

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            /*
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    connectionState.setText(String.valueOf(mScanning));
                    connectionStatus.setText(getString(R.string.scanning));
                    connectionStatus.setTextColor(getResources().getColor(R.color.error_green));
                    mScanToggleButton.setText(getString(R.string.disable_scan_message));
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

             */


            runOnUiThread(new Runnable(){

                @Override
                public void run(){
                    // update ui here


                    mScanning = true;
                    connectionState.setText(String.valueOf(mScanning));
                    mainMessage.setText(R.string.ble_scanning_message);
                    connectionStatus.setText(getString(R.string.scanning));
                    connectionStatus.setTextColor(getResources().getColor(R.color.error_green));
                    mScanToggleButton.setText(getString(R.string.disable_scan_message));
                }
            });

            mBluetoothAdapter.startLeScan(mLeScanCallback);

        } else {
            mScanning = false;

            runOnUiThread(new Runnable(){

                @Override
                public void run(){
                    // update ui here

                    connectionState.setText(String.valueOf(mScanning));
                    mainMessage.setText(R.string.ble_scanning_message);
                    mScanToggleButton.setText(getString(R.string.enable_scan_message));
                    connectionStatus.setText(getString(R.string.not_scanning));
                    mainMessage.setText(R.string.ble_not_scanning_message);
                    connectionStatus.setTextColor(getResources().getColor(R.color.error_red));

                }
            });
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = BLEScanPage.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
            /********
             CALLING AUTOCONNECT FUNCTION
             ********/
            //Log.e(TAG, "Address being added: " + device.getAddress());
            if(doorAddress.equals(device.getAddress()))
            {
                doorFound = true;
                doorConnect(device);
            }
            else
            {
                doorFound = false;
            }

        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    /***************************************
     *
     * MY FUNCTIONS
     *
     ***************************************/

    public void doorConnect(BluetoothDevice connectDevice)
    {
        if (connectDevice == null) return;
        final Intent intent = new Intent(this, BLEControlPage.class);
        intent.putExtra(BLEControlPage.EXTRAS_DEVICE_NAME, connectDevice.getName());
        intent.putExtra(BLEControlPage.EXTRAS_DEVICE_ADDRESS, connectDevice.getAddress());

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;

            //update UI
            connectionState.setText(String.valueOf(mScanning));
            mainMessage.setText(R.string.ble_not_scanning_message);
            mScanToggleButton.setText(getString(R.string.enable_scan_message));
            connectionStatus.setText(getString(R.string.not_scanning));
            mainMessage.setText(R.string.ble_not_scanning_message);
            connectionStatus.setTextColor(getResources().getColor(R.color.error_red));
        }
        startActivity(intent);
    }

    public void doorReScan()
    {
        Log.e(TAG, "doorReScan() has been called. Scanning(false)");
        //scanLeDevice(false);

        //new Handler is purely a 2 sec delay
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                //do something
            }
        }, 2000 );//time in milisecond

        Log.e(TAG, "doorReScan() has been called. Scanning(true)");
        mLeDeviceListAdapter.clear();
        scanLeDevice(true);

        Log.e(TAG, "doorReScan() is finished");

    }
}
