package com.example.sharedoc.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Home;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Post_most_seen_Adapter extends RecyclerView.Adapter<Post_most_seen_Adapter.MyHolder> {
    private ArrayList<DB_Post_info> mDataset;
    private Activity activity;
    ItemClickListener_Home itemClickListener;
    String 제목, 작성자;
    FirebaseFirestore firebaseFirestore;
    public Post_most_seen_Adapter(Activity activity, ArrayList<DB_Post_info> myDataset) {
        mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public MyHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_most_view, parent, false);
        return new MyHolder(view);
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        TextView tv_제목, tv_작성자, tv_ranking;

        public MyHolder(View itemView) {
            super(itemView);

            tv_제목 = itemView.findViewById(R.id.tv_제목);
            tv_작성자 = itemView.findViewById(R.id.tv_작성자);
            tv_ranking = itemView.findViewById(R.id.tv_ranking);

        }
    }



    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {

        final DB_Post_info dbPostInfo = mDataset.get(position);

        holder.tv_ranking.setText(String.valueOf(position + 1));

        holder.tv_제목.setText(dbPostInfo.get제목());

        holder.tv_작성자.setText("(by. "+dbPostInfo.getId()+")");

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

    public void setOnItemclickListener(ItemClickListener_Home itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
