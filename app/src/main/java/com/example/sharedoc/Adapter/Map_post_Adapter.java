package com.example.sharedoc.Adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Map_post;

import java.util.ArrayList;

public class Map_post_Adapter extends RecyclerView.Adapter<Map_post_Adapter.MyHolder> {
    private ArrayList<DB_Post_info> mDataset;
    private Context context;
    ItemClickListener_Map_post itemClickListener;


    public Map_post_Adapter(Context context, ArrayList<DB_Post_info> myDataset) {
        mDataset = myDataset;
        this.context = context;
    }

    @Override
    public MyHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_map_post, parent, false);
        return new MyHolder(view);
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tv_제목, tv_작성자, tv_첨부파일이름, tv_태그;

        public MyHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tv_제목 = itemView.findViewById(R.id.tv_제목);
            tv_작성자 = itemView.findViewById(R.id.tv_작성자);
            tv_첨부파일이름 = itemView.findViewById(R.id.tv_첨부파일이름);
            tv_태그 = itemView.findViewById(R.id.tv_태그);
        }
    }



    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {

        final DB_Post_info dbPostInfo = mDataset.get(position);

        holder.tv_제목.setText(dbPostInfo.get제목());

        holder.tv_작성자.setText("작성자: " + dbPostInfo.getId());

        if(dbPostInfo.get파일이름() != null){
            holder.tv_첨부파일이름.setText(dbPostInfo.get파일이름());
        } else {
            holder.tv_첨부파일이름.setText("첨부파일 없음");
        }

        holder.tv_태그.setText(dbPostInfo.get태그());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.OnItemClick(position, dbPostInfo);
            }
        });

    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setOnItemclickListener(ItemClickListener_Map_post itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
