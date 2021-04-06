package com.example.cshop1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashBActivity extends AppCompatActivity {

    //Create Firebase auth

    FirebaseAuth firebaseAuth;




    //views
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashb);

        //create action bar along with the action bars title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //in this piece of code i initialized the fire base auth
        firebaseAuth = FirebaseAuth.getInstance();

        //initialise all the views

        //bottom nav bar
        BottomNavigationView navigationView = findViewById(R.id.mnavigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //this block of code is used to set the home fragment as the default fragment on start
        actionBar.setTitle("Home");//this line of code is used to change the actionbar title
        HomeFragment hfragment = new HomeFragment();
        FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
        fragmentTransaction1.replace(R.id.content, hfragment, "");
        fragmentTransaction1.commit();

    }


    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //handle item clicks
            switch (item.getItemId())
            {
                case R.id.nav_home:
                    //home fragment transaction
                    actionBar.setTitle("Home");
                    HomeFragment hfragment = new HomeFragment();
                    FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction1.replace(R.id.content, hfragment, "");
                    fragmentTransaction1.commit();
                    return true;
                case R.id.nav_profile:
                    //profile fragment transaction
                    actionBar.setTitle("Profile");
                    ProfileFragment pfragment = new ProfileFragment();
                    FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction2.replace(R.id.content, pfragment, "");
                    fragmentTransaction2.commit();
                    return true;
                case R.id.nav_users:
                    //users fragment transaction
                    actionBar.setTitle("Users");
                    UsersFragment ufragment = new UsersFragment();
                    FragmentTransaction fragmentTransaction3 = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction3.replace(R.id.content, ufragment, "");
                    fragmentTransaction3.commit();
                    return true;
            }

            return false;
        }
    };



    private void checkuserloggedin()
    {
        //with the piece of code below i am able to retrieve the current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //check if there is user signed in
        if (user != null)
        {
            //user is signed in stay here
            //set email logged in user

        }
        else
        {
            //user is not signed in go to main activity
            startActivity(new Intent(DashBActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        //check the user sign in at the start of the app
        checkuserloggedin();
        super.onStart();
    }
}