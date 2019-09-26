package com.example.sharedoc.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.sharedoc.Adapter.Following_Adapter;
import com.example.sharedoc.DB.DB_Following;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Following;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Following extends AppCompatActivity {

    String 팔로잉수;
    TextView tv_팔로잉수;
    ImageButton btn_뒤로가기;

    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        tv_팔로잉수 = findViewById(R.id.tv_팔로잉수);
        btn_뒤로가기 = findViewById(R.id.btn_뒤로가기);


        btn_뒤로가기.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        팔로잉수 = intent.getStringExtra("팔로잉수");
        tv_팔로잉수.setText(팔로잉수);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.rv_팔로잉리스트);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Following.this));

    }

    @Override
    public void onResume(){
        super.onResume();

        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());
        final CollectionReference col = documentReference1.collection("following");

        col.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final ArrayList<DB_Following> dbFollowings = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                dbFollowings.add(new DB_Following(
                                        document.getData().get("uid").toString(),
                                        document.getData().get("id").toString()
                                ));


                            }

                            RecyclerView.Adapter mAdapter = new Following_Adapter(Following.this, dbFollowings);
                            recyclerView.setAdapter(mAdapter);

                            ((Following_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_Following() {
                                @Override
                                public void OnItemClick(int position, DB_Following dbFollowing) {
                                    Intent intent = new Intent(Following.this, OtherUserPage.class);
                                    intent.putExtra("uid",dbFollowings.get(position).getUid());
                                    startActivity(intent);
                                }
                            });
                        } else {

                        }
                    }
                });
    }

}
