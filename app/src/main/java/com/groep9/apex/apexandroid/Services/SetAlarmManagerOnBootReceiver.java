package com.groep9.apex.apexandroid.Services;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SetAlarmManagerOnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            setAlarmManager(context);
        }
    }

    private void setAlarmManager(Context context) {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);

        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

            //After every 60 seconds
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60000, pi);
        }
    }
}
