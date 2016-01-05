package bluetooth.android.bluetoothapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Manju.
 */
public class BluetoothLowEnergyScan
{
    private final static String TAG = "BluetoothLowEnergy";
    public BluetoothLeScanner BTLEscanner;
    private BluetoothAdapter mBTAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private boolean mScanning;
    private Handler mHandler;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private long mTime;
    private int mDeviceCount = 0;
    private SimpleCursorAdapter mCursorAdapter;

    private BTDevicesListActivity mActivity;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BluetoothLowEnergyScan(BTDevicesListActivity instance)
    {
        mActivity = instance;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Log.d(TAG, "BTLEscanner creating...............");
            BTLEscanner = mBTAdapter.getBluetoothLeScanner();
            Log.d(TAG, "BTLEscanner created " + BTLEscanner);
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<ScanFilter>();

        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    final int signal = rssi;
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "LeScanCallback " + device.toString());

                            // Add the name and address to an array adapter to show in a ListView
                            Log.d(TAG, device.getName() + "  " + device.getAddress());

                            String time = String.valueOf((System.currentTimeMillis() - mTime)) + "ms";
                            ContentValues values = new ContentValues();
                            values.put(DatabaseContentPovider.ROW_ID, ++mDeviceCount);
                            values.put(DatabaseContentPovider.DEVICE_NAME, device.getName() == null ? "LE_DEVICE" : device.getName());
                            values.put(DatabaseContentPovider.DEVICE_ADDRESS, device.getAddress());
                            values.put(DatabaseContentPovider.DEVICE_RSSI, signal);
                            values.put(DatabaseContentPovider.DEVICE_TIME, time);

                            Uri uri = mActivity.getContentResolver().insert(DatabaseContentPovider.CONTENT_URI, values);
                            connectToDevice(device);
                        }
                    });
                }
            };


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

    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            Log.d(TAG, "onScanResult........................" + (System.currentTimeMillis() - mTime));
            Log.d(TAG, "onScanResult result "  + String.valueOf(result.getRssi()));
            Log.d(TAG, "Address" + device.getAddress().toString());
            Log.d(TAG, "Name" + result.getScanRecord().getDeviceName());


            String time = String.valueOf((System.currentTimeMillis() - mTime)) + "ms";
            ContentValues values = new ContentValues();
            values.put(DatabaseContentPovider.ROW_ID, ++ mDeviceCount);
            values.put(DatabaseContentPovider.DEVICE_NAME, device.getName() == null ? "LE_DEVICE" : device.getName());
            values.put(DatabaseContentPovider.DEVICE_ADDRESS, device.getAddress());
            values.put(DatabaseContentPovider.DEVICE_RSSI, result.getRssi());
            values.put(DatabaseContentPovider.DEVICE_TIME, time);

            Uri uri = mActivity.getContentResolver().insert(DatabaseContentPovider.CONTENT_URI, values);
            connectToDevice(device);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public void scanLeDevice(final boolean enable)
    {
        int count = mActivity.getContentResolver().delete(DatabaseContentPovider.CONTENT_URI, null, null);
        Log.d(TAG, "Deleted " + count);
        mDeviceCount = 0;
        mTime = System.currentTimeMillis();

        Log.d(TAG, "BTLEscanner " + BTLEscanner);
        if (enable)
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                mBTAdapter.startLeScan(mLeScanCallback);
            }
            else
            {
                BTLEscanner.startScan(
                        mScanCallback);
                Log.d(TAG, "start discovery ");
            }
        }
        else
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                mBTAdapter.stopLeScan(mLeScanCallback);
            }
            else
            {
                Log.d(TAG, "stop discovery " + mScanCallback + " ===================== " + BTLEscanner);
                BTLEscanner.stopScan(mScanCallback);

            }
        }

    }

    public void connectToDevice(BluetoothDevice device) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//        {
//            mBTAdapter.stopLeScan(mLeScanCallback);
//        }
//        else
//        {
//            BTLEscanner.stopScan(mScanCallback);
//            Log.d(TAG, "stop discovery ");
//        }
//        if (mGatt == null) {
//            mGatt = device.connectGatt(mActivity, false, mGattCallback);
//            scanLeDevice(false);// will stop after first device detection
//        }
    }
}
