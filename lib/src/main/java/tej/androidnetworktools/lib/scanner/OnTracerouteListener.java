package tej.androidnetworktools.lib.scanner;

import java.util.List;

import tej.androidnetworktools.lib.Route;

public interface OnTracerouteListener {
    void onRouteAdd(Route route);
    void onComplete(List<Route> routes);
    void onFailed();
}
