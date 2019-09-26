package com.example.sharedoc.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.sharedoc.DB.DB_Member_info;
import com.example.sharedoc.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    Boolean 카카오회원가입성공 = false;
    Boolean 카카오회원정보업로드 = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseFirestore = FirebaseFirestore.getInstance();

        Intent intent = new Intent();

        final String email = intent.getStringExtra("email");
        final String password = intent.getStringExtra("email");
        final String name = intent.getStringExtra("name");
        final String profile = intent.getStringExtra("profile");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {  // 이메일이 없다면 일단 회원가입 완료 후 로그인 진행
                            // Sign in success, update UI with the signed-in user's information

                            카카오회원가입성공 = true;
                            Handler timer = new Handler(); //Handler 생성

                            timer.postDelayed(new Runnable(){ //5초후 쓰레드를 생성하는 postDelayed 메소드
                                public void run(){

                                }
                            }, 1500); // 대기시간 1.5


                        } else {
                            if (task.getException() != null) {
                                //startToast("이미 존재하는 이메일입니다");
                                카카오회원정보업로드 = true;
                            }
                        }

                        // ...
                    }
                });
        if(카카오회원가입성공 == true){
            FirebaseUser user = mAuth.getCurrentUser();

            DB_Member_info memberInfo = new DB_Member_info("", name, "", "", "", profile, user.getUid(), "0", "0");
            DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
            documentReference.set(memberInfo)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                           // Log.d(TAG, "DocumentSnapshot successfully written!");
                            카카오회원정보업로드 = true;
                            Handler timer = new Handler(); //Handler 생성

                            timer.postDelayed(new Runnable(){ //5초후 쓰레드를 생성하는 postDelayed 메소드
                                public void run(){

                                }
                            }, 1500); // 대기시간 1.5
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                           // Log.w(TAG, "Error writing document", e);
                        }
                    });
        }

        if(카카오회원정보업로드 = true){
            final FirebaseUser user = mAuth.getCurrentUser();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                              //  Log.d(TAG, "signInWithEmail:success");

                                //startToast("로그인 성공");
                                DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
                                // user 의 파이어베이스에 있는 정보를 sharedPreference 로 업데이트;
                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            final DocumentSnapshot document = task.getResult();

                                            if (document != null) {
                                                if (document.exists()) {

                                                    // 쉐어드에 저장!
                                                    SharedPreferences pref =getSharedPreferences("userProfile", MODE_PRIVATE);

                                                    SharedPreferences.Editor editor = pref.edit();
                                                    editor.putString("id", document.get("id").toString());
                                                    editor.putString("name", document.get("name").toString());
                                                    editor.putString("introduce", document.get("introduce").toString());

                                                    if(document.get("followers") != null){
                                                        editor.putString("followers", document.get("followers").toString());
                                                    } else {
                                                        editor.putString("followers", "0");
                                                    }

                                                    if(document.get("following") != null){
                                                        editor.putString("following", document.get("following").toString());
                                                    } else {
                                                        editor.putString("following","0");
                                                    }


                                                    if(document.get("school") != null) {
                                                        editor.putString("school", document.get("school").toString());
                                                    }
                                                    if(document.get("major") != null) {
                                                        editor.putString("major", document.get("major").toString());
                                                    }
                                                    if(document.get("photoUrl")!= null) {
                                                        editor.putString("photoUrl", document.get("photoUrl").toString());
                                                    }

                                                    editor.apply();
                                                    //startToast("데이터 받아옴");
                                                }
                                            }

                                        }
                                    }
                                });

                                Handler timer = new Handler(); //Handler 생성

                                timer.postDelayed(new Runnable(){ //5초후 쓰레드를 생성하는 postDelayed 메소드
                                    public void run(){

                                        Intent intent = new Intent(MainActivity.this, MyPage.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 5000); // 쉐어드 저장하는 대기시간 5초!


                            } else {
                                if (task.getException() != null) {
                                 //   loaderLayout.setVisibility(View.GONE);
                                }
                            }

                        }
                    });
        }

    }
}