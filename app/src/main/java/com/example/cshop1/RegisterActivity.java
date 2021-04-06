package com.example.cshop1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText mEmailet, mPasswordet;
    Button mRegisterbtn;
    TextView mHaveaccountalreadytv;

    //create progressbar to display when registering the user

    ProgressDialog progressDialog;

    //declare an instance of firebaseAUTH
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //create action bar along with the action bars title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // initialize the views that will be used
        mEmailet = findViewById(R.id.emailet);
        mPasswordet = findViewById(R.id.passwordet);
        mRegisterbtn = findViewById(R.id.Rregisterbtn);
        mHaveaccountalreadytv = findViewById(R.id.haveaccountalreadytv);

        //initialize the firebase instance.
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering your account");

        //handle the register btn click
        mRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email/password
                String email = mEmailet.getText().toString().trim();
                String pass = mPasswordet.getText().toString().trim();
                //handle validation
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    //set error message focused on the incorrect email being entered
                    mEmailet.setError("Invalid Email");
                    mEmailet.setFocusable(true);

                }
                else if (pass.length()<6)
                {
                    //set error message focused on the invalid password
                    mEmailet.setError("Invalid Email");
                    mEmailet.setFocusable(true);

                }
                else
                {
                    registerUser(email,pass);//register the user
                }
            }
        });
        //this code handles the login text view
        mHaveaccountalreadytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    private void registerUser(String email, String pass)
    {
        //email and password is valid, show the progress dialog
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            //if the sign in is correct update the UI with the signed in users information
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            //get the users id and email from the auth
                            String email = user.getEmail();
                            String userid = user.getUid();

                            //when the user is completely registered store the users info in the realtime firebase database aswell
                            HashMap<Object, String> hashMap = new HashMap<>();
                            //this piece of code will put information into the hashmap
                            hashMap.put("email", email);
                            hashMap.put("uid", userid);
                            hashMap.put("name", "");//aadd later
                            hashMap.put("onlinestatus", "online");//aadd later
                            hashMap.put("phone", "");//add later
                            hashMap.put("image", "");//add later
                            hashMap.put("cover", "");//add later



                            //this line of code creates a firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //this is the path where the users data will be stored called "Users"
                            DatabaseReference reference = database.getReference("Users");
                            //this code will put the data from the hashmap into the database
                            reference.child(userid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashBActivity.class));
                            finish();
                        }
                        else
                        {
                            //if sign in fails display a message to the user
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dissmiss progress bar and show error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onSupportNavigateUp()
    {
        onBackPressed();//go to previous activity
        return super.onSupportNavigateUp();
    }
}