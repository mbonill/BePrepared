package mycompany.beprepared;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.location.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.GregorianCalendar;

/**
 * Created by Matthew Bonilla on 3/23/2015.
 * This Activity defines GPS mode.
 */
public class GPSModeActivity extends Activity {

    private AlarmManager alarm;
    private PendingIntent pIntent;
    private static final double meter = 0.000009029926;//about one meter in gps coordinate
    private double[] bestLoc = {0.0, 0.0};
    private float bestAcc;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;
    private LocationManager locationManager;
    private Button start;
    private LocationListener locationListener, locationListener2;
    private TextView curLat, curLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_mode);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        data = this.getSharedPreferences("GPS", Context.MODE_PRIVATE);
        editor = data.edit();
        editor.commit();
        curLat = (TextView) findViewById(R.id.currentLat);
        curLon = (TextView) findViewById(R.id.currentLong);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(getBaseContext(), Alarm.class);
        pIntent = PendingIntent.getBroadcast(getBaseContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        initializeButtons();
        initializeLocationListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsOn) {
            Toast.makeText(getApplicationContext(),"Please enable GPS for this mode" , Toast.LENGTH_SHORT).show();
        }
        if (isScanning()) {
            start.setBackgroundColor(Color.RED);
            start.setText("Stop");
        } else {
            start.setBackgroundColor(Color.GREEN);
            start.setText("Start");
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    /**
     * Creates a frequently updated LocationListener. It is used to help scan for new locations for
     * the four direction buttons. This listener is removed upon onStop() and reactivated upon onStart()
     */
    private void initializeLocationListener() {
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
                curLat.setText("Latitude: " + Double.toString(latitude));
                curLon.setText("Longitude: " + Double.toString(longitude));
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
    }

    /**
     * This is a second LocationListener used separately from the one above. This one is meant to
     * persist after onStop();
     */
    private void initializeScanner(){
        locationListener2 = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (isScanning()) {
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    double latitude = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();
                    float accuracy = lastKnownLocation.getAccuracy();
                    accuracy*=2;
                    checkBoundary(latitude, longitude, accuracy);
                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {
                if(isScanning())
                Toast.makeText(getApplicationContext(),"GPS disabled. " +
                        "\nBe Prepared GPS Mode will no longer work until GPS is enabled" , Toast.LENGTH_LONG).show();
            }
        };
    }

    /**
     * Check if the device is inside the bounded area and trigger the alarm if not.
     *
     * @param latitude  the current latitude of the device
     * @param longitude the current longitude of the device
     */
    private void checkBoundary(double latitude, double longitude, float accuracy) {
        float acc = data.getFloat("northAcc", 0.0f);
        double northLat = Double.parseDouble(data.getString("northLat", "err")) + acc * meter * 2;
        acc = data.getFloat("eastAcc", 0.0f);
        double eastLong = Double.parseDouble(data.getString("eastLong", "err")) + acc * meter * 2;
        acc = data.getFloat("southAcc", 0.0f);
        double southLat = Double.parseDouble(data.getString("southLat", "err")) - acc * meter * 2;
        acc = data.getFloat("westAcc", 0.0f);
        double westLong = Double.parseDouble(data.getString("westLong", "err")) - acc * meter * 2;
        boolean northOut = latitude - (accuracy * meter) > northLat;
        boolean southOut = latitude + (accuracy * meter) < southLat;
        boolean eastOut = longitude - (accuracy * meter) > eastLong;
        boolean westOut = longitude + (accuracy * meter) < westLong;
        if(northOut||southOut||eastOut||westOut){
                stopScan();
                alarm.set(AlarmManager.RTC_WAKEUP, new GregorianCalendar().getTimeInMillis(), pIntent);
        }
    }

    /**
     * Set up the direction buttons and the scan button.
     */
    private void initializeButtons() {
        Button northLocation = (Button) findViewById(R.id.northButton);
        northLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setButtonAction("northLat", "northAcc", 0);
            }
        });
        Button eastLocation = (Button) findViewById(R.id.eastButton);
        eastLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setButtonAction("eastLong", "eastAcc", 1);
            }
        });
        Button southLocation = (Button) findViewById(R.id.southButton);
        southLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setButtonAction("southLat", "southAcc", 0);
            }
        });
        Button westLocation = (Button) findViewById(R.id.westButton);
        westLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setButtonAction("westLong", "westAcc", 1);
            }
        });
        start = (Button) findViewById(R.id.track);
        if (isScanning()) {
            start.setBackgroundColor(Color.RED);
            start.setText("Stop");
        } else {
            start.setBackgroundColor(Color.GREEN);
            start.setText("Start");
        }
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isScanning()) {
                    stopScan();
                    start.setBackgroundColor(Color.GREEN);
                    start.setText("Start");

                } else {
                    boolean gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (!gpsOn) {
                        Toast.makeText(getApplicationContext(),"please enable GPS for this action" , Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String n = data.getString("northLat", "");
                    String e = data.getString("eastLong", "");
                    String s = data.getString("southLat", "");
                    String w = data.getString("westLong", "");
                    if (n.equals("") || e.equals("") || s.equals("") || w.equals("")) {
                        Toast.makeText(getApplicationContext(), "Boundaries Not Set", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startScan();
                    start.setBackgroundColor(Color.RED);
                    start.setText("Stop");
                }
            }
        });
    }

    /**
     * Determine if a GPS scan is in progress.
     * @return true if scanning false if not.
     */
    private boolean isScanning() {
        String s = data.getString("track", "false");
        return s.equals("true");

    }

    /**
     * Stop a scan and update the scan button.
     */
    private void stopScan() {
        start.setBackgroundColor(Color.GREEN);
        start.setText("Start");
        editor.putString("track", "false");
        editor.commit();
    }

    /**
     * Start a scan.
     */
    public void startScan() {
        String track = data.getString("track", "false");
        if (track.equals("true")) return;
        editor.putString("track", "true");
        editor.commit();
        initializeScanner();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener2);
        Thread t;
        t = new Thread(){
            public void run(){
                while(data.getString("track","false").equals("true")) {
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                locationManager.removeUpdates(locationListener2);
            }
        };
        t.start();
    }

    /**
     * Uses a three second window to try and calculate an accurate location to use for bounds.
     */
    private void calculateLocation() {
        bestLoc[0] = 0.0;
        bestLoc[1] = 0.0;
        bestAcc = Float.MAX_VALUE;
        float accuracy;
        double latitude;
        double longitude;
        for (int i = 0; i < 3; i++) {
            try {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();
                accuracy = lastKnownLocation.getAccuracy();
                if (accuracy < bestAcc) {
                    bestLoc[0] = latitude;
                    bestLoc[1] = longitude;
                    bestAcc = accuracy;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /**
     * The action to take when a direction button is pressed. Opens a dialog that gives the user the
     * ability to scan for a new coordinate or delete an existing coordinate. If a coordinate already
     * exists the scan will store a new one only if a more accurate measurement is made.
     *
     * @param direction String to save coordinate. Should be northLat, eastLong, southLat, or westLong.
     * @param accuracy  Accuracy of measurement. should be northAcc, eastAcc, southAcc, or westAcc.
     * @param latOrLong 0 for latitude coordinate. 1 for longitude coordinate
     */
    private void setButtonAction(final String direction, final String accuracy, final int latOrLong) {
        boolean gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsOn) {
            Toast.makeText(getApplicationContext(),"please enable GPS for this action" , Toast.LENGTH_SHORT).show();
            return;
        }
        final Dialog mainDialog = new Dialog(GPSModeActivity.this);
        mainDialog.setContentView(R.layout.custom_dialog);
        mainDialog.setTitle("Do What With Location?");
        final EditText text = (EditText) mainDialog.findViewById(R.id.dialogText);
        String location = data.getString(direction, "No location");
        if (location.equals("No location") || location.equals("")) {
            text.setText("No location");
        } else {
            text.setText(location + " \nAccurate to " +
                    Float.toString(data.getFloat(accuracy, 0.0f)) + "meters");
        }
        text.setEnabled(false);
        Button edit = (Button) mainDialog.findViewById(R.id.ok);
        edit.setText("Scan");
        edit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                calculateLocation();
                if (bestAcc < data.getFloat(accuracy, Float.MAX_VALUE)) {
                    editor.putString(direction, Double.toString(bestLoc[latOrLong]));
                    editor.putFloat(accuracy, bestAcc);
                    editor.commit();
                    text.setText(data.getString(direction, "No location") + " \nAccurate to " +
                            Float.toString(data.getFloat(accuracy, 0.0f)) + "meters");
                    Toast.makeText(getApplicationContext(), "New Location Saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Higher accuracy was not found", Toast.LENGTH_SHORT).show();
                }

            }
        });
        Button delete = (Button) mainDialog.findViewById(R.id.cancel);
        delete.setText("Delete");
        delete.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mainDialog.dismiss();
                final Dialog dialog = new Dialog(GPSModeActivity.this);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setTitle("Confirm Delete");
                final EditText text = (EditText) dialog.findViewById(R.id.dialogText);
                String location = data.getString(direction, "No location");
                if (location.equals("No location") || location.equals("")) {
                    text.setText(location);
                } else {
                    text.setText(location + " \nAccurate to " +
                            Float.toString(data.getFloat(accuracy, 0.0f)) + "meters");
                }
                text.setEnabled(false);
                Button ok = (Button) dialog.findViewById(R.id.ok);
                ok.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        editor.putString(direction, "");
                        editor.putFloat(accuracy, Float.MAX_VALUE);
                        editor.commit();
                        dialog.dismiss();
                    }
                });
                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        Button button = (Button) mainDialog.findViewById(R.id.extra);
        button.setText("Cancel");
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mainDialog.dismiss();
            }
        });
        mainDialog.show();
    }
}