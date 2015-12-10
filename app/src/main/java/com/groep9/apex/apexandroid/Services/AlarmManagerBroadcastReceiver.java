package com.groep9.apex.apexandroid.Services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;


public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");

        //Acquire the lock
        wl.acquire();

        // use this to start and trigger a service
        Intent iHartslagService = new Intent(context, HartslagService.class);
        context.startService(iHartslagService);

        System.out.println("Starting Heart Rate Service...");

        Intent iTemperatuurService = new Intent(context, TemperatuurService.class);
        context.startService(iTemperatuurService);

        System.out.println("Starting Skin Temperature Service...");

        Intent iStappenService = new Intent(context, StappenService.class);
        context.startService(iStappenService);

        System.out.println("Starting Stappen Service...");

        //Release the lock
        wl.release();
    }
}
