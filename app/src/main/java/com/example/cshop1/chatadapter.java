package com.example.cshop1;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class chatadapter extends RecyclerView.Adapter<chatadapter.myholder>
{

    private  static final int MSG_TYPE_LEFT = 0;
    private  static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<chatmodel> chatlist;
    String imageurl;

    FirebaseUser firebaseUser;

    public chatadapter(Context context, List<chatmodel> chatlist, String imageurl) {
        this.context = context;
        this.chatlist = chatlist;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //this piece of code is used to inflate the layouts: for the left and right chat rows
        if(viewType==MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chatrow_right, parent, false);
            return new myholder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.chatrow_left, parent, false);
            return new myholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull myholder holder, int position) {

        //get the data
        String message = chatlist.get(position).getMessage();
        String timest = chatlist.get(position).getTimestamp();

        //this block of code handles the conversion of the timestamp to dd/mm/yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timest));
        String dateTime = DateFormat.format("dd/MM/yyyy HH:MM AA", calendar).toString();

        //set the da
        holder.messtv.setText(message);
        holder.timetv.setText(dateTime);

        try
        {
            Picasso.get().load(imageurl).into(holder.profilechativ);
        }
        catch (Exception e)
        {

        }

        //this block of code sets the seen and delivered status of the message
        if(position==chatlist.size()-1)
        {
            if(chatlist.get(position).isSeen())
            {
                holder.seenTv.setText("Seen");
            }
            else
            {
                holder.seenTv.setText("Delivered");
            }
        }
        else
        {
            holder.seenTv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }

    @Override
    public int getItemViewType(int position) {
        //this piece of code gets the current signed in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatlist.get(position).getSender().equals(firebaseUser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class

    class myholder extends RecyclerView.ViewHolder
    {

        //views from the xml
        ImageView profilechativ;
        TextView messtv, timetv, seenTv;

        public myholder(@NonNull View itemView) {
            super(itemView);

            //initialize the views
            profilechativ = itemView.findViewById(R.id.profilechatleftiv);
            messtv = itemView.findViewById(R.id.chatrowleftmessagetv);
            timetv = itemView.findViewById(R.id.timeTV);
            seenTv = itemView.findViewById(R.id.seentv);

        }
    }


}
