package com.example.cshop1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TheirProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    //views created to correspond to xml file
    ImageView profilepic,coverimage;
    TextView pname, pemail, pphone;

    RecyclerView listingrecyclerview;

    List<Listingmodel> listingmodelList;
    Listingadapter listingadapter;
    String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_their_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        profilepic = findViewById(R.id.profilepicic);
        coverimage = findViewById(R.id.coverimage);
        pname = findViewById(R.id.pnametv);
        pemail = findViewById(R.id.pemailtv);
        pphone = findViewById(R.id.pphonetv);
        listingrecyclerview = findViewById(R.id.userslistingsrv);

        firebaseAuth = FirebaseAuth.getInstance();

        //this piece of code will get the id of the clicked user
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");



        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //run check until required data is retreived
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cimage = "" + ds.child("cover").getValue();

                    //set data
                    pname.setText(name);
                    pemail.setText(email);
                    pphone.setText(phone);

                    try
                    {
                        //try to load the image
                        Picasso.get().load(image).into(profilepic);
                    }
                    catch (Exception e)
                    {
                        //if image cannot be loaded this piece of code will set a default image to be displayed
                        Picasso.get().load(R.drawable.default_img).into(profilepic);
                    }

                    try
                    {
                        //try to load the image
                        Picasso.get().load(cimage).into(coverimage);
                    }
                    catch (Exception e)
                    {
                        //if image cannot be loaded this piece of code will set a default image to be displayed
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        listingmodelList = new ArrayList<>();

        checkuserloggedin();
        loadotheruserslistings();

    }

    private void loadotheruserslistings() {
        //this will be the linear layout for the listings recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show the newest listing first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //this piece of code sets the layout to the recycler view
        listingrecyclerview.setLayoutManager(layoutManager);

        //initialize the list of posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Listings");
        //this will be the query used to load the users listings
        Query query = ref.orderByChild("uid").equalTo(uid);
        //this block of code will retrieve the dat from the ref variable
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listingmodelList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    Listingmodel luserslistings = ds.getValue(Listingmodel.class);

                    //add to the list

                    listingmodelList.add(luserslistings);

                    //create the adapter
                    listingadapter = new Listingadapter(TheirProfileActivity.this, listingmodelList);
                    //this piece of code will set the adapter to the recycler view
                    listingrecyclerview.setAdapter(listingadapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(TheirProfileActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchotheruserslistings(String sq)
    {

        //this will be the linear layout for the listings recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(TheirProfileActivity.this);
        //show the newest listing first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //this piece of code sets the layout to the recycler view
        listingrecyclerview.setLayoutManager(layoutManager);

        //initialize the list of posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Listings");
        //this will be the query used to load the users listings
        Query query = ref.orderByChild("uid").equalTo(uid);
        //this block of code will retrieve the dat from the ref variable
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listingmodelList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    Listingmodel luserslistings = ds.getValue(Listingmodel.class);



                    if(luserslistings.getPtitle().toLowerCase().contains(sq.toLowerCase()) || luserslistings.getPdescr().toLowerCase().contains(sq.toLowerCase()) || luserslistings.getPcategory().toLowerCase().contains(sq.toLowerCase()))
                    {
                        //add to the list

                        listingmodelList.add(luserslistings);
                    }

                    //create the adapter
                    listingadapter = new Listingadapter(TheirProfileActivity.this, listingmodelList);
                    //this piece of code will set the adapter to the recycler view
                    listingrecyclerview.setAdapter(listingadapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(TheirProfileActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.publish_listing_action).setVisible(false);


        MenuItem item = menu.findItem(R.id.search_action);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //this method is called when the user presses the search button
                if (!TextUtils.isEmpty(query))
                {
                    searchotheruserslistings(query);
                }
                else
                {
                    loadotheruserslistings();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //this method is called when ever the system detects a change in the text that the user has typed

                if (!TextUtils.isEmpty(newText))
                {
                    searchotheruserslistings(newText);
                }
                else
                {
                    loadotheruserslistings();
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.logout_action)
        {
            firebaseAuth.signOut();
            checkuserloggedin();
        }

        return super.onOptionsItemSelected(item);
    }
}