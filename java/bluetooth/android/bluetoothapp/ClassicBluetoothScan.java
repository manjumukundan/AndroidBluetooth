package bluetooth.android.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

/**
 * Created by Manju .
 */
public class ClassicBluetoothScan
{

    private final static String TAG = "ClassicBluetooth";
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
//    private static long scan_frequency = 10000;
//    private static String name = "";
    private SimpleCursorAdapter mCursorAdapter;
    private BluetoothGatt mGatt;
    private long mTime;
    private int mDeviceCount = 0;
    private BTDevicesListActivity mActivity;

    public ClassicBluetoothScan(BTDevicesListActivity instance)
    {
        mActivity = instance;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void registerReceiver()
    {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mActivity.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    public void unRegisterReceiver()
    {
        mActivity.unregisterReceiver(mReceiver);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive called.......................");
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                // Add the name and address to an array adapter to show in a ListView
                Log.d(TAG, device.getName() + "  " + device.getAddress());

                String time = String.valueOf((System.currentTimeMillis() - mTime)) + "ms";
                ContentValues values = new ContentValues();
                values.put(DatabaseContentPovider.ROW_ID, ++ mDeviceCount);
                values.put(DatabaseContentPovider.DEVICE_NAME, device.getName() == null ? "LE_DEVICE" : device.getName());
                values.put(DatabaseContentPovider.DEVICE_ADDRESS, device.getAddress());
                values.put(DatabaseContentPovider.DEVICE_RSSI, rssi);
                values.put(DatabaseContentPovider.DEVICE_TIME, time);

                int deviceType = device.getType();
                if(deviceType == BluetoothDevice.DEVICE_TYPE_CLASSIC)
                {
                    Log.d(TAG, "DEVICE_TYPE_CLASSIC");
                }
                else if(deviceType == BluetoothDevice.DEVICE_TYPE_LE)
                {
                    Log.d(TAG, "DEVICE_TYPE_LE");
                }
                else if(deviceType == BluetoothDevice.DEVICE_TYPE_DUAL)
                {
                    Log.d(TAG, "DEVICE_TYPE_DUAL");
                }

                Uri uri = mActivity.getContentResolver().insert(DatabaseContentPovider.CONTENT_URI, values);

                connectToDevice(device);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                Log.d(TAG, "ACTION_DISCOVERY_FINISHED.............");
                scanDevices(true);
            }
        }
    };


    public void connectToDevice(BluetoothDevice device)
    {
//        mBluetoothAdapter.cancelDiscovery();
//        if (mGatt == null) {
//            mGatt = device.connectGatt(mActivity, false, mGattCallback);
//
//            if (null != mGatt)
//            {
//                Log.d(TAG, "mGatt not null");
//            }
//        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange");

            Log.i(TAG, "Connected to GATT server.");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "GATT_SUCCESS onServicesDiscovered received: " + status);
                gatt.getServices();
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.w(TAG, "onCharacteristicRead read from remote smart bt device : " + status);
        }
    };

    public void scanDevices(boolean enable) {

        int count = mActivity.getContentResolver().delete(DatabaseContentPovider.CONTENT_URI, null, null);
        Log.d(TAG, "Deleted " + count);
        mDeviceCount = 0;
        mTime = System.currentTimeMillis();
        boolean res = false;

        if (enable && !mBluetoothAdapter.isDiscovering())
        {
            res = mBluetoothAdapter.startDiscovery();
            Log.d(TAG, "start discovery " + res);
        }
        else
        {
            if (mBluetoothAdapter.isDiscovering())
            {
                res = mBluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "cancel discovery " + res);
            }
        }

    }

    public BroadcastReceiver getReceiverInstance()
    {
        return mReceiver;
    }

}


