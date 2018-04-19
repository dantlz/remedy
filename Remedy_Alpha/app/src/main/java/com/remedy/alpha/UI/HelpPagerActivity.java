package com.remedy.alpha.UI;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.remedy.alpha.R;

public class HelpPagerActivity extends AppCompatActivity {

    //Number of pages (steps) to be displayed
    private static final int NUM_PAGES = 3;

    //View pager instance
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_pager);
    }
}
