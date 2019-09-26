package com.example.sharedoc.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.sharedoc.Adapter.Home_Adapter;
import com.example.sharedoc.Adapter.ViewPage_Adapter;
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
import java.util.Timer;
import java.util.TimerTask;

public class Home extends AppCompatActivity{

    RecyclerView recyclerView;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;
    TextView  tv_메모, tv_개발자공지;
    ViewPage_Adapter viewPage_adapter;
    ViewPager viewPager;
    private Timer timer;
    private int current_position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPage_adapter = new ViewPage_Adapter(this);
        viewPager.setAdapter(viewPage_adapter);
        createSlideShow();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_menu1);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu1:
                        return true;
                    case R.id.navigation_menu2:
                        Intent intent2 = new Intent(Home.this, Search.class);
                        startActivity(intent2);
                        return true;
                    case R.id.navigation_menu3:
                        Intent intent3 = new Intent(Home.this, WritePost.class);
                        startActivity(intent3);
                        return true;
                    case R.id.navigation_menu4:
                        Intent intent4 = new Intent(Home.this, ChatList.class);
                        startActivity(intent4);
                        return true;
                    case R.id.navigation_menu5:
                        Intent intent5 = new Intent(Home.this, MyPage.class);
                        startActivity(intent5);
                        return true;
                }
                return false;
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        tv_개발자공지 = findViewById(R.id.tv_개발자공지);
        tv_메모 = findViewById(R.id.tv_메모);

        recyclerView = findViewById(R.id.rv_home_전체포스트);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_home);

        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.black
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                recyclerView = findViewById(R.id.rv_home_전체포스트);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));

                DocumentReference developer = firebaseFirestore.collection("developer").document("개발자공지");

                developer.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                if (document.exists()) {

                                    if(document.get("공지").toString() != ""){
                                        tv_개발자공지.setText("<공지> "+document.get("공지").toString());
                                        tv_개발자공지.setSelected(true);
                                    } else {
                                        tv_개발자공지.setText("멍!");
                                    }

                                } else {

                                }
                            }
                        } else {

                        }
                    }
                });


                DocumentReference doc1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());
                CollectionReference co = doc1.collection("메모");
                DocumentReference doc2 = co.document("메모");

                doc2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                if (document.exists()) {
                                    if(document.get("메모").toString().length() != 0){
                                        tv_메모.setText("- "+document.get("메모").toString());
                                    } else {
                                        tv_메모.setText("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t메모가 없습니다");
                                    }
                                } else {
                                    tv_메모.setText("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t메모가 없습니다");
                                }
                            }
                        } else {

                        }
                    }
                });

                final CollectionReference collectionReference = firebaseFirestore.collection("posts");


                collectionReference.orderBy("createdAt", Query.Direction.DESCENDING).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                final ArrayList<DB_Post_info> postList = new ArrayList<>();

                                if (task.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document : task.getResult()) {

                                        DocumentReference documentReference1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());
                                        final CollectionReference col = documentReference1.collection("following");
                                        final DocumentReference docRef = col.document(document.getData().get("작성자").toString());

                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot follower = task.getResult(); /**중요!!!!*/
                                                    if (follower.exists()) {   // 팔로워라면

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

                                                        RecyclerView.Adapter mAdapter = new Home_Adapter(Home.this, postList);
                                                        recyclerView.setAdapter(mAdapter);

                                                        ((Home_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_Home() {
                                                            @Override
                                                            public void OnItemClick(final int position, DB_Post_info db_post_info) {

                                                                DocumentReference 포스트존재유무 = firebaseFirestore.collection("posts").document(postList.get(position).get문서ID());

                                                                포스트존재유무.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            DocumentSnapshot document = task.getResult();
                                                                            if (document.exists()) {
                                                                                Intent intent = new Intent(Home.this, Board.class);
                                                                                intent.putExtra("문서ID", postList.get(position).get문서ID());
                                                                                startActivity(intent);
                                                                            } else {
                                                                                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
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

                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_menu1);

        DocumentReference developer = firebaseFirestore.collection("developer").document("개발자공지");

        developer.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {

                            if(document.get("공지").toString() != ""){
                                tv_개발자공지.setText("<공지> "+document.get("공지").toString());
                                tv_개발자공지.setSelected(true);
                            } else {
                                tv_개발자공지.setText("멍!");
                            }

                        } else {

                        }
                    }
                } else {

                }
            }
        });


        DocumentReference doc1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());
        CollectionReference co = doc1.collection("메모");
        DocumentReference doc2 = co.document("메모");

        doc2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        if (document.exists()) {
                            if(document.get("메모").toString().length() != 0){
                                tv_메모.setText("- "+document.get("메모").toString());
                            } else {
                                tv_메모.setText("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t메모가 없습니다");
                            }
                        } else {
                            tv_메모.setText("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t메모가 없습니다");
                        }
                    }
                } else {

                }
            }
        });



        final CollectionReference collectionReference = firebaseFirestore.collection("posts");


        collectionReference.orderBy("createdAt", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final ArrayList<DB_Post_info> postList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference documentReference1 = firebaseFirestore.collection("users").document(firebaseUser.getUid());
                                final CollectionReference col = documentReference1.collection("following");
                                final DocumentReference docRef = col.document(document.getData().get("작성자").toString());

                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot follower = task.getResult(); /**중요!!!!*/
                                            if (follower.exists()) {   // 팔로워라면

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

                                                recyclerView = findViewById(R.id.rv_home_전체포스트);
                                                recyclerView.setHasFixedSize(true);
                                                recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));

                                                RecyclerView.Adapter mAdapter = new Home_Adapter(Home.this, postList);
                                                recyclerView.setAdapter(mAdapter);

                                                ((Home_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_Home() {
                                                    @Override
                                                    public void OnItemClick(final int position, DB_Post_info db_post_info) {
                                                        DocumentReference 포스트존재유무 = firebaseFirestore.collection("posts").document(postList.get(position).get문서ID());

                                                        포스트존재유무.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();
                                                                    if (document.exists()) {
                                                                        Intent intent = new Intent(Home.this, Board.class);
                                                                        intent.putExtra("문서ID", postList.get(position).get문서ID());
                                                                        startActivity(intent);
                                                                    } else {
                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
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

    private void createSlideShow() {
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if(current_position==viewPage_adapter.getCount())

                        current_position = 0;
                        viewPager.setCurrentItem(current_position++, true);
                }
            };

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(runnable);
                }
            }, 300, 2500);
    }

}



