package tej.androidnetworktools.lib;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnection {

    public static boolean isWifiConnected(ConnectivityManager connectivityManager) {
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiInfo.isConnected();
    }

    public static ConnectivityManager connectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isWifiConnected(Context context) {
        return isWifiConnected(connectivityManager(context));
    }

    public static boolean isInternetAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isInternetAvailable(Context context) {
        return isInternetAvailable(connectivityManager(context));
    }
}
