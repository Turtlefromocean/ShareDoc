package com.example.sharedoc.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sharedoc.DB.DB_Member_info;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Search;

import java.util.ArrayList;

public class Search_Adapter extends RecyclerView.Adapter<Search_Adapter.MyHolder>{

    private ArrayList<DB_Member_info> mDataset;
    private Activity activity;
    ItemClickListener_Search itemClickListener;

    public Search_Adapter(Activity activity, ArrayList<DB_Member_info> myDataset) {
        mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public Search_Adapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_following,parent,false);

        return new Search_Adapter.MyHolder(view);
    }

    @Override
    public void onBindViewHolder(Search_Adapter.MyHolder holder, final int position) {

        final DB_Member_info dbMemberInfo = mDataset.get(position);

        holder.tv_아이디.setText(dbMemberInfo.getId());
        holder.tv_이름.setText("("+dbMemberInfo.getName()+")");

        if(dbMemberInfo.getSchool().length() != 0){
            holder.tv_소속.setText("  "+dbMemberInfo.getSchool());
        }

        if(dbMemberInfo.getMajor().length() != 0){
            holder.tv_분야.setText("  "+dbMemberInfo.getMajor());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.OnItemClick(position, dbMemberInfo);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder{

        TextView tv_아이디, tv_소속, tv_분야, tv_이름;

        public MyHolder(View itemView) {
            super(itemView);

            tv_아이디 = itemView.findViewById(R.id.tv_아이디);
            tv_소속 = itemView.findViewById(R.id.tv_소속);
            tv_이름 = itemView.findViewById(R.id.tv_이름);
            tv_분야 = itemView.findViewById(R.id.tv_분야);
        }
    }

    public void setOnItemclickListener(ItemClickListener_Search itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


}