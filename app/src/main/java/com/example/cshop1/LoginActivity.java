package com.example.cshop1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN =100 ;
    GoogleSignInClient mGoogleSignInClient;

    //views
    EditText lEmailet, lPasswordet;
    Button loginbtn;
    TextView donthaveaccounttv,lrecoverpass;
    SignInButton lgoogleloginbtn;


    //declare an instance of firebase

    private FirebaseAuth mAuth;

    //progress dialog bar
    ProgressDialog progressdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //create action bar along with the action bars title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        //initialize the firebase instance
        mAuth = FirebaseAuth.getInstance();

        //inititialize the views
        lEmailet = findViewById(R.id.lemailet);
        lPasswordet = findViewById(R.id.lpasswordet);
        donthaveaccounttv = findViewById(R.id.donothaveaccounttv);
        lrecoverpass = findViewById(R.id.recoverpasstv);
        loginbtn = findViewById(R.id.Loginbtn);
        lgoogleloginbtn = findViewById(R.id.googlesignin);

        //this piece of code sets the click listener for the login button

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //take data input from user
                String email = lEmailet.getText().toString();
                String pass = lPasswordet.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    //invalid email pattern is detected so set the error
                    lEmailet.setError("Invalid Email");
                    lEmailet.setFocusable(true);
                }
                else
                {
                    //valid email address entered
                    loguserin(email,pass);
                }

            }
        });

        //this piece of code sets the on click listener for the do not have account link

        donthaveaccounttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        //this block of code will handle the recover password textview click
        lrecoverpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverpassdialog();
            }
        });

        //this block of code is used to handle the login button click
        lgoogleloginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start the login process for google
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //initialize the progress dialog bar
        progressdialog = new ProgressDialog(this);
    }

    private void showRecoverpassdialog()
    {
        //alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //set linear layout
        LinearLayout linearLayout = new LinearLayout(this);
        //views that will be used in this dialog
        EditText Remailet = new EditText(this);
        Remailet.setHint("Email");
        Remailet.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        Remailet.setMinEms(20);


        linearLayout.addView(Remailet);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //recover button
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //take in users email as an input
                String remail = Remailet.getText().toString().trim();
                beginpassrecovery(remail);
            }
        });

        //cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //this piece of code will be used to dismiss the recover pass dialog
                dialog.dismiss();
            }
        });

        //show the dialog
        builder.create().show();

    }

    private void beginpassrecovery(String remail)
    {
        //show the loading progress
        progressdialog.setMessage("Sending recovery email...");
        progressdialog.show();

        mAuth.sendPasswordResetEmail(remail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressdialog.dismiss();
                if(task.isSuccessful())
                {
                    Toast.makeText(LoginActivity.this,"Email sent",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Failed...",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressdialog.dismiss();
                //get and display the proper error message to the user
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loguserin(String email, String pass)
    {
        //show the loading progress
        progressdialog.setMessage("Logging you in...");
        progressdialog.show();
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            //dissmiss the loading bar
                            progressdialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // since the user has successfully logged in this piece of code will start the profile activity
                            startActivity(new Intent(LoginActivity.this, DashBActivity.class));
                            finish();
                        }
                        else
                            {
                            //dissmiss the loading bar
                            progressdialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dissmiss the loading bar
                progressdialog.dismiss();
                //get and display error message
                Toast.makeText(LoginActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onSupportNavigateUp()
    {
        onBackPressed();//go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //if this is the first time the user is signing in with google then get and
                            //show the users google account information

                            if (task.getResult().getAdditionalUserInfo().isNewUser())
                            {
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
                            }


                            //show toast message along with the users email
                            Toast.makeText(LoginActivity.this,""+user.getEmail() , Toast.LENGTH_SHORT).show();
                            //update the ui
                            startActivity(new Intent(LoginActivity.this, DashBActivity.class));
                            finish();
                        } else {

                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"Login has Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show the user the error message
                Toast.makeText(LoginActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}