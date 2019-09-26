package com.example.sharedoc.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;


public class WritePost extends AppCompatActivity {
    private static final String TAG = "WritePost";
    EditText et_첨부파일이름, et_태그;
    TextView tv_업로드진행률;
    ImageView iv_btn_첨부파일;
    ProgressBar progressBar;
    private FirebaseUser user;
    private DB_Post_info write_info;
    Uri pdfUri;
    private RelativeLayout loaderLayout;
    DocumentReference dr;
    String id, 수정전본횟수, 수정전공감, 수정전파일url ="";
    Date 수정전날짜;
    Boolean 수정시업로드시도함 = false;
    SharedPreferences userProfile;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    int 문서가격 = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_write);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        et_첨부파일이름 = (EditText) findViewById(R.id.et_첨부파일이름);
        et_태그 = (EditText) findViewById(R.id.et_태그);
        tv_업로드진행률 = (TextView) findViewById(R.id.tv_업로드진행률);
        iv_btn_첨부파일 = (ImageView) findViewById(R.id.iv_btn_첨부파일);
        loaderLayout = (RelativeLayout) findViewById(R.id.loaderLayout);

        user = FirebaseAuth.getInstance().getCurrentUser();
        et_첨부파일이름.setVisibility(View.INVISIBLE);

        findViewById(R.id.iv_finishWriting).setOnClickListener(onClickListener);
        findViewById(R.id.iv_cancel).setOnClickListener(onClickListener);

        iv_btn_첨부파일.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(WritePost.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdf();
                } else {
                    ActivityCompat.requestPermissions(WritePost.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
                }
            }
        });



        write_info = (DB_Post_info) getIntent().getSerializableExtra("postInfo");


        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        if(id != null) {

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            dr = firebaseFirestore.collection("posts").document(id);
            dr.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {

                                ((EditText) findViewById(R.id.et_제목)).setText(document.get("제목").toString());
                                ((EditText) findViewById(R.id.et_내용)).setText(document.get("내용").toString());
                                et_태그.setText(document.get("태그").toString());

                                수정전날짜 =document.getDate("createdAt");
                                수정전공감 =document.get("공감").toString();
                                수정전본횟수 = document.get("본횟수").toString();
                                문서가격 = Integer.parseInt(document.get("가격").toString());   // 수정 전!


                               if (document.get("파일url") == null) {

                                } else {
                                    et_첨부파일이름.setVisibility(View.VISIBLE);
                                    et_첨부파일이름.setText(document.get("파일이름").toString());
                                   수정전파일url = document.get("파일url").toString();

                                }
                            } else {
                            }
                        }
                    } else {

                    }
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        } else {
            startToast("스토리지 접근 권한을 허용해주세요");
        }
    }

    private void selectPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_finishWriting:

                    if(!et_첨부파일이름.getText().toString().equals("")){     /** 여기서 코인 정하기!! */
                        setPrice();
                    } else {
                        writePost();    // 첨부파일이 없다면 코인 세팅 X
                    }
                    break;

                case R.id.iv_cancel:
                    finish();
                    break;

            }
        }
    };

    private void setPrice(){
        builder = new AlertDialog.Builder(WritePost.this);
        builder.setTitle("포스트 가격을 매겨주세요");
        builder.setCancelable(false);
        View view = LayoutInflater.from(WritePost.this).inflate(R.layout.dialog_set_coin, null, false);
        InitDialogSetCoin(view);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();

    }

    /** 카카오 충전 금액 정하기 */
    public void InitDialogSetCoin(View view) {

        EditText et_문서가격 = view.findViewById(R.id.et_문서가격);

        // et_문서가격 4자리 이상 설정 못하게 막기


        InputFilter[] IntroduceFilter = new InputFilter[]{
                new InputFilter.LengthFilter(4)  // et_충전금액 4자리 이상 충전 못하게 막기
        };
        et_문서가격.setFilters(IntroduceFilter);

        Button btn_확인 = view.findViewById(R.id.btn_확인);
        Button btn_취소 = view.findViewById(R.id.btn_취소);

        btn_확인.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_문서가격.getText() != null){
                    문서가격 = Integer.parseInt(et_문서가격.getText().toString());
                } else {
                    문서가격 = 0;
                }
                dialog.dismiss();
                writePost();
            }
        });
        btn_취소.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();

            수정시업로드시도함 = true;

            et_첨부파일이름.setVisibility(View.VISIBLE);
            et_첨부파일이름.setText(System.currentTimeMillis() + "");
            startToast(pdfUri.toString());
        } else {
            startToast("파일이 선택되지 않았습니다.");
        }
    }


    private void writePost() {



        final String 제목 = ((EditText) findViewById(R.id.et_제목)).getText().toString();
        final String 내용 = ((EditText) findViewById(R.id.et_내용)).getText().toString();
        final Date date = write_info == null ? new Date() : write_info.getCreatedAt();

        userProfile =getSharedPreferences("userProfile", MODE_PRIVATE);
        final String 아이디 = userProfile.getString("id", "");
        final String 소속 = userProfile.getString("school", "");
        final String 주소 = userProfile.getString("address", "");

        String tag;

        if(et_태그.getText() != null) {
            tag = et_태그.getText().toString();
            tag = tag.replaceAll(" ", "");
        } else {
            tag = "";
        }

        final String 태그 = tag;

        if (제목.length() > 0 && 내용.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);

            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();


            if(id == null) {
                dr = firebaseFirestore.collection("posts").document();

                final DocumentReference documentReference = dr;


                if (pdfUri == null) {
                    DB_Post_info write_info = new DB_Post_info(제목, 내용, user.getUid(), date, documentReference.getId(), "0", "0", 태그, 아이디, 소속, 주소,문서가격);
                    storeUploader(documentReference, write_info);
                } else {

                    StorageReference uploadRef = storageRef.child("게시글첨부파일/" + documentReference.getId() + "/" + et_첨부파일이름.getText().toString());

                    uploadRef.putFile(pdfUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String pdfUrl = uri.toString();
                                            progressBar.setVisibility(View.GONE);
                                            tv_업로드진행률.setText("업로드 완료");

                                            DB_Post_info write_info = new DB_Post_info(제목, 내용, user.getUid(), et_첨부파일이름.getText().toString()+".pdf", pdfUrl, date, documentReference.getId(), "0", "0", 태그, 아이디, 소속, 주소, 문서가격);
                                            storeUploader(documentReference, write_info);

                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            tv_업로드진행률.setVisibility(View.VISIBLE);
                            tv_업로드진행률.setText((int) progress + "% Uploading...");
                        }
                    });

                }

            } else {

                dr = firebaseFirestore.collection("posts").document(id);

                final DocumentReference documentReference = dr;


                if (pdfUri == null && 수정시업로드시도함 == false ) {

                    dr.update(
                            "제목", 제목,
                            "내용", 내용,
                            "태그", 태그
                            )
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });

                }
                else if(pdfUri == null && 수정시업로드시도함 == true){   // 문서삭제
                    Toast.makeText(WritePost.this, "첨부파일 없음", Toast.LENGTH_SHORT).show();
                    DB_Post_info write_info = new DB_Post_info(제목, 내용, user.getUid(), 수정전날짜, documentReference.getId(), 수정전공감, 수정전본횟수, 태그, 아이디, 소속, 주소, 문서가격);
                    storeUploader(documentReference, write_info);

                }
                else{

                    StorageReference uploadRef = storageRef.child("게시글첨부파일/" + documentReference.getId() + "/" + et_첨부파일이름.getText().toString());

                    uploadRef.putFile(pdfUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String pdfUrl = uri.toString();
                                            progressBar.setVisibility(View.GONE);
                                            tv_업로드진행률.setText("업로드 완료");

                                            DB_Post_info write_info = new DB_Post_info(제목, 내용, user.getUid(), et_첨부파일이름.getText().toString()+".pdf", pdfUrl, 수정전날짜, documentReference.getId(), 수정전공감, 수정전본횟수, 태그, 아이디, 소속, 주소, 문서가격);
                                            storeUploader(documentReference, write_info);

                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            tv_업로드진행률.setVisibility(View.VISIBLE);
                            tv_업로드진행률.setText((int) progress + "% Uploading...");
                        }
                    });

                }


            }



        } else {
            startToast("게시물을 바르게 작성해 주세요");
        }
    }

    private void storeUploader(DocumentReference documentReference, DB_Post_info write_info) {

        documentReference.set(write_info)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loaderLayout.setVisibility(View.GONE);
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage("작성중인 내용을 저장하지 않고 나가시겠습니까?");
        builder.setPositiveButton("확인", dialogListener);
        builder.setNegativeButton("취소", null);
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    DialogInterface.OnClickListener dialogListener=new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    };

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
