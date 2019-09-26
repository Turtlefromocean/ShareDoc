package com.example.sharedoc.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sharedoc.Activity.Chat;
import com.example.sharedoc.DB.DB_Following;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Following;

import java.util.ArrayList;

public class Following_Adapter extends RecyclerView.Adapter<Following_Adapter.MyHolder>{

    private ArrayList<DB_Following> mDataset;
    private Activity activity;
    ItemClickListener_Following itemClickListener;

    public Following_Adapter(Activity activity, ArrayList<DB_Following> myDataset) {
        mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public Following_Adapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_following,parent,false);

        return new Following_Adapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(Following_Adapter.MyHolder holder, final int position) {

        final DB_Following db_following = mDataset.get(position);


        holder.tv_아이디.setText(db_following.getId());

        holder.btn_채팅하기.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Chat.class);
                intent.putExtra("destinationUid", db_following.getUid());
                v.getContext().startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.OnItemClick(position, db_following);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder{

        TextView tv_아이디;
        ImageButton btn_채팅하기;

        public MyHolder(View itemView) {
            super(itemView);

            tv_아이디 = itemView.findViewById(R.id.tv_아이디);
            btn_채팅하기 = itemView.findViewById(R.id.btn_채팅하기);
        }
    }

    public void setOnItemclickListener(ItemClickListener_Following itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


}