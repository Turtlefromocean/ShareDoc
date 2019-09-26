package com.example.sharedoc.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sharedoc.DB.DB_Comment;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Comment_Adapter extends RecyclerView.Adapter<Comment_Adapter.MyHolder> {

    private ArrayList<DB_Comment> mDataset;
    private Activity activity;
    ItemClickListener_Comment itemClickListener;
    private Boolean clicked = false;

    public Comment_Adapter(Activity activity, ArrayList<DB_Comment> myDataset) {
        mDataset = myDataset;
        this.activity = activity;
    }


    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment,parent,false);

        return new MyHolder(view);
    }


    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {

        final DB_Comment db_comment = mDataset.get(position);



        holder.tv_댓글_ID.setText(db_comment.get작성자());
        holder.tv_작성날짜.setText(new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(db_comment.getCreatedAt()));


        holder.tv_댓글.setText(db_comment.get댓글());
        holder.tv_댓글.post(new Runnable() {
            @Override
            public void run() {
                final int lineCnt = holder.tv_댓글.getLineCount();
                if(lineCnt > 2){
                    holder.iv_더보기.setVisibility(View.VISIBLE);
                    holder.iv_더보기.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(clicked != true){
                                holder.tv_댓글.setMaxLines(lineCnt);
                                clicked = true;
                            } else {
                                holder.tv_댓글.setMaxLines(2);
                                clicked = false;
                            }

                        }
                    });
                } else {
                    Log.e("댓글 줄 수", String.valueOf(lineCnt));
                    holder.tv_댓글.setText(db_comment.get댓글());
                }
            }
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(db_comment.getUid().equals(user.getUid())) {
            holder.tv_삭제.setVisibility(View.VISIBLE);
            holder.tv_삭제.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.OnItemClick(position, db_comment);
                }
            });
        } else {
            Log.e("아님", "아님");
        }

    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder{

        TextView  tv_댓글_ID, tv_댓글, tv_작성날짜;
        ImageView iv_더보기, tv_삭제;

        public MyHolder(View itemView) {
            super(itemView);

            tv_댓글_ID = itemView.findViewById(R.id.tv_댓글_ID);
            tv_댓글 = itemView.findViewById(R.id.tv_댓글);
            tv_작성날짜 = itemView.findViewById(R.id.tv_작성날짜);
            iv_더보기 = itemView.findViewById(R.id.iv_더보기);
            tv_삭제 = itemView.findViewById(R.id.tv_삭제);
        }
    }

    public void setOnItemclickListener(ItemClickListener_Comment itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


}

