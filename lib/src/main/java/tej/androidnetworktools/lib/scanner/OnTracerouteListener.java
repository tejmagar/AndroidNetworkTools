package tej.androidnetworktools.lib.scanner;

import java.util.List;

public interface OnTracerouteListener {
    void onRouteAdd(String route);
    void onComplete(List<String> routes);
    void onFailed();
}
