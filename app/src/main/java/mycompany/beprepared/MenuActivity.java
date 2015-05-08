package mycompany.beprepared;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Matt on 3/23/2015.
 * This Activity defines the Menu.
 */
public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initializeButtons();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    /**
     * Set up menu buttons
     */
    private void initializeButtons(){
        Button gpsMode = (Button) findViewById(R.id.gps);
        gpsMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), GPSModeActivity.class);
                startActivity(intent);
            }
        });
        Button wifiMode = (Button) findViewById(R.id.wifi);
        wifiMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), WifiModeActivity.class);
                startActivity(intent);
            }
        });
        Button timerMode = (Button) findViewById(R.id.timer);
        timerMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), TimerModeActivity.class);
                startActivity(intent);
            }
        });
        Button manualMode = (Button) findViewById(R.id.manual);
        manualMode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), NotificationScreenActivity.class);
                startActivity(intent);
            }
        });
        Button lists = (Button) findViewById(R.id.lists);
        lists.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), EditDestinationsActivity.class);
                startActivity(intent);
            }
        });
        Button help = (Button) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), HelpMenuActivity.class);
                startActivity(intent);
            }
        });
    }
}
