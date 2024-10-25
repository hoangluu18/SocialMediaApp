package com.mobile.catchy;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mobile.catchy.adapter.ViewPagerAdapter;
import com.mobile.catchy.fragments.Search;

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
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.baseline_person_24));

        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        pagerAdapter  = new ViewPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_fill);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
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
                    case 4:
                        tab.setIcon(R.drawable.baseline_person_24);
                        break;
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
                    case 4:
                        tab.setIcon(R.drawable.baseline_person_24);
                        break;
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
                    case 4:
                        tab.setIcon(R.drawable.baseline_person_24);
                        break;
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
    public void onChange(int position) {
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onBackPressed() {
        if(viewPager.getCurrentItem() == 4){
            viewPager.setCurrentItem(0);
        }
        else
            super.onBackPressed();
    }
}