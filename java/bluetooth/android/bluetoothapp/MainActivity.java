package bluetooth.android.bluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static int REQUEST_BT_DISCOVERABLE_MODE = 2;
    private Button classic_button;
    private Button le_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        String [] PermissionsLocation =
                {

                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                };
        ActivityCompat.requestPermissions(this, PermissionsLocation, 1);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                classic_button = (Button)findViewById(R.id.button);
                classic_button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(MainActivity.this, BTDevicesListActivity.class);
                        intent.putExtra("ClassName", "ClassicBluetoothScan");
                        startActivity(intent);

                    }
                });

                le_button = (Button)findViewById(R.id.button2);
                le_button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                            Intent intent = new Intent(MainActivity.this, BTDevicesListActivity.class);
                            intent.putExtra("ClassName", "BluetoothLowEnergyScan");
                            startActivity(intent);

                    }
                });

                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    classic_button.setEnabled(false);
                    le_button.setEnabled(false);
                    Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
                {
                    le_button.setEnabled(false);
                }


            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)
        {
            Toast.makeText(this, "Bluetooth is enabled!", Toast.LENGTH_LONG).show();

            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, REQUEST_BT_DISCOVERABLE_MODE);
        }
        else if (requestCode == REQUEST_BT_DISCOVERABLE_MODE)
        {
            Toast.makeText(this, "Device is discoverable to others for 5min!", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Bluetooth Failure!", Toast.LENGTH_LONG).show();
            mBluetoothAdapter.disable();
            classic_button.setEnabled(false);
            le_button.setEnabled(false);
        }

    }
}
