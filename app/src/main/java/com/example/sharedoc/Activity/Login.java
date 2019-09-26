package com.example.sharedoc.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.sharedoc.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ApiErrorCode;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import org.json.JSONObject;

import java.util.HashMap;


public class Login extends AppCompatActivity {

    private static final String TAG = "Login.activity";
    private FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    private SessionCallback sessionCallback; //카카오로그인api
    String 카카오프로필사진, 카카오이름;

    LoginButton loginButton;
    RelativeLayout loaderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        findViewById(R.id.btn_login).setOnClickListener(onClickListener);
        findViewById(R.id.btn_signIn).setOnClickListener(onClickListener);
        findViewById(R.id.btn_findPassword).setOnClickListener(onClickListener);
        loaderLayout = findViewById(R.id.loaderLayout);
        loginButton = findViewById(R.id.kakaologin);

        sessionCallback = new SessionCallback();
        Session.getCurrentSession().addCallback(sessionCallback);
        Session.getCurrentSession().checkAndImplicitOpen();


    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_login:
                    login();
                    break;

                case R.id.btn_signIn:
                    Intent intent = new Intent(Login.this, Register.class);
                    startActivity(intent);
                    break;

                case R.id.btn_findPassword:
                    Intent intent2 = new Intent(Login.this, FindPassword.class);
                    startActivity(intent2);
                    break;
            }
        }
    };

    private void login() {

            String email = ((EditText) findViewById(R.id.etEmail)).getText().toString();
            String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();


            if (email.length() > 0 && password.length() > 0) {


                loaderLayout.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //startToast("로그인 성공");

                                    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                    // user 의 파이어베이스에 있는 정보를 sharedPreference 로 업데이트;
                                    DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
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
                                                        if(document.get("address") != null) {
                                                            editor.putString("address", document.get("address").toString());
                                                        }

                                                        editor.apply();
                                                        //startToast("데이터 받아옴");
                                                    }
                                                }

                                            } else {
                                                Log.d(TAG, "get failed with ", task.getException());

                                                SharedPreferences pref = getSharedPreferences("userProfile", Activity.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = pref.edit();

                                                editor.putString("id", "");
                                                editor.putString("name", "");
                                                editor.putString("introduce", "");
                                                editor.putString("school", "");
                                                editor.putString("major", "");
                                                editor.putString("profilePath", "");
                                                editor.putString("photoUrl", "");
                                                editor.putString("followers", "0");
                                                editor.putString("following", "0");
                                                editor.putString("address", "");
                                                editor.apply();
                                            }
                                        }
                                    });

                                    Handler timer = new Handler(); //Handler 생성

                                    timer.postDelayed(new Runnable(){ //5초후 쓰레드를 생성하는 postDelayed 메소드
                                        public void run(){
                                            loaderLayout.setVisibility(View.GONE);
                                            Intent intent = new Intent(Login.this, MyPage.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 5000); // 쉐어드 저장하는 대기시간 5초!


                                } else {
                                    if (task.getException() != null) {
                                        loaderLayout.setVisibility(View.GONE);
                                        startToast("이메일 또는 비밀번호를 확인해주세요");
                                    }
                                }

                            }
                        });

            } else {
                startToast("이메일 또는 비밀번호를 확인해주세요.");
            }


    }

    private void startToast(String msg) {
        Toast.makeText(this, msg,Toast.LENGTH_SHORT).show();
    }

    /** 카카오 로그인 api*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();

                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {

                    Log.e("카카오로그인 성공", String.valueOf(result.getId()));
                    kakaoRegister(String.valueOf(result.getId())+"@kakao.com", "000000");
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void kakaoRegister(String email, String password){
        final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
        loaderLayout.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            startToast("회원가입 완료");
                            Log.e("카카오로그인", "카카오계정으로 회원가입 완료");
                            KakaoLogin(email, password);

                        } else {
                            if (task.getException() != null) {

                                Log.e("카카오로그인", "이미존재하는계정, 바로 로그인 시작");
                                KakaoLogin(email, password);
                            }
                        }

                        // ...
                    }
                });
    }

    public void KakaoLogin(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //startToast("로그인 성공");
                            Log.e("카카오로그인", "카카오계정으로 로그인 완료");
                            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                            // user 의 파이어베이스에 있는 정보를 sharedPreference 로 업데이트;
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(user.getUid());
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
                                                if(document.get("address") != null) {
                                                    editor.putString("address", document.get("address").toString());
                                                }

                                                editor.apply();
                                                //startToast("데이터 받아옴");
                                            }
                                        }

                                    } else {
                                        Log.d(TAG, "get failed with ", task.getException());

                                        SharedPreferences pref = getSharedPreferences("userProfile", Activity.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();

                                        editor.putString("id", "");
                                        editor.putString("name", "");
                                        editor.putString("introduce", "");
                                        editor.putString("school", "");
                                        editor.putString("major", "");
                                        editor.putString("profilePath", "");
                                        editor.putString("photoUrl", "");
                                        editor.putString("followers", "0");
                                        editor.putString("following", "0");
                                        editor.putString("address", "");
                                        editor.apply();
                                    }
                                }
                            });

                            Handler timer = new Handler(); //Handler 생성

                            timer.postDelayed(new Runnable(){ //5초후 쓰레드를 생성하는 postDelayed 메소드
                                public void run(){
                                    loaderLayout.setVisibility(View.GONE);
                                    Intent intent = new Intent(Login.this, MyPage.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 5000); // 쉐어드 저장하는 대기시간 5초!


                        } else {
                            if (task.getException() != null) {
                                loaderLayout.setVisibility(View.GONE);
                               // startToast("이메일 또는 비밀번호를 확인해주세요");
                            }
                        }

                    }
                });

    }
}