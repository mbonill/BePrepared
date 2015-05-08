package mycompany.beprepared;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Matt on 3/23/2015.
 * This Activity defines Wifi mode.
 */
public class WifiModeActivity extends Activity {

    private LinearLayout scrollableList;
    private SharedPreferences data;
    private WifiManager wifiManager;
    private TextView homeNetwork;
    private boolean wifiOn;
    private SharedPreferences.Editor editor;
    private AlarmManager alarm;
    private PendingIntent pIntent;
    private WifiScanner scanner;
    private Button start;
    private Dialog dialog;

    private class WifiScanner extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            initializeWifiList();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_mode);
        scrollableList = (LinearLayout) findViewById(R.id.wifiScroll);
        data = this.getSharedPreferences("wifi", Context.MODE_MULTI_PROCESS);
        editor = data.edit();
        editor.commit();
        homeNetwork = (TextView) findViewById(R.id.selected);
        String home = data.getString("home","not defined");
        homeNetwork.setText("Home Network: " + home);
        homeNetwork.setBackgroundColor(Color.LTGRAY);
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(getBaseContext(), Alarm.class);
        pIntent = PendingIntent.getBroadcast(getBaseContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanner = new WifiScanner();
        registerReceiver(scanner, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        initializeButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        wifiOn = wifiManager.isWifiEnabled();
        if (!wifiOn)
            Toast.makeText(getApplicationContext(),"Please enable Wifi for this mode" , Toast.LENGTH_SHORT).show();
    }

    /**
     * Set up buttons for Wifi Mode
     */
    private void initializeButtons(){
        start = (Button) findViewById(R.id.start);
        if (isScanning()) {
            start.setBackgroundColor(Color.RED);
            start.setText("Stop");
        }else{
            start.setBackgroundColor(Color.GREEN);
            start.setText("Start");
        }
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isScanning()) {
                    stopScan();
                    start.setBackgroundColor(Color.GREEN);
                    start.setText("Start");
                }else{
                    wifiOn = wifiManager.isWifiEnabled();
                    if (!wifiOn) {
                        Toast.makeText(getApplicationContext(), "Please enable Wifi for this action", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startScan();
                    start.setBackgroundColor(Color.RED);
                    start.setText("Stop");
                }
            }
        });
        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wifiManager.startScan();
            }
        });
    }

    /**
     * Determine if Wifi Mode is active
     * @return true if active false if not active
     */
    private boolean isScanning(){
        String s = data.getString("scan","false");
        return s.equals("true");
    }

    /**
     *Change the status of scan to false. The executing thread will exit upon detecting this state.
     */
    private void stopScan(){
        start.setBackgroundColor(Color.GREEN);
        start.setText("Start");
        editor.putString("scan","false");
        editor.commit();
    }

    /**
     * Start the thread to scan for wifi networks. The thread will rescan every 5 seconds.
     */
    private void startScan(){
        String s = data.getString("scan","false");
        if (s.equals("true"))return;
        registerReceiver(scanner, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        editor.putString("scan","true");
        editor.commit();
        Thread t = new Thread(){
            public void run(){
                while(data.getString("scan","false").equals("true")) {
                    wifiManager.startScan();
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        t.start();
    }

    /**
     * This method initializes the list of Wifi Networks in range. Call this method anytime the list
     * needs to be updated. If scan is running this method also will detect if the home network is
     * in range.
     */
    public void initializeWifiList() {
        boolean inRange = false;
        String home = data.getString("home","");
        scrollableList.removeAllViews();
        List<ScanResult> scanList = wifiManager.getScanResults();
        HashMap<String, Integer> networks = new HashMap<>();
        for (ScanResult r : scanList)
            if (!networks.containsKey(r.SSID) || r.level > networks.get(r.SSID))
                networks.put(r.SSID, r.level);
        for (String key : networks.keySet()) {
            final String current = key;
            if (home.equals("") || home.equals(current))
                inRange = true;
            final Button button = new Button(getApplicationContext());
            button.setTextAppearance(this, android.R.style.TextAppearance_Large);
            button.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_button));
            button.setText(current);
            button.setTextColor(Color.WHITE);
            TextView v = new TextView(getApplicationContext());
            v.setVisibility(View.INVISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    initializeDialog(current);
                    dialog.show();
                }
            });
            scrollableList.addView(button);
            scrollableList.addView(v);
        }
            if (!inRange) {
                if (data.getString("scan", "false").equals("false")) return;
                stopScan();
                alarm.set(AlarmManager.RTC_WAKEUP, new GregorianCalendar().getTimeInMillis(), pIntent);

            }
        }

    /**
     * Set up the dialog that appears if a button on the list is clicked.
     * @param current THe name of the Wifi network listed on the button.
     */
    private void initializeDialog(final String current){
        dialog = new Dialog(WifiModeActivity.this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Do What With Network?");
        final EditText text = (EditText) dialog.findViewById(R.id.dialogText);
        text.setText(current);
        text.setEnabled(false);
        Button edit = (Button) dialog.findViewById(R.id.ok);
        edit.setText("Set As Home");
        edit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                homeNetwork.setText("Home Network: " + current);
                editor.putString("home", current);
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
    }
}