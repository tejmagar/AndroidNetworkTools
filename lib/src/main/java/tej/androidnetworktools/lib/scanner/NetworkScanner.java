package tej.androidnetworktools.lib.scanner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import tej.androidnetworktools.lib.Device;
import tej.androidnetworktools.lib.NetworkConnection;
import tej.androidnetworktools.lib.parsers.DeviceInfo;
import tej.androidnetworktools.lib.Ping;
import tej.androidnetworktools.lib.parsers.Utils;

public class NetworkScanner {
    private static NetworkScanner instance;
    private String ipPrefix;
    private String currentIPAddress;
    private boolean showVendorInfo = true;
    private boolean showMacAddress = true;

    private boolean taskRunning = false;
    private int scanTimeout = 500;
    private int reachableRescanCount = 3;

    private final Handler handler;

    private final HashMap<String, Device> ipAndDeviceHashMap = new HashMap<>();
    private JSONArray vendorsJson;

    private final ConnectivityManager connectivityManager;

    private NetworkScanner(Context context) {
        handler = new Handler(Looper.getMainLooper());

        initIPConfigs(context);
        initVendorsJson(context);

        connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void initIPConfigs(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        int ipAddressLong = wifiManager.getDhcpInfo().ipAddress;
        String ipAddress = Utils.parseIpAddress(ipAddressLong);

        int currentIPAddressLong = wifiManager.getConnectionInfo().getIpAddress();
        currentIPAddress = Utils.parseIpAddress(currentIPAddressLong);

        if (ipAddress != null) {
            int lastDotIndex = ipAddress.lastIndexOf(".");
            ipPrefix = ipAddress.substring(0, lastDotIndex + 1);
        }
    }

    private void initVendorsJson(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("vendors.json");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String json = stringBuilder.toString();
            vendorsJson = new JSONArray(json);

            bufferedReader.close();
            inputStream.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isTaskRunning() {
        return taskRunning;
    }

    public void setScanTimeout(int scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

    public void scanNetwork(OnNetworkScanListener onNetworkScanListener) {
        if (taskRunning || !NetworkConnection.isWifiConnected(connectivityManager)) {
            onNetworkScanListener.onFailed();
            return;
        }

        taskRunning = true;
        ipAndDeviceHashMap.clear();

        new Thread(() -> {
            ExecutorService executorService = Executors.newFixedThreadPool(
                    255 * reachableRescanCount);

            // Example: 192.168.1.

            String ipAddress;
            Ping ping;

            for (int i = 0; i < 255; i++) {
                ipAddress = ipPrefix + (i + 1);

                ping = new Ping(ipAddress, scanTimeout);
                ping.setIpAndDeviceHashMap(ipAndDeviceHashMap);

                // Improve accuracy
                for (int j = 0; j < reachableRescanCount; j++) {
                    executorService.execute(ping);
                }
            }

            executorService.shutdown();

            try {
                boolean completed = executorService.awaitTermination(5, TimeUnit.MINUTES);

                if (completed) {
                    DeviceInfo.parse(ipAndDeviceHashMap);
                    handler.post(() -> onNetworkScanListener.onComplete(
                            new ArrayList<>(ipAndDeviceHashMap.values())));

                    taskRunning = false;
                    return;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            taskRunning = false;
            handler.post(onNetworkScanListener::onFailed);
        }).start();
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new NetworkScanner(context);
        }
    }

    public static NetworkScanner getInstance() {
        return instance;
    }

    public static void scan(OnNetworkScanListener onNetworkScanListener) {
        instance.scanNetwork(onNetworkScanListener);
    }

    public static String getCurrentIPAddress() {
        return instance.currentIPAddress;
    }

    public static boolean isShowVendorInfo() {
        return instance.showVendorInfo;
    }

    public static void setShowVendorInfo(boolean enable) {
        instance.showVendorInfo = enable;
    }

    public static boolean isShowMacAddress() {
        return instance.showMacAddress;
    }

    public static void setShowMacAddress(boolean enable) {
        instance.showMacAddress = enable;
    }

    public static void setTimeout(int timeout) {
        instance.setScanTimeout(timeout);
    }

    public static boolean isRunning() {
        return instance.isTaskRunning();
    }

    public static int getReachableRescanCount() {
        return instance.reachableRescanCount;
    }

    public static void setReachableRescanCount(int reachableRescanCount) {
        instance.reachableRescanCount = reachableRescanCount;
    }

    public static JSONArray getVendorsJsonArray() {
        return instance.vendorsJson;
    }

}
