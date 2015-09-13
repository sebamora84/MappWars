package com.tweego.games.mappwars;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button swithcMapButton = (Button) findViewById(R.id.switchMapButton);
        swithcMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchMap();
            }
        });

        Button intelButton = (Button) findViewById(R.id.intelButton);
        intelButton.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GainIntel();
            }
        });

        Button launchButton = (Button) findViewById(R.id.launchButton);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MissileLaunch();
            }
        });
        SetEnemyBase();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        setMap(map);
    }

    final Random random = new Random();
    private boolean IsSourceChosen = false;

    private GoogleMap Map;
    private Circle LaserMarkBound;
    private Circle LaserMarkCenter;
    private Circle MissileSourceBound;
    private Circle MissileSourceCenter;
    private LatLng EnemyBase;
    Address[] EnemyBaseAddresses;


    public void setMap(GoogleMap map) {
        Map = map;
        Map.setMyLocationEnabled(true);
        LaserMarkBound = Map.addCircle( new CircleOptions()
                .strokeWidth(5)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT)
                .radius(0)
                .center(new LatLng(0,0)));
        LaserMarkCenter = Map.addCircle( new CircleOptions()
                .strokeWidth(5)
                .strokeColor(Color.RED)
                .fillColor(Color.TRANSPARENT)
                .radius(0)
                .center(new LatLng(0,0)));
        MissileSourceBound = Map.addCircle( new CircleOptions()
                .strokeWidth(5)
                .strokeColor(Color.GREEN)
                .fillColor(Color.TRANSPARENT)
                .radius(0)
                .center(new LatLng(0,0)));
        MissileSourceCenter = Map.addCircle( new CircleOptions()
                .strokeWidth(5)
                .strokeColor(Color.GREEN)
                .fillColor(Color.TRANSPARENT)
                .radius(0)
                .center(new LatLng(0,0)));

        Map.setOnMapClickListener(
                new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        MoveMarks(latLng);
                    }
                });
    }

    private void MoveMarks(LatLng latLng) {

        if(IsSourceChosen)
            MoveLaserMark(latLng);
        else
            MoveMissileSource(latLng);
    }

    private void MoveLaserMark(LatLng latLng) {
        LaserMarkBound.setCenter(latLng);
        LaserMarkBound.setRadius(50000);
        LaserMarkCenter.setCenter(latLng);
        LaserMarkCenter.setRadius(1000);
    }

    private void MoveMissileSource(LatLng latLng) {
        MissileSourceBound.setCenter(latLng);
        MissileSourceBound.setRadius(50000);
        MissileSourceCenter.setCenter(latLng);
        MissileSourceCenter.setRadius(1000);
        IsSourceChosen = true;
    }

    private void SwitchMap() {
        if (Map.getMapType() == GoogleMap.MAP_TYPE_HYBRID)
            Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else
            Map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    private void GainIntel() {

        Bundle coords = new Bundle();
        coords.putParcelable("source", MissileSourceCenter.getCenter());
        coords.putParcelable("target", LaserMarkCenter.getCenter());
        coords.putParcelable("enemy", EnemyBase);
        coords.putParcelableArray("enemyAddresses", EnemyBaseAddresses);
        Intent intent = new Intent(this, IntelActivity.class);
        intent.putExtra("bundle", coords);
        startActivity(intent);
    }

    private void MissileLaunch() {

        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(MissileSourceCenter.getCenter(), 9), 2, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                MissileUp();
            }

            @Override
            public void onCancel() {
                IsSourceChosen = false;
            }
        });

    }

    private void MissileUp() {

        LatLng sourceCenter = MissileSourceCenter.getCenter();
        LatLng laserCenter = LaserMarkCenter.getCenter();

        double latMidDistance =(laserCenter.latitude - sourceCenter.latitude) / 2;
        double lngMidDistance =(laserCenter.longitude - sourceCenter.longitude) / 2;

        LatLng topPosition = new LatLng(
                sourceCenter.latitude + latMidDistance,
                sourceCenter.longitude + lngMidDistance
        );

        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(topPosition, 4), 3000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                MissileDown();
            }

            @Override
            public void onCancel() {
                IsSourceChosen = false;
            }
        });

    }

    private void MissileDown() {
        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(LaserMarkCenter.getCenter(), 10), 3000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                MissileExplosion();
            }

            @Override
            public void onCancel() {
                IsSourceChosen = false;
            }
        });
    }

    private void MissileExplosion() {
        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(LaserMarkCenter.getCenter(), 9), 4000, null);
        IsSourceChosen = false;


        double distance = GetDistanceBetween(LaserMarkCenter.getCenter(), EnemyBase);

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        if (distance > 25) {
            alertDialog.setTitle(R.string.launchMissTitle);
            String message = String.format(getString(R.string.launchMissMessage), distance);
            alertDialog.setMessage(message);
            SetEnemyBase();
        }
        else
        {
            alertDialog.setTitle(R.string.launchHitTitle);
            String message = String.format(getString(R.string.launchHitMessage), distance);
            alertDialog.setMessage(message);
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    private void SetEnemyBase() {
        double enemyBaseLat = 0;
        double enemyBaseLng = 0;
        for (int i = 0; i < 5 ; i++)
        {
            enemyBaseLat = random.nextDouble() * 180 - 90;
            enemyBaseLng = random.nextDouble() * 180 - 90 ;

            try
            {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(enemyBaseLat, enemyBaseLng, 1);
                if (addresses.size() > 0)
                {
                    EnemyBaseAddresses = addresses.toArray(new Address[addresses.size()]);
                    break;
                }
            }
            catch (IOException e)
            {
                break;
            }
        }
        EnemyBase= new LatLng(enemyBaseLat,enemyBaseLng);
    }

    private float GetDistanceBetween(LatLng latLng1, LatLng latLng2) {

        Location loc1 = new Location(LocationManager.GPS_PROVIDER);
        Location loc2 = new Location(LocationManager.GPS_PROVIDER);

        loc1.setLatitude(latLng1.latitude);
        loc1.setLongitude(latLng1.longitude);

        loc2.setLatitude(latLng2.latitude);
        loc2.setLongitude(latLng2.longitude);

        return loc1.distanceTo(loc2) / 1000;
    }
}
