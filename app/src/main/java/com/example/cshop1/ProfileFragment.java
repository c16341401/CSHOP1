package com.example.cshop1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cshop1.Listingadapter;
import com.example.cshop1.Listingmodel;
import com.example.cshop1.MainActivity;
import com.example.cshop1.R;
import com.example.cshop1.postforsaleActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.cshop1.R.*;
import static com.example.cshop1.R.layout.*;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


public class ProfileFragment extends Fragment {

    //firebase instance being created
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //storage
    StorageReference storageReference;

    //path to where the user profile and cover image will be kept
    String storagepath = "Users_Profile_Cover_Image/";

    //views created to correspond to xml file
    ImageView profilepic,coverimage;
    TextView pname, pemail, pphone;
    FloatingActionButton floatingActionButton;
    RecyclerView listingrecyclerview;

    //progress dialog bar
    ProgressDialog progressDialog;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;

    //arrays of permissions that need to be requested
    String cameraPermissions [];
    String storagePermissions [];

    List<Listingmodel> listingmodelList;
    Listingadapter listingadapter;
    String uid;


    //uri of the image that was selected
    Uri imageuri;

    //String to check whether the user wants to edit profile or cover image
    String profileorcover = "";

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(fragment_profile, container, false);

        //firebase auth and user is initialized below
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();
        //initialize arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //the views are initialized below

        profilepic = view.findViewById(id.profilepicic);
        coverimage = view.findViewById(id.coverimage);
        pname = view.findViewById(id.pnametv);
        pemail = view.findViewById(id.pemailtv);
        pphone = view.findViewById(id.pphonetv);
        floatingActionButton = view.findViewById(id.floatingactionbtn);
        listingrecyclerview = view.findViewById(id.userslistingsrv);

        //this piece of code initializes the progress dialog bar
        progressDialog = new ProgressDialog(getActivity());

        //this block of code will retrieve the signed in users email address then scan the database to find a match
        //for that email address, once a match is found all the details of the match will be pulled from the database

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
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
                        Picasso.get().load(drawable.default_img).into(profilepic);
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

