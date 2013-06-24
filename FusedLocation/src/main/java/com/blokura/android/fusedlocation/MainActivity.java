package com.blokura.android.fusedlocation;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Show a view that contains the 3 location examples: Last Location, Location Request Listener and a background location pending intent.
 *
 * @author Estornino. All credit goes to Ketan Parmar
 * @see // http://github.com/kpbird/fused-location-provider-example
 */
public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    /**
     * String tag for logging purposes *
     */
    private String TAG = this.getClass().getSimpleName();
    /**
     * TextView contains the connection status *
     */
    private TextView mTvConnectionStatus;
    /**
     * TextView contains last known location *
     */
    private TextView mTvLastKnownLoc;
    /**
     * EditText contains the customizable location interval *
     */
    private EditText mEtLocationInterval;
    /**
     * TextView location request *
     */
    private TextView mTvLocationRequest;
    /**
     * A {@link com.google.android.gms.location.LocationClient} *
     */
    private LocationClient mLocationclient;
    /**
     * The {@link com.google.android.gms.location.LocationRequest} *
     */
    private LocationRequest mLocationrequest;
    /**
     * {@link android.app.IntentService} Runs in background to get the location updates every 100 ms *
     */
    private Intent mIntentService;
    private PendingIntent mPendingIntent;

    /** Button location request listener **/
    private Button btnListener;

    /** Button pending intent **/
    private Button btnPending;

    static final String STATE_LISTENER = "stateListener";
    static final String STATE_PENDING_INTENT = "statePendingIntent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvConnectionStatus = (TextView) findViewById(R.id.tvConnectionStatus);
        mTvLastKnownLoc = (TextView) findViewById(R.id.tvLastKnownLoc);
        mEtLocationInterval = (EditText) findViewById(R.id.etLocationInterval);
        mTvLocationRequest = (TextView) findViewById(R.id.tvLocationRequest);
        btnListener = (Button) findViewById(R.id.btnStartRequest);
        btnPending = (Button) findViewById(R.id.btnRequestLocationIntent);

        if (savedInstanceState != null) {
            btnListener.setText(savedInstanceState.getString(STATE_LISTENER));
            btnPending.setText(savedInstanceState.getString(STATE_PENDING_INTENT));
        }
        mIntentService = new Intent(this, LocationService.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, 0);

    }

    @Override
    protected void onResume() {
        super.onResume();

        /** Check if Google play services is available
         * @see http://developer.android.com/google/play-services/setup.html
         */
        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resp == ConnectionResult.SUCCESS) {
            mLocationclient = new LocationClient(this, this, this);
            mLocationclient.connect();
        } else {
            GooglePlayServicesUtil.getErrorDialog(resp, this, 2);
        }


    }

    /**
     * Listen for button clicks and launch the appropriate action.
     *
     * @param v View that is clicked
     */
    public void buttonClicked(View v) {
        if (v.getId() == R.id.btnLastLoc) {
            if (mLocationclient != null && mLocationclient.isConnected()) {
                Location loc = mLocationclient.getLastLocation();
                Log.i(TAG, "Last Known Location :" + loc.getLatitude() + "," + loc.getLongitude());
                mTvLastKnownLoc.setText(loc.getLatitude() + "," + loc.getLongitude());
            }
        }
        if (v.getId() == R.id.btnStartRequest) {
            if (mLocationclient != null && mLocationclient.isConnected()) {

                if (((Button) v).getText().equals("Start")) {
                    mLocationrequest = LocationRequest.create();
                    mLocationrequest.setInterval(Long.parseLong(mEtLocationInterval.getText().toString()));
                    mLocationclient.requestLocationUpdates(mLocationrequest, this);
                    ((Button) v).setText("Stop");
                } else {
                    mLocationclient.removeLocationUpdates(this);
                    ((Button) v).setText("Start");
                }

            }
        }
        if (v.getId() == R.id.btnRequestLocationIntent) {
            if (((Button) v).getText().equals("Start")) {

                mLocationrequest = LocationRequest.create();
                mLocationrequest.setInterval(100);
                mLocationclient.requestLocationUpdates(mLocationrequest, mPendingIntent);

                ((Button) v).setText("Stop");
            } else {
                mLocationclient.removeLocationUpdates(mPendingIntent);
                ((Button) v).setText("Start");
            }


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationclient != null)
            mLocationclient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected");
        mTvConnectionStatus.setText("Connection Status : Connected");

    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "onDisconnected");
        mTvConnectionStatus.setText("Connection Status : Disconnected");

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "onConnectionFailed");
        mTvConnectionStatus.setText("Connection Status : Fail");

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i(TAG, "Location Request :" + location.getLatitude() + "," + location.getLongitude());
            mTvLocationRequest.setText(location.getLatitude() + "," + location.getLongitude());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // Save state
        if (btnPending != null) {
            outState.putString(STATE_PENDING_INTENT, btnListener.getText().toString());
        }

        if (btnListener != null) {
            outState.putString(STATE_LISTENER, btnListener.getText().toString());
        }

        super.onSaveInstanceState(outState);

    }
}
