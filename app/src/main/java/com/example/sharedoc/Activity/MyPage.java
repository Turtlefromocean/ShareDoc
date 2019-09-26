package com.example.sharedoc.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedoc.Adapter.MyPage_Adapter;
import com.example.sharedoc.BackPressCloseHandler;
import com.example.sharedoc.DB.DB_Following;
import com.example.sharedoc.DB.DB_KakaoPay;
import com.example.sharedoc.DB.DB_Member_coin;
import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.DB.DB_token;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_MyPage;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MyPage extends AppCompatActivity {
    public static Context mPContext;
    String 이름, 아이디, 자기소개, photoUrl, profilePath, 메모, 학교, 전공, 팔로워, 팔로잉;
    ImageView iv_프로필사진, iv_로그아웃;
    TextView tv_이름, tv_아이디, tv_introduce, tv_팔로워수, tv_팔로잉수, tv_코인;
    Bitmap bitmap;
    EditText et_메모;
    Handler timer;
    Button btn_저장, btn_취소;

    private BackPressCloseHandler backPressCloseHandler;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private  RecyclerView recyclerView;

    private static final String TAG = "MyPage.activity";

    AlertDialog.Builder builder;
    AlertDialog dialog;
    SharedPreferences userProfile;


    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3, floatingActionButton4, floatingActionButton5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_my_page_2);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);
        floatingActionButton4 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item4);
        floatingActionButton5 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item5);

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   // 프로필 수정
                Intent intent = new Intent(MyPage.this, Profile.class);
                startActivity(intent);

            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MyPage.this, Following.class);
                intent.putExtra("팔로잉수", tv_팔로잉수.getText().toString());
                startActivity(intent);
            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {   //코인충전
            public void onClick(View v) {
                builder = new AlertDialog.Builder(MyPage.this);
                builder.setTitle("코인충전");
                builder.setCancelable(false);
                View view = LayoutInflater.from(MyPage.this).inflate(R.layout.dialog_kakao_pay, null, false);
                InitDialogKakaoPay(view);
                builder.setView(view);
                dialog = builder.create();
                dialog.show();

            }
        });
        floatingActionButton4.setOnClickListener(new View.OnClickListener() {   // 구입한 문서
            public void onClick(View v) {
                Intent intent = new Intent(MyPage.this, Home_all.class);
                startActivity(intent);
            }
        });
        floatingActionButton5.setOnClickListener(new View.OnClickListener() {   // 메모
            public void onClick(View v) {
                builder = new AlertDialog.Builder(MyPage.this);
                builder.setTitle("메모장");
                builder.setCancelable(false);
                View view = LayoutInflater.from(MyPage.this).inflate(R.layout.dialog_memo, null, false);
                InitUpdateDialog(view);
                builder.setView(view);
                dialog = builder.create();
                dialog.show();

            }
        });


/** (시작) 60000초마다 "viewCount" 쉐어드 초기화 */
        TimerTask viewCount = new TimerTask() {
            @Override
            public void run() {
                SharedPreferences pref =getSharedPreferences("viewCount", MODE_PRIVATE);
                final SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.apply();
            }
        };

        Timer timer = new Timer();
        timer.schedule(viewCount,60000, 60000);
/** (끝) 60000초마다 "viewCount" 쉐어드 초기화 */


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu1:
                        Intent intent1 = new Intent(MyPage.this, Home.class);
                        startActivity(intent1);
                        return true;
                    case R.id.navigation_menu2:
                        Intent intent2 = new Intent(MyPage.this, Search.class);
                        startActivity(intent2);
                        return true;
                    case R.id.navigation_menu3:
                        Intent intent3 = new Intent(MyPage.this, WritePost.class);
                        startActivity(intent3);
                        return true;
                    case R.id.navigation_menu4:
                        Intent intent4 = new Intent(MyPage.this, ChatList.class);
                        startActivity(intent4);
                        return true;
                    case R.id.navigation_menu5:
                        return true;
                }
                return false;
            }
        });

        tv_아이디 = findViewById(R.id.tv_아이디);
        tv_이름 = findViewById(R.id.tv_이름);
        tv_introduce = findViewById(R.id.tv_introduce);
        iv_프로필사진 = findViewById(R.id.iv_프로필사진);
        tv_팔로워수 = findViewById(R.id.tv_팔로워수);
        tv_팔로잉수 = findViewById(R.id.tv_팔로잉수);

        tv_코인 = findViewById(R.id.tv_코인);

        backPressCloseHandler = new BackPressCloseHandler(this);


        if (firebaseUser == null) {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        } else {
            // sharedPreference 에서 저장된 값 불러오기
            userProfile = getSharedPreferences("userProfile", MODE_PRIVATE);
            아이디 = userProfile.getString("id", "");

            if(아이디.equals("")){
                Intent intent = new Intent(this, Profile.class);
                startActivity(intent);
            }
        }

        iv_로그아웃 = (ImageView) findViewById(R.id.iv_로그아웃);

        iv_로그아웃.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MyPage.this);
                builder.setMessage("로그아웃 하시겠습니까?");
                builder.setPositiveButton("예", dialogListener);
                builder.setNegativeButton("아니오", null);
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });



        mPContext = this;





        recyclerView = findViewById(R.id.rv_내마켓);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MyPage.this));


    }



    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_menu5);
        if (firebaseUser != null) {
            passPushTokenToServer();
/// 메모 불러오기
            DocumentReference doc1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());

            final CollectionReference col = doc1.collection("메모");
            DocumentReference doc2 = col.document("메모");

            doc2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
                                메모 = document.get("메모").toString();
                            } else {
                                메모 = "";
                            }
                        }
                    } else {

                    }
                }
            });
