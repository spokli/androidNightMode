package de.spokli.nightmode;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    Button _btn_on = null;
    Button _btn_off = null;
    boolean nightMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _btn_on = (Button) findViewById(R.id.btn_on);
        _btn_off = (Button) findViewById(R.id.btn_off);

        _btn_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableNightMode();
            }
        });

        _btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableNightMode();
            }
        });


        mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void enableNightMode(){
        setWifiEnabled(false);
        setRingModeSilent(false);
        nightMode = true;
        mGoogleApiClient.connect();
        mGoogleApiClient.disconnect();
    }

    private void disableNightMode(){
        setWifiEnabled(true);
        setRingModeSilent(true);
        nightMode = false;
        mGoogleApiClient.connect();
        mGoogleApiClient.disconnect();
    }

    private void setWifiEnabled(boolean status) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(status);

        String toastText = "";
        if(status)
            toastText = "WLAN aktiviert";
        else
            toastText = "WLAN deaktiviert";

        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

    }

    private void setRingModeSilent(boolean silent){
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        String toastText = "";
        if(silent) {
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            toastText = "Lautlos aktiviert";
        }
        else {
            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            toastText = "Vibration aktiviert";
        }

        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Toast.makeText(MainActivity.this, "Verbunden", Toast.LENGTH_SHORT).show();
        int priority;

        if(nightMode)
            priority = LocationRequest.PRIORITY_NO_POWER;
        else
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

        LocationRequest mLocationRequest = LocationRequest.create().setPriority(priority);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        Toast.makeText(MainActivity.this, "GPS bereits korrekt", Toast.LENGTH_SHORT).show();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                            Toast.makeText(MainActivity.this, "GPS geändert", Toast.LENGTH_SHORT).show();
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.

                        Toast.makeText(MainActivity.this, "Fehler bei Einstellungsabfrage", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

        Toast.makeText(MainActivity.this, "Verbindung unterbrochen", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(MainActivity.this, "Verbindung fehlgeschlagen", Toast.LENGTH_SHORT).show();
    }
}
