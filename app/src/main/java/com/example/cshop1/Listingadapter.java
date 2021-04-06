package com.example.cshop1;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Path;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Listingadapter extends RecyclerView.Adapter<Listingadapter.MyHolder> {

    Context context;
    List <Listingmodel> listingadapter;

    String currentuid;

    private DatabaseReference likeref;
    private DatabaseReference listingref;

    boolean likingprocess = false;

    public Listingadapter(Context context, List<Listingmodel> listingadapter) {
        this.context = context;
        this.listingadapter = listingadapter;
        currentuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeref = FirebaseDatabase.getInstance().getReference().child("Likes");
        listingref = FirebaseDatabase.getInstance().getReference().child("Listings");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate listing row layout
        View view = LayoutInflater.from(context).inflate(R.layout.listing_row, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //get data
        String uid = listingadapter.get(position).getUid();
        String uemail = listingadapter.get(position).getUemail();
        String uname = listingadapter.get(position).getUname();
        String udp = listingadapter.get(position).getUdp();
        String lid = listingadapter.get(position).getPtid();
        String ltitle = listingadapter.get(position).getPtitle();
        String ldescrip = listingadapter.get(position).getPdescr();
        String limage = listingadapter.get(position).getPimage();
        String ltimestamp = listingadapter.get(position).getpTime();
        String lcategory = listingadapter.get(position).getPcategory();
        String lprice = listingadapter.get(position).getPprice();
        String llikes = listingadapter.get(position).getPlikes();//this will contain the total amount of likes a listing has


        //this piece of code will convert the timestamp to the correct format
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(ltimestamp));
        String ltime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //set the data
        holder.unametv.setText(uname);
        holder.ltimetv.setText(ltime);
        holder.ltitletv.setText(ltitle);
        holder.ldecriptiontv.setText(ldescrip);
        holder.listingcategory.setText(lcategory);
        holder.listingpricetv.setText(lprice);
        holder.llikestv.setText(llikes + " Likes");

        //set the likes for each listing
        setlistingLikes(holder, ltimestamp);

        //set the users display picture
        try
        {
            Picasso.get().load(udp).placeholder(R.drawable.default_img).into(holder.upictureiv);
        }
        catch (Exception e)
        {

        }

        holder.limageiv.setVisibility(View.VISIBLE);
        //set the listing image
        try
        {
            Picasso.get().load(limage).into(holder.limageiv);
        }
        catch (Exception e)
        {

        }

        //this block of code handles the button clicks

        holder.seemorebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showmore(holder.seemorebutton,uid,currentuid,ltimestamp,limage);
            }
        });
        holder.likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //this method is called when the like button is clicked
                //it will first get the total amount of likes that the clicked post has
                //and if the user has not already liked the post it will increase the like value by one
                //if user has liked before and clicks it will reduce the like value by 1
                final int listinglikes = Integer.parseInt(listingadapter.get(position).getPlikes());
                likingprocess = true;

                //this piece of code retrieves the id of the post clicked
                String listingid = listingadapter.get(position).getpTime();
                likeref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (likingprocess)
                        {
                            if(snapshot.child(listingid).hasChild(currentuid))
                            {
                                //current user has already liked the listing
                                listingref.child(listingid).child("plikes").setValue(""+(listinglikes-1));
                                likeref.child(listingid).child("plikes").setValue(""+(listinglikes-1));
                                likeref.child(listingid).child(currentuid).removeValue();
                                likingprocess = false;

                            }
                            else
                            {
                                //the current user hasnt liked the post so add to the like counter
                                likeref.child(listingid).child("plikes").setValue(""+(listinglikes+1));
                                listingref.child(listingid).child("plikes").setValue(""+(listinglikes+1));
                                likeref.child(listingid).child(currentuid).setValue("Liked");
                                likingprocess = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        holder.lprofilelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //once this is clicked the theirprofileactivity will be opened
               //the uid along with the clicked listing will be used to show user
                //data specific to the user

                Intent intent = new Intent(context, TheirProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });

        holder.limageiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchbyimage(holder.limageiv,ltimestamp,limage);
            }
        });
    }

    private void searchbyimage(ImageView limageiv, String ltimestamp, String limage) {

        PopupMenu popupMenu = new PopupMenu(context, limageiv, Gravity.END);

        popupMenu.getMenu().add(Menu.NONE,0,0,"Search by this image?");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0)
                {

                    Intent intent = new Intent(context, SearchbyImageActivity.class);
                    intent.putExtra("lid", ltimestamp);
                    context.startActivity(intent);

                }
                return false;
            }
        });

        //show the menu
        popupMenu.show();

    }

    private void setlistingLikes(MyHolder holder, String lid) {
        likeref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(lid).hasChild(currentuid))
                {
                    //this if statement will run if the signed in user has liked the post
                    //it will change the like image color

                    holder.likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_img, 0, 0, 0);
                    holder.likebtn.setText("Liked");
                }
                else
                {

                    holder.likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.liked_img, 0, 0, 0);
                    holder.likebtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showmore(ImageButton seemorebutton, String uid, String currentuid, String lid, String limage) {
    //this method will create a popup dialog containing the delete button allowing the user to delete a listing
        PopupMenu popupMenu = new PopupMenu(context, seemorebutton, Gravity.END);


        //only allow users to delete their own listings
        if(uid.equals(currentuid))
        {
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete listing");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == 0)
                {
                    //this block of code will run if the user clicks the delete button
                    deletelisting(lid, limage);
                }
                return false;
            }
        });

        //show the menu
        popupMenu.show();

    }

    private void deletelisting(String lid, String limage) {
        //set the progress bar to show
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Removing your listing from the app");


        //first we will remove the image from the firebase storage
        StorageReference refforpic = FirebaseStorage.getInstance().getReferenceFromUrl(limage);
        refforpic.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //if the image is removed successfully then remove the instance from the firebase database
                Query dquery = FirebaseDatabase.getInstance().getReference("Listings").orderByChild("ptid").equalTo(lid);
                dquery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren())
                        {
                            ds.getRef().removeValue();
                        }

                        //show message to user when the delete is done
                        Toast.makeText(context,"Your post has been removed", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //failed to remove image
                progressDialog.dismiss();
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public int getItemCount() {
        return listingadapter.size();
    }


    //view holder class
    class MyHolder extends RecyclerView.ViewHolder
    {

        //views from the listing row xml
        ImageView upictureiv, limageiv;
        TextView unametv,ltimetv,ltitletv,ldecriptiontv,llikestv,listingpricetv,listingcategory;
        ImageButton seemorebutton;
        Button likebtn;
        LinearLayout lprofilelayout;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //initialize the views
            upictureiv = itemView.findViewById(R.id.listingppiciv);
            limageiv = itemView.findViewById(R.id.l_imageiv);
            unametv = itemView.findViewById(R.id.l_nametv);
            ltimetv = itemView.findViewById(R.id.l_timetv);
            ltitletv = itemView.findViewById(R.id.l_titletv);
            ldecriptiontv = itemView.findViewById(R.id.l_descriptiontv);
            llikestv = itemView.findViewById(R.id.listing_likestv);
            listingpricetv = itemView.findViewById(R.id.l_pricetv);
            listingcategory = itemView.findViewById(R.id.l_categorytv);
            seemorebutton = itemView.findViewById(R.id.seemorebtn);
            likebtn = itemView.findViewById(R.id.listinglikebtn);
            lprofilelayout = itemView.findViewById(R.id.lprofilell);

        }
    }

}
