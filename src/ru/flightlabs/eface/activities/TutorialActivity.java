package ru.flightlabs.eface.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.viewpagerindicator.CirclePageIndicator;

import ru.flightlabs.eface.R;
import ru.flightlabs.eface.adapters.TutorialViewPagerAdapter;

/**
 * Created by sov on 24.01.2016.
 */
public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);

        final TutorialActivity act = this;
        final View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.onBackPressed();
            }
        });
        TutorialViewPagerAdapter viewPagerAdapter = new TutorialViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 5) {
                    skip.setVisibility(View.INVISIBLE);
                } else {
                    skip.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        CirclePageIndicator circleIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);
    }
}
