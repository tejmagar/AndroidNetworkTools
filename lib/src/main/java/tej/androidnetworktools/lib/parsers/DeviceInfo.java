package tej.androidnetworktools.lib.parsers;


import android.content.Context;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import tej.androidnetworktools.lib.Device;
import tej.androidnetworktools.lib.scanner.NetworkScanner;

public class DeviceInfo {
    public static String UNKNOWN = "-";

    public static void parse(HashMap<String, Device> ipAndDeviceHashMap) {
        if (NetworkScanner.isShowMacAddress()) {
            setMacAddress(ipAndDeviceHashMap);
        }

        if (NetworkScanner.isShowVendorInfo()) {
            Device device;
            String vendorName;

            for (String key : ipAndDeviceHashMap.keySet()) {
                device = ipAndDeviceHashMap.get(key);

                if (device != null) {
                    vendorName = getVendorName(device.macAddress);
                    device.vendorName = vendorName;
                }
            }
        }

        Device currentDevice = ipAndDeviceHashMap.get(NetworkScanner.getCurrentIPAddress());

        if (currentDevice != null) {
            currentDevice.hostname = Build.MODEL;
            currentDevice.vendorName = Build.MANUFACTURER;
        }
    }

    public static void setMacAddress(HashMap<String, Device> ipAndDeviceHashMap) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("ip n");
            process.waitFor();

            int code = process.exitValue();
            if (code != 0) {
                return;
            }

            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            String[] cols;
            String ipAddress, macAddress;

            Device device;

            while ((line = bufferedReader.readLine()) != null) {
                cols = line.split(" ");

                if (cols.length > 4) {
                    ipAddress = cols[0];
                    macAddress = cols[4];

                    // Insert mac address
                    device = ipAndDeviceHashMap.get(ipAddress);
                    if (device != null) {
                        device.macAddress = macAddress;
                    }
                }
            }

            // Set mac address for current device
            String currentIPAddress = NetworkScanner.getCurrentIPAddress();
            device = ipAndDeviceHashMap.get(currentIPAddress);

            if (device != null) {
                device.macAddress = getCurrentDeviceMacAddress(currentIPAddress);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentDeviceMacAddress(String ipAddress) {
        try {
            InetAddress localIP = InetAddress.getByName(ipAddress);
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localIP);

            if (networkInterface == null) {
                return UNKNOWN;
            }

            byte[] hardwareAddress = networkInterface.getHardwareAddress();

            if (hardwareAddress == null) {
                return UNKNOWN;
            }

            StringBuilder stringBuilder = new StringBuilder(18);
            for (byte b : hardwareAddress) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(":");
                }

                stringBuilder.append(String.format("%02x", b));
            }

            return stringBuilder.toString();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        return UNKNOWN;
    }

    private static JSONObject vendorLookup(String macAddress) {
        try {
            JSONArray jsonArray = NetworkScanner.getVendorsJsonArray();

            String macAddressPrefix;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                macAddressPrefix = jsonObject.getString("m");

                if (macAddress.toLowerCase().startsWith(macAddressPrefix.toLowerCase())) {
                    return jsonObject;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getVendorName(String macAddress) {
        if (macAddress == null) {
            return UNKNOWN;
        }

        JSONObject jsonObject = vendorLookup(macAddress);
        if (jsonObject != null) {
            try {
                return jsonObject.getString("n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return UNKNOWN;
    }

    public static JSONObject getVendorInfo(Context context, String macAddress) {
        if (NetworkScanner.getInstance() == null) {
            NetworkScanner.init(context);
        }

        return vendorLookup(macAddress);
    }
}
