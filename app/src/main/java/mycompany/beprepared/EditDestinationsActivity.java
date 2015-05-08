package mycompany.beprepared;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Matthew Bonilla on 3/23/2015.
 * This activity allows the user to edit a list of destinations.
 */
public class EditDestinationsActivity extends Activity {

    private SharedPreferences data;
    private String currentDestination, lists;
    private LinearLayout scrollableList;
    private SharedPreferences.Editor editor;
    private Dialog mainDialog, newDestinationDialog;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_destinations);
        data = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        lists = data.getString("list","");
        scrollableList = (LinearLayout) findViewById(R.id.destinationScroll);
        currentDestination="";
        initializeList();

        Button addDestination = (Button) findViewById(R.id.addDestination);
        addDestination.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initializeNewDestinationDialog();
                newDestinationDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getBaseContext(), MenuActivity.class);
        startActivity(intent);
    }

    /**
     * Set up the scrollable list of destination buttons
     */
    private void initializeList(){
        scrollableList.removeAllViews();
        lists = data.getString("list","");
        if(lists.isEmpty())return;
        String[] list = lists.split("\n");
        for(String s : list) {
            final Button button = new Button(getApplicationContext());
            button.setTextAppearance(this, android.R.style.TextAppearance_Large);
            button.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_button));
            button.setText(s);
            button.setTextColor(Color.WHITE);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    currentDestination = button.getText().toString();
                    initializeMainDialog();
                    mainDialog.show();
                }
            });
            TextView v = new TextView(getApplicationContext());
            v.setVisibility(View.INVISIBLE);
            scrollableList.addView(button);
            scrollableList.addView(v);
        }
    }

    /**
     * Defines the dialog box that opens when the new destination button is pressed.
     */
    private void initializeNewDestinationDialog(){
        newDestinationDialog = new Dialog(EditDestinationsActivity.this);
        newDestinationDialog.setContentView(R.layout.custom_dialog);
        newDestinationDialog.setTitle("Enter New Destination");
        final EditText text = (EditText) newDestinationDialog.findViewById(R.id.dialogText);
        Button ok = (Button) newDestinationDialog.findViewById(R.id.ok);
        ok.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                String destination = text.getText().toString();
                if(!destination.equals("")&&!destination.contains("\n")) {
                    lists += destination + "\n";
                    editor.putString("list", lists);
                    editor.commit();
                    initializeList();
                }
                newDestinationDialog.dismiss();
            }
        });

        Button cancel = (Button) newDestinationDialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                newDestinationDialog.dismiss();
            }
        });
    }

    /**
     * Defines the dialog box that opens when a destination button is pressed. Allows the user
     * to delete the destination or edit the items for that destination.
     */
    private void initializeMainDialog(){
        mainDialog = new Dialog(EditDestinationsActivity.this);
        mainDialog.setContentView(R.layout.custom_dialog);
        mainDialog.setTitle("Do What With Destination?");
        final EditText text = (EditText) mainDialog.findViewById(R.id.dialogText);
        text.setText(currentDestination);
        text.setEnabled(false);
        Button edit = (Button) mainDialog.findViewById(R.id.ok);
        edit.setText("Edit");
        edit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (!currentDestination.equals("")) {
                    editor.putString("currentDestination", currentDestination);
                    editor.commit();
                    Intent intent = new Intent(getBaseContext(), EditItemsActivity.class);
                    startActivity(intent);
                }
                mainDialog.dismiss();
            }
        });

        Button delete = (Button) mainDialog.findViewById(R.id.cancel);
        delete.setText("Delete");
        delete.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mainDialog.dismiss();
                final Dialog dialog = new Dialog(EditDestinationsActivity.this);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setTitle("Confirm Delete");
                final EditText text = (EditText) dialog.findViewById(R.id.dialogText);
                text.setText(currentDestination);
                text.setEnabled(false);
                Button ok = (Button) dialog.findViewById(R.id.ok);
                ok.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        lists = data.getString("list","");
                        lists = lists.replaceAll(currentDestination+"\n","");
                        editor.putString("list",lists);
                        editor.commit();
                        SharedPreferences d = getBaseContext().getSharedPreferences(currentDestination,Context.MODE_PRIVATE);
                        SharedPreferences.Editor e = d.edit();
                        e.clear();
                        e.commit();
                        currentDestination="";
                        initializeList();
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
    }
}
