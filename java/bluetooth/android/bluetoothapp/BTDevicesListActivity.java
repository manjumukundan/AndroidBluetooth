package bluetooth.android.bluetoothapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Created by Manju on 12/31/2015.
 */
public class BTDevicesListActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private final static String TAG = "BTDevicesListActivity";
    private SimpleCursorAdapter mCursorAdapter;
    String mClassName = null;

    private static final String CLASSIC_BT_SCAN = "ClassicBluetoothScan";
    private static final String LE_BT_SCAN = "BluetoothLowEnergyScan";
    private BluetoothLowEnergyScan  mLowEnergyBT;
    private ClassicBluetoothScan mClassicBt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluetoothlist);

        Intent intent = getIntent();
        mClassName = intent.getStringExtra("ClassName");
        Log.d(TAG, "Classname : "  + mClassName);

        getLoaderManager().initLoader(1, null, this);

        ListView devicesListView = (ListView)findViewById(R.id.listView);

        mCursorAdapter = new SimpleCursorAdapter(this, R.layout.listview_item_layout, null,
                new String[]
                        {
                                DatabaseContentPovider.ROW_ID, DatabaseContentPovider.DEVICE_NAME, DatabaseContentPovider.DEVICE_TIME,
                                DatabaseContentPovider.DEVICE_ADDRESS, DatabaseContentPovider.DEVICE_RSSI
                        },
                new int[]
                        {
                                R.id.num, R.id.name, R.id.type, R.id.address, R.id.rssi
                        }, 0);

        devicesListView.setAdapter(mCursorAdapter);

        if (mClassName.equals(CLASSIC_BT_SCAN))
        {
            mClassicBt = new ClassicBluetoothScan(this);
            mClassicBt.registerReceiver();
            mClassicBt.scanDevices(true);
        }
        else if (mClassName.equals(LE_BT_SCAN))
        {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, "BlueTooth Low Energy Not Supported",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
            mLowEnergyBT = new BluetoothLowEnergyScan(this);
            mLowEnergyBT.scanLeDevice(true);
        }

        Button startScan = (Button)findViewById(R.id.startscan_btn);
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (mClassName.equals(CLASSIC_BT_SCAN))
                {
                    mClassicBt.scanDevices(true);
                }
                else if (mClassName.equals(LE_BT_SCAN))
                {
                    mLowEnergyBT.scanLeDevice(true);
                }


            }
        });

        Button stopScan = (Button)findViewById(R.id.stopscan_btn);
        stopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mClassName.equals(CLASSIC_BT_SCAN))
                {
                    mClassicBt.scanDevices(false);
                }
                else if (mClassName.equals(LE_BT_SCAN))
                {
                    mLowEnergyBT.scanLeDevice(false);
                }
            }
        });
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        return new CursorLoader(this, DatabaseContentPovider.CONTENT_URI,
                new String[]
                {
                        DatabaseContentPovider.ROW_ID, DatabaseContentPovider.DEVICE_NAME, DatabaseContentPovider.DEVICE_TIME,
                        DatabaseContentPovider.DEVICE_ADDRESS,
                        DatabaseContentPovider.DEVICE_RSSI
                }, null, null, "desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        Log.d(TAG, "onLoadFinished " + data.getCount());
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mCursorAdapter.swapCursor(null);
    }



    @Override
    protected void onDestroy() {
        if (mClassName.equals(CLASSIC_BT_SCAN))
        {
            mClassicBt.unRegisterReceiver();
            mClassicBt.scanDevices(false);
        }
        else if (mClassName.equals(LE_BT_SCAN))
        {
            mLowEnergyBT.scanLeDevice(false);
        }
        super.onDestroy();
    }
}
