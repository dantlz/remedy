package com.remedy.alpha.UI;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.remedy.alpha.R;

public class QueueActivity extends AppCompatActivity {

    private String name;
    private String phoneNumber;
    private String notes;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_queue);

        name = getIntent().getStringExtra("NAME");
        phoneNumber = getIntent().getStringExtra("PHONE");
        notes = getIntent().getStringExtra("NOTES");
        type = getIntent().getStringExtra("TYPE");


        Handler handler = new Handler();
        Runnable response =  new Runnable() {
            public void run() {

                Intent intent;
                intent = new Intent(QueueActivity.this, ChatActivity.class);

                intent.putExtra("TYPE", type);
                intent.putExtra("NAME", name);
                intent.putExtra("PHONE", phoneNumber);
                intent.putExtra("NOTES", notes);
                if (type.equals("CALL"))
                    intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                startActivity(intent);
            }
        };
        if (type.equals("CHAT"))
            handler.postDelayed(response, 3000);
        if(type.equals("CALL")) {
            response.run();
        }
    }

    //TODO Move connections here


}
