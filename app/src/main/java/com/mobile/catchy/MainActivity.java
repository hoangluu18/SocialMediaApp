package com.mobile.catchy;

import static com.mobile.catchy.utils.Constants.PREF_DIRECTORY;
import static com.mobile.catchy.utils.Constants.PREF_NAME;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mobile.catchy.adapter.ViewPagerAdapter;
import com.mobile.catchy.fragments.Search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity implements Search.OnDataPass {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    ViewPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        addTabs();

    }

    private void addTabs() {
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_home));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_search));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_add));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_heart));

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String directory = preferences.getString(PREF_DIRECTORY, "");

        Bitmap bitmap = loadProfileImage(directory);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

        tabLayout.addTab(tabLayout.newTab().setIcon(drawable));
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        pagerAdapter  = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_fill);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                IS_SEARCHED_USER = false;
                viewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.ic_home_fill);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.ic_search);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.ic_add);
                        break;
                    case 3:
                        tab.setIcon(R.drawable.ic_heart_fill);
                        break;
//                    case 4:
//                        tab.setIcon(R.drawable.baseline_person_24);
//                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.ic_home);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.ic_search);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.ic_add);
                        break;
                    case 3:
                        tab.setIcon(R.drawable.ic_heart);
                        break;
//                    case 4:
//                        tab.setIcon(R.drawable.baseline_person_24);
//                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {


                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.ic_home_fill);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.ic_search);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.ic_add);
                        break;
                    case 3:
                        tab.setIcon(R.drawable.ic_heart_fill);
                        break;
//                    case 4:
//                        tab.setIcon(R.drawable.baseline_person_24);
//                        break;
                }
            }
        });
    }

    private void init(){
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
    }


    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem() == 4){
            viewPager.setCurrentItem(0);
            IS_SEARCHED_USER = false;
        }
        else
            super.onBackPressed();
    }
    public static String USER_ID;
    public static boolean IS_SEARCHED_USER = false;
    @Override
    public void onChange(String uid) {
        USER_ID = uid;
        IS_SEARCHED_USER = true;
        viewPager.setCurrentItem(4);
    }


    private Bitmap loadProfileImage(String directory) {

        try {
            File file = new File(directory, "profile.png");

            return BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }



}