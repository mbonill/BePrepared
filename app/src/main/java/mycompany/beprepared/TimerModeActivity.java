package mycompany.beprepared;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Matt on 3/23/2015.
 * Timer mode allows the user to set a notification to go of at a specific time.
 */
public class TimerModeActivity extends Activity {

    private SharedPreferences.Editor editor;
    private TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_mode);
        SharedPreferences data = this.getSharedPreferences("time", Context.MODE_PRIVATE);
        editor = data.edit();
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        Calendar calendar = Calendar.getInstance();
        int min = calendar.get(Calendar.MINUTE);
        int hr = calendar.get(Calendar.HOUR_OF_DAY);
        if (data.contains("minute")) min = data.getInt("minute",12);
        if (data.contains("hour")) hr = data.getInt("hour",0);
        timePicker.setCurrentMinute(min);
        timePicker.setCurrentHour(hr);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            public void onTimeChanged(TimePicker view, int hour, int minute) {
                hour = formatHour(hour);
                String ap = (hour < 12) ? "AM" : "PM";
                editor.putInt("hour", hour);
                editor.putInt("minute", minute);
                editor.putString("ap", ap);
            }
        });

        Button setTime = (Button) findViewById(R.id.setTime);
        setTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int hr = timePicker.getCurrentHour();
                int min = timePicker.getCurrentMinute();
                String ap = (hr < 12) ? "AM" : "PM";
                editor.putInt("hour",hr);
                editor.putInt("minute",min);
                editor.putString("ap", ap);
                editor.commit();
                Long t = new GregorianCalendar().getTimeInMillis();
                Calendar calendar = Calendar.getInstance();
                int currMin = calendar.get(Calendar.MINUTE);
                int currHr = calendar.get(Calendar.HOUR_OF_DAY);
                int[] result = timeDifference(currHr,currMin,hr,min);
                t+=result[0]*60*60*1000+result[1]*60*1000;
                Toast.makeText(getApplicationContext(), "Alarm in \n"+Integer.toString(result[0])
                        +"h "+Integer.toString(result[1])+"m", Toast.LENGTH_SHORT).show();
                Intent alarmIntent = new Intent(getBaseContext(), Alarm.class);
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent pIntent = PendingIntent.getBroadcast(getBaseContext(), 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarm.set(AlarmManager.RTC_WAKEUP,t, pIntent);
            }
        });
    }

    /**
     * Convert military to standard hour.
     * @param hr hour in military time
     * @return hour in standard time
     */
    private int formatHour(int hr){
        if (hr == 0) hr = 12;
        if (hr > 12) hr -= 12;
        return hr;
    }

    /**
     * Calculate the time difference in hours and minutes between an initial time and a future time.
     * THe result is less than one day apart.
     * @param initialHr starting hour
     * @param initialMin starting minute
     * @param finalHr future hour
     * @param finalMin future minute
     * @return int[] with result hr at index 0 and result min at index 1
     */
    private int[] timeDifference(int initialHr, int initialMin, int finalHr, int finalMin){
        int hr=0;
        int min=0;
        while(initialHr!=finalHr){
            initialHr++;
            hr++;
            if(initialHr==24) initialHr=0;
        }
        while(initialMin!=finalMin){
            initialMin++;
            min++;
            if(initialMin==60) {
                initialMin=0;
                hr--;
                if(hr<0)hr=23;
            }
        }
        return new int[]{hr,min};
    }
}
