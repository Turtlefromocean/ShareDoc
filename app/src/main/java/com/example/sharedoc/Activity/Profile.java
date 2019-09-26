package com.example.sharedoc.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedoc.DB.DB_Member_info;
import com.example.sharedoc.R;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;


public class Profile extends AppCompatActivity {

    public String id, name, introduce, school, major, photoUrl;
    public String profilePath, followers, following;
    public EditText etName, etIntroduce, et_전공, et_아이디;
    public TextView tv_아이디, tv_이메일계정, tv_소속위치;
    public Button btn_중복검사, btn_취소, btn_회원탈퇴;
    ImageView profileImage, iv_btn_editID, btn_뒤로가기, iv_btn_map;
    String 프로필사진url;
    int 파일업로드여부;
    private FirebaseUser user;
    private RelativeLayout loaderLayout;
    public Uri downloadUri;
    DocumentReference dr;
    Bitmap bitmap;
    Boolean 중복 = false;

    AlertDialog.Builder builder;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = (ImageView) findViewById(R.id.profileImage);
        loaderLayout = (RelativeLayout) findViewById(R.id.loaderLayout);
        btn_뒤로가기 = (ImageButton) findViewById(R.id.btn_뒤로가기);
        iv_btn_map = (ImageView) findViewById(R.id.iv_btn_map);
        btn_회원탈퇴 = (Button) findViewById(R.id.btn_회원탈퇴);

