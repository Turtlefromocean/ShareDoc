package com.example.sharedoc.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Home;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Home_all_Adapter extends RecyclerView.Adapter<Home_all_Adapter.MyHolder> {
    private ArrayList<DB_Post_info> mDataset;
    private Activity activity;
    ItemClickListener_Home itemClickListener;
    String 제목, 작성자;
    FirebaseFirestore firebaseFirestore;
    public Home_all_Adapter(Activity activity, ArrayList<DB_Post_info> myDataset) {
        mDataset = myDataset;
        this.activity = activity;
    }

    @Override
    public MyHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
        return new MyHolder(view);
    }

    public static class MyHolder extends RecyclerView.ViewHolder {

        TextView tv_아이디, tv_제목, tv_공감수, tv_본횟수, tv_첨부파일이름, tv_작성날짜;
        ImageView iv_share;

        public MyHolder(View itemView) {
            super(itemView);

            tv_제목 = itemView.findViewById(R.id.tv_제목);
            tv_아이디 = itemView.findViewById(R.id.tv_아이디);
            tv_첨부파일이름 = itemView.findViewById(R.id.tv_첨부파일이름);
            tv_작성날짜 = itemView.findViewById(R.id.tv_작성날짜);
            tv_공감수 = itemView.findViewById(R.id.tv_공감수);
            tv_본횟수 = itemView.findViewById(R.id.tv_본횟수);
            iv_share = itemView.findViewById(R.id.iv_share);
        }
    }



    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {

        final DB_Post_info dbPostInfo = mDataset.get(position);

        holder.tv_제목.setText("[제목] " +dbPostInfo.get제목());


        if(dbPostInfo.get파일이름() != null){
            holder.tv_첨부파일이름.setText(dbPostInfo.get파일이름());
        } else {
            holder.tv_첨부파일이름.setText("첨부파일 없음");
        }


        holder.tv_아이디.setText("작성자: "+dbPostInfo.getId());

        holder.iv_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String 문자내용 = "(공유)"+ dbPostInfo.getId()+"님의 포스트"+": "+ dbPostInfo.get제목();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");

                // Set default text message
                // 카톡, 이메일, MMS 다 이걸로 설정 가능
                //String subject = "문자의 제목";
                String text = 문자내용;
                //intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, text);

                // Title of intent
                Intent chooser = Intent.createChooser(intent, "포스트 공유하기");
                v.getContext().startActivity(chooser);
            }
        });

        holder.tv_공감수.setText(dbPostInfo.get공감());

        holder.tv_본횟수.setText(dbPostInfo.get본횟수());

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

    public void setOnItemclickListener(ItemClickListener_Home itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
