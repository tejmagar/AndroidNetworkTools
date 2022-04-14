package tej.androidnetworktools;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tej.androidnetworktools.lib.Device;
import tej.androidnetworktools.lib.Route;
import tej.androidnetworktools.lib.parsers.DeviceInfo;
import tej.androidnetworktools.lib.scanner.NetworkScanner;
import tej.androidnetworktools.lib.scanner.OnNetworkScanListener;
import tej.androidnetworktools.lib.scanner.OnTracerouteListener;
import tej.androidnetworktools.lib.scanner.Traceroute;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> connectedDevices = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView scannedDevices = findViewById(R.id.scanned_devices);
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, connectedDevices);
        scannedDevices.setAdapter(arrayAdapter);

        // Pass context to initialize network scanner
        NetworkScanner.init(this);
        scan();

        Traceroute.init(this);
        Traceroute.start("google.com", new OnTracerouteListener() {
            @Override
            public void onRouteAdd(Route route) {
                Log.d(TAG, "traceroute: IP Address =>" + route.ipAddress + "=>"
                        + "RAW: " + route.rawAddress);
            }

            @Override
            public void onComplete(List<Route> routes) {
                Log.d(TAG, "traceroute: " + "completed total: " + routes.size());
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "traceroute failed");
            }
        });

        JSONObject vendorInfo = DeviceInfo.getVendorInfo(this, "24:11:45:c2:ba:3f");
        Log.e("info", vendorInfo.toString());
    }

    private void scan() {
        if (NetworkScanner.isRunning()) {
            Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();

        NetworkScanner.scan(new OnNetworkScanListener() {
            @Override
            public void onComplete(List<Device> devices) {
                connectedDevices.clear();

                for (Device device : devices) {
                    connectedDevices.add(
                            "Device: " + device.hostname + "\n"
                                    + "IP Address: " + device.ipAddress + "\n"
                                    + "Mac Address: " + device.macAddress + "\n"
                                    + "Vendor Name: " + device.vendorName);

                }

                arrayAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, devices.size() + " devices detected",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "Failed to scan",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            scan();
        }

        return super.onOptionsItemSelected(item);
    }
}