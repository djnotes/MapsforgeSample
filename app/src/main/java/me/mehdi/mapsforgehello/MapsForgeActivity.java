package me.mehdi.mapsforgehello;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.io.IOException;

public class MapsForgeActivity extends AppCompatActivity implements LocationListener {
    // name of the map file in the external storage
    private static final String MAP_FILE = "iran.map";
    private static final String TAG = "MapsForgeActivity";
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_CODE = 100;
    private View mLayout;
    private MapView mMapView;
    private final Activity mActivity = this;
    private final Context mContext = this;
    private LocationManager mLocationManager;
    private MyLocationOverlay mMyLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.mMapView = new MapView(this);
        setContentView(this.mMapView);

        //Get location service

        mLayout = findViewById(android.R.id.content);
        MapView view;

        this.mMapView.setClickable(true);
        this.mMapView.getMapScaleBar().setVisible(true);
        this.mMapView.setBuiltInZoomControls(true);
        this.mMapView.setZoomLevelMin((byte) 10);
        this.mMapView.setZoomLevelMax((byte) 20);

        final int granted = PackageManager.PERMISSION_GRANTED;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != granted ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != granted ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != granted ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != granted) {
            requestPermissions();
        } else {
            initOfflineMap();
            createOverlays();
        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


    }

    private void initOfflineMap() {
        // tile renderer layer using internal render theme
        File file = new File(Environment.getExternalStorageDirectory(), MAP_FILE);
        Log.d(TAG, "onCreate: file exists: " + file.exists());
        MapDataStore mapDataStore = new MapFile(file);
        // create a tile cache of suitable size
        TileCache tileCache = AndroidUtil.createTileCache(this, "mapcache",
                mMapView.getModel().displayModel.getTileSize(), 1f,
                this.mMapView.getModel().frameBufferModel.getOverdrawFactor());
        TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                this.mMapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);

        // only once a layer is associated with a mMapView the rendering starts
        this.mMapView.getLayerManager().getLayers().add(tileRendererLayer);

        this.mMapView.setCenter(new LatLong(35.6892, 51.3890));
        this.mMapView.setZoomLevel((byte) 12);
        mMapView.invalidate();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            initOfflineMap();
        } else if (requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createOverlays();
        } else {
            Toast.makeText(this, R.string.storage_access_required_offline, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onDestroy() {
        this.mMapView.destroyAll();
        AndroidGraphicFactory.clearResourceMemoryCache();
        super.onDestroy();
    }

    private void requestPermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) )
        {
            Snackbar.make(mLayout, R.string.rationale_permissions, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(mActivity, PERMISSIONS, REQUEST_CODE);
                        }
                    })
                    .show();
        } else{
            ActivityCompat.requestPermissions(mActivity, PERMISSIONS, REQUEST_CODE);
        }

    }

    private static Paint getPaint(int color, int strokeWidth, Style style) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    private void createOverlays() {
        Drawable drawable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? getDrawable(R.drawable.ic_maps_indicator_current_location) : getResources().getDrawable(R.drawable.ic_maps_indicator_current_location);
        Marker marker = new Marker(null, AndroidGraphicFactory.convertToBitmap(drawable), 0, 0);

        //Circle to show the location accuracy (optional)
        Circle circle = new Circle(null, 0,
                getPaint(AndroidGraphicFactory.INSTANCE.createColor(48, 0, 0, 255), 0, Style.FILL),
                getPaint(AndroidGraphicFactory.INSTANCE.createColor(160, 0, 0, 255), 2, Style.STROKE));

        //Create an overlay
        mMyLocationOverlay = new MyLocationOverlay(marker, circle);
        mMapView.getLayerManager().getLayers().add(mMyLocationOverlay);
        Log.d(TAG, "createOverlays: overlay added");

    }

    @Override
    public void onLocationChanged(Location location) {
        mMyLocationOverlay.setPosition(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        mMapView.setCenter(new LatLong(location.getLatitude(), location.getLongitude()));
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource("asset://notify.wav");
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.start();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @SuppressLint("MissingPermission")
    private void enableAvailableProviders() {
        if (MyUtil.hasLocationPermission(mContext)) {
            mLocationManager.removeUpdates(this);

            for (String provider : mLocationManager.getProviders(true)) {
                if (LocationManager.GPS_PROVIDER.equals(provider)
                        || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                    mLocationManager.requestLocationUpdates(provider, 0, 0, this);
                }
            }
        } else {
            Snackbar.make(mLayout, R.string.location_permission_denied, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(mActivity, PERMISSIONS, REQUEST_CODE);
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableAvailableProviders();
    }
}