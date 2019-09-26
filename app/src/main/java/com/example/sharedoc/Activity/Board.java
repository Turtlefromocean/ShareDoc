package com.example.sharedoc.Activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedoc.Adapter.Comment_Adapter;
import com.example.sharedoc.DB.DB_Comment;
import com.example.sharedoc.DB.DB_Following;
import com.example.sharedoc.DB.DB_Member_coin;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Comment;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Board extends AppCompatActivity {

    String  본횟수, 삭제댓글ID;
    TextView tv_제목, tv_내용, tv_태그, tv_공감수, tv_본횟수, tv_수정, tv_삭제,
            tv_첨부파일이름, tv_btn_첨부파일다운, tv_작성자ID, tv_위치, tv_가격, tv_코인;
    ImageView iv_btn_공감, iv_태그더보기, iv_btn_카톡링크;
    EditText et_댓글;
    CardView cardView_첨부파일;
    RecyclerView recyclerView;
    String 파일url;

    Comment_Adapter adapter_comment;

    private FirebaseFirestore firebaseFirestore;

    private FirebaseUser user;
    private DB_Comment dbComment;

    String 문서ID, 다운로드url, 문서작성자Uid;
    Date date;
    int 최종공감수;
    Boolean clicked = false;
    Boolean 구입여부 = false;
    Boolean 확인 = false;
    public Context bContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_board);
        bContext = this;

        firebaseFirestore = FirebaseFirestore.getInstance();

        tv_작성자ID = (TextView) findViewById(R.id.tv_작성자ID);
        tv_제목 = (TextView) findViewById(R.id.tv_제목);
        tv_내용 = (TextView) findViewById(R.id.tv_내용);
        tv_태그 = (TextView) findViewById(R.id.tv_태그);
        tv_수정 = (TextView) findViewById(R.id.tv_수정);
        tv_삭제 = (TextView) findViewById(R.id.tv_삭제);
        tv_첨부파일이름 = (TextView) findViewById(R.id.et_첨부파일이름);
        tv_btn_첨부파일다운 = (TextView) findViewById(R.id.tv_btn_첨부파일다운);
        et_댓글 = (EditText) findViewById(R.id.et_댓글);
        tv_공감수 = (TextView) findViewById(R.id.tv_공감수);
        tv_본횟수 = (TextView) findViewById(R.id.tv_본횟수);
        iv_태그더보기 = (ImageView) findViewById(R.id.iv_태그더보기);
        tv_위치 = (TextView) findViewById(R.id.tv_위치);
        tv_가격 = (TextView) findViewById(R.id.tv_가격);
        tv_코인 = (TextView) findViewById(R.id.tv_코인);
        cardView_첨부파일 = (CardView) findViewById(R.id.cardView_첨부파일);

        iv_btn_카톡링크 = (ImageView) findViewById(R.id.iv_btn_카톡링크);
        iv_btn_공감 = (ImageView) findViewById(R.id.iv_btn_공감);

        findViewById(R.id.btn_댓글).setOnClickListener(onClickListener);
        findViewById(R.id.btn_뒤로가기).setOnClickListener(onClickListener);

        user = FirebaseAuth.getInstance().getCurrentUser();


        recyclerView = findViewById(R.id.rv_댓글);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if(intent.getStringExtra("문서ID") != null){
            문서ID = intent.getStringExtra("문서ID");
        }

        Intent intent2	=	getIntent();   /** 만일 카카오 링크를 통해 들어오게 된다면! */
        Uri uri	=	intent2.getData();

        if(uri != null)
        {
            문서ID	=	uri.getQueryParameter("key1");

            if(문서ID != null)
                Log.d("key1", 문서ID);
        }

        /** 카톡 링크 공유! */
        iv_btn_카톡링크.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedTemplate params = FeedTemplate
                        .newBuilder(ContentObject.newBuilder("포스트공유",
                                "https://firebasestorage.googleapis.com/v0/b/sharedoc-cd259.appspot.com/o/sharedoc_kakao.png?alt=media&token=a22813ca-1c9f-45a9-b033-4710fd051d2f",
                                LinkObject.newBuilder().build())
                                .build())
                        .addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
                                .setAndroidExecutionParams("key1="+문서ID) //요기에 있는 key1 을 나중에 intent 로 uri 가져올때 씀!
                                .build()))
                        .build();

                Map<String, String> serverCallbackArgs = new HashMap<String, String>();

                serverCallbackArgs.put("executeurl", 문서ID);

               // serverCallbackArgs.put("product_id", "${shared_product_id}");

                KakaoLinkService.getInstance().sendDefault(Board.this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Logger.e(errorResult.toString());
                    }

                    @Override
                    public void onSuccess(KakaoLinkResponse result) {
                        // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                    }
                });


            }
        });
        /// 카톡 링크 공유 끝

        final DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                firebaseFirestore = FirebaseFirestore.getInstance();

                firebaseFirestore.collection("posts").document(문서ID)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Board.this, "포스트를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                finish();
            }
        };

        tv_삭제.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Board.this);
            builder.setMessage("이 게시물을 삭제하시겠습니까?");
            builder.setPositiveButton("삭제", dialogListener);
            builder.setNegativeButton("취소", null);
            AlertDialog dialog = builder.create();
            dialog.show();

        });

        tv_수정.setOnClickListener(v -> {
            Intent intent13 = new Intent(Board.this, WritePost.class);
            intent13.putExtra("id", 문서ID);
            startActivity(intent13);
        });

        DocumentReference documentReference = firebaseFirestore.collection("posts").document(문서ID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();

                    if (document != null) {
                        if (document.exists()) {

                            문서작성자Uid = document.get("작성자").toString();

                            if (document.get("작성자").equals(user.getUid())) {
                                tv_삭제.setVisibility(View.VISIBLE);
                                tv_수정.setVisibility(View.VISIBLE);
                            }

                            tv_제목.setText(document.get("제목").toString());
                            tv_내용.setText(document.get("내용").toString());

                            if (document.get("파일url") != null) {
                                cardView_첨부파일.setVisibility(View.VISIBLE);
                                tv_첨부파일이름.setText(document.get("파일이름").toString());
                                파일url = document.get("파일url").toString();

                                if(Integer.parseInt(document.get("가격").toString()) != 0){
                                    tv_가격.setText(document.get("가격").toString());
                                    tv_코인.setVisibility(View.VISIBLE);
                                } else {
                                    tv_가격.setText("무료");

                                }

                            } else {
                                cardView_첨부파일.setVisibility(View.INVISIBLE);
                            }

                            if(document.get("태그") != null) {
                                tv_태그.setText(document.get("태그").toString());

                                tv_태그.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        final int lineCnt = tv_태그.getLineCount();
                                        if(lineCnt > 1){
                                            iv_태그더보기.setVisibility(View.VISIBLE);
                                            iv_태그더보기.setOnClickListener(v -> {
                                                if(clicked != true){
                                                    tv_태그.setMaxLines(lineCnt);
                                                    clicked = true;
                                                } else {
                                                    tv_태그.setMaxLines(1);
                                                    clicked = false;
                                                }

                                            });
                                        } else {
                                            Log.e("댓글 줄 수", String.valueOf(lineCnt));
                                            tv_태그.setText(document.get("태그").toString());
                                        }
                                    }
                                });

                            }

                            if (document.get("소속") != null) {
                                tv_위치.setText(document.get("소속").toString());
                            }



                            tv_공감수.setText(document.get("공감").toString());
                            tv_본횟수.setText(document.get("본횟수").toString());
                            본횟수 = document.get("본횟수").toString();

                            tv_btn_첨부파일다운.setOnClickListener(v -> {                               /** 여기에 결제 창 넣기! */
                                // 일단 자기가 문서 주인이면 바로 다운
                                // 다운받은 파일 목록에 있어도 바로 다운!


                                DocumentReference documentReference_구입포스트 = firebaseFirestore.collection("users").document(user.getUid());
                                CollectionReference collectionReference_구입포스트 = documentReference_구입포스트.collection("다운로드한문서");

                                collectionReference_구입포스트.whereEqualTo("문서ID", 문서ID).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                확인 = true;
                                                if (task.isSuccessful()) {
                                                    Log.i("포스트결제", "1");
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Log.i("포스트결제", "2");
                                                        if(document != null) {
                                                            Log.i("포스트결제", "3");
                                                            Intent intent12 = new Intent(Intent.ACTION_VIEW, Uri.parse(파일url));
                                                            startActivity(intent12);
                                                            구입여부 = true;
                                                        }
                                                    }
                                                } else {

                                                }
                                            }
                                        });


                                if(확인== true && 구입여부 == false){

                                    if(user.getUid().equals(문서작성자Uid)){
                                        Intent intent12 = new Intent(Intent.ACTION_VIEW, Uri.parse(파일url));
                                        startActivity(intent12);

                                    } else {
                                        if(!tv_가격.getText().equals("무료")){  // 유료면 결제 다이얼로그 실행
                                            다운로드url = document.get("파일url").toString();
                                            AlertDialog.Builder builder=new AlertDialog.Builder(Board.this);
                                            builder.setMessage(tv_가격.getText().toString() + "코인이 차감됩니다");
                                            builder.setPositiveButton("결제", dialogListenerDownLoadFile);
                                            builder.setNegativeButton("취소", null);
                                            AlertDialog dialog=builder.create();
                                            dialog.show();
                                        } else{  // 무료일경우는 바로 다운
                                            Intent intent12 = new Intent(Intent.ACTION_VIEW, Uri.parse(파일url));
                                            startActivity(intent12);
                                        }
                                    }
                                }

                                확인 = false;

                            });

                            tv_작성자ID.setText("by."+document.get("id").toString());

                            tv_작성자ID.setOnClickListener(v -> {

                                if(document.get("작성자").toString().equals(user.getUid())){
                                    Intent intent1 = new Intent(Board.this, MyPage.class);
                                    startActivity(intent1);
                                } else{
                                    Intent intent1 = new Intent(Board.this, OtherUserPage.class);
                                    intent1.putExtra("uid", document.get("작성자").toString());
                                    startActivity(intent1);
                                }

                            });


                            DocumentReference documentReference_post = firebaseFirestore.collection("posts").document(문서ID);

                            final CollectionReference collectionReference = documentReference_post.collection("comments");

                            collectionReference.orderBy("createdAt", Query.Direction.ASCENDING).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            final ArrayList<DB_Comment> dbComments = new ArrayList<>();

                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    dbComments.add(new DB_Comment(
                                                            document.getData().get("작성자").toString(),
                                                            document.getData().get("댓글").toString(),
                                                            new Date(document.getDate("createdAt").getTime()),
                                                            document.getData().get("문서ID").toString(),
                                                            document.getData().get("uid").toString(),
                                                            document.getData().get("댓글ID").toString()
                                                    ));
                                                }

                                                RecyclerView.Adapter adapter_comment = new Comment_Adapter(Board.this, dbComments);
                                                recyclerView.setAdapter(adapter_comment);



                                                ((Comment_Adapter) adapter_comment).setOnItemclickListener(new ItemClickListener_Comment() {
                                                    @Override
                                                    public void OnItemClick(int position, DB_Comment comment) {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(Board.this);
                                                        builder.setMessage("댓글을 삭제하시겠습니까?");
                                                        builder.setPositiveButton("예", dialogListener_댓글삭제);
                                                        builder.setNegativeButton("아니오", null);
                                                        AlertDialog dialog = builder.create();
                                                        dialog.show();
                                                        삭제댓글ID = dbComments.get(position).get댓글ID();
                                                    }
                                                });

                                            } else {

                                            }
                                        }
                                    });


                        } else {

                        }
                    }

                } else {
                    // Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });



        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(user.getUid());
        final CollectionReference collectionReference = documentReference1.collection("좋아요 누른 글");
        final DocumentReference docRef = documentReference1.collection("좋아요 누른 글").document(문서ID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {   // 좋아요를 누른적이 있다면

                        iv_btn_공감.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                        iv_btn_공감.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                iv_btn_공감.setImageResource(R.drawable.ic_thumb_up_gray);
                                Toast.makeText(Board.this, "'좋아요' 취소", Toast.LENGTH_SHORT).show();
                                최종공감수 = Integer.parseInt(tv_공감수.getText().toString()) - 1;

                                tv_공감수.setText(String.valueOf(최종공감수));

                                collectionReference.document(문서ID)
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
                    } else {   //좋아요를 누른적이 없다면

                        iv_btn_공감.setImageResource(R.drawable.ic_thumb_up_gray);
                        iv_btn_공감.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                iv_btn_공감.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                                Toast.makeText(Board.this, "'좋아요'", Toast.LENGTH_SHORT).show();
                                최종공감수 = Integer.parseInt(tv_공감수.getText().toString()) + 1;

                                tv_공감수.setText(String.valueOf(최종공감수));

                                Map<String, Object> 공감 = new HashMap<>();
                                공감.put("공감", 문서ID);

                                docRef.set(공감)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                               // Log.d(TAG, "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //Log.w(TAG, "Error writing document", e);
                                            }
                                        });
                                onStop();
                                onResume();
                            }
                        });

                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_댓글:
                        addComment();
                        ((EditText) findViewById(R.id.et_댓글)).setText("");
                    break;

                case R.id.btn_뒤로가기:
                    finish();
                    break;

            }
        }
    };


    private void addComment() {

        if(et_댓글.getText().length() != 0){
            final String 댓글 = ((EditText) findViewById(R.id.et_댓글)).getText().toString();

            user = FirebaseAuth.getInstance().getCurrentUser();

            long now = System.currentTimeMillis();
            final Date date = new Date(now);

            DocumentReference documentReference_post = firebaseFirestore.collection("posts").document(문서ID);
            final DocumentReference documentReference_comment = documentReference_post.collection("comments").document();


            DocumentReference docRef = firebaseFirestore.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot doc = task.getResult();

                        if (doc != null) {
                            if (doc.exists()) {
                                String 작성자 = doc.get("id").toString();

                                dbComment = new DB_Comment(작성자, 댓글, date, 문서ID, user.getUid(), documentReference_comment.getId());

                                storeUploader(documentReference_comment, dbComment);
                            }
                        }
                    }
                }
            });
        }
       else {

        }


    }

    private void storeUploader(DocumentReference documentReference, DB_Comment db_comment) {
        documentReference.set(db_comment)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onResume();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }



        DialogInterface.OnClickListener dialogListener_댓글삭제 = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                firebaseFirestore = FirebaseFirestore.getInstance();

                DocumentReference documentReference_post = firebaseFirestore.collection("posts").document(문서ID);
                final DocumentReference docRef = documentReference_post.collection("comments").document(삭제댓글ID);

                docRef.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Board.this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                onResume();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        };

    DialogInterface.OnClickListener dialogListenerDownLoadFile =new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            DocumentReference doc1 = firebaseFirestore.collection("users").document(user.getUid());
            doc1.collection("충전코인").document(user.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final ArrayList<DB_Following> dbFollowings = new ArrayList<>();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            if(Integer.parseInt(document.getData().get("coin").toString()) > Integer.parseInt(tv_가격.getText().toString())){

                                int 최종코인 = Integer.parseInt(document.getData().get("coin").toString()) - Integer.parseInt(tv_가격.getText().toString());

                                DB_Member_coin dbMemberCoin = new DB_Member_coin(user.getUid(), 최종코인);
                                doc1.collection("충전코인").document(user.getUid())
                                        .set(dbMemberCoin)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                상대방코인플러스();
                                                다운받은파일목록추가();
                                                Toast.makeText(Board.this, "결제완료", Toast.LENGTH_SHORT).show();
                                                Intent intent12 = new Intent(Intent.ACTION_VIEW, Uri.parse(다운로드url));
                                                startActivity(intent12);
                                                dialog.dismiss();
                                                //  Log.d(TAG, "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Board.this, "결제오류", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        });

                            } else {    // 보유 코인이 부족할 때
                                Toast.makeText(Board.this, "결제오류: 코인이 부족합니다", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        } else {
                            Toast.makeText(Board.this, "결제오류: 코인을 충전해주세요", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(Board.this, "결제오류: 코인을 충전해주세요", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });

        }
    };


    private void 상대방코인플러스(){


        DocumentReference 상대방코인 = firebaseFirestore.collection("users").document(문서작성자Uid);
        상대방코인.collection("충전코인").document(문서작성자Uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {   // 상대방 코인 document 있으면 플러스!
                        int 상대코인 = Integer.parseInt(document.getData().get("coin").toString());

                        DB_Member_coin dbMemberCoin = new DB_Member_coin(문서작성자Uid, 상대코인 + Integer.parseInt(tv_가격.getText().toString()));
                        상대방코인.collection("충전코인").document(문서작성자Uid)
                                .set(dbMemberCoin)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    } else {  // 없으면 새로 만들어 주기!
                        DB_Member_coin dbMemberCoin = new DB_Member_coin(문서작성자Uid, Integer.parseInt(tv_가격.getText().toString()));

                        DocumentReference 상대방코인 = firebaseFirestore.collection("users").document(문서작성자Uid);
                        상대방코인.collection("충전코인").document(문서작성자Uid)
                                .set(dbMemberCoin)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    }
                } else {

                }
            }
        });
    }

    private void 다운받은파일목록추가(){

        Map<String, Object> 다운파일 = new HashMap<>();
        다운파일.put("문서ID", 문서ID);

        DocumentReference 다운파일문서 = firebaseFirestore.collection("users").document(user.getUid());
        다운파일문서.collection("다운로드한문서").document(문서ID)
                .set(다운파일)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    @Override
    public void onStop(){
        super.onStop();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.collection("posts").document(문서ID);

        documentReference
                .update("공감", tv_공감수.getText().toString())
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

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        SharedPreferences pref =getSharedPreferences("viewCount", MODE_PRIVATE);

        String 본문서 = pref.getString(문서ID, "");

        if(!본문서.equals(문서ID)){
            final SharedPreferences.Editor editor = pref.edit();
            editor.putString(문서ID, 문서ID);
            editor.apply();

            int 최종본횟수 = Integer.parseInt(본횟수) + 1;
            본횟수 = String.valueOf(최종본횟수);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference documentReference = firebaseFirestore.collection("posts").document(문서ID);

            documentReference
                    .update("본횟수", 본횟수)
                    .addOnSuccessListener(aVoid -> {
                        //Log.d(TAG, "DocumentSnapshot successfully updated!");
                    })
                    .addOnFailureListener(e -> {
                        //Log.w(TAG, "Error updating document", e);
                    });
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // super.onNewIntent(intent);
        setIntent(intent);
    }

}