package com.example.sharedoc.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedoc.Adapter.MyPage_Adapter;
import com.example.sharedoc.DB.DB_Following;
import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_MyPage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class OtherUserPage extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;

    TextView tv_아이디, tv_이름, tv_팔로워수, tv_btn_홈, tv_포스트개수;
    Bitmap bitmap;
    RecyclerView recyclerView;
    MyPage_Adapter mAdapter;
    String uid, 팔로잉아이디;
    FirebaseUser firebaseUser;
    Button btn_팔로우;
    int 최종팔로워;
    String 팔로잉수;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_page);

        tv_아이디 = findViewById(R.id.tv_아이디);
        tv_이름 = findViewById(R.id.tv_이름);
        tv_팔로워수 = findViewById(R.id.tv_팔로워수);
        tv_btn_홈 = findViewById(R.id.tv_btn_홈);
        tv_포스트개수 = findViewById(R.id.tv_포스트개수);

        btn_팔로우 = findViewById(R.id.btn_팔로우);

        recyclerView = findViewById(R.id.rv_내마켓);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(OtherUserPage.this));

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();




        tv_btn_홈.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserPage.this, Home.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu1:  // 이미 home 어떻게 할까???????????????
                        return true;
                    case R.id.navigation_menu2:
                        Intent intent2 = new Intent(OtherUserPage.this, Search.class);
                        startActivity(intent2);
                        return true;
                    case R.id.navigation_menu3:
                        Intent intent3 = new Intent(OtherUserPage.this, WritePost.class);
                        startActivity(intent3);
                        return true;
                    case R.id.navigation_menu4:
                        Intent intent4 = new Intent(OtherUserPage.this, ChatList.class);
                        startActivity(intent4);
                        return true;
                    case R.id.navigation_menu5:
                        Intent intent5 = new Intent(OtherUserPage.this, MyPage.class);
                        startActivity(intent5);
                        return true;
                }
                return false;
            }
        });




    }




    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        findViewById(R.id.btn_채팅).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherUserPage.this, Chat.class);
                intent.putExtra("destinationUid",uid);
                startActivity(intent);
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_menu1);

        final DocumentReference documentReference = firebaseFirestore.collection("users").document(uid);
        final CollectionReference collectionReference = firebaseFirestore.collection("posts");

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();

                    if (document != null) {
                        if (document.exists()) {
                            팔로잉아이디 = document.get("id").toString();
                            tv_아이디.setText(document.get("id").toString()+"님의 그라운드");
                            ((TextView)findViewById(R.id.tv_introduce)).setText(document.get("introduce").toString());

                            String 이름 = document.get("name").toString();

                            if(document.get("school").toString().length() != 0 && document.get("major").toString().length() == 0){
                                tv_이름.setText(이름+"("+document.get("school").toString()+")");
                            } else if(document.get("major").toString().length() != 0 && document.get("school").toString().length() == 0) {
                                tv_이름.setText(이름+"("+document.get("major").toString()+")");
                            } else if(document.get("major").toString().length() != 0 && document.get("school").toString().length() != 0) {
                                tv_이름.setText(이름+"("+document.get("school").toString()+", "+document.get("major").toString()+")");
                            } else if(document.get("major").toString().length() == 0 && document.get("school").toString().length() == 0){
                                tv_이름.setText(이름);
                            }

                            tv_팔로워수.setText(document.get("followers").toString());

                            if(document.get("photoUrl") != null) {
                                Thread mThread = new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            URL url = new URL(document.get("photoUrl").toString());
                                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                            conn.setDoInput(true);
                                            conn.connect();
                                            InputStream is = conn.getInputStream();
                                            bitmap = BitmapFactory.decodeStream(is);


                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                };
                                mThread.start();
                                try {
                                    mThread.join();
                                    ((ImageView)findViewById(R.id.iv_프로필사진)).setImageBitmap(bitmap);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                        } else {

                        }
                    }

                } else {

                }
            }
        });


        collectionReference.whereEqualTo("작성자", uid).get() // .orderBy("createdAt", Query.Direction.DESCENDING)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final ArrayList<DB_Post_info> postList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if(document.getData().get("파일이름") != null) {
                                    postList.add(new DB_Post_info(
                                            document.getData().get("제목").toString(),
                                            document.getData().get("내용").toString(),
                                            document.getData().get("작성자").toString(),
                                            document.getData().get("파일이름").toString(),
                                            document.getData().get("파일url").toString(),
                                            new Date(document.getDate("createdAt").getTime()),
                                            document.getData().get("문서ID").toString(),
                                            document.getData().get("공감").toString(),
                                            document.getData().get("본횟수").toString(),
                                            document.getData().get("태그").toString(),
                                            document.getData().get("id").toString(),
                                            document.getData().get("소속").toString(),
                                            document.getData().get("주소").toString(),
                                            Integer.parseInt(document.getData().get("가격").toString())
                                    ));
                                } else {
                                    postList.add(new DB_Post_info(
                                            document.getData().get("제목").toString(),
                                            document.getData().get("내용").toString(),
                                            document.getData().get("작성자").toString(),
                                            new Date(document.getDate("createdAt").getTime()),
                                            document.getData().get("문서ID").toString(),
                                            document.getData().get("공감").toString(),
                                            document.getData().get("본횟수").toString(),
                                            document.getData().get("태그").toString(),
                                            document.getData().get("id").toString(),
                                            document.getData().get("소속").toString(),
                                            document.getData().get("주소").toString(),
                                            Integer.parseInt(document.getData().get("가격").toString())
                                    ));
                                }
                            }

                            recyclerView = findViewById(R.id.rv_내마켓);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(OtherUserPage.this));

                            RecyclerView.Adapter mAdapter = new MyPage_Adapter(OtherUserPage.this, postList);
                            recyclerView.setAdapter(mAdapter);


                            tv_포스트개수.setText(String.valueOf(mAdapter.getItemCount()));

                            ((MyPage_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_MyPage() {
                                @Override
                                public void OnItemClick(final int position, DB_Post_info db_post_info) {
                                    DocumentReference 포스트존재유무 = firebaseFirestore.collection("posts").document(postList.get(position).get문서ID());

                                    포스트존재유무.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Intent intent = new Intent(OtherUserPage.this, Board.class);
                                                    intent.putExtra("문서ID",postList.get(position).get문서ID());
                                                    startActivity(intent);
                                                } else {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(OtherUserPage.this);
                                                    builder.setMessage("삭제된 포스트 입니다");
                                                    builder.setPositiveButton("확인", null);
                                                    AlertDialog dialog = builder.create();
                                                    dialog.show();
                                                    onResume();
                                                }
                                            } else {
                                                //  Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });





                                }
                            });
                        } else {
                        }
                    }
                });

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());
        final CollectionReference col = documentReference1.collection("following");
        final DocumentReference docRef = col.document(uid);

        documentReference1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        팔로잉수 = document.getData().get("following").toString();
                    }
                }
            }
        });


        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document.exists()) {   // 팔로워 한 적이 있다면

                        btn_팔로우.setText("팔로잉 중..");
                        btn_팔로우.setBackground(getDrawable(R.drawable.follow));


                        btn_팔로우.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                btn_팔로우.setText("팔로우 하기");
                                btn_팔로우.setBackground(getDrawable(R.drawable.nonfollow));
                                Toast.makeText(OtherUserPage.this, "'팔로우' 취소", Toast.LENGTH_SHORT).show();
                                최종팔로워 = Integer.parseInt(tv_팔로워수.getText().toString()) - 1;

                                팔로잉수 = Integer.toString(Integer.parseInt(팔로잉수) - 1);

                                tv_팔로워수.setText(String.valueOf(최종팔로워));

                                docRef
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //Log.w(TAG, "Error deleting document", e);
                                            }
                                        });



                                onStop();
                                onResume();
                            }
                        });
                    } else {   // 팔로워 한 적이 없다면

                        btn_팔로우.setText("팔로우 하기");
                        btn_팔로우.setBackground(getDrawable(R.drawable.nonfollow));
                        btn_팔로우.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                btn_팔로우.setText("팔로잉 중..");
                                btn_팔로우.setBackground(getDrawable(R.drawable.follow));
                                Toast.makeText(OtherUserPage.this, "'팔로우'", Toast.LENGTH_SHORT).show();
                                최종팔로워 = Integer.parseInt(tv_팔로워수.getText().toString()) + 1;

                                팔로잉수 = Integer.toString(Integer.parseInt(팔로잉수) + 1);

                                tv_팔로워수.setText(String.valueOf(최종팔로워));

                                DB_Following dbFollowing = new DB_Following(uid, 팔로잉아이디);
                                storeUploader(docRef, dbFollowing);

                            }
                        });

                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    @Override
    public void onStop(){
        super.onStop();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentOther = firebaseFirestore.collection("users").document(uid);
        DocumentReference documentMy = firebaseFirestore.collection("users").document(firebaseUser.getUid());

        documentOther
                .update("followers", tv_팔로워수.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error updating document", e);
                    }
                });

        documentMy
                .update("following", 팔로잉수)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error updating document", e);
                    }
                });

        SharedPreferences pref =getSharedPreferences("userProfile", MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("following", 팔로잉수);
        editor.apply();

    }

    private void storeUploader(DocumentReference documentReference, DB_Following dbFollowing) {

        documentReference.set(dbFollowing)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        onStop();
                        onResume();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });


    }

}
