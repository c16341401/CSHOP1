package com.example.cshop1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class Useradapter extends RecyclerView.Adapter<Useradapter.myholder>
{
    Context context;
    List<Usermodel> userlist;

    //constructor

    public Useradapter(Context context, List<Usermodel> userlist)
    {
        this.context = context;
        this.userlist = userlist;
    }
    @NonNull
    @Override
    public myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the user row layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent,false);

        return new myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myholder holder, int position) {
        //this piece of code will grab the data
        String hisUID = userlist.get(position).getUid();
        String userimage = userlist.get(position).getImage();
        String username = userlist.get(position).getName();
        String useremail = userlist.get(position).getEmail();

        //set the data
        holder.rnametv.setText(username);
        holder.remailtv.setText(useremail);

        try
        {
            Picasso.get().load(userimage).placeholder(R.drawable.default_img_ppl)
                    .into(holder.rpicimageview);
        }
        catch (Exception e)
        {

        }
        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Message"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which==0)
                        {
                            //profile is selected instead of message
                            Intent intent1 = new Intent(context, TheirProfileActivity.class);
                            intent1.putExtra("uid", hisUID);
                            context.startActivity(intent1);
                        }
                        if(which==1)
                        {
                            //message is selected instead of profile
                            //click user from the list of registered users to start chatting
                            //the user id is what we will use to identify the user that will receive the message

                            Intent intent = new Intent(context, ChattingActivity.class);
                            intent.putExtra("hisUID", hisUID);
                            context.startActivity(intent);
                        }

                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userlist.size();
    }

    //view holder class
    class  myholder extends RecyclerView.ViewHolder
    {
        ImageView rpicimageview;
        TextView rnametv, remailtv;

        public myholder (View itemView)
        {
            super(itemView);

            //initialize the views that will be used
            rpicimageview = itemView.findViewById(R.id.profilepicuserrow);
            rnametv = itemView.findViewById(R.id.user_row_nametv);
            remailtv = itemView.findViewById(R.id.user_row_emailtv);

        }
    }
}
