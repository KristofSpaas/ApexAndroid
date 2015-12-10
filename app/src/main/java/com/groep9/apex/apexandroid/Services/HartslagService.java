package com.groep9.apex.apexandroid.Services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.groep9.apex.apexandroid.DB.HartslagDataItem;
import com.groep9.apex.apexandroid.DB.HartslagDataSource;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateQuality;


public class HartslagService extends Service {

    private BandClient client = null;
    private Handler mHandler = new Handler();

    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                if (event.getQuality() == HeartRateQuality.LOCKED) {
                    System.out.println("Heart rate: " + event.getHeartRate());
                    System.out.println("Timestamp: " + event.getTimestamp());

                    HartslagDataItem hartslagDataItem = new HartslagDataItem();

                    hartslagDataItem.setHartslag(event.getHeartRate());
                    hartslagDataItem.setDateInMiliis(event.getTimestamp());

                    HartslagDataSource hartslagDataSource = new HartslagDataSource(getBaseContext());
                    hartslagDataSource.open();

                    hartslagDataSource.createHartslagDataItem(hartslagDataItem);

                    hartslagDataSource.close();

                    onSucces();
                }
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
        new HeartRateSubscriptionTask().execute();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                onFail("Couldn't acquire Heart Rate");
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

    public class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);
                    } else {
                        onFail("You have not given this application consent to access heart rate data yet."
                                + " Please press the Heart Rate Consent button.");
                    }
                } else {
                    onFail("Band isn't connected. Please make sure bluetooth is on and the band is in range.");
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
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        return ConnectionState.CONNECTED == client.connect().await();
    }

    private void onSucces() {
        mHandler.removeCallbacksAndMessages(null);
        System.out.println("Stopping Hartslag Service..");
        stopSelf();
    }

    public void onFail(String msg) {
        System.out.println(msg);
        System.out.println("Stopping Hartslag Service..");
        mHandler.removeCallbacksAndMessages(null);
        stopSelf();
    }
}
