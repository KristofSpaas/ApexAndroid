package com.groep9.apex.apexandroid.Instellingen;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.groep9.apex.apexandroid.R;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class InstellingenFragment extends Fragment {

    private SettingsFragment settingsFragment;

    public InstellingenFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        settingsFragment = new SettingsFragment();

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.frame_container, settingsFragment, settingsFragment
                        .getClass().getSimpleName()).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        TextView tvToolbarText = (TextView) getActivity().findViewById(R.id.tv_toolbar_text);
        tvToolbarText.setText(R.string.Instellingen);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRemoving()) {
            getActivity().getFragmentManager().beginTransaction()
                    .remove(settingsFragment).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements DatePickerDialog.OnDateSetListener {

        private SharedPreferences sharedPref;
        private SharedPreferences.OnSharedPreferenceChangeListener listener;
        public static final String KEY_LENGTE = "pref_key_lengte";
        public static final String KEY_GEWICHT = "pref_key_gewicht";
        public static final String KEY_GEBOORTEDATUM = "pref_key_geboortedatum";
        public static final String KEY_CONSENT = "pref_key_consent";
        private String geboortedatumPrefString;
        private BandClient client = null;


        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.instellingen);

            sharedPref = PreferenceManager.getDefaultSharedPreferences(
                    this.getActivity());

            String lengtePrefString = sharedPref.getString(KEY_LENGTE, "");
            String gewichtPrefString = sharedPref.getString(KEY_GEWICHT, "");
            geboortedatumPrefString = sharedPref.getString(KEY_GEBOORTEDATUM, "");

            if (!lengtePrefString.equals("")) {
                EditTextPreference etPrefLengte = (EditTextPreference) findPreference(KEY_LENGTE);
                etPrefLengte.setSummary(Integer.parseInt(lengtePrefString) + " cm");
            }

            if (!gewichtPrefString.equals("") && !gewichtPrefString.equals(".")) {
                EditTextPreference etPrefGewicht = (EditTextPreference) findPreference(KEY_GEWICHT);
                etPrefGewicht.setSummary(Double.parseDouble(gewichtPrefString) + " kg");
            }

            if (!geboortedatumPrefString.equals("")) {
                Preference prefGeboortedatum = findPreference(KEY_GEBOORTEDATUM);
                prefGeboortedatum.setSummary(geboortedatumPrefString);
            }

            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    Preference connectionPref;

                    switch (key) {
                        case KEY_LENGTE:
                            connectionPref = findPreference(key);
                            // Set summary to be the user-description for the selected value
                            if (!prefs.getString(key, "").equals("")) {
                                connectionPref.setSummary(Integer.parseInt(prefs.getString(key, "")) + " cm");
                            } else {
                                connectionPref.setSummary("Geef je lengte in");
                            }
                            break;

                        case KEY_GEWICHT:
                            connectionPref = findPreference(key);
                            // Set summary to be the user-description for the selected value
                            if (!prefs.getString(key, "").equals("") && !prefs.getString(key, "").equals(".")) {
                                connectionPref.setSummary(Double.parseDouble(prefs.getString(key, "")) + " kg");
                            } else {
                                connectionPref.setSummary("Geef je gewicht in");
                            }
                            break;

                        default:
                            break;
                    }
                }
            };

            Preference geboorteDatumPreference = findPreference(KEY_GEBOORTEDATUM);
            geboorteDatumPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showDateDialog();
                    return false;
                }
            });

            Preference btnConsent = findPreference(KEY_CONSENT);
            btnConsent.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final WeakReference<FragmentActivity> reference = new WeakReference<>((FragmentActivity) getActivity());

                    new HeartRateConsentTask().execute(reference);
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listener);
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

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Preference connectionPref = findPreference(KEY_GEBOORTEDATUM);

            String geboortedatum = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;

            if (year != 0) {
                connectionPref.setSummary(geboortedatum);
                sharedPref.edit().putString(KEY_GEBOORTEDATUM, geboortedatum).apply();
            } else {
                connectionPref.setSummary("Geef je geboortedatum in");
            }
        }

        private void showDateDialog() {
            int year = 1991, month = 8, day = 26;

            geboortedatumPrefString = sharedPref.getString(KEY_GEBOORTEDATUM, "");

            if (!geboortedatumPrefString.equals("")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
                Calendar dob = Calendar.getInstance();

                try {
                    dob.setTime(sdf.parse(geboortedatumPrefString));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                year = dob.get(Calendar.YEAR);
                month = dob.get(Calendar.MONTH);
                day = dob.get(Calendar.DAY_OF_MONTH);
            }

            new DatePickerDialog(getActivity(), this, year, month, day).show();
        }

        private class HeartRateConsentTask extends AsyncTask<WeakReference<FragmentActivity>, Void, Void> {
            @Override
            protected Void doInBackground(WeakReference<FragmentActivity>... params) {
                try {
                    if (getConnectedBandClient()) {
                        if (params[0].get() != null) {
                            client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                                @Override
                                public void userAccepted(boolean consentGiven) {
                                }
                            });
                        }
                    } else {
                        alert("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
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
                    alert(exceptionMessage);

                } catch (Exception e) {
                    alert(e.getMessage());
                }
                return null;
            }


            private boolean getConnectedBandClient() throws InterruptedException, BandException {
                if (client == null) {
                    BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
                    if (devices.length == 0) {
                        System.out.println("Band isn't paired with your phone.\n");
                        return false;
                    }
                    client = BandClientManager.getInstance().create(getActivity().getBaseContext(), devices[0]);
                } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
                    return true;
                }

                System.out.println("Band is connecting...\n");
                return ConnectionState.CONNECTED == client.connect().await();
            }
        }

        private void alert(final String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Er ging iets mis")
                            .setMessage(message)
                            .setNegativeButton("Sluiten", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(R.drawable.alert)
                            .show();
                }
            });
        }
    }
}