// 메모 불러오기 끝

            /** 일단 쉐어드에서 가져오기 팔로워, 팔로잉 빼고! */

            userProfile =getSharedPreferences("userProfile", MODE_PRIVATE);
            아이디 = userProfile.getString("id", "");

            이름 = userProfile.getString("name", "");
            자기소개 = userProfile.getString("introduce", "");
            photoUrl = userProfile.getString("photoUrl", "");
            profilePath = userProfile.getString("profilePath", "");
            학교 = userProfile.getString("school", "");
            전공 = userProfile.getString("major", "");
            tv_아이디.setText(아이디+"님의 그라운드");
            tv_introduce.setText(자기소개);
            ///

            doc1.collection("충전코인").document(firebaseUser.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final ArrayList<DB_Following> dbFollowings = new ArrayList<>();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            tv_코인.setText(document.getData().get("coin").toString());
                        } else {
                            tv_코인.setText("0");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

            doc1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    final ArrayList<DB_Following> dbFollowings = new ArrayList<>();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            tv_팔로워수.setText(document.getData().get("followers").toString());
                            tv_팔로잉수.setText(document.getData().get("following").toString());

                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        } else {


                            팔로워 = userProfile.getString("followers", "0");
                            팔로잉 = userProfile.getString("following", "0");

                            tv_팔로워수.setText(팔로워);
                            tv_팔로잉수.setText(팔로잉);

                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });


            final CollectionReference collectionReference = firebaseFirestore.collection("posts");





                if(학교.length() != 0 && 전공.length() == 0){
                    tv_이름.setText(이름+"("+학교+")");
                }

                if(전공.length() != 0 && 학교.length() == 0) {
                    tv_이름.setText(이름+"("+전공+")");
                }

                if(전공.length() != 0 && 학교.length() != 0) {
                    tv_이름.setText(이름+"("+학교+", "+전공+")");
                }

                if(전공.length() == 0 && 학교.length() == 0){
                    tv_이름.setText(이름);
                }


                if(!profilePath.equals("")){
                    Bitmap bmp = BitmapFactory.decodeFile(profilePath);
                    iv_프로필사진.setImageBitmap(bmp);
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
                        iv_프로필사진.setImageBitmap(bitmap);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            collectionReference.whereEqualTo("작성자", firebaseUser.getUid()).get()   // .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            final ArrayList<DB_Post_info> postList = new ArrayList<>();

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());

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

                                RecyclerView.Adapter mAdapter = new MyPage_Adapter(MyPage.this, postList);
                                recyclerView.setAdapter(mAdapter);

                                /** 삭제가 중간에 돼었는지 안돼었는지에 따라*/
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
                                                        Intent intent = new Intent(MyPage.this, Board.class);
                                                        intent.putExtra("문서ID",postList.get(position).get문서ID());
                                                        startActivity(intent);
                                                    } else {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(MyPage.this);
                                                        builder.setMessage("삭제된 포스트 입니다");
                                                        builder.setPositiveButton("확인", null);
                                                        AlertDialog dialog = builder.create();
                                                        dialog.show();
                                                        onResume();
                                                    }
                                                } else {
                                                    Log.d(TAG, "get failed with ", task.getException());
                                                }
                                            }
                                        });



                                    }
                                });
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }



    DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            //sharedPreference ""
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
            //

            /** 토큰 초기화 */
            DocumentReference documentReference1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());
            final CollectionReference col = documentReference1.collection("pushToken");
            final DocumentReference docRef = col.document(firebaseUser.getUid());

            docRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });
            //
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MyPage.this, "로그아웃 완료", Toast.LENGTH_SHORT).show();


            if(UserManagement.getInstance() != null ){
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        Intent intent = new Intent(MyPage.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                });
            } else {
                Intent intent = new Intent(MyPage.this, Login.class);
                startActivity(intent);
                finish();
            }

        }
    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }


    public void InitUpdateDialog(View view) {

        et_메모 = view.findViewById(R.id.et_메모);
        btn_저장 = view.findViewById(R.id.btn_저장);
        btn_취소 = view.findViewById(R.id.btn_취소);

        et_메모.setText(메모);

        btn_저장.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> data = new HashMap<>();
                data.put("메모", et_메모.getText().toString());

                DocumentReference dr = firebaseFirestore.collection("users").document(firebaseUser.getUid());
                CollectionReference co = dr.collection("메모");
                co.document("메모").set(data);
                dialog.dismiss();
            }
        });
        btn_취소.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /** 카카오 충전 금액 정하기 */
    public void InitDialogKakaoPay(View view) {

        EditText et_충전금액 = view.findViewById(R.id.et_충전금액);

        InputFilter[] IntroduceFilter = new InputFilter[]{
                new InputFilter.LengthFilter(4)  // et_충전금액 4자리 이상 충전 못하게 막기
        };
        et_충전금액.setFilters(IntroduceFilter);



        Button btn_결제 = view.findViewById(R.id.btn_결제);
        Button btn_취소 = view.findViewById(R.id.btn_취소);

        btn_결제.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String 충전금액 = et_충전금액.getText().toString();

                Gson gson = new Gson();

                DB_KakaoPay KakaoPay = new DB_KakaoPay();
                KakaoPay.kakaoPay.cid = "key=TC0ONETIME";
                KakaoPay.kakaoPay.partner_order_id = "partner_order_id";
                KakaoPay.kakaoPay.partner_user_id = "partner_user_id";
                KakaoPay.kakaoPay.item_name = "토큰충전";
                KakaoPay.kakaoPay.quantity = 1;
                KakaoPay.kakaoPay.total_amount = 1000;
                KakaoPay.kakaoPay.tax_free_amount = 0;
                KakaoPay.kakaoPay.approval_url = "https://developers.kakao.com/success";
                KakaoPay.kakaoPay.fail_url = "https://developers.kakao.com/fail";
                KakaoPay.kakaoPay.cancel_url = "https://developers.kakao.com/cancel";

                String json = gson.toJson(KakaoPay);
                Log.i("Push", json);

                String jj = "{\"cid\""+":"+"\"TC0ONETIME\""+","+"{\"partner_order_id\""+":"+"\"partner_order_id\"";

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"),jj);

                Request request = new Request.Builder()
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("Authorization", "KakaoAK 4d23582c0b9b3ebcd5a599fc8dbec4b4")
                        .url("https://kapi.kakao.com/v1/payment/ready?cid=TC0ONETIME&partner_order_id=partner_order_id&partner_user_id=partner_user_id&item_name=코인충전&quantity=1&total_amount="+충전금액+"&tax_free_amount=0&approval_url=https://developers.kakao.com/success&fail_url=https://developers.kakao.com/fail&cancel_url=https://developers.kakao.com/cancel")
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
                        String result = response.body().string();


                        Log.i("Push", result);

                        String[] data = result.split(",");
                        String redirect_app_url = data[2];

                        String[] data2 = redirect_app_url.split("\":\"");
                        String app_url = data2[1];
                        String 최종_redirect_app_url = app_url.replace("\"", "");

                        Log.i("Push", 최종_redirect_app_url);



                        Intent intent = new Intent(MyPage.this, KakaoPay.class);
                        intent.putExtra("redirect_app_url", 최종_redirect_app_url);
                        intent.putExtra("충전금액", 충전금액);
                        startActivityForResult(intent, 555);
                        dialog.dismiss();
                    }
                });
            }
        });
        btn_취소.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /** 카카오페이 결제 결과 */
    @Override
    protected void onActivityResult(int RequestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
           int 최종코인 = Integer.parseInt(tv_코인.getText().toString()) + Integer.parseInt(data.getStringExtra("충전금액"));
            tv_코인.setText(String.valueOf(최종코인));

            // 파이어베이스에 올리기
            DB_Member_coin dbMemberCoin = new DB_Member_coin(firebaseUser.getUid(), 최종코인);

            DocumentReference dr = firebaseFirestore.collection("users").document(firebaseUser.getUid());
            dr.collection("충전코인").document(firebaseUser.getUid())
                    .set(dbMemberCoin)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }
    }

    void passPushTokenToServer(){                                                                     // 채팅 FCM 토큰
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        Map<String, Object> map = new HashMap<>();
                        map.put("pushToken", token);

                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(map);

                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());
                        final CollectionReference col = documentReference1.collection("pushToken");
                        final DocumentReference docRef = col.document(firebaseUser.getUid());

                        DB_token db_token = new DB_token(token);

                        docRef.set(db_token)
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

                    }
                });
    }


}