        //this block of code handles the action button click

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayeditprofiledialog();
            }
        });




        listingmodelList = new ArrayList<>();

        checkuserloggedin();
        loadloggedinuserslistings();


        return view;
    }

    private void loadloggedinuserslistings() {
        //this will be the linear layout for the listings recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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
                    listingadapter = new Listingadapter(getActivity(), listingmodelList);
                    //this piece of code will set the adapter to the recycler view
                    listingrecyclerview.setAdapter(listingadapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                //Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchoggedinuserslistings(String squery) {
        //this will be the linear layout for the listings recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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



                    if(luserslistings.getPtitle().toLowerCase().contains(squery.toLowerCase()) || luserslistings.getPdescr().toLowerCase().contains(squery.toLowerCase()) || luserslistings.getPcategory().toLowerCase().contains(squery.toLowerCase()))
                    {
                        //add to the list

                        listingmodelList.add(luserslistings);
                    }

                    //create the adapter
                    listingadapter = new Listingadapter(getActivity(), listingmodelList);
                    //this piece of code will set the adapter to the recycler view
                    listingrecyclerview.setAdapter(listingadapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean checkStoragePermissions()
    {
        //this block of code will check id the storage permissions
        //are enabled or not
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermissions()
    {
        //request runtime storage permissions
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermissions()
    {
        //this block of code will check id the storage permissions
        //are enabled or not
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermissions()
    {
        //request runtime storage permissions
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void displayeditprofiledialog()
    {
        /* inside this method i will allow the user to edit
        their profile picture, their cover photo,their name, and their phone number
         */

        //this array holds all the options that will be displayed in the dialog
        String options[] = {"Edit Profile Pictures","Edit Cover Photo","Edit Name","Edit Phone"};
        //this piece of code creates the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set the title
        builder.setTitle("choose Action");
        //this block of code sets the items into the dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the below if else statements are used to handle the different item clicks
                if (which == 0)
                {
                    //edit profile has been clicked
                    progressDialog.setMessage("Updating your profile picture");
                    profileorcover = "image";
                    showImagepicdialog();
                }
                else if (which == 1)
                {
                    //edit cover piture has been clicked
                    progressDialog.setMessage("Updating your Cover Photo ");
                    profileorcover = "cover";
                    showImagepicdialog();
                }
                else if (which == 2)
                {
                    //edit name has been clicked
                    progressDialog.setMessage("Updating your name ");
                    shownamephoneupdatedialog("name");
                }
                else if (which == 3)
                {
                    //edit phone has been clicked
                    progressDialog.setMessage("Updating your phone number ");
                    shownamephoneupdatedialog("phone");
                }
            }
        });

        //this piece of code will create and show the user the dialog
        builder.create().show();
    }

    private void shownamephoneupdatedialog(String key) {
        //the key is passed in to tell the method whether the user wants to edit
        //their phone number or their name

        //create a custom dialog for the user
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+ key);
        //Set the layout for the dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add the edit text to the dialog
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add the buttons to the dialog
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //validate if user entered something
                if (!TextUtils.isEmpty(value))
                {
                    progressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),"Updated",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });

                    if(profileorcover.equals("image"))
                    {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Listings");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for (DataSnapshot ds: snapshot.getChildren())
                                {
                                    String child=ds.getKey();
                                    snapshot.getRef().child(child).child("udp").setValue(imageuri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }


                    if(key.equals("name"))
                    {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Listings");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for (DataSnapshot ds: snapshot.getChildren())
                                {
                                    String child=ds.getKey();
                                    snapshot.getRef().child(child).child("uname").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                else
                {
                    Toast.makeText(getActivity(),"Please Enter "+key+"",Toast.LENGTH_SHORT).show();

                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showImagepicdialog() {
        //ths piece of code shows the user a dialog allowing them to select upload from gallery
        //or take from camera

        String options[] = {"Camera","Gallery"};
        //this piece of code creates the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set the title
        builder.setTitle("Pick between choosing from your gallery or open your camera ");
        //this block of code sets the items into the dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the below if else statements are used to handle the different item clicks
                if (which == 0)
                {
                    //camera has been clicked
                    if (!checkCameraPermissions())
                    {
                        requestCameraPermissions();
                    }
                    else
                    {
                        pickfromcamera();
                    }
                }
                else if (which == 1)
                {
                    //gallery has been clicked
                    if (!checkStoragePermissions())
                    {
                        checkStoragePermissions();
                    }
                    else
                    {
                        pickfromgallery();
                    }
                }
            }
        });

        //this piece of code will create and show the user the dialog
        builder.create().show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        /*This block of code is used to handle what happens when the user chooses
        to either allow the permissions or deny them
         */

        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:{
                //camera is picked so this block of code will check if camera and storage permissions allowed or not
                if (grantResults.length>0)
                {
                    boolean cameraAcc = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writertostorageAcc = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAcc && writertostorageAcc)
                    {
                        //permissions enabled
                        pickfromcamera();
                    }
                    else
                    {
                        //permissions are denied
                        Toast.makeText(getActivity(),"Please enable camera & storage permissions to continue", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{

                //gallery is picked so this block of code will check if camera and storage permissions allowed or not
                if (grantResults.length>0)
                {
                    boolean writertostorageAcc = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(writertostorageAcc)
                    {
                        //permissions enabled
                        pickfromgallery();
                    }
                    else
                    {
                        //permissions are denied
                        Toast.makeText(getActivity(),"Please enable storage permissions to continue", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;

        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method is called after the user has picked an image from either the gallery or taken from camera
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                //if the image is taken from camera grab the uri code
                imageuri = data.getData();

                uploadprofilepictures(imageuri);

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                //if the image is picked from gallery grab the uri code

                uploadprofilepictures(imageuri);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void uploadprofilepictures(Uri uri)
    {
        //show progress dialog
        progressDialog.show();
        /* this block of code will handle the logic that is needed for the user to update
        both their profile picture and their cover photo

        how i will check which section the user wants to edit is by, adding a string variable
        that will be asssigned the value "cover" if the user wants to edit the cover photo
        and "image" when the user clicks to edit his/her profile picture
         */

        /*
        the user id of the currently signed in user is how i made sure that only one profile picture
        and cover photo is assigned to each user
         */

        //path and the name of the image to be stored in the fire base cloud storage

        String filepathandname = storagepath + "" + profileorcover + "_" + user.getUid();


        StorageReference storageReference2nd = storageReference.child(filepathandname);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage so this code will take its url and store it in the users database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //this piece of code will check if the image is uploaded or not and if the url is received
                        if(uriTask.isSuccessful())
                        {
                            //image is uploaded
                            HashMap<String, Object> results = new HashMap<>();
                            results.put(profileorcover, downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            //url was successfully added
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(),"Image Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            //url wasnt added
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(),"error, failed to update the image", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                        else
                        {
                            //error
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //there were errors get and show the errors
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void pickfromcamera() {
        //intent used in picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //this piece of code puts in the image uri
        imageuri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start the camera
        Intent cameraintent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(cameraintent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickfromgallery() {
        //this block of code handles the logic to allow the users to pick from the gallery
        Intent galleryintent = new Intent(Intent.ACTION_PICK);
        galleryintent.setType("image/*");
        startActivityForResult(galleryintent, IMAGE_PICK_GALLERY_CODE);
    }


    private void checkuserloggedin()
    {
        //with the piece of code below i am able to retrieve the current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        //check if there is user signed in
        if (user != null)
        {
            //user is signed in stay here
            uid = user.getUid();

        }
        else
        {
            //user is not signed in go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    public void onCreate(@com.google.firebase.database.annotations.Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //inflate menu
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(id.search_action);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //this method is called when the user presses the search button
                if (!TextUtils.isEmpty(query))
                {
                    searchoggedinuserslistings(query);
                }
                else
                {
                    loadloggedinuserslistings();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //this method is called when ever the system detects a change in the text that the user has typed

                if (!TextUtils.isEmpty(newText))
                {
                    searchoggedinuserslistings(newText);
                }
                else
                {
                    loadloggedinuserslistings();
                }

                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    //this block of code will handle the menu item clicks

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get the selected items id
        int id = item.getItemId();
        if(id == R.id.logout_action)
        {
            firebaseAuth.signOut();
            checkuserloggedin();
        }
        if(id == R.id.publish_listing_action)
        {
            startActivity(new Intent(getActivity(), postforsaleActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}