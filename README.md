# Android Network Tools

```
NetworkScanner.init(this);

NetworkScanner.scan(new OnNetworkScanListener() {
    @Override
    public void onComplete(List<Device> devices) {
        for (Device device : devices) {
            Log.d("device", device.hostname + "\n" + device.vendorName + "\n" + device.macAddress);
        }
    }

    @Override
    public void onFailed() {

    }
);
```