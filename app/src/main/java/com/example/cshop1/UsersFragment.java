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


public class UsersFragment extends Fragment {

    //Create Firebase auth

    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    Useradapter userAd;
    List<Usermodel> userlist;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        //initialize recycler view
        recyclerView = view.findViewById(R.id.user_recyclerview);
        //set the properties for the recycler view
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        //initialize the user list
        userlist = new ArrayList<>();

        getallusers();

        return view;
    }

    private void getallusers() {
        //get the current user
        FirebaseUser cuser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of the database called "users" that has the users info stored inside
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //grab all the data from that path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userlist.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    Usermodel um = ds.getValue(Usermodel.class);

                    //get all users except the one that is currently signed in
                    if (!um.getUid().equals(cuser.getUid()))
                    {
                        userlist.add(um);
                    }
                    //adapter
                    userAd = new Useradapter(getActivity(),userlist);
                    //set the adapter to recycler view
                    recyclerView.setAdapter(userAd);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchusers(String newText) {
        //get the current user
        FirebaseUser cuser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of the database called "users" that has the users info stored inside
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //grab all the data from that path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userlist.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    Usermodel um = ds.getValue(Usermodel.class);




                    //get all users that were searched except the one that is currently signed in
                    if (!um.getUid().equals(cuser.getUid()))
                    {
                        if(um.getName().toLowerCase().contains(newText.toLowerCase())||um.getEmail().toLowerCase().contains(newText.toLowerCase()))
                        {
                            userlist.add(um);
                        }
                    }
                    //adapter
                    userAd = new Useradapter(getActivity(),userlist);
                    //refresh the adapter
                    userAd.notifyDataSetChanged();
                    //set the adapter to recycler view
                    recyclerView.setAdapter(userAd);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        //this piece of code will hide the publish listing icon from this fragment
        menu.findItem(R.id.publish_listing_action).setVisible(false);

        //search bar
        MenuItem item = menu.findItem(R.id.search_action);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search view listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //this is called when the user presses the search button on their keyboard
                //if the user entered something then search that phrase

                if (!TextUtils.isEmpty(query.trim()))
                {
                    searchusers(query);
                }
                else
                {
                    getallusers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //this is called when the user presses the search button on their keyboard
                //if the user entered something then search that phrase

                if (!TextUtils.isEmpty(newText.trim()))
                {
                    searchusers(newText);
                }
                else
                {
                    getallusers();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get the selected items id
        int id = item.getItemId();
        if(id == R.id.logout_action)
        {
            firebaseAuth.signOut();
            checkuserloggedin();
        }

        return super.onOptionsItemSelected(item);
    }
}