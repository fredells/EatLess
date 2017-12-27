package fredells.eatless;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Settings extends Activity implements View.OnClickListener {

    private Button timeSet1, timeSet2, timeSet3;
    private Switch onSet1, onSet2, onSet3;
    private String time1, time2, time3;
    private Boolean on1, on2, on3;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = this.getSharedPreferences("com.fredells.eatless", Context.MODE_PRIVATE);
        time1 = prefs.getString("time1", "8:00 AM");
        time2 = prefs.getString("time2", "12:00 PM");
        time3 = prefs.getString("time3", "6:00 PM");
        on1 = prefs.getBoolean("on1", false);
        on2 = prefs.getBoolean("on2", false);
        on3 = prefs.getBoolean("on3", false);

        //setup buttons
        timeSet1 = findViewById(R.id.timeSet1);
        timeSet1.setText(time1);
        timeSet2 = findViewById(R.id.timeSet2);
        timeSet2.setText(time2);
        timeSet3 = findViewById(R.id.timeSet3);
        timeSet3.setText(time3);

        //setup switches
        onSet1 = findViewById(R.id.onSet1);
        onSet1.setChecked(on1);
        onSet1.setOnClickListener(this);

        onSet2 = findViewById(R.id.onSet2);
        onSet2.setChecked(on2);
        onSet2.setOnClickListener(this);

        onSet3 = findViewById(R.id.onSet3);
        onSet3.setChecked(on3);
        onSet3.setOnClickListener(this);

        timeSet1.setOnClickListener(this);
        timeSet2.setOnClickListener(this);
        timeSet3.setOnClickListener(this);


    }

    public void setAlarm(int requestCode, int hour, int minute) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),alarmManager.INTERVAL_DAY,pendingIntent);
    }

    public void cancelAlarm(int requestCode) {

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void showTimePicker(View v, String time, Button timeSet, String timePref, Boolean switchChecked, int code) {
        int hours = 0;
        int minutes = 0;

        final Button button = timeSet;
        final String pref = timePref;
        final Boolean setAlarm = switchChecked;
        final int requestCode = code;

        DateFormat df = new SimpleDateFormat("hh:mm a");
        Date dt;
        try {
            dt = df.parse(time);
            hours = dt.getHours();
            minutes = dt.getMinutes();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(v.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                DateFormat df = new SimpleDateFormat("hh:mm a");
                Date dt = new Date();
                dt.setHours(hour);
                dt.setMinutes(minute);
                String str = df.format(dt);
                button.setText(str);
                prefs.edit().putString(pref, str).apply();

                if (setAlarm) {
                    setAlarm(requestCode, hour, minute);
                }


            }
        }, hours, minutes, false);

        timePickerDialog.updateTime(hours, minutes);
        timePickerDialog.show();
    }

    @Override
    public void onClick(View v) {

        Log.v("DEBUGGING", "ID: " + v.getId());

        switch (v.getId()) {
            case R.id.onSet1:
                on1 = onSet1.isChecked();
                prefs.edit().putBoolean("on1",onSet1.isChecked()).apply();
                if (on1) {
                    showTimePicker(v, time1, timeSet1, "time1", on1, 1);
                }
                else {
                    cancelAlarm(1);
                }

                break;

            case R.id.onSet2:
                on2 = onSet2.isChecked();
                prefs.edit().putBoolean("on2",onSet2.isChecked()).apply();
                if (on2) {
                    showTimePicker(v, time2, timeSet2, "time2", on2, 2);
                }
                else {
                    cancelAlarm(2);
                }
                break;

            case R.id.onSet3:
                on3 = onSet3.isChecked();
                prefs.edit().putBoolean("on3",onSet3.isChecked()).apply();
                if (on3) {
                    showTimePicker(v, time3, timeSet3, "time3", on3, 3);
                }
                else {
                    cancelAlarm(3);
                }
                break;

            case R.id.timeSet1: {
                showTimePicker(v, time1, timeSet1, "time1", on1, 1);
                break;
            }

            case R.id.timeSet2: {
                showTimePicker(v, time2, timeSet2, "time2", on2, 2);
                break;
            }

            case R.id.timeSet3: {
                showTimePicker(v, time3, timeSet3, "time3", on3, 3);
                break;
            }


        }

    }


}
