package com.remedy.alpha.UI;

import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.remedy.alpha.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        //IBM Sentiment Analysis
        ToneAnalyzer service = new ToneAnalyzer("2017-09-21");
        service.setUsernameAndPassword("3d35e35a-6ade-4659-961a-a0a39bb34340", "vin2ozD8qTtT");
        String text = "I know the times are difficult! Our sales have been "
                + "disappointing for the past three quarters for our data analytics "
                + "product suite. We have a competitive data analytics product "
                + "suite in the industry. But we need to do our job selling it! "
                + "We need to acknowledge and fix our sales challenges. "
                + "We can’t blame the economy for our lack of execution! " + "We are missing critical sales opportunities. "
                + "Our product is in no way inferior to the competitor products. "
                + "Our clients are hungry for analytical tools to improve their "
                + "business outcomes. Economy has nothing to do with it.";

        // Call the service and get the tone
        new SentimentAnalysis().execute(new String[]{text});
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

    private class SentimentAnalysis extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... textToAnalyze) {
            ToneAnalyzer service = new ToneAnalyzer("2017-09-21");
            service.setUsernameAndPassword("3d35e35a-6ade-4659-961a-a0a39bb34340", "vin2ozD8qTtT");

            ToneOptions toneOptions = new ToneOptions.Builder()
                    .text(textToAnalyze[0])
                    .build();
            ToneAnalysis tone = service.tone(toneOptions).execute();

            return tone.toString();
        }

        @Override
        protected void onPostExecute(String response) {
            //Log.d("wxh", response);

            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.has("document_tone")){
                    JSONObject data = jsonObject.getJSONObject("document_tone");
                    if(data.has("tones")){
                        JSONArray jsonArray = data.getJSONArray("tones");
                        JSONObject firstElement = jsonArray.getJSONObject(0);
                        if(firstElement.has("score") && firstElement.has("tone_name")){
                            String type = firstElement.getString("tone_name");
                            Double score = firstElement.getDouble("score");
                            Log.d("wxh", type + " score: " + String.valueOf(score));
                        }

                        JSONObject secondElement = jsonArray.getJSONObject(1);
                        if(secondElement.has("score") && secondElement.has("tone_name")){
                            String type = secondElement.getString("tone_name");
                            Double score = secondElement.getDouble("score");
                            Log.d("wxh", type + " score: " + String.valueOf(score));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}

