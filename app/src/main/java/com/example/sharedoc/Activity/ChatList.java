package com.example.sharedoc.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sharedoc.DB.DB_Chat;
import com.example.sharedoc.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatList extends AppCompatActivity {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        RecyclerView recyclerView = findViewById(R.id.rv_chatList);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu1:
                        Intent intent1 = new Intent(ChatList.this, Home.class);
                        startActivity(intent1);
                        return true;
                    case R.id.navigation_menu2:
                        Intent intent2 = new Intent(ChatList.this, Search.class);
                        startActivity(intent2);
                        return true;
                    case R.id.navigation_menu3:
                        Intent intent3 = new Intent(ChatList.this, WritePost.class);
                        startActivity(intent3);
                        return true;
                    case R.id.navigation_menu4:
                        return true;
                    case R.id.navigation_menu5:
                        Intent intent5 = new Intent(ChatList.this, MyPage.class);
                        startActivity(intent5);
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_menu4);

    }

    String 상대방이름;
    String 상대방프로필사진;
    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<DB_Chat> dbChat= new ArrayList<>();
        private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();

        public ChatRecyclerViewAdapter() {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dbChat.clear();
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        dbChat.add(item.getValue(DB_Chat.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list,parent,false);

            return new CustomViewHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {

            final CustomViewHolder customViewHolder = ((CustomViewHolder)viewHolder);

            String destinationUid;

            //챗방에 있는 유저를 체크하는 것
            for(String user: dbChat.get(position).users.keySet()){
                if(!user.equals(uid)){
                    destinationUid = user;
                    destinationUsers.add(destinationUid);

                    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                    final DocumentReference documentReference = firebaseFirestore.collection("users").document(destinationUid);

                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                final DocumentSnapshot document = task.getResult();

                                if (document != null) {
                                    if (document.exists()) {
                                        상대방이름 = document.get("id").toString();
                                        상대방프로필사진 = document.get("photoUrl").toString();

                                        Glide.with(customViewHolder.itemView.getContext())   /** 오류 생긴 적 있음*/
                                                .load(상대방프로필사진)
                                                .apply(new RequestOptions().circleCrop())
                                                .into(customViewHolder.chatListItem_iv);
                                        customViewHolder.chatListItem_tv_방이름.setText(상대방이름+"님과의 채팅");

                                    } else {

                                    }
                                }

                            } else {

                            }
                        }
                    });
                }
            }

            //메시지를 내림 차순으로 정렬 후 마지막 메세지의 키값을 가져옴
            Map<String, DB_Chat.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
            commentMap.putAll(dbChat.get(position).comments);
            String lastMessageKey = (String) commentMap.keySet().toArray()[0];
            customViewHolder.chatListItem_tv_마지막메시지.setText(dbChat.get(position).comments.get(lastMessageKey).message);

            customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), Chat.class);
                    intent.putExtra("destinationUid", destinationUsers.get(position));
                    startActivity(intent);
                }
            });

            //TimeStamp
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            long unixTime = (long) dbChat.get(position).comments.get(lastMessageKey).timestamp;
            Date date = new Date(unixTime);
            customViewHolder.chatListItem_tv_timestamp.setText(simpleDateFormat.format(date));
        }

        @Override
        public int getItemCount() {
            return dbChat.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView chatListItem_iv;
            public TextView chatListItem_tv_방이름;
            public TextView chatListItem_tv_마지막메시지;
            public TextView chatListItem_tv_timestamp;
            public CardView cardView_chatList;

            public CustomViewHolder(View view) {
                super(view);

                chatListItem_iv = (ImageView)view.findViewById(R.id.chatListItem_iv);
                chatListItem_tv_방이름 = (TextView)view.findViewById(R.id.chatListItem_tv_방이름);
                chatListItem_tv_마지막메시지 = (TextView)view.findViewById(R.id.chatListItem_tv_마지막메시지);
                chatListItem_tv_timestamp = (TextView)view.findViewById(R.id.chatListItem_tv_timestamp);
                cardView_chatList = (CardView)view.findViewById(R.id.cardView_chatList);
            }
        }
    }
}
