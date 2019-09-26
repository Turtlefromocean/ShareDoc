package com.example.sharedoc.Activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.example.sharedoc.R;

public class KakaoPay extends AppCompatActivity {

    WebView webView;
    Boolean 결제완료 = false;
    Button btn_결제완료, btn_결제취소;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_pay);

        Intent intent = getIntent();
        String redirect_app_url = intent.getStringExtra("redirect_app_url");
        String 충전금액 = intent.getStringExtra("충전금액");

        if(결제완료 == false){
            Intent KakaoPay = new Intent(Intent.ACTION_VIEW, Uri.parse(redirect_app_url));
            결제완료 = true;
            startActivity(KakaoPay);
        }

        btn_결제완료 = findViewById(R.id.btn_결제완료);
        btn_결제취소 = findViewById(R.id.btn_결제취소);

        btn_결제완료.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("충전금액",충전금액);
                setResult(RESULT_OK,resultIntent);
                finish();
            }
        });

        btn_결제취소.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
}
