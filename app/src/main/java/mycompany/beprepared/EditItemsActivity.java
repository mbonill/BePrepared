package mycompany.beprepared;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Matt on 4/8/2015.
 * This Activity allows the user to edit a list of items.
 */
public class EditItemsActivity extends Activity {

    private SharedPreferences data;
    private SharedPreferences.Editor editor;
    private String currentItem;
    private LinearLayout scrollableList;
    private String items, title;
    private Button ok, cancel;
    private Dialog dialog, mainDialog;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_items);
        scrollableList = (LinearLayout) findViewById(R.id.itemScroll);
        data = this.getSharedPreferences("data", Context.MODE_PRIVATE);
        TextView top = (TextView) findViewById(R.id.currentDestination);
        title = data.getString("currentDestination", "Unknown");
        top.setText(title);
        data = this.getSharedPreferences(title, Context.MODE_PRIVATE);
        editor = data.edit();
        items = data.getString(title, "");
        initializeList();
        Button addItem = (Button) findViewById(R.id.addItem);
        addItem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog = new Dialog(EditItemsActivity.this);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setTitle("Enter New Item");
                text = (TextView) dialog.findViewById(R.id.dialogText);
                ok = (Button) dialog.findViewById(R.id.ok);
                ok.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        String item = text.getText().toString();
                        if (!item.equals("") && !item.contains("\n")) {
                            items += item + "\n";
                            editor.putString(title, items);
                            editor.commit();
                            initializeList();
                        }
                        dialog.dismiss();
                    }
                });
                cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    /**
     * Add list of buttons to the scrollable layout. Call this method anytime the list changes.
     */
    public void initializeList() {
        scrollableList.removeAllViews();
        items = data.getString(title, "");
        if(items.isEmpty())return;
        String[] list = items.split("\n");
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
                    currentItem = button.getText().toString();
                    initializeMainDialog();
                    mainDialog.show();
                }
            });
            scrollableList.addView(button);
            scrollableList.addView(v);
        }
    }

    /**
     * Define the dialog box that opens when the user clicks on an item button. The user can then
     * delete this item if they wish.
     */
    private void initializeMainDialog() {
        mainDialog = new Dialog(EditItemsActivity.this);
        mainDialog.setContentView(R.layout.custom_dialog);
        mainDialog.setTitle("Do What With Item?");
        final TextView text = (TextView) mainDialog.findViewById(R.id.dialogText);
        text.setText(currentItem);
        text.setEnabled(false);
        Button edit = (Button) mainDialog.findViewById(R.id.ok);
        edit.setText("Cancel");
        edit.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mainDialog.dismiss();
            }
        });
        Button delete = (Button) mainDialog.findViewById(R.id.cancel);
        delete.setText("Delete");
        delete.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mainDialog.dismiss();
                final Dialog dialog = new Dialog(EditItemsActivity.this);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setTitle("Confirm Delete");
                final TextView text = (TextView) dialog.findViewById(R.id.dialogText);
                text.setText(currentItem);
                text.setEnabled(false);
                Button ok = (Button) dialog.findViewById(R.id.ok);
                ok.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        items = data.getString(title, "");
                        items = items.replaceFirst(currentItem + "\n", "");
                        editor.putString(title, items);
                        editor.commit();
                        currentItem = "";
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
    }
}
