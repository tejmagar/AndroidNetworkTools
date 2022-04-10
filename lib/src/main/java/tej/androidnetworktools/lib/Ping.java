package tej.androidnetworktools.lib;


import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

public class Ping implements Runnable {
    private final String ipAddress;
    private final int timeout;

    private HashMap<String, Device> ipAndDeviceHashMap;

    public Ping(String ipAddress, int timeout) {
        this.ipAddress = ipAddress;
        this.timeout = timeout;
    }

    public void setIpAndDeviceHashMap(HashMap<String, Device> ipAndDeviceHashMap) {
        this.ipAndDeviceHashMap = ipAndDeviceHashMap;
    }

    @Override
    public void run() {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);

            if (inetAddress.isReachable(timeout) && ipAndDeviceHashMap != null) {

                Device device = new Device();
                device.ipAddress = ipAddress;
                device.hostname = inetAddress.getHostName();

                ipAndDeviceHashMap.put(ipAddress, device);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
