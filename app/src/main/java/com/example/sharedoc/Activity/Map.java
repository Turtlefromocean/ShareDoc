package com.example.sharedoc.Activity;

import android.content.Intent;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedoc.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private Geocoder geocoder;
    ImageButton btn_location_search;
    EditText et_지도에서위치검색;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btn_location_search = findViewById(R.id.btn_location_search);
        et_지도에서위치검색 = findViewById(R.id.et_지도에서위치검색);
        mapView = findViewById(R.id.map_view);

        mapView.getMapAsync(this);
        geocoder = new Geocoder(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {

        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);


       /* naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                final Marker marker = new Marker();
                marker.setPosition(new LatLng(latLng.latitude, latLng.longitude));
                marker.setMap(naverMap);
            }
        });*/


        btn_location_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String 위치 = et_지도에서위치검색.getText().toString();
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
                        final Marker marker = new Marker();
                        marker.setPosition(point);
                        marker.setMap(naverMap);
                        naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(point, 16));

                        Overlay.OnClickListener listener = overlay -> {

                            Intent intent = new Intent(Map.this, Profile.class);
                            intent.putExtra("소속", 위치);
                            intent.putExtra("address", address);
                            startActivity(intent);
                            finish();
                            return true;
                        };

                        marker.setOnClickListener(listener);


                    } else {
                        Toast.makeText(Map.this, "찾는 위치정보가 없습니다", Toast.LENGTH_SHORT).show();
                    }
                } else {   // 팀노바 검색할 경우
                    LatLng point = new LatLng(37.482991, 126.97423170000002);
                    final Marker marker = new Marker();
                    marker.setPosition(point);
                    marker.setMap(naverMap);
                    naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(point, 16));

                    Overlay.OnClickListener listener = overlay -> {

                        Intent intent = new Intent(Map.this, Profile.class);
                        intent.putExtra("소속", 위치);
                        intent.putExtra("address", "대한민국 서울특별시 동작구 사당4동 318-12");
                        startActivity(intent);
                        finish();
                        return true;
                    };

                    marker.setOnClickListener(listener);
                }


            }
        });
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
