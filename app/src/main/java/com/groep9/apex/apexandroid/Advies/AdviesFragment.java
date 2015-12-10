package com.groep9.apex.apexandroid.Advies;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.brnunes.swipeablerecyclerview.SwipeableRecyclerViewTouchListener;
import com.groep9.apex.apexandroid.AppFunctions;
import com.groep9.apex.apexandroid.DB.AdviesDataSource;
import com.groep9.apex.apexandroid.DB.AdviesItem;
import com.groep9.apex.apexandroid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;

public class AdviesFragment extends Fragment implements View.OnClickListener {

    private List<CardItemAdvies> cardsAdvies;
    private RVAdapterAdvies adapterAdvies;
    private RecyclerView rvAdvies;
    private String access_token;
    private int patientId;
    private ImageView iv_refresh;
    private int[] drawables;

    public AdviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_advies, container, false);

        TextView tvToolbarText = (TextView) getActivity().findViewById(R.id.tv_toolbar_text);
        tvToolbarText.setText(R.string.Advies);

        iv_refresh = (ImageView) getActivity().findViewById(R.id.iv_refresh);
        iv_refresh.setVisibility(View.VISIBLE);
        iv_refresh.setOnClickListener(this);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
                this.getActivity());

        access_token = sharedPref.getString("pref_access_token", "");
        patientId = sharedPref.getInt("pref_patient_id", 0);

        drawables = new int[]{R.drawable.healthy, R.drawable.exercise};

        cardsAdvies = new ArrayList<>();

        rvAdvies = (RecyclerView) v.findViewById(R.id.rv_advies);

        LinearLayoutManager llm = new LinearLayoutManager(this.getActivity());
        rvAdvies.setLayoutManager(llm);

        adapterAdvies = new RVAdapterAdvies(cardsAdvies);
        rvAdvies.setAdapter(adapterAdvies);

        setSwipeListener();
        setScrollListener(v);

        updateView();

        return v;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        rvAdvies.scrollToPosition(0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_refresh) {
            // Do animation start
            Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_refresh);
            rotation.setRepeatCount(Animation.INFINITE);
            iv_refresh.startAnimation(rotation);

            if (AppFunctions.isNetworkAvailable(getActivity())) {
                new GetAdvicesTask(access_token, patientId).execute();
            } else {
                iv_refresh.clearAnimation();
                Toast.makeText(getActivity(), "Netwerk niet beschikbaar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GetAdvicesTask extends AsyncTask<Void, Void, JSONArray> {

        private String access_token;
        private int patientId = 0;

        public GetAdvicesTask(String access_token, int patientId) {
            this.access_token = access_token;
            this.patientId = patientId;
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://apexbackend.azurewebsites.net/api/advices/patient/"
                    + patientId);

            httpget.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
            httpget.setHeader("Authorization", "Bearer " + access_token);

            JSONArray json = new JSONArray();

            // Execute HTTP Get Request
            try {
                HttpResponse response = httpclient.execute(httpget);
                String json_string = EntityUtils.toString(response.getEntity());
                json = new JSONArray(json_string);
                System.out.println(json);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            if (jsonArray != null) {

                AdviesDataSource adviesDataSource = new AdviesDataSource(getActivity());
                adviesDataSource.open();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row;
                    int adviceId = 0;
                    String adviceTitle = null;
                    String adviceContent = null;
                    int adviceCategory = 0;

                    try {
                        row = jsonArray.getJSONObject(i);
                        adviceId = row.getInt("AdviceId");
                        adviceTitle = row.getString("AdviceTitle");
                        adviceContent = row.getString("AdviceContent");
                        adviceCategory = row.getInt("AdviceCategory");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (!adviesDataSource.adviceExistInDb(adviceId)) {
                        AdviesItem adviesItem = new AdviesItem();
                        adviesItem.setId(adviceId);
                        adviesItem.setTitel(adviceTitle);
                        adviesItem.setContent(adviceContent);
                        adviesItem.setCategoryId(adviceCategory);

                        adviesDataSource.createAdviesItem(adviesItem);
                    }
                }

                adviesDataSource.close();

                updateView();
            }
        }
    }

    private void updateView() {
        AdviesDataSource dataSource = new AdviesDataSource(getActivity());
        dataSource.open();

        List<AdviesItem> adviezen = dataSource.getAllAdviesItems();

        cardsAdvies.clear();

        for (int i = 0; i < adviezen.size(); i++) {
            AdviesItem advies = adviezen.get(i);
            long id = advies.getId();
            String title = advies.getTitel();
            String content = advies.getContent();
            long categoryId = advies.getCategoryId();
            CardItemAdvies card = new CardItemAdvies(id, title, content, drawables[(int) categoryId - 1]);
            cardsAdvies.add(card);
        }

        adapterAdvies.notifyDataSetChanged();
        iv_refresh.clearAnimation();

        dataSource.close();
    }

    private void setScrollListener(View v) {
        final View coloredBackgroundView = v.findViewById(R.id.colored_background_view_advies);

        rvAdvies.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
    }

    private void setSwipeListener() {
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(rvAdvies,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView,
                                                               int[] reverseSortedPositions) {
                                deleteAdvies(reverseSortedPositions);
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView,
                                                                int[] reverseSortedPositions) {
                                deleteAdvies(reverseSortedPositions);
                            }
                        });

        rvAdvies.addOnItemTouchListener(swipeTouchListener);
    }

    private void deleteAdvies(int[] reverseSortedPositions) {
        AdviesDataSource dataSource = new AdviesDataSource(getActivity());
        dataSource.open();

        for (int position : reverseSortedPositions) {
            CardItemAdvies card = cardsAdvies.get(position);
            long id = card.getId();
            AdviesItem item = new AdviesItem();
            item.setId(id);

            dataSource.deleteAdviesItem(item);

            cardsAdvies.remove(position);
            adapterAdvies.notifyItemRemoved(position);
        }

        dataSource.close();
        adapterAdvies.notifyDataSetChanged();
    }

}