package com.example.cshop1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChattingActivity extends AppCompatActivity {

    //views from the xml
    androidx.appcompat.widget.Toolbar tb;
    RecyclerView rc;
    ImageView ppiciv;
    TextView nametv, userstatustv;
    EditText messageET;
    ImageButton sendbtn;

    //firebase auth
    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference userdbref;

    //this will be used to check if the user has seen the message or not

    ValueEventListener seenlistener;
    DatabaseReference refforuserseen;

    List<chatmodel> chatlist;
    chatadapter chatad;



    String hisUid;
    String userID;
    String hispimage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        //initialize the views
        tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setTitle("");
        rc =findViewById(R.id.chatrecyclerview);
        ppiciv = findViewById(R.id.profilepicchat);
        nametv = findViewById(R.id.namechattv);
        userstatustv = findViewById(R.id.useronlinestatus);
        messageET = findViewById(R.id.messageet);
        sendbtn = findViewById(R.id.sendbtn);

        //linear layout for the recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recycler view properties
        rc.setHasFixedSize(true);
        rc.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUID");

        //initialize the firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userdbref = firebaseDatabase.getReference("Users");

        //search user to get that users info
        Query userQuery = userdbref.orderByChild("uid").equalTo(hisUid);
        //grab the users picture and their name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //this block of code will run the check until the required picture and name is
                //received
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    //grab the data
                    String name = ""+ds.child("name").getValue();
                    hispimage  = ""+ds.child("image").getValue();

                    //this line of code will get the users online status
                    String onlinestat = ""+ds.child("onlinestatus").getValue();

                    if(onlinestat.equals("online"))
                    {
                        userstatustv.setText(onlinestat);

                    }
                    else
                    {
                        //this piece of code will convert the timestamp to a useable format

                        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                        calendar.setTimeInMillis(Long.parseLong(onlinestat));
                        String dateTime = DateFormat.format("dd/MM/yyyy HH:MM AA", calendar).toString();

                        userstatustv.setText("Last seen at: "+dateTime);
                    }
                    //set data
                    nametv.setText(name);

                    try
                    {
                        //image is recieved,set it to image view inside the tool bar
                        Picasso.get().load(hispimage).placeholder(R.drawable.default_img).into(ppiciv);

                    }
                    catch(Exception e)
                    {
                        //error getting the picture set default picture
                        Picasso.get().load(R.drawable.default_img).into(ppiciv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //when the button is clicked send message
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the text from the edit text view
                String message = messageET.getText().toString().trim();
                //check if text is filled in
                if (TextUtils.isEmpty(message))
                {
                    //text empty
                    Toast.makeText(ChattingActivity.this,"Cannot send the empty message... ",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendMessage(message);
                }
            }
        });

        readmessages();
        seenmessage();

    }

    private void seenmessage() {
        refforuserseen = FirebaseDatabase.getInstance().getReference("Chats");
        seenlistener = refforuserseen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    chatmodel chat = ds.getValue(chatmodel.class);
                    if (chat.getReceiver().equals(userID)&&chat.getSender().equals(hisUid))
                    {
                        HashMap<String, Object> seenhashmap = new HashMap<>();
                        seenhashmap.put("isseen", true);
                        ds.getRef().updateChildren(seenhashmap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readmessages() {
        chatlist = new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlist.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    chatmodel chat = ds.getValue(chatmodel.class);
                    if(chat.getReceiver().equals(userID) && chat.getSender().equals(hisUid)||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(userID))
                    {
                        chatlist.add(chat);
                    }

                    //adapter initializing
                    chatad = new chatadapter(ChattingActivity.this,chatlist,hispimage);
                    chatad.notifyDataSetChanged();

                    //connect the adapter to the recycler view
                    rc.setAdapter(chatad);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String mess) {
        //this block of code will create a chat node in the firbase database
        //whenever the user sends a message a child of the chat node will be created
        //this child contains the sender uid the receiver uid and the message


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", userID);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", mess);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isseen", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        //reset the edit text after the message has sent
        messageET.setText("");

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
            userID = user.getUid();

        }
        else
        {
            //user is not signed in go to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkuseronline(String online)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlinestatus",online);

        //this line of code updates the user online status in the realtime firebase data base
        databaseReference.updateChildren(hashMap);
    }

    protected void onStart()
    {
        checkuserloggedin();
        checkuseronline("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //this block of code will get the time stamp
        //and use this to set the offline time of the user as their last seen time stamp

        String timestamp = String.valueOf(System.currentTimeMillis());
        checkuseronline(timestamp);
        refforuserseen.removeEventListener(seenlistener);
    }

    @Override
    protected void onResume() {

        checkuseronline("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        //hide search view cuz it is not needed in this activity
        menu.findItem(R.id.search_action).setVisible(false);
        menu.findItem(R.id.publish_listing_action).setVisible(false);

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