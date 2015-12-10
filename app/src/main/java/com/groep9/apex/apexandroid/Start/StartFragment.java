package com.groep9.apex.apexandroid.Start;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.groep9.apex.apexandroid.DB.StappenDataItem;
import com.groep9.apex.apexandroid.DB.StappenDataSource;
import com.groep9.apex.apexandroid.R;


public class StartFragment extends Fragment {

    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_start, container, false);

        TextView tvToolbarText = (TextView) getActivity().findViewById(R.id.tv_toolbar_text);
        TextView tvStappenDoel = (TextView) v.findViewById(R.id.tv_stappen_doel_start);

        tvToolbarText.setText(R.string.app_name);

        StappenDataSource stappenDataSource = new StappenDataSource(getActivity());
        stappenDataSource.open();

        StappenDataItem stappenDataItem = stappenDataSource.getLatestStappenDataItem();

        long stepsToday = 0;

        if (stappenDataItem != null) {
            stepsToday = stappenDataItem.getStappen();
        }

        stappenDataSource.close();

        tvStappenDoel.setText("Stappen: " + stepsToday + "/5000");

        return v;
    }
}