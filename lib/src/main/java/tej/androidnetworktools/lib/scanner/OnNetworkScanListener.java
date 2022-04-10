package tej.androidnetworktools.lib.scanner;

import java.util.List;

import tej.androidnetworktools.lib.Device;

public interface OnNetworkScanListener {
    void onComplete(List<Device> devices);
    void onFailed();
}
