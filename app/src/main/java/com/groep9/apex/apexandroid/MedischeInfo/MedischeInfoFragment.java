package com.groep9.apex.apexandroid.MedischeInfo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groep9.apex.apexandroid.DB.HartslagDataSource;
import com.groep9.apex.apexandroid.DB.StappenDataSource;
import com.groep9.apex.apexandroid.DB.TemperatuurDataSource;
import com.groep9.apex.apexandroid.Instellingen.InstellingenFragment;
import com.groep9.apex.apexandroid.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MedischeInfoFragment extends Fragment {

    private String lengtePrefString, gewichtPrefString, geboortedatumPrefString;
    private RecyclerView rvMedischeInfo;

    public MedischeInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_medische_info, container, false);

        TextView tvToolbarText = (TextView) getActivity().findViewById(R.id.tv_toolbar_text);
        tvToolbarText.setText(R.string.MedischeInfo);

        // Get SharedPreference file
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                this.getActivity());

        // Get Preferences
        lengtePrefString = sharedPref.getString(InstellingenFragment.SettingsFragment.KEY_LENGTE, "");
        gewichtPrefString = sharedPref.getString(InstellingenFragment.SettingsFragment.KEY_GEWICHT, "");
        geboortedatumPrefString = sharedPref.getString(InstellingenFragment.SettingsFragment.KEY_GEBOORTEDATUM, "");

        // Create ArrayList for Cards
        List<CardItemMedischeInfo> cardsMedischeInfo = new ArrayList<>();

        // Create BMI Card
        CardItemMedischeInfo cardItemBMI = createBMICard();

        // Create Hartslag Card
        CardItemMedischeInfo cardItemHartslag = createHartslagCard();

        // Create Temperatuur Card
        CardItemMedischeInfo cardItemTemperatuur = createTemperatuurCard();

        // Create Stappen Card
        CardItemMedischeInfo cardItemStappen = createStappenCard();

        // Add cards to ArrayList
        cardsMedischeInfo.add(cardItemHartslag);
        cardsMedischeInfo.add(cardItemTemperatuur);
        cardsMedischeInfo.add(cardItemStappen);
        cardsMedischeInfo.add(cardItemBMI);

        // Set up Recyclerview and Adapter
        rvMedischeInfo = (RecyclerView) v.findViewById(R.id.rv_medische_info);

        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        rvMedischeInfo.setLayoutManager(llm);

        RVAdapterMedischeInfo rvAdapterMedischeInfo = new RVAdapterMedischeInfo(cardsMedischeInfo);
        rvMedischeInfo.setAdapter(rvAdapterMedischeInfo);

        final View coloredBackgroundView = v.findViewById(R.id.colored_background_view_medische_info);

        rvMedischeInfo.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollColoredViewParallax(dy);
            }

            private void scrollColoredViewParallax(int dy) {
                coloredBackgroundView.setTranslationY(coloredBackgroundView.getTranslationY() - dy / 3.0f);
            }

        });

        return v;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        rvMedischeInfo.scrollToPosition(0);
    }

    private CardItemMedischeInfo createHartslagCard() {
        HartslagDataSource hartslagDataSource = new HartslagDataSource(getActivity());

        hartslagDataSource.open();

        int minHartslag = hartslagDataSource.getMinHartslag();
        int maxHartslag = hartslagDataSource.getMaxHartslag();

        hartslagDataSource.close();

        CardItemMedischeInfo cardItemHartslag = new CardItemMedischeInfo(
                "Hartslag", "Min: " + minHartslag, "Max: " + maxHartslag, R.drawable.heartbeat);

        if (!geboortedatumPrefString.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            Calendar dob = Calendar.getInstance();
            Calendar today = Calendar.getInstance();

            try {
                dob.setTime(sdf.parse(geboortedatumPrefString));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            int leeftijd = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
                leeftijd--;
            } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                leeftijd--;
            }

            int maxHartslagLeeftijd = 220 - leeftijd;

            cardItemHartslag.setContent3("Max voor je leeftijd: " + maxHartslagLeeftijd);
        }

        return cardItemHartslag;
    }

    private CardItemMedischeInfo createTemperatuurCard() {
        TemperatuurDataSource temperatuurDataSource = new TemperatuurDataSource(getActivity());
        temperatuurDataSource.open();

        float minTemperatuur = temperatuurDataSource.getMinTemperatuur();
        float maxTemperatuur = temperatuurDataSource.getMaxTemperatuur();

        temperatuurDataSource.close();

        return new CardItemMedischeInfo(
                "Temperatuur", "Min: " + minTemperatuur + (char) 0x00B0,
                "Max: " + maxTemperatuur + (char) 0x00B0, R.drawable.temperature
        );
    }

    private CardItemMedischeInfo createStappenCard() {
        StappenDataSource stappenDataSource = new StappenDataSource(getActivity());
        stappenDataSource.open();

        long minStappen = stappenDataSource.getMinStappen();
        long maxStappen = stappenDataSource.getMaxStappen();

        stappenDataSource.close();

        return new CardItemMedischeInfo("Stappen", "Min per dag: " + minStappen,
                "Max per dag: " + maxStappen, R.drawable.steps);
    }

    private CardItemMedischeInfo createBMICard() {
        double lengteInM, gewichtInKg;

        CardItemMedischeInfo cardItemBMI;

        if (!lengtePrefString.equals("") && !gewichtPrefString.equals("")
                && !gewichtPrefString.equals(".")) {
            lengteInM = Double.parseDouble(lengtePrefString) / 100;
            gewichtInKg = Double.parseDouble(gewichtPrefString);

            double BMI = (gewichtInKg / (lengteInM * lengteInM));
            double BMIAfgekapt = (double) Math.round(BMI * 100d) / 100d;

            cardItemBMI = new CardItemMedischeInfo(
                    "BMI", "Lengte: " + lengteInM + " m", "Gewicht: " + gewichtInKg + " kg",
                    R.drawable.scale
            );

            cardItemBMI.setContent3("BMI: " + BMIAfgekapt);
        } else {
            cardItemBMI = new CardItemMedischeInfo(
                    "BMI", "Als je je lengte en gewicht ingeeft bij 'Instellingen', " +
                    "komt hier je BMI te staan.", R.drawable.scale
            );
        }

        return cardItemBMI;
    }

}
