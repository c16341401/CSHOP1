package com.example.cshop1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class postforsaleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    FirebaseAuth firebaseAuth;
    DatabaseReference udbref;

    ActionBar actionBar;
    String catselected;

    //permision constants
    private  static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constants
    private  static final int IMAGE_PICK_CAMERA_CODE = 300;
    private  static final int IMAGE_PICK_GALLERY_CODE = 400;


    //permissions array
    String[] camerapermissions;
    String[] storagepermissions;

    //views
    Spinner catspinner;
    EditText titleet, descriptionet,price;
    ImageView listingimage;
    Button publishlist;

    //user info
    String name,email,uid,dp;


    //uri for image picked
    Uri iamge_rui = null;

    //progress bar
    ProgressDialog progressDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postforsale);


        actionBar = getSupportActionBar();
        actionBar.setTitle("Publish listing");
        //this block of code enables a back button in the action bar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //initialize the permissions array
        camerapermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        checkuserloggedin();


        actionBar.setSubtitle(email);

        //get some the logged in users info to include in the listing

        udbref = FirebaseDatabase.getInstance().getReference("Users");
        Query query = udbref.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds: snapshot.getChildren())
                {
                    name = ""+ ds.child("name").getValue();
                    email = ""+ ds.child("email").getValue();
                    dp = ""+ ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //initialize the views

        titleet = findViewById(R.id.listingtitleet);
        descriptionet = findViewById(R.id.listingdescription);
        listingimage = findViewById(R.id.listingimageiv);
        price = findViewById(R.id.listingpriceet);
        publishlist = findViewById(R.id.publishlistingbtn);


        //initialize the categories spinner
        catspinner = (Spinner) findViewById(R.id.categories);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(postforsaleActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.listingcategories));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catspinner.setAdapter(myAdapter);
        catspinner.setOnItemSelectedListener(this);

        //get image from the users camera or the gallery when clicked
        listingimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog to pic image
                showimagepickdialog();
            }
        });

        //publish listing click listener
        publishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get the data from the edit texts
                String title = titleet.getText().toString().trim();
                String desc = descriptionet.getText().toString().trim();
                String itemprice = price.getText().toString().trim();

                if (TextUtils.isEmpty(title))
                {
                    Toast.makeText(postforsaleActivity.this,"A title for the listing is needed",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(desc))

                {
                    Toast.makeText(postforsaleActivity.this,"A description for the listing is needed",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(itemprice))
                {
                    Toast.makeText(postforsaleActivity.this,"A price for the listing is needed",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (iamge_rui == null || catselected == null)
                {
                    //dont post show message
                    Toast.makeText(postforsaleActivity.this,"An image and a category need to be selected",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //post the listing
                    uploadlisting(title,desc,String.valueOf(iamge_rui),catselected,itemprice);
                }

            }
        });
    }

    private void uploadlisting(String title, String desc, String imageuri, String catselected, String itemprice) {
        progressDialog.setMessage("Publishing your listing");
        progressDialog.show();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String Filepathandname = "Listings/" + "listing_" + timestamp;

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(Filepathandname);
        ref.putFile(Uri.parse(imageuri))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is posted to firebase now get its uri
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if(uriTask.isSuccessful())
                        {
                            //uri was received correctly now upload post to firebase database

                            HashMap<Object, String> hashMap = new HashMap<>();
                            //add the listing info
                            hashMap.put("uid", uid);
                            hashMap.put("uname",name);
                            hashMap.put("uemail",email);
                            hashMap.put("udp",dp);
                            hashMap.put("plikes","0");
                            hashMap.put("ptid",timestamp);
                            hashMap.put("ptitle",title);
                            hashMap.put("pdescr", desc);
                            hashMap.put("pimage", downloadUri);
                            hashMap.put("pTime", timestamp);
                            hashMap.put("pprice", itemprice);
                            hashMap.put("pcategory", catselected);

                            //path for listing data to be stored
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Listings");
                            //put the data into the reference
                            ref.child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //post is added successfully to the database
                                            progressDialog.dismiss();
                                            Toast.makeText(postforsaleActivity.this,"Listing published",Toast.LENGTH_SHORT).show();
                                            //reset the view
                                            titleet.setText("");
                                            descriptionet.setText("");
                                            price.setText("");
                                            listingimage.setImageURI(null);
                                            iamge_rui = null;
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed to add the post
                                    progressDialog.dismiss();
                                    Toast.makeText(postforsaleActivity.this,"" + e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });


                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //image upload failed
                progressDialog.dismiss();
            }
        });
    }

    private void showimagepickdialog() {

        //these will be the options that are shown in the dialog
        String[] options = {"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from");
        //this piece of code sets the options to the alert dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                if(which==0)
                {
                    //if camera clicked
                    if (!checkcamerapermissions())
                    {
                        requestcamerapermissions();
                    }
                    else
                    {
                        pickfromcamera();
                    }
                }
                if(which==1)
                {
                    //if gallery is clicked
                    if(!checkstoragepermissions())
                    {
                        requeststoragepermissions();
                    }
                    else
                    {
                        pickfromgallery();
                    }
                }
            }
        });
        //this block of code creates and shows the dialog
        builder.create().show();
    }

    private void pickfromgallery() {
        //intent to pick image from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickfromcamera() {
        //intent to open the camera to take a picture to use
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp image");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        iamge_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);


        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, iamge_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkstoragepermissions()
    {
        //check if storage permissions are enabled
        //return 1 if enabled
        //return 0 if not
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requeststoragepermissions()
    {
        //at runtime request the storage permissions
        ActivityCompat.requestPermissions(this, storagepermissions, STORAGE_REQUEST_CODE);
    }



    private boolean checkcamerapermissions()
    {
        //check if camera permissions are enabled
        //return 1 if enabled
        //return 0 if not
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result1&&result2;
    }

    private void requestcamerapermissions()
    {
        //at runtime request the camera permissions
        ActivityCompat.requestPermissions(this, camerapermissions, CAMERA_REQUEST_CODE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkuserloggedin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkuserloggedin();
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
            email = user.getEmail();
            uid = user.getUid();

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
        onBackPressed();//this will go to the previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);


        menu.findItem(R.id.publish_listing_action).setVisible(false);
        menu.findItem(R.id.search_action).setVisible(false);

        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        catselected = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    //handle the permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method will be called when ever the user presses allow or deny from the permissions request dialog
        //the permission cases will be handled here for allowed and denied

        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:
            {
                if (grantResults.length>0)
                {
                    boolean cameraacc = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAcc = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraacc && storageAcc)
                    {
                        pickfromcamera();
                    }
                    else
                    {
                        Toast.makeText(this,"Camera and Storage permissions are needed for this",Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {

                }
            }
            break;
            case  STORAGE_REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    boolean storageAcc = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAcc)
                    {
                        //storage permissions granted
                        pickfromcamera();
                    }
                    else
                    {
                        //camera or gallery or both were denied
                        Toast.makeText(this,"Storage permissions are needed for this",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {

                }
            }
            break;


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after the user has chosen an image for their listing
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                //image is picked from gallery, get uri of image
                iamge_rui = data.getData();

                //set the image to the image view
                listingimage.setImageURI(iamge_rui);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                //image is picked from camera, get uri of image

                listingimage.setImageURI(iamge_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}