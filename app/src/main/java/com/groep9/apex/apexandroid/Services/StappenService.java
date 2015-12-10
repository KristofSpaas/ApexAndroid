package com.groep9.apex.apexandroid.Services;


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.groep9.apex.apexandroid.DB.StappenDataItem;
import com.groep9.apex.apexandroid.DB.StappenDataSource;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;

import java.util.Calendar;

public class StappenService extends Service {

    private BandClient client = null;
    private Handler mHandler = new Handler();
    private StappenDataSource stappenDataSource;
    private SharedPreferences sharedPref;

    private BandPedometerEventListener mPedoMeterEventListener = new BandPedometerEventListener() {
        @Override
        public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
            if (bandPedometerEvent != null) {
                long totalStepsMinusToday = sharedPref.getLong("TotalStepsMinusToday", 0);
                long stepsToday = bandPedometerEvent.getTotalSteps() - totalStepsMinusToday;

                System.out.println("Total steps: " + bandPedometerEvent.getTotalSteps());
                System.out.println("Timestamp: " + bandPedometerEvent.getTimestamp());
                System.out.println("Total steps minus today: " + totalStepsMinusToday);
                System.out.println("Steps today: " + stepsToday);

                StappenDataItem oldStappenDataItem = stappenDataSource.getLatestStappenDataItem();

                oldStappenDataItem.setStappen(stepsToday);
                oldStappenDataItem.setDateInMiliis(bandPedometerEvent.getTimestamp());

                stappenDataSource.updateStappenDataItem(oldStappenDataItem);
                stappenDataSource.close();

                onSucces();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(
                getBaseContext());

        Calendar calendar = Calendar.getInstance();

        stappenDataSource = new StappenDataSource(getBaseContext());
        stappenDataSource.open();

        StappenDataItem oldStappenDataItem = stappenDataSource.getLatestStappenDataItem();

        if (oldStappenDataItem != null) {
            if (!isToday(oldStappenDataItem.getDateInMiliis())) {
                long totalStepsMinusToday = sharedPref.getLong("TotalStepsMinusToday", 0);

                sharedPref.edit().putLong("TotalStepsMinusToday"
                        , totalStepsMinusToday + oldStappenDataItem.getStappen()).apply();

                StappenDataItem newStappenDataItem = new StappenDataItem();
                newStappenDataItem.setStappen(0);
                newStappenDataItem.setDateInMiliis(calendar.getTimeInMillis());

                stappenDataSource.createStappenDataItem(newStappenDataItem);
            }
        } else {
            StappenDataItem newStappenDataItem = new StappenDataItem();
            newStappenDataItem.setStappen(0);
            newStappenDataItem.setDateInMiliis(calendar.getTimeInMillis());

            stappenDataSource.createStappenDataItem(newStappenDataItem);
        }

        new PedoMeterSubscriptionTask().execute();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                onFail("Couldn't acquire total steps");
            }
        }, 60000);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException | BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }

    private class PedoMeterSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    client.getSensorManager().registerPedometerEventListener(mPedoMeterEventListener);
                } else {
                    onFail("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage;
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                onFail(exceptionMessage);

            } catch (Exception e) {
                onFail(e.getMessage());
            }
            return null;
        }
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                System.out.println("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        System.out.println("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }

    private Boolean isToday(long dateMillis) {
        Calendar calendar = Calendar.getInstance();

        // set the calendar to start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long todayInMillis = calendar.getTimeInMillis();

        return dateMillis >= todayInMillis;
    }

    private void onSucces() {
        mHandler.removeCallbacksAndMessages(null);
        System.out.println("Stopping Stappen Service..");
        stopSelf();
    }

    public void onFail(String msg) {
        System.out.println(msg);
        System.out.println("Stopping Stappen Service..");
        mHandler.removeCallbacksAndMessages(null);
        stappenDataSource.close();
        stopSelf();
    }
}