        user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.btn_저장).setOnClickListener(onClickListener);
        findViewById(R.id.btn_uploadPic).setOnClickListener(onClickListener);
        iv_btn_editID = (ImageView) findViewById(R.id.iv_btn_editID);

        profileImage = (ImageView) findViewById(R.id.profileImage);
        loaderLayout = (RelativeLayout) findViewById(R.id.loaderLayout);
        btn_뒤로가기 = (ImageButton) findViewById(R.id.btn_뒤로가기);
        iv_btn_map = (ImageView) findViewById(R.id.iv_btn_map);
        btn_회원탈퇴 = (Button) findViewById(R.id.btn_회원탈퇴);

        user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.btn_저장).setOnClickListener(onClickListener);
        findViewById(R.id.btn_uploadPic).setOnClickListener(onClickListener);
        iv_btn_editID = (ImageView) findViewById(R.id.iv_btn_editID);

        iv_btn_editID.setOnClickListener(new View.OnClickListener() {   //id 편집 시 다이얼로그 띄워서 중복검사
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("닉네임을 입력해주세요");
                builder.setCancelable(false);
                View view = LayoutInflater.from(Profile.this).inflate(R.layout.dialog_id_profile, null, false);
                InitUpdateDialog(view);
                builder.setView(view);
                dialog = builder.create();
                dialog.show();
            }
        });

        btn_회원탈퇴.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(Profile.this);
                builder.setMessage("정말 회원탈퇴를 진행하시겠습니까?");
                builder.setPositiveButton("확인", dialogListener);
                builder.setNegativeButton("취소", null);
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });




        iv_btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, Map.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        profileImage = (ImageView) findViewById(R.id.profileImage);
        loaderLayout = (RelativeLayout) findViewById(R.id.loaderLayout);
        btn_뒤로가기 = (ImageButton) findViewById(R.id.btn_뒤로가기);
        iv_btn_map = (ImageView) findViewById(R.id.iv_btn_map);
        btn_회원탈퇴 = (Button) findViewById(R.id.btn_회원탈퇴);

        findViewById(R.id.btn_저장).setOnClickListener(onClickListener);
        findViewById(R.id.btn_uploadPic).setOnClickListener(onClickListener);
        iv_btn_editID = (ImageView) findViewById(R.id.iv_btn_editID);

        iv_btn_editID.setOnClickListener(new View.OnClickListener() {   //id 편집 시 다이얼로그 띄워서 중복검사
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("닉네임을 입력해주세요");
                builder.setCancelable(false);
                View view = LayoutInflater.from(Profile.this).inflate(R.layout.dialog_id_profile, null, false);
                InitUpdateDialog(view);
                builder.setView(view);
                dialog = builder.create();
                dialog.show();
            }
        });

        btn_회원탈퇴.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(Profile.this);
                builder.setMessage("정말 회원탈퇴를 진행하시겠습니까?");
                builder.setPositiveButton("확인", dialogListener);
                builder.setNegativeButton("취소", null);
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });




        iv_btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Profile.this, Map.class);
                startActivity(intent);
            }
        });



        Intent 처음확인인텐트 = getIntent();
        if(처음확인인텐트 != null) {

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser();

            dr = firebaseFirestore.collection("users").document(user.getUid());
            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
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
                                    프로필사진url = document.get("photoUrl").toString();
                                }

                            } else {
                            }
                        }
                    } else {

                    }
                }
            });
        }

        // sharedPreference 에서 저장된 값 불러오기
        SharedPreferences userProfile =getSharedPreferences("userProfile", MODE_PRIVATE);

        id = userProfile.getString("id", "");
        name = userProfile.getString("name", "");
        introduce = userProfile.getString("introduce", "");
        school = userProfile.getString("school", "");
        major = userProfile.getString("major", "");
        photoUrl = userProfile.getString("photoUrl", "");
        String profilePath = userProfile.getString("profilePath", "");

        followers = userProfile.getString("followers", "0");
        following = userProfile.getString("following", "0");

        tv_아이디 = (TextView) findViewById(R.id.tv_아이디);
        tv_이메일계정 = (TextView) findViewById(R.id.tv_이메일계정);
        etName = (EditText) findViewById(R.id.etName);
        etIntroduce = (EditText) findViewById(R.id.etIntroduce);
        tv_소속위치= (TextView) findViewById(R.id.tv_소속위치);
        et_전공 = (EditText) findViewById(R.id.et_전공);

        if(id.length() != 0){
            btn_뒤로가기.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            Toast.makeText(Profile.this, "프로필 정보를 바르게 입력해주세요", Toast.LENGTH_SHORT).show();
        }

        InputFilter[] nameFilter = new InputFilter[]{
                new InputFilter.LengthFilter(6)
        };
        etName.setFilters(nameFilter);

        InputFilter[] IntroduceFilter = new InputFilter[]{
                new InputFilter.LengthFilter(58)
        };

        InputFilter[] selectedInfo = new InputFilter[]{
                new InputFilter.LengthFilter(10)
        };

        etIntroduce.setFilters(IntroduceFilter);

        et_전공.setFilters(selectedInfo); /** 주관심분야 */

        tv_아이디.setText(id);
        etName.setText(name);
        etIntroduce.setText(introduce);



        tv_이메일계정.setText(user.getEmail());


        if(!profilePath.equals("")){
            Bitmap bmp = BitmapFactory.decodeFile(profilePath);
            profileImage.setImageBitmap(bmp);
        } else if(!photoUrl.equals("")) {
            Thread mThread = new Thread() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(photoUrl);
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
                profileImage.setImageBitmap(bitmap);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Intent 소속받아오기 = getIntent();
        if(소속받아오기.getStringExtra("소속") != null){
            tv_소속위치 = findViewById(R.id.tv_소속위치);
            tv_소속위치.setText(소속받아오기.getStringExtra("소속"));
            SharedPreferences pref = getSharedPreferences("userProfile", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("address", 소속받아오기.getStringExtra("address"));
            editor.apply();
        } else {
            tv_소속위치.setText(school);
        }

        et_전공.setText(major);


    }

    DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i("파이어베이스회원탈퇴", "성공");
                                회원데이터베이스삭제();
                                startToast("회원탈퇴가 완료되었습니다");



                            /** 쉐어드 초기화 */
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
                                editor.apply();

                                Intent intent = new Intent(Profile.this, Login.class);
                                startActivity(intent);
                                finish();

                            } else {
                                Log.i("파이어베이스회원탈퇴", "실패");
                            }
                        }
                    });



        }
    };

    private void 회원데이터베이스삭제(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("파이어베이스회원탈퇴", "문서삭제완료");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("파이어베이스회원탈퇴", "문서삭제실패");
                    }
                });

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_저장:
                    addProfile();
                    break;

                case R.id.btn_uploadPic:
                    if (ContextCompat.checkSelfPermission(Profile.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        if (ActivityCompat.shouldShowRequestPermissionRationale(Profile.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {

                            ActivityCompat.requestPermissions(Profile.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                        } else {

                            ActivityCompat.requestPermissions(Profile.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            startToast("권한을 허용해 주세요");

                        }
                    } else {
                        Intent intent = new Intent(Profile.this, Gallery.class);
                        startActivityForResult(intent, 0);
                    }
                    파일업로드여부 = 10; //업로드 함!
                    break;

            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(Profile.this, Gallery.class);
                    startActivityForResult(intent, 0);

                } else {
                    startToast("권한을 허용해 주세요");
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: {
                if (resultCode == Activity.RESULT_OK) {
                    profilePath = data.getStringExtra("profilePath");
                    Bitmap bmp = BitmapFactory.decodeFile(profilePath);

                    profileImage.setImageBitmap(bmp);

                }
            }
        }
    }




    private void addProfile() {

        final String id = ((TextView) findViewById(R.id.tv_아이디)).getText().toString();
        final String name = ((EditText) findViewById(R.id.etName)).getText().toString();
        final String introduce = ((EditText) findViewById(R.id.etIntroduce)).getText().toString();

        final String school = ((TextView) findViewById(R.id.tv_소속위치)).getText().toString();  //선택사항
        final String major = ((EditText) findViewById(R.id.et_전공)).getText().toString();   //선택사항


        if (id.length() > 0 && name.length() > 0 && introduce.length() > 0) {

            loaderLayout.setVisibility(View.VISIBLE);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            user = FirebaseAuth.getInstance().getCurrentUser();
            SharedPreferences pref = getSharedPreferences("userProfile", Activity.MODE_PRIVATE);
            String address = pref.getString("address","");
            final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");

            if (profilePath == null && 프로필사진url == null) {
                DB_Member_info memberInfo = new DB_Member_info(id, name, introduce, school, major, user.getUid(), followers, following, address);
                storeUploader(memberInfo);
            } else if (파일업로드여부 == 10) {
                try {
                    InputStream stream = new FileInputStream(new File(profilePath));
                    UploadTask uploadTask = mountainImagesRef.putStream(stream);
                    uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return mountainImagesRef.getDownloadUrl();
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadUri = task.getResult();

                                DB_Member_info  memberInfo = new DB_Member_info(id, name, introduce, school, major, downloadUri.toString(), user.getUid(), followers, following, address);
                                storeUploader(memberInfo);
                            } else {
                                startToast("프로필 정보 저장 실패");
                            }
                        }
                    });
                } catch (FileNotFoundException e) {
                    Log.e("로그", "에러: " + e.toString());
                }

            } else {
                DB_Member_info memberInfo = new DB_Member_info(id, name, introduce, school, major, 프로필사진url, user.getUid(), followers, following, address);
                storeUploader(memberInfo);
            }
        } else {
            startToast("프로필 정보를 바르게 입력해 주세요");
        }
    }

    private void storeUploader(DB_Member_info memberInfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).set(memberInfo)
                .addOnSuccessListener(aVoid -> {

                    SharedPreferences pref =getSharedPreferences("userProfile", MODE_PRIVATE);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("id", tv_아이디.getText().toString());
                    editor.putString("name", etName.getText().toString());
                    editor.putString("introduce", etIntroduce.getText().toString());

                    if(tv_소속위치.getText() != null) {
                        editor.putString("school", tv_소속위치.getText().toString());
                    }
                    if(et_전공.getText() != null) {
                        editor.putString("major", et_전공.getText().toString());
                    }
                    if(profilePath != null) {
                        editor.putString("profilePath", profilePath);
                    }
                    if(downloadUri != null) {
                        editor.putString("photoUrl", downloadUri.toString());
                    }

                    editor.apply();


                    startToast("프로필 등록 성공");
                    loaderLayout.setVisibility(View.GONE);
                    Intent intent = new Intent(Profile.this, MyPage.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        startToast("프로필 등록 실패");
                        loaderLayout.setVisibility(View.GONE);
                    }
                });
    }


    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected class CustomInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start,
                                   int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[-_a-zA-Z0-9]+$");

            if(source.equals("") || ps.matcher(source).matches()){
                return source;
            }

            Toast.makeText(Profile.this, "영문, 숫자, _ , -만 입력 가능합니다.", Toast.LENGTH_SHORT).show();

            return "";
        }

    }

    public void InitUpdateDialog(View view) {   //아이디 편집 다이얼로그

        et_아이디 = view.findViewById(R.id.et_아이디);
        btn_중복검사 = view.findViewById(R.id.btn_중복검사);
        btn_취소 = view.findViewById(R.id.btn_취소);


        if(tv_아이디.getText() != null){
            et_아이디.setText(tv_아이디.getText().toString());
        }
        // 글자수 15자리 & 영문, 숫자, 밑줄만 입력 제한
        InputFilter[] filters = new InputFilter[]{
                new InputFilter.LengthFilter(15),
                new CustomInputFilter()
        };
        et_아이디.setFilters(filters);

        // 포커스가 주어졌을 때 보여지는 키보드의 타입을 영어로 설정.
        et_아이디.setPrivateImeOptions("defaultInputmode=english;");


        btn_중복검사.setOnClickListener(v -> {   //아이디 중복검사
            중복 = false;
            if(et_아이디.getText().length() != 0){
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                CollectionReference collectionReference = firebaseFirestore.collection("users");

                collectionReference.whereEqualTo("id", et_아이디.getText().toString())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    startToast("이미 사용하고 있는 닉네임 입니다");  // 동작..
                                    중복 = true;
                                }
                                if(중복 == false){
                                    startToast("사용하실 수 있는 닉네임 입니다");
                                    tv_아이디.setText(et_아이디.getText().toString());
                                    dialog.dismiss();
                                }
                            } else {

                            }
                        });
            } else {
                startToast("아이디를 입력해주세요");

            }

        });
        btn_취소.setOnClickListener(v -> dialog.dismiss());
    }


    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences pref =getSharedPreferences("userProfile", MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();

        if(tv_아이디.getText() != null) {
            editor.putString("id", tv_아이디.getText().toString());
        }

        if(etName.getText() != null) {
            editor.putString("name", etName.getText().toString());
        }

        if(etIntroduce.getText() != null) {
            editor.putString("introduce", etIntroduce.getText().toString());
        }

        if(tv_소속위치.getText() != null) {
            editor.putString("school", tv_소속위치.getText().toString());
        }
        if(et_전공.getText() != null) {
            editor.putString("major", et_전공.getText().toString());
        }
        if(profilePath != null) {
            editor.putString("profilePath", profilePath);
        }
        if(downloadUri != null) {
            editor.putString("photoUrl", downloadUri.toString());
        }

        editor.apply();

    }






}
