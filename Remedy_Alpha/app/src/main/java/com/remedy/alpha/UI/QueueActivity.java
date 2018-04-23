package com.remedy.alpha.UI;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.remedy.alpha.R;
import com.wang.avi.AVLoadingIndicatorView;

public class QueueActivity extends AppCompatActivity {

    private String name;
    private String phoneNumber;
    private String notes;
    private String type;

    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        name = getIntent().getStringExtra("NAME");
        phoneNumber = getIntent().getStringExtra("PHONE");
        notes = getIntent().getStringExtra("NOTES");
        type = getIntent().getStringExtra("TYPE");

        avi = (AVLoadingIndicatorView) findViewById(R.id.loading_indicator);


        if (type.equals("CHAT"))
            startAnim();
            Handler handler = new Handler();
            Runnable response =  new Runnable() {
                public void run() {

                    Intent intent;
                    intent = new Intent(QueueActivity.this, ChatActivity.class);

                    intent.putExtra("TYPE", type);
                    intent.putExtra("NAME", name);
                    intent.putExtra("PHONE", phoneNumber);
                    intent.putExtra("NOTES", notes);
                    startActivity(intent);
                }
            };
            handler.postDelayed(response, 3000);
        if(type.equals("CALL")) {
            startAnim();
        }
    }

    void startAnim(){
        avi.show();
        // or avi.smoothToShow();
    }

    //TODO Move connections here


}
