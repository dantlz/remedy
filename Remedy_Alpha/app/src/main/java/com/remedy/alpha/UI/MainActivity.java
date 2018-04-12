package com.remedy.alpha.UI;

import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.remedy.alpha.R;
import com.robohorse.pagerbullet.PagerBullet;

public class MainActivity extends AppCompatActivity {
    private PagerBullet pagerBullet;
    private PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pagerBullet = (PagerBullet) findViewById(R.id.pager_bullet);
        adapter = new com.remedy.alpha.Support.PagerAdapter(this);
        pagerBullet.setAdapter(adapter);
    }
}
