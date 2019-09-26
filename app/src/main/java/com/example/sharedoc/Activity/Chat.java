package com.example.sharedoc.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sharedoc.DB.DB_Chat;
import com.example.sharedoc.DB.DB_Notification;
import com.example.sharedoc.MyFirebaseMessagingService;
import com.example.sharedoc.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;



import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Chat extends AppCompatActivity {

    ImageView btn_채팅전송, btn_음성인식;
    ImageButton btn_뒤로가기;
    EditText et_채팅입력;
    String 채팅;
    String destinationUid;
    String uid;
    String chatRoomUid;
    String 상대방이름;
    String 상대방프로필사진;

    String destinationToken;
    String myToken;
    RecyclerView recyclerView;

    Intent i;
    SpeechRecognizer mRecognizer;
    private static final int REQUEST_CODE = 1234;
    Dialog dialog_stt;
    ArrayList<String> matches_text;
    ListView textlist;

    MyFirebaseMessagingService myFirebaseMessagingService;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 채팅을 요구하는 아이디 (단말기에 로그인 된 아이디)
        destinationUid = getIntent().getStringExtra("destinationUid"); //채팅을 당하는 아이디


        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");


        btn_음성인식 = (ImageView) findViewById(R.id.btn_음성인식);
        btn_음성인식.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, REQUEST_CODE);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Plese Connect to Internet", Toast.LENGTH_LONG).show();
                }}

        });
        et_채팅입력 = (EditText) findViewById(R.id.et_채팅입력);
        btn_채팅전송 = (ImageView) findViewById(R.id.btn_채팅전송);
        btn_뒤로가기 = (ImageButton) findViewById(R.id.btn_뒤로가기);

        recyclerView = (RecyclerView) findViewById(R.id.rv_채팅);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();


        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(destinationUid);
        CollectionReference col = documentReference1.collection("pushToken");
        final DocumentReference docRef = col.document(destinationUid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        destinationToken = document.getData().get("token").toString();

                    } else {

                    }

                } else {
                    //Toast.makeText(Chat.this, destinationToken, Toast.LENGTH_SHORT).show();
                }
            }
        });

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

                            ((TextView)findViewById(R.id.tv_채팅방이름)).setText(상대방이름+"님과의 채팅");
                        } else {

                        }
                    }

                } else {

                }
            }
        });
        checkChatRoom();




        btn_뒤로가기.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_채팅전송.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(et_채팅입력.getText().length() != 0){
                    DB_Chat dbChat = new DB_Chat();
                    dbChat.users.put(uid, true);
                    dbChat.users.put(destinationUid, true);

                    if (chatRoomUid == null) {
                        btn_채팅전송.setEnabled(false);
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(dbChat).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                checkChatRoom();
                            }
                        });

                    } else {

                        DB_Chat.Comment comment = new DB_Chat.Comment();
                        comment.uid = uid;
                        comment.message = et_채팅입력.getText().toString();
                        comment.timestamp = ServerValue.TIMESTAMP;
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sendFcm();
                                et_채팅입력.setText("");
                            }
                        });

                    }
                }

            }
        });


    }

/** 푸시알림 보내기 */
    void sendFcm(){

        Gson gson = new Gson();

        DB_Notification notificationModel = new DB_Notification();
        notificationModel.to = destinationToken;
        notificationModel.notification.title = 상대방이름;
        notificationModel.notification.text = et_채팅입력.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));
        Log.i("Push", gson.toJson(notificationModel));
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=" + "AAAAee5D9oM:APA91bEKR12Wg6sXV6Txijme0B4hi-Eni_wXpDQRumUIb2d8h1n2NWZEgC-Vxvr3g7ZpuUgSb92_F4v1xEPPA4KwVNVJfVAh1mTk0eA9tvLSiVbXPzSKCNuqcqVC82z2LR-lKHN27YBl")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("Push", "실패");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("Push", "성공");
            }
        });


    }


    void checkChatRoom() {
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    DB_Chat dbChat = item.getValue(DB_Chat.class);
                    if (dbChat.users.containsKey(destinationUid)) {
                        chatRoomUid = item.getKey();
                        btn_채팅전송.setEnabled(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(Chat.this));
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /** 채팅 어댑터 시작 */

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<DB_Chat.Comment> comments;

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            getMessageList();
        }

        void getMessageList() {
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        comments.add(item.getValue(DB_Chat.Comment.class));
                    }
                    //메세지가 갱신
                    notifyDataSetChanged();
                    recyclerView.scrollToPosition(comments.size() - 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ChatViewHolder chatViewHolder = ((ChatViewHolder)holder);

            //내가 보낸 메세지
            if (comments.get(position).uid.equals(uid)) {
                chatViewHolder.tv_message.setText(comments.get(position).message);
                chatViewHolder.tv_message.setBackgroundResource(R.drawable.chat_me);
                chatViewHolder.chatItem_linearlayout_destination.setVisibility(View.INVISIBLE);
                chatViewHolder.chatItem_linearlayout_main.setGravity(Gravity.RIGHT);
                //상대방이 보낸 메세지
            } else {

                Glide.with(holder.itemView.getContext())
                        .load(상대방프로필사진)
                        .apply(new RequestOptions().circleCrop())
                        .into(chatViewHolder.chatItem_iv_profile);

               /* chatViewHolder.chatItem_tv_name.setText(상대방이름);*/
                chatViewHolder.chatItem_linearlayout_destination.setVisibility(View.VISIBLE);
                chatViewHolder.tv_message.setText(comments.get(position).message);
                chatViewHolder.tv_message.setBackgroundResource(R.drawable.chat_other);
                chatViewHolder.tv_message.setTextSize(16);
                chatViewHolder.chatItem_linearlayout_main.setGravity(Gravity.LEFT);

            }
            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);

            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            chatViewHolder.chatItem_tv_timestamp.setText(time);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class ChatViewHolder extends RecyclerView.ViewHolder {
            public TextView tv_message;
           /* public TextView chatItem_tv_name;*/
            public TextView chatItem_tv_timestamp;
            public LinearLayout chatItem_linearlayout_destination;
            public LinearLayout chatItem_linearlayout_main;
            public ImageView chatItem_iv_profile;

            public ChatViewHolder(View view) {
                super(view);
                tv_message = (TextView) view.findViewById(R.id.tv_message);
               /* chatItem_tv_name = (TextView) view.findViewById(R.id.chatItem_tv_name);*/
                chatItem_tv_timestamp = (TextView) view.findViewById(R.id.chatItem_tv_timestamp);
                chatItem_linearlayout_destination = (LinearLayout) view.findViewById(R.id.chatItem_linearlayout_destination);
                chatItem_iv_profile = (ImageView) view.findViewById(R.id.chatItem_iv_profile);
                chatItem_linearlayout_main = (LinearLayout) view.findViewById(R.id.chatItem_linearlayout_main);
            }
        }
    }

// 채팅 어댑터 끝

    /** 구글 음성 인식 */
    public  boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net!=null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            dialog_stt = new Dialog(Chat.this);
            dialog_stt.setContentView(R.layout.dialog_stt);
            dialog_stt.setTitle("Select Matching Text");
            textlist = (ListView)dialog_stt.findViewById(R.id.list);
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, matches_text);
            textlist.setAdapter(adapter);
            textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    et_채팅입력.setText(matches_text.get(position));
                    dialog_stt.hide();
                }
            });
            dialog_stt.show();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /// 구글 음성인식 끝
}
