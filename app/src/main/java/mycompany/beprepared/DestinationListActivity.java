package mycompany.beprepared;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Matthew Bonilla on 3/23/2015.
 * This Activity defines the list of destinations.
 */
public class DestinationListActivity extends Activity {

    private LinearLayout scrollableList;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;
    private String currentDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        scrollableList = (LinearLayout) findViewById(R.id.dScroll);
        data = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        editor.commit();
        initializeList();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getBaseContext(), MenuActivity.class);
        startActivity(intent);
    }

    /**
     * Add list of buttons to the scrollable layout. Call this method anytime the list changes.
     */
    public void initializeList() {
        scrollableList.removeAllViews();
        String destinations = data.getString("list", "");
        if(destinations.isEmpty()) return;
        String[] list = destinations.split("\n");
        for (String s : list) {
            final Button button = new Button(getApplicationContext());
            button.setTextAppearance(this, android.R.style.TextAppearance_Large);
            button.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_button));
            button.setText(s);
            button.setTextColor(Color.WHITE);
            TextView v = new TextView(getApplicationContext());
            v.setVisibility(View.INVISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    currentDestination = button.getText().toString();
                    if (!currentDestination.equals("")) {
                        editor.putString("currDestination", currentDestination);
                        editor.commit();
                        Intent intent = new Intent(getBaseContext(), ChecklistActivity.class);
                        startActivity(intent);
                    }
                }
            });
            scrollableList.addView(button);
            scrollableList.addView(v);
        }
    }
}