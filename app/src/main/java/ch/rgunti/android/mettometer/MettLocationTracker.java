package ch.rgunti.android.mettometer;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class MettLocationTracker extends Service implements LocationListener {
    public interface Listener {
        void updated(MettLocationTracker source, Location location);
    }

    private final Context context;

    private List<Listener> listeners = new ArrayList<>();

    boolean checkGps = false;
    boolean checkNetwork = false;
    boolean canGetLocation = false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 500;

    private static final long BREAD_SIZE = 13;

    protected LocationManager locationManager;

    Location location;

    public MettLocationTracker(Context context) {
        this.context = context;
        initializeLocation();
    }

    private void initializeLocation() {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            if (locationManager == null) {
                return;
            }

            checkGps = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            checkNetwork = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGps && !checkNetwork) {
                Toast.makeText(context,
                        "No service available",
                        Toast.LENGTH_SHORT)
                    .show();
            } else {
                this.canGetLocation = true;
                if (checkGps) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    }

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        for (Listener listener : listeners) {
            listener.updated(this, location);
        }
    }

    @Override
    @Deprecated
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public void stopListener() {
        if (locationManager != null
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    /* *** Properties *** */
    public Location getLastKnownLocation() {
        return location;
    }

    public float getCurrentSpeed() {
        if (location != null) {
            return location.getSpeed();
        }
        return 0;
    }

    public float getCurrentSpeedKmh() {
        return getCurrentSpeed() * 3.6f;
    }

    public float getCurrentSpeedMettbps() {
        return getCurrentSpeed() * 100f /* cm/s */ / BREAD_SIZE /* Mettb/s */;
    }
}
