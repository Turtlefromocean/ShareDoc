package com.example.sharedoc.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;

import com.example.sharedoc.Adapter.Home_Adapter;
import com.example.sharedoc.Adapter.Home_all_Adapter;
import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Home;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class Home_all extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseFirestore firebaseFirestore;
    ImageButton btn_뒤로가기;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_all);

        recyclerView = findViewById(R.id.rv_home_전체포스트_전체보기);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Home_all.this));

        firebaseFirestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        btn_뒤로가기 = findViewById(R.id.btn_뒤로가기);
        btn_뒤로가기.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        /** Home 에서 팔로잉 포스트만 거르는 거랑 같음! */

        final CollectionReference collectionReference = firebaseFirestore.collection("posts");

        collectionReference.orderBy("createdAt", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final ArrayList<DB_Post_info> postList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference documentReference1 = firebaseFirestore.collection("users").document(user.getUid());
                                final CollectionReference col = documentReference1.collection("다운로드한문서");
                                final DocumentReference docRef = col.document(document.getData().get("문서ID").toString());

                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot 문서 = task.getResult(); /**중요!!!!*/
                                            if (문서.exists()) {   // 문서를 다운받았다면

                                                //Toast.makeText(Home.this, "following", Toast.LENGTH_SHORT).show();

                                                if (document.getData().get("파일이름") != null) {
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

                                                RecyclerView.Adapter mAdapter = new Home_all_Adapter(Home_all.this, postList);
                                                recyclerView.setAdapter(mAdapter);

                                                ((Home_all_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_Home() {
                                                    @Override
                                                    public void OnItemClick(final int position, DB_Post_info db_post_info) {

                                                        DocumentReference 포스트존재유무 = firebaseFirestore.collection("posts").document(postList.get(position).get문서ID());

                                                        포스트존재유무.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();
                                                                    if (document.exists()) {
                                                                        Intent intent = new Intent(Home_all.this, Board.class);
                                                                        intent.putExtra("문서ID", postList.get(position).get문서ID());
                                                                        startActivity(intent);
                                                                    } else {
                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(Home_all.this);
                                                                        builder.setMessage("삭제된 포스트 입니다");
                                                                        builder.setPositiveButton("확인", null);
                                                                        AlertDialog dialog = builder.create();
                                                                        dialog.show();
                                                                        onResume();
                                                                    }
                                                                } else {
                                                                    // Log.d(TAG, "get failed with ", task.getException());
                                                                }
                                                            }
                                                        });



                                                    }
                                                });

                                            } else {   // 팔로워가 아니라면

                                            }
                                        } else {
                                            //Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });



                            }


                        } else {
                        }
                    }
                });

    }
}
