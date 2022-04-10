# Android Network Tools

## Update settings.gradle(Project Settings)

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url 'https://jitpack.io' }
    }
}
```

## Sample code

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