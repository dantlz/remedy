package com.remedy.alpha.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.remedy.alpha.R;

public class ProbDescActivity extends AppCompatActivity {

    // Views
    private Spinner probDescSpinner;
    private Button enterButton;

    private String selectedOption;

    //Google Analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prob_desc);

        probDescSpinner = (Spinner)findViewById(R.id.probDescSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.prob_desc_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        probDescSpinner.setAdapter(adapter);
        enterButton = (Button)findViewById(R.id.enterButton);
        AttachListeners();

        //Google Analytics: Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);


    }

    private void AttachListeners(){
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedOption.equals("Select an option")){
                    System.out.println("Please select an option!!!");
                } else {
                    System.out.println(selectedOption);
                    Intent intent = new Intent(ProbDescActivity.this, ContactTypeActivity.class);
                    startActivity(intent);
                }
            }
        });
        probDescSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
                selectedOption = (String)parent.getItemAtPosition(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
