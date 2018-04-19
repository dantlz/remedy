package com.remedy.alpha.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.remedy.alpha.R;

public class ContactInfoActivity extends AppCompatActivity {

    //Views
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText notesEditText;
    private Button doneButton;

    private String name;
    private String phoneNumber;
    private String notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        nameEditText = (EditText)findViewById(R.id.nameEditText);
        phoneEditText = (EditText)findViewById(R.id.phoneEditText);
        notesEditText = (EditText)findViewById(R.id.notesEditText);
        doneButton = (Button)findViewById(R.id.doneButton);

        AttachListeners();
    }

    private void AttachListeners(){
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                phoneNumber = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        notesEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                notes = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactInfoActivity.this, QueueActivity.class);
                intent.putExtra("TYPE", getIntent().getStringExtra("TYPE"));
                intent.putExtra("NAME", name);
                intent.putExtra("PHONE", phoneNumber);
                intent.putExtra("NOTES", notes);

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
