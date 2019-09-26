package com.example.sharedoc.Adapter;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_MyPage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MyPage_Adapter extends RecyclerView.Adapter<MyPage_Adapter.MyHolder> {
    private ArrayList<DB_Post_info> mDataset;
    private Activity activity;
    ItemClickListener_MyPage itemClickListener;


    public MyPage_Adapter(Activity activity, ArrayList<DB_Post_info> myDataset) {
        mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public MyHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_page2, parent, false);
        return new MyHolder(view);
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tv_제목, tv_내용, tv_공감수, tv_본횟수, tv_첨부파일이름, tv_작성날짜, tv_태그;

        public MyHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tv_제목 = itemView.findViewById(R.id.tv_제목);
            tv_내용 = itemView.findViewById(R.id.tv_내용);
            tv_첨부파일이름 = itemView.findViewById(R.id.tv_첨부파일이름);
            tv_작성날짜 = itemView.findViewById(R.id.tv_작성날짜);
            tv_공감수 = itemView.findViewById(R.id.tv_공감수);
            tv_본횟수 = itemView.findViewById(R.id.tv_본횟수);
            tv_태그 = itemView.findViewById(R.id.tv_태그);
        }
    }



    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {

        final DB_Post_info dbPostInfo = mDataset.get(position);

        holder.tv_제목.setText(dbPostInfo.get제목());

        holder.tv_내용.setText(dbPostInfo.get내용());

        if(dbPostInfo.get파일이름() != null){
            holder.tv_첨부파일이름.setText(dbPostInfo.get파일이름());
        } else {
            holder.tv_첨부파일이름.setText("첨부파일 없음");
        }

        holder.tv_공감수.setText(dbPostInfo.get공감());

        holder.tv_본횟수.setText(dbPostInfo.get본횟수());

        holder.tv_태그.setText(dbPostInfo.get태그());

        holder.tv_작성날짜.setText(new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(dbPostInfo.getCreatedAt()));

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

    public void setOnItemclickListener(ItemClickListener_MyPage itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
