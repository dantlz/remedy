package com.remedy.alpha.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.remedy.alpha.R;

public class ContactTypeActivity extends AppCompatActivity {

    //Views
    private Button callButton;
    private Button chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_type);

        callButton = (Button)findViewById(R.id.callButton);
        chatButton = (Button)findViewById(R.id.chatButton);

        AttachListeners();
    }

    private void AttachListeners(){
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactTypeActivity.this, ContactInfoActivity.class);
                intent.putExtra("TYPE", "CALL");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactTypeActivity.this, ContactInfoActivity.class);
                intent.putExtra("TYPE", "CHAT");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
