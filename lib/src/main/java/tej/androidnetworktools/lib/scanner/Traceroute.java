package tej.androidnetworktools.lib.scanner;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tej.androidnetworktools.lib.NetworkConnection;
import tej.androidnetworktools.lib.Route;

public class Traceroute {
    private static Traceroute instance;
    private final ConnectivityManager connectivityManager;
    private int ttl = 64;
    private boolean isRunning = false;

    private static final String UNKNOWN_HOST_PREFIX = "ping: unknown host";

    private final Pattern routePattern;
    private final Pattern ipAddressPattern;

    private final Handler handler;
    private String lastRoute;

    private final List<Route> routes = new ArrayList<>();

    private Traceroute(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        handler = new Handler(Looper.getMainLooper());
        routePattern = Pattern.compile("(From|from)(.*?):");
        ipAddressPattern = Pattern.compile("[(](.*?)[)]");
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new Traceroute(context);
        }
    }

    private Route parseRoute(String response) {
        Matcher routeMatcher = routePattern.matcher(response);

        if (routeMatcher.find()) {
            Route route = new Route();
            route.rawAddress = routeMatcher.group(2);

            if (route.rawAddress != null) {
                Matcher ipAddressMatcher = ipAddressPattern.matcher(route.rawAddress);

                if (ipAddressMatcher.find()) {
                    route.ipAddress = ipAddressMatcher.group(1);
                } else {
                    route.ipAddress = route.rawAddress;
                }
            }

            return route;
        }

        return null;
    }

    public void startTask(String target, OnTracerouteListener onTracerouteListener) {
        if (isRunning || !NetworkConnection.isInternetAvailable(connectivityManager)) {
            onTracerouteListener.onFailed();
            return;
        }

        isRunning = true;
        routes.clear();

        new Thread(() -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            TraceroutePing ping;

            String result;

            for (int i = 0; i < ttl; i++) {

                ping = new TraceroutePing(target, i + 1);
                Future<String> future = executorService.submit(ping);
                try {
                    result = future.get();

                    if (result != null) {
                        if (result.equals(UNKNOWN_HOST_PREFIX)) {
                            isRunning = false;
                            handler.post(onTracerouteListener::onFailed);
                            return;
                        }

                        Route route = parseRoute(result);

                        if (route != null) {
                            if (route.rawAddress.equals(lastRoute)) {
                                isRunning = false;
                                handler.post(() -> onTracerouteListener.onComplete(routes));
                                return;
                            }

                            routes.add(route);
                            handler.post(() -> onTracerouteListener.onRouteAdd(route));

                            lastRoute = route.rawAddress;
                        }
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            executorService.shutdown();

            isRunning = false;
            handler.post(() -> onTracerouteListener.onComplete(routes));
        }).start();
    }

    public static void setTTL(int ttl) {
        instance.ttl = ttl;
    }

    public static void start(String target, OnTracerouteListener onTracerouteListener) {
        instance.startTask(target, onTracerouteListener);
    }

    static class TraceroutePing implements Callable<String> {
        private final String target;
        private final int ttl;

        public TraceroutePing(String target, int ttl) {
            this.target = target;
            this.ttl = ttl;
        }

        @Override
        public String call() throws Exception {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("ping -c 1 -w 1 -t " + ttl + " " + target);

            process.waitFor();

            int exitCode = process.exitValue();

            InputStream inputStream;

            if (exitCode == 2) {
                inputStream = process.getErrorStream();
            } else {
                inputStream = process.getInputStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(UNKNOWN_HOST_PREFIX)) {
                    return UNKNOWN_HOST_PREFIX;
                }

                if (line.contains("From") || line.contains("from")) {
                    return line;
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            return null;
        }
    }
}
