package com.groep9.apex.apexandroid.Services;


import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.groep9.apex.apexandroid.DB.TemperatuurDataItem;
import com.groep9.apex.apexandroid.DB.TemperatuurDataSource;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;

public class TemperatuurService extends Service {

    private BandClient client = null;
    private Handler mHandler = new Handler();

    private BandSkinTemperatureEventListener mSkinTemperatureEventListener = new BandSkinTemperatureEventListener() {
        @Override
        public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
            if (bandSkinTemperatureEvent != null) {
                if (bandSkinTemperatureEvent.getTemperature() != 0) {
                    System.out.println("Skin temperature: " + bandSkinTemperatureEvent.getTemperature());
                    System.out.println("Timestamp: " + bandSkinTemperatureEvent.getTimestamp());

                    TemperatuurDataItem temperatuurDataItem = new TemperatuurDataItem();

                    temperatuurDataItem.setTemperatuur(bandSkinTemperatureEvent.getTemperature());
                    temperatuurDataItem.setDateInMiliis(bandSkinTemperatureEvent.getTimestamp());

                    TemperatuurDataSource temperatuurDataSource = new TemperatuurDataSource(getBaseContext());
                    temperatuurDataSource.open();

                    temperatuurDataSource.createTemperatuurDataItem(temperatuurDataItem);

                    temperatuurDataSource.close();

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
        new SkinTemperatureSubscriptionTask().execute();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                onFail("Couldn't acquire skin temperature");
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

    private class SkinTemperatureSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    client.getSensorManager().registerSkinTemperatureEventListener(mSkinTemperatureEventListener);
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

    private void onSucces() {
        mHandler.removeCallbacksAndMessages(null);
        System.out.println("Stopping Temperatuur Service..");
        stopSelf();
    }

    public void onFail(String msg) {
        System.out.println(msg);
        System.out.println("Stopping Temperatuur Service..");
        mHandler.removeCallbacksAndMessages(null);
        stopSelf();
    }
}
