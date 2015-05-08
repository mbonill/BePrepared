package mycompany.beprepared;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Matthew Bonilla on 3/23/2015.
 * This Activity defines the checklist of items.
 */
public class ChecklistActivity extends Activity {

    private LinearLayout scroll;
    private SharedPreferences data;
    private String items;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        scroll = (LinearLayout) findViewById(R.id.iScroll);
        data = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        TextView top = (TextView) findViewById(R.id.checklistTitle);
        title = data.getString("currDestination", "Unknown");
        top.setText("Checklist for " + title);
        data = this.getSharedPreferences(title, Context.MODE_PRIVATE);
        items = data.getString(title, "");
        Button done = (Button) findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MenuActivity.class);
                startActivity(intent);
            }
        });
        initializeCheckBox();
    }

    /**
     * Add checkboxes to the scrollable layout. Call this method anytime the list changes.
     */
    private void initializeCheckBox() {
        scroll.removeAllViews();
        items = data.getString(title, "");
        if(items.isEmpty())return;
        String[] list = items.split("\n");
        for (String s: list) {
            final CheckBox box = new CheckBox(getApplicationContext());
            box.setTextAppearance(this, android.R.style.TextAppearance_Large);
            box.setTextColor(Color.WHITE);
            box.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_button));
            box.setText(s);
            box.setTextSize(30);
            scroll.addView(box);
        }
    }
}
