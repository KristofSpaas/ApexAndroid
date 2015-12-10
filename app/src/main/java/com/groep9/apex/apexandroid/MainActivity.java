package com.groep9.apex.apexandroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.groep9.apex.apexandroid.Advies.AdviesFragment;
import com.groep9.apex.apexandroid.DrawerMenu.DrawerMenuItem;
import com.groep9.apex.apexandroid.DrawerMenu.DrawerMenuItemAdapter;
import com.groep9.apex.apexandroid.Instellingen.InstellingenFragment;
import com.groep9.apex.apexandroid.MedischeInfo.MedischeInfoFragment;
import com.groep9.apex.apexandroid.Services.AlarmManagerBroadcastReceiver;
import com.groep9.apex.apexandroid.Start.StartFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mLvDrawerMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLvDrawerMenu = (ListView) findViewById(R.id.lv_drawer_menu);

        List<DrawerMenuItem> menuItems = generateDrawerMenuItems();
        DrawerMenuItemAdapter mDrawerMenuAdapter = new DrawerMenuItemAdapter(getApplicationContext(), menuItems);
        mLvDrawerMenu.setAdapter(mDrawerMenuAdapter);

        mLvDrawerMenu.setOnItemClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            setFragment(0, StartFragment.class);
        }

        setAlarmManager(getBaseContext());
    }

    private void setAlarmManager(Context context) {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);

        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

            //After every 60 seconds
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 600000, pi);
        } else {
            System.out.println("Alarm is already set");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 4) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (position != 5) {
            ImageView ivRefresh = (ImageView) findViewById(R.id.iv_refresh);
            ivRefresh.setVisibility(View.GONE);
        }

        switch (position) {
            case 1:
                setFragment(0, StartFragment.class);
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                setFragment(0, MedischeInfoFragment.class);
                break;
            case 5:
                setFragment(0, AdviesFragment.class);
                break;
            case 6:
                break;
            case 7:
                setFragment(0, InstellingenFragment.class);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mLvDrawerMenu)) {
            mDrawerLayout.closeDrawer(mLvDrawerMenu);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void setFragment(final int position, Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment, fragmentClass.getSimpleName());
            fragmentTransaction.commit();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLvDrawerMenu.setItemChecked(position, true);
                    mDrawerLayout.closeDrawer(mLvDrawerMenu);
                    mLvDrawerMenu.invalidateViews();
                }
            }, 50);
        } catch (Exception ex) {
            Log.e("setFragment", ex.getMessage());
        }
    }

    private List<DrawerMenuItem> generateDrawerMenuItems() {
        String[] itemsText = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray itemsIcon = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        List<DrawerMenuItem> result = new ArrayList<>();
        for (int i = 0; i < itemsText.length; i++) {
            DrawerMenuItem item = new DrawerMenuItem();
            item.setText(itemsText[i]);
            item.setIcon(itemsIcon.getResourceId(i, -1));
            result.add(item);
        }
        return result;
    }
}
