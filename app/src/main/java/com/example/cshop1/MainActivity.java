  package com.example.cshop1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

  public class MainActivity extends AppCompatActivity {
    //views

    Button mRegisterbtn, mLoginbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the views that will be used
        mRegisterbtn = findViewById(R.id.registerbutton);
        mLoginbtn = findViewById(R.id.Loginbutton);

        //this piece of code will handle the registration button click
        mRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this piece of code will start the Register Activity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        //this piece of code will handle the login button click

        mLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this piece of code will start the Login Activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

    }
}