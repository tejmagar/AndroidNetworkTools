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

## Update build.gradle(Module: project.app)
```
implementation 'com.github.tejmagar:AndroidNetworkTools:1.0.0alpha'
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

## Optional

<p>After Network scanner is initialized, you can disable MAC Address and Vendor names processing by.
Note: MAC Address is required to lookup vendor name.</p>

```
NetworkScanner.setShowMacAddress(false);
NetworkScanner.setShowVendorInfo(false);
```

## 

## Use Target SDK 29 or below for MAC Address
<p>See: https://developer.android.com/about/versions/11/privacy/mac-address