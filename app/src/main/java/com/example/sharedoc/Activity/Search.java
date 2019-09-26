package com.example.sharedoc.Activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedoc.Adapter.Map_post_Adapter;
import com.example.sharedoc.Adapter.Search_Adapter;
import com.example.sharedoc.Adapter.Search_post_Adapter;
import com.example.sharedoc.DB.DB_Member_info;
import com.example.sharedoc.DB.DB_Post_info;
import com.example.sharedoc.R;
import com.example.sharedoc.itemClickListener.ItemClickListener_Home;
import com.example.sharedoc.itemClickListener.ItemClickListener_Map_post;
import com.example.sharedoc.itemClickListener.ItemClickListener_Search;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Search extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseFirestore firebaseFirestore;
    RecyclerView recyclerView, recyclerView2, rv_map_post;
    LinearLayout linearLayout_실시간인기글, 리스트검색;
    Search_Adapter search_adapter;
    FirebaseUser user;
    TextView tv_사용자검색결과, tv_글검색결과, tv_소속위치;
    EditText et_search;
    ImageButton btn_search;
    Switch switch_search_map;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    Button btn_닫기;
    private MapView mapView;
    private Geocoder geocoder;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        firebaseFirestore = FirebaseFirestore.getInstance();

        et_search = findViewById(R.id.et_search);
        btn_search = findViewById(R.id.btn_search);
        tv_사용자검색결과 = findViewById(R.id.tv_사용자검색결과);
        tv_글검색결과 = findViewById(R.id.tv_글검색결과);
        switch_search_map = findViewById(R.id.switch_search_map);
        리스트검색 = findViewById(R.id.리스트검색);

        /** 사용자 recyclerView */
        recyclerView = findViewById(R.id.rv_검색결과);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        /** 글 recyclerView */
        recyclerView2 = findViewById(R.id.rv_검색결과_태그_제목_작성자);
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));

        /** 실시간 인기글 recyclerView */
        /*recyclerView3= findViewById(R.id.rv_실시간인기글);
        recyclerView3.setHasFixedSize(true);
        recyclerView3.setLayoutManager(new LinearLayoutManager(this));*/

        mapView = findViewById(R.id.map_view_search);

        /** 일단 처음에는 기본적인 글, 사용자 검색*/
        switch_search_map.setChecked(false);
        if(switch_search_map.isChecked() == false){
            mapView.setVisibility(View.INVISIBLE);
            리스트검색.setVisibility(View.VISIBLE);


            final CollectionReference collectionReference_user = firebaseFirestore.collection("users");

            final CollectionReference collectionReference_post = firebaseFirestore.collection("posts");

            user = FirebaseAuth.getInstance().getCurrentUser();

            btn_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    final String 검색어 =et_search.getText().toString().replaceAll("(^\\p{Z}+|\\p{Z}+$)", "");  // editText 앞뒤 공백 제거

                    /** (시작) 사용자 검색 */
                    collectionReference_user.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    final ArrayList<DB_Member_info> dbMemberInfos = new ArrayList<>();

                                    if (task.isSuccessful()) {

                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if(document.getData().get("id").equals(검색어) ||
                                                    document.getData().get("name").toString().equals(검색어) ||
                                                    document.getData().get("school").toString().equals(검색어) ||
                                                    document.getData().get("major").toString().equals(검색어)) {

                                                dbMemberInfos.add(new DB_Member_info(
                                                        document.getData().get("id").toString(),
                                                        document.getData().get("name").toString(),
                                                        document.getData().get("introduce").toString(),
                                                        document.getData().get("school").toString(),
                                                        document.getData().get("major").toString(),
                                                        document.getData().get("uid").toString(),
                                                        document.getData().get("followers").toString(),
                                                        document.getData().get("following").toString(),
                                                        document.getData().get("address").toString()
                                                ));

                                            }


                                        }


                                        Search_Adapter search_adapter = new Search_Adapter(Search.this, dbMemberInfos);
                                        recyclerView.setAdapter(search_adapter);

                                        if(((Search_Adapter) search_adapter).getItemCount() != 0){
                                            tv_사용자검색결과.setVisibility(View.VISIBLE);
                                        }

                                        ((Search_Adapter) search_adapter).setOnItemclickListener(new ItemClickListener_Search() {
                                            @Override
                                            public void OnItemClick(int position, DB_Member_info dbMemberInfo) {
                                                if(dbMemberInfos.get(position).getUid().equals(user.getUid())) {
                                                    Intent intent = new Intent(Search.this, MyPage.class);
                                                    startActivity(intent);
                                                } else {
                                                    Intent intent = new Intent(Search.this, OtherUserPage.class);
                                                    intent.putExtra("uid", dbMemberInfos.get(position).getUid());
                                                    startActivity(intent);
                                                }
                                            }
                                        });



                                    } else {

                                    }

                                }
                            });


                    /** (끝) 사용자 검색 */

                    /** (시작) 글 태그 검색 */
                    collectionReference_post.get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    final ArrayList<DB_Post_info> postList = new ArrayList<>();

                                    if (task.isSuccessful()) {

                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if(document.getData().get("태그").toString().contains(검색어) ||
                                                    document.getData().get("제목").toString().contains(검색어) ||
                                                    document.getData().get("id").toString().contains(검색어)) {


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

                                            }


                                        }


                                        RecyclerView.Adapter mAdapter = new Search_post_Adapter(Search.this, postList);
                                        recyclerView2.setAdapter(mAdapter);

                                        if(((Search_post_Adapter) mAdapter).getItemCount() != 0){
                                            tv_글검색결과.setVisibility(View.VISIBLE);
                                        }

                                        ((Search_post_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_Home() {
                                            @Override
                                            public void OnItemClick(final int position, DB_Post_info db_post_info) {


                                                DocumentReference 포스트존재유무 = firebaseFirestore.collection("posts").document(postList.get(position).get문서ID());

                                                포스트존재유무.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                Intent intent = new Intent(Search.this, Board.class);
                                                                intent.putExtra("문서ID", postList.get(position).get문서ID());
                                                                startActivity(intent);
                                                            } else {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
                                                                builder.setMessage("삭제된 포스트 입니다");
                                                                builder.setPositiveButton("확인", null);
                                                                AlertDialog dialog = builder.create();
                                                                dialog.show();
                                                                onResume();
                                                            }
                                                        } else {
                                                            //  Log.d(TAG, "get failed with ", task.getException());
                                                        }
                                                    }
                                                });

                                            }
                                        });

                                    } else {

                                    }

                                }
                            });



                    /** (끝) 글 태그 검색 */
                }


            });
        } else {

            /** 네이버 지도 api 등록 */

            mapView.getMapAsync(this);
            geocoder = new Geocoder(this);

            locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

            mapView.setVisibility(View.VISIBLE);
            리스트검색.setVisibility(View.INVISIBLE);


        }


        /** 그 다음에 switch 값 변하는 거 확인 */
        switch_search_map.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(switch_search_map.isChecked() == false){
                    mapView.setVisibility(View.INVISIBLE);
                    리스트검색.setVisibility(View.VISIBLE);


                    final CollectionReference collectionReference_user = firebaseFirestore.collection("users");

                    final CollectionReference collectionReference_post = firebaseFirestore.collection("posts");

                    user = FirebaseAuth.getInstance().getCurrentUser();

                    btn_search.setOnClickListener(v -> {


                        final String 검색어 =et_search.getText().toString().replaceAll("(^\\p{Z}+|\\p{Z}+$)", "");  // editText 앞뒤 공백 제거

                        /** (시작) 사용자 검색 */
                        collectionReference_user.get()
                                .addOnCompleteListener(task -> {
                                    final ArrayList<DB_Member_info> dbMemberInfos = new ArrayList<>();

                                    if (task.isSuccessful()) {

                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if(document.getData().get("id").equals(검색어) ||
                                                    document.getData().get("name").toString().equals(검색어) ||
                                                    document.getData().get("school").toString().equals(검색어) ||
                                                    document.getData().get("major").toString().equals(검색어)) {

                                                dbMemberInfos.add(new DB_Member_info(
                                                        document.getData().get("id").toString(),
                                                        document.getData().get("name").toString(),
                                                        document.getData().get("introduce").toString(),
                                                        document.getData().get("school").toString(),
                                                        document.getData().get("major").toString(),
                                                        document.getData().get("uid").toString(),
                                                        document.getData().get("followers").toString(),
                                                        document.getData().get("following").toString(),
                                                        document.getData().get("address").toString()
                                                ));

                                            }


                                        }


                                        Search_Adapter search_adapter = new Search_Adapter(Search.this, dbMemberInfos);
                                        recyclerView.setAdapter(search_adapter);

                                        if(((Search_Adapter) search_adapter).getItemCount() != 0){
                                            tv_사용자검색결과.setVisibility(View.VISIBLE);
                                        }

                                        ((Search_Adapter) search_adapter).setOnItemclickListener(new ItemClickListener_Search() {
                                            @Override
                                            public void OnItemClick(int position, DB_Member_info dbMemberInfo) {
                                                if(dbMemberInfos.get(position).getUid().equals(user.getUid())) {
                                                    Intent intent = new Intent(Search.this, MyPage.class);
                                                    startActivity(intent);
                                                } else {
                                                    Intent intent = new Intent(Search.this, OtherUserPage.class);
                                                    intent.putExtra("uid", dbMemberInfos.get(position).getUid());
                                                    startActivity(intent);
                                                }
                                            }
                                        });



                                    } else {

                                    }

                                });


                        /** (끝) 사용자 검색 */

                        /** (시작) 글 태그 검색 */
                        collectionReference_post.get()
                                .addOnCompleteListener(task -> {
                                    final ArrayList<DB_Post_info> postList = new ArrayList<>();

                                    if (task.isSuccessful()) {

                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if(document.getData().get("태그").toString().contains(검색어) ||
                                                    document.getData().get("제목").toString().contains(검색어) ||
                                                    document.getData().get("id").toString().contains(검색어)) {


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

                                            }


                                        }


                                        RecyclerView.Adapter mAdapter = new Search_post_Adapter(Search.this, postList);
                                        recyclerView2.setAdapter(mAdapter);

                                        if(((Search_post_Adapter) mAdapter).getItemCount() != 0){
                                            tv_글검색결과.setVisibility(View.VISIBLE);
                                        }

                                        ((Search_post_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_Home() {
                                            @Override
                                            public void OnItemClick(final int position, DB_Post_info db_post_info) {


                                                DocumentReference 포스트존재유무 = firebaseFirestore.collection("posts").document(postList.get(position).get문서ID());

                                                포스트존재유무.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                Intent intent = new Intent(Search.this, Board.class);
                                                                intent.putExtra("문서ID", postList.get(position).get문서ID());
                                                                startActivity(intent);
                                                            } else {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
                                                                builder.setMessage("삭제된 포스트 입니다");
                                                                builder.setPositiveButton("확인", null);
                                                                AlertDialog dialog = builder.create();
                                                                dialog.show();
                                                                onResume();
                                                            }
                                                        } else {
                                                            //  Log.d(TAG, "get failed with ", task.getException());
                                                        }
                                                    }
                                                });

                                            }
                                        });

                                    } else {

                                    }

                                });



                        /** (끝) 글 태그 검색 */
                    });
                } else {
                    mapView.setVisibility(View.VISIBLE);
                    리스트검색.setVisibility(View.INVISIBLE);

                    /** 네이버 지도 api 등록 */
                    mapView = findViewById(R.id.map_view_search);
                    mapView.getMapAsync(Search.this);
                    geocoder = new Geocoder(Search.this);

                    locationSource = new FusedLocationSource(Search.this, LOCATION_PERMISSION_REQUEST_CODE);

                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.navigation_menu2);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_menu1:
                        Intent intent1 = new Intent(Search.this, Home.class);
                        startActivity(intent1);
                        return true;
                    case R.id.navigation_menu2:
                        return true;
                    case R.id.navigation_menu3:
                        Intent intent3 = new Intent(Search.this, WritePost.class);
                        startActivity(intent3);
                        return true;
                    case R.id.navigation_menu4:
                        Intent intent4 = new Intent(Search.this, ChatList.class);
                        startActivity(intent4);
                        return true;
                    case R.id.navigation_menu5:
                        Intent intent5 = new Intent(Search.this, MyPage.class);
                        startActivity(intent5);
                        return true;
                }
                return false;
            }
        });




    }

    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {

        naverMap.setLocationSource(locationSource);

        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        final CollectionReference collectionReference = firebaseFirestore.collection("posts");
        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final ArrayList<DB_Post_info> postList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String 위치 = document.getData().get("주소").toString();
                                List<Address> addressList = null;

                                try {
                                    addressList = geocoder.getFromLocationName(
                                            위치, // 주소
                                            1); // 최대 검색 결과 개수
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if(addressList.size() != 0){
                                    Log.i("위치검색", addressList.get(0).toString());
                                    String []splitStr = addressList.get(0).toString().split(",");
                                    final String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
                                    String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                                    String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도

                                    LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                                    final Marker marker = new Marker();
                                    marker.setPosition(point);
                                    marker.setMap(naverMap);
                               /** 마커 이미지 커스텀 marker.setIcon(OverlayImage.fromResource(R.drawable.ic_rate_review_black_24dp));*/
                                    //naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(point, 16));  //이거는 직접 검색시!

                                    Overlay.OnClickListener listener = overlay -> {

                                        builder = new AlertDialog.Builder(Search.this);
                                        builder.setTitle("위치로 문서찾기");
                                        builder.setCancelable(false);
                                        View view = LayoutInflater.from(Search.this).inflate(R.layout.dialog_post_list, null, false);
                                        InitUpdateDialog(view, 위치);
                                        builder.setView(view);
                                        dialog = builder.create();
                                        dialog.show();

                                        return true;
                                    };

                                    marker.setOnClickListener(listener);  // 마커로 몇개 있는지 보여주고 마커 클릭 시 다이얼로그로 글 리스트 띄우기!


                                } else {
                                   // Toast.makeText(Search.this, "찾는 위치정보가 없습니다", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {
                        }
                    }
                });
        btn_search.setOnClickListener(new View.OnClickListener() {   //버튼 클릭 시  & 버튼 클릭 전에는 전체 마커 만들어서 보여주기
            @Override
            public void onClick(View v) {
                String 위치 = et_search.getText().toString().replaceAll("(^\\p{Z}+|\\p{Z}+$)", "");  // editText 앞뒤 공백 제거
                List<Address> addressList = null;

                if(!위치.contains("팀노바")){
                    try {
                        addressList = geocoder.getFromLocationName(
                                위치, // 주소
                                1); // 최대 검색 결과 개수
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(addressList.size() != 0){
                        Log.i("위치검색", addressList.get(0).toString());
                        String []splitStr = addressList.get(0).toString().split(",");
                        final String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // 주소
                        String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
                        String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도

                        LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(point, 16));

                        Overlay.OnClickListener listener = overlay -> {

                            builder = new AlertDialog.Builder(Search.this);
                            builder.setTitle("위치로 문서찾기");
                            builder.setCancelable(false);
                            View view = LayoutInflater.from(Search.this).inflate(R.layout.dialog_post_list, null, false);
                            InitUpdateDialog(view, address);
                            builder.setView(view);
                            dialog = builder.create();
                            dialog.show();

                            return true;
                        };

                        /** marker.setOnClickListener(listener);   검색 할 때는 마커로 표시해 줄 필요가 없음! 이미 문서 있는거는 마커로 표시가 되니까 */


                    } else {
                        Toast.makeText(Search.this, "찾는 위치정보가 없습니다", Toast.LENGTH_SHORT).show();
                    }
                } else {   // 팀노바 검색할 경우
                    LatLng point = new LatLng(37.482991, 126.97423170000002);

                    naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(point, 16));

                    Overlay.OnClickListener listener = overlay -> {

                        builder = new AlertDialog.Builder(Search.this);
                        builder.setTitle("위치로 문서찾기");
                        builder.setCancelable(false);
                        View view = LayoutInflater.from(Search.this).inflate(R.layout.dialog_post_list, null, false);
                        InitUpdateDialog(view, "대한민국 서울특별시 동작구 사당4동 318-12");
                        builder.setView(view);
                        dialog = builder.create();
                        dialog.show();

                        return true;
                    };


                }


            }
        });


    }

    public void InitUpdateDialog(View view, String 위치) {

        tv_소속위치 = view.findViewById(R.id.tv_소속위치);
        rv_map_post = view.findViewById(R.id.rv_map_post);
        btn_닫기 = view.findViewById(R.id.btn_닫기);
        tv_소속위치.setText(위치);

        final CollectionReference collectionReference = firebaseFirestore.collection("posts");
        collectionReference.whereEqualTo("주소", 위치).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final ArrayList<DB_Post_info> postList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

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

                            rv_map_post = view.findViewById(R.id.rv_map_post);
                            rv_map_post.setHasFixedSize(true);
                            rv_map_post.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                            RecyclerView.Adapter mAdapter = new Map_post_Adapter(getApplicationContext(), postList);
                            rv_map_post.setAdapter(mAdapter);

                            ((Map_post_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_Map_post() {
                                @Override
                                public void OnItemClick(final int position, DB_Post_info db_post_info) {
                                    DocumentReference 포스트존재유무 = firebaseFirestore.collection("posts").document(postList.get(position).get문서ID());

                                    포스트존재유무.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Intent intent = new Intent(getApplicationContext(), Board.class);
                                                    intent.putExtra("문서ID",postList.get(position).get문서ID());
                                                    startActivity(intent);
                                                } else {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                                    builder.setMessage("삭제된 포스트 입니다");
                                                    builder.setPositiveButton("확인", null);
                                                    AlertDialog dialog = builder.create();
                                                    dialog.show();
                                                    onResume();
                                                }
                                            } else {
                                                //  Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });

                                }
                            });
                        } else {
                        }
                    }
                });

        btn_닫기.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    public void onResume(){
        super.onResume();

        /** 실시간 인기글 */ //일단 보류
        /*final CollectionReference collectionReference = firebaseFirestore.collection("posts");

        collectionReference.orderBy("본횟수", Query.Direction.DESCENDING).limit(3).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final ArrayList<DB_Post_info> postList = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (final QueryDocumentSnapshot document : task.getResult()) {

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
                                            document.getData().get("소속").toString()
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
                                            document.getData().get("소속").toString()
                                    ));
                                }

                                final RecyclerView.Adapter mAdapter = new Post_most_seen_Adapter(Search.this, postList);
                                recyclerView3.setAdapter(mAdapter);

                                ((Post_most_seen_Adapter) mAdapter).setOnItemclickListener(new ItemClickListener_Home() {
                                    @Override
                                    public void OnItemClick(final int position, DB_Post_info db_post_info) {

                                        DocumentReference 포스트존재유무 = firebaseFirestore.collection("posts").document(postList.get(position).get문서ID());

                                        포스트존재유무.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Intent intent = new Intent(Search.this, Board.class);
                                                        intent.putExtra("문서ID", postList.get(position).get문서ID());
                                                        startActivity(intent);
                                                    } else {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
                                                        builder.setMessage("삭제된 포스트 입니다");
                                                        builder.setPositiveButton("확인", null);
                                                        AlertDialog dialog = builder.create();
                                                        dialog.show();
                                                        onResume();
                                                    }
                                                } else {
                                                    //  Log.d(TAG, "get failed with ", task.getException());
                                                }
                                            }
                                        });


                                    }
                                });

                            }

                        } else {
                        }
                    }
                });*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




}
