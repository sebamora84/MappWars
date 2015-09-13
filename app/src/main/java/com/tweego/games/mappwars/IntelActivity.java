package com.tweego.games.mappwars;

import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class IntelActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intel);

        Button getIntelButton = (Button) findViewById(R.id.getIntelButton);
        getIntelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetIntel();
            }
        });

        Bundle bundle = getIntent().getParcelableExtra("bundle");
        MissileSource =  bundle.getParcelable("source");
        LaserMark = bundle.getParcelable("target");
        EnemyBase = bundle.getParcelable("enemy");
        Parcelable[] addresses = bundle.getParcelableArray("enemyAddresses");
        EnemyBaseAddresses = new Address[addresses.length];
        for (int i = 0; i < addresses.length; i++){
            EnemyBaseAddresses[i] =  (Address) addresses[i];
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_intel, menu);
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

    final Random random = new Random();
    private LatLng EnemyBase;
    private LatLng LaserMark;
    private LatLng MissileSource;
    Address[] EnemyBaseAddresses;
    Map<Integer,String> IntelMessages = new HashMap<Integer,String>();;

    private void GetIntel() {
        int intelOption = random.nextInt(6);

       if (IntelMessages.size() < 6)
       {
            while (IntelMessages.containsKey(intelOption) )
            {
                intelOption ++;
                if(intelOption > 5)
                    intelOption = 0;
            }
       }
        else
       {
           intelOption = IntelMessages.size() + 1;
       }

        String intelText = getString(R.string.intelEmpty);
        switch (intelOption)
        {
            case 0:
                intelText = String.format(getString(R.string.intelDistSourceEnemy),GetDistanceBetween(MissileSource, EnemyBase));
                break;
            case 1:
                intelText = String.format(getString(R.string.intelDistTargetEnemy),GetDistanceBetween(LaserMark, EnemyBase));
                break;
            case 2:
                intelText = String.format(getString(R.string.intelCountryEnemy),GetEnemyCountry());
                break;
            case 3:
                intelText = String.format(getString(R.string.intelEnemyCoordinates),EnemyBase.latitude,EnemyBase.longitude);
                break;
            case 4:
                intelText = String.format(getString(R.string.intelCardinalSourceEnemy),GetCardinalDirection(MissileSource, EnemyBase));
                break;
            case 5:
                intelText = String.format(getString(R.string.intelCardinalTargetEnemy),GetCardinalDirection(LaserMark, EnemyBase));
                break;
            default:
                break;
        }
        IntelMessages.put(intelOption, intelText);
        ShowIntel(intelText);
    }


    private void ShowIntel(String intelText)
    {
        LinearLayout layout = (LinearLayout) findViewById(R.id.intelLinear);
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setText(intelText);
        textView.setTextColor(Color.WHITE);
        params.gravity = Gravity.LEFT;
        textView.setLayoutParams(params);
        layout.addView(textView);
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

    private String GetEnemyCountry() {
        if(EnemyBaseAddresses.length == 0)
            return "an unknown country";
        Address enemyAddress = EnemyBaseAddresses[0];
        return enemyAddress.getCountryName();
    }

    private String GetCardinalDirection(LatLng latLng1, LatLng latLng2) {
      double verticalDif = latLng2.latitude - latLng1.latitude;
        double horizontalDif = latLng2.longitude - latLng1.longitude;

       String vertical = "Center";
        if ( verticalDif > 10)
           vertical = "North";
        else if(verticalDif < -10)
            vertical = "South";

        String horizontal = "Center";
        if ( horizontalDif > 10)
            horizontal = "East";
        else if(horizontalDif < -10)
            horizontal = "West";
        return vertical+horizontal;
    }
}
