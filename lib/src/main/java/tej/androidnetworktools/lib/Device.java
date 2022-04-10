package tej.androidnetworktools.lib;

import tej.androidnetworktools.lib.parsers.DeviceInfo;

public class Device {
    public String hostname;
    public String ipAddress;
    public String macAddress = DeviceInfo.UNKNOWN;
    public String vendorName = DeviceInfo.UNKNOWN;
}
