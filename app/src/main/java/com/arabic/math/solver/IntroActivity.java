package com.arabic.math.solver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class IntroActivity extends AppCompatActivity {
    private Button next;
    private Button previous;
    private ViewPager viewPager;
    private final String INTRO_PREF_KEY = "intro";
    private final String FIRST_TIME_PREF_KEY = "firstTime ?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkFirstTime();
        setContentView(R.layout.activity_intro);
        next = findViewById(R.id.button1);
        previous = findViewById(R.id.button2);
        viewPager = findViewById(R.id.viewPager);
        int[] layouts = {
                R.layout.intro_page,
                R.layout.first,
                R.layout.second
        };

        viewPager.setAdapter(new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(layouts[position], container, false);
                container.addView(view);
                return view;
            }

            @Override
            public int getCount() {
                return layouts.length;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                View view = (View) object;
                container.removeView(view);

            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                Log.e(TAG,"onPageSelected: "+position);
                if (position == 2) {
                    next.setText("Start");
                } else {
                    next.setText("next");
                    previous.setText("previous");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        next.setOnClickListener(view -> {
            int current = viewPager.getCurrentItem();
            if (current < layouts.length - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                launchDashBoard();
            }
        });
        previous.setOnClickListener(view -> {
            int current = viewPager.getCurrentItem();
            if (current > 0) {
                viewPager.setCurrentItem(current - 1);
            }
        });
    }

    private void checkFirstTime() {
        SharedPreferences sharedpreferences = getSharedPreferences(INTRO_PREF_KEY, Context.MODE_PRIVATE);
        if (sharedpreferences.contains(FIRST_TIME_PREF_KEY))
            launchDashBoard();
        else {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(FIRST_TIME_PREF_KEY, true);
            editor.apply();
        }
    }

    private void launchDashBoard() {
        startActivity(new Intent(IntroActivity.this, MainActivity.class));
        finish();
    }
}