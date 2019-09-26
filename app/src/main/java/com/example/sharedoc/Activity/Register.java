package com.example.sharedoc.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.sharedoc.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        checkBox = (CheckBox) findViewById(R.id.checkBox);
        findViewById(R.id.btnDone).setOnClickListener(onClickListener);

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
                case R.id.btnDone:
                    signUp();
                    break;
            }
        }
    };

    private void signUp() {
        String email = ((EditText) findViewById(R.id.etEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();
        String passwordCheck = ((EditText) findViewById(R.id.etPasswordConfirm)).getText().toString();

        if(checkBox.isChecked() == true){
            if (email.length() > 0 && password.length() > 5 && passwordCheck.length() > 5) {
                if (password.equals(passwordCheck)) {
                    final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
                    loaderLayout.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    loaderLayout.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        startToast("회원가입 완료");
                                        //UI
                                        finish();
                                    } else {
                                        if (task.getException() != null) {
                                            startToast("이미 존재하는 이메일입니다");
                                        }
                                    }

                                    // ...
                                }
                            });
                } else {
                    startToast("비밀번호가 일치하지 않습니다.");
                    ((EditText) findViewById(R.id.etPassword)).setText("");
                }
            } else {
                startToast("정보가 제대로 입력되었는지 확인해주세요.");
            }
        } else {
            startToast("약관 동의를 체크해주세요");
        }

    }



    private void startToast(String msg) {
        Toast.makeText(this, msg,Toast.LENGTH_SHORT).show();
    }
}
