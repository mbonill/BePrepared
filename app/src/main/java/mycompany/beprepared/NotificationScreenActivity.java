package mycompany.beprepared;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Matt on 3/23/2015.
 * Notification screen asks the user if they are heading out.
 */
public class NotificationScreenActivity extends Activity {

    Intent menuIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_screen);
        menuIntent = new Intent(getBaseContext(), MenuActivity.class);
        initializeButtons();
    }

    @Override
    public void onBackPressed(){
        startActivity(menuIntent);
    }

    private void initializeButtons(){
        Button yes = (Button) findViewById(R.id.Yes);
        yes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), DestinationListActivity.class);
                startActivity(intent);
            }
        });
        Button no = (Button) findViewById(R.id.No);
        no.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(menuIntent);
            }
        });
    }
}
