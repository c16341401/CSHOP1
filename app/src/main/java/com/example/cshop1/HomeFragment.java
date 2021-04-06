package com.example.cshop1;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    List<Listingmodel> listingmodelList;
    Listingadapter ladapter;



    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //init
        firebaseAuth = FirebaseAuth.getInstance();


        //initialize the recycler view and set its properties
        recyclerView = view.findViewById(R.id.listingrecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //this piece of code will display the latest listing first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set the layout to recycler view
        recyclerView.setLayoutManager(layoutManager);

        //initialize the post list
        listingmodelList = new ArrayList<>();
        
        
        loadListings();

        return view;
    }

    private void loadListings() {
        //path containing all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Listings");
        //get all the data from the database reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                listingmodelList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    Listingmodel modelisting = ds.getValue(Listingmodel.class);


                    listingmodelList.add(modelisting);

                    //initialize the adapter

                    ladapter = new Listingadapter(getActivity(), listingmodelList);

                    //set the adapter to the recyclerview

                    recyclerView.setAdapter(ladapter);
                }
            }

            @Override
            public void onCancelled( DatabaseError error) {
                //this will be used to process errors
                //Toast.makeText(getActivity(),"" + error.getMessage() ,Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void searchlistings(String searchquery)
    {

        //path containing all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Listings");
        //get all the data from the database reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                listingmodelList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    Listingmodel modelisting = ds.getValue(Listingmodel.class);

                    if (modelisting.getPcategory().toLowerCase().contains(searchquery.toLowerCase())||
                            modelisting.getPtitle().toLowerCase().contains(searchquery.toLowerCase())||
                            modelisting.getPdescr().toLowerCase().contains(searchquery.toLowerCase()))
                    {
                        listingmodelList.add(modelisting);
                    }
                    //initialize the adapter

                    ladapter = new Listingadapter(getActivity(), listingmodelList);

                    //set the adapter to the recyclerview

                    recyclerView.setAdapter(ladapter);
                }
            }

            @Override
            public void onCancelled( DatabaseError error) {
                //this will be used to process errors
                Toast.makeText(getActivity(),"" + error.getMessage() ,Toast.LENGTH_SHORT).show();
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

        }
        else
        {
            //user is not signed in go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //inflate menu
        inflater.inflate(R.menu.main_menu, menu);

        //searchview to search the listings by listing title, description or category
        MenuItem item = menu.findItem(R.id.search_action);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                //this is called when the user presses the search button
                if(!TextUtils.isEmpty(query))
                {
                    searchlistings(query);
                }
                else
                {
                    loadListings();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //this will be called any time the device detects a change
                if(!TextUtils.isEmpty(newText))
                {
                    searchlistings(newText);
                }
                else
                {
                    loadListings();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    //this block of code will handle the menu item clicks

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        //get the selected items id
        int id = item.getItemId();
        if(id == R.id.logout_action)
        {
            firebaseAuth.signOut();
            checkuserloggedin();
        }
        if(id == R.id.publish_listing_action)
        {
            startActivity(new Intent(getActivity(),postforsaleActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}