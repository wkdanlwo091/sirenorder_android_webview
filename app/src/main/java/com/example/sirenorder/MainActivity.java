package com.example.sirenorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private WebSettings mWebSettings;
    private String idPassword;
    private String myToken;

    private GpsTracker gpsTracker;

    //위도 경도 나타내는 변수
    private TextView tvLatitude,tvLongitude;

    //gps를 새로 고침하는 버튼
    private Button myButton;

    //아마존 웹서버에 httpconnection 열기
    public void request2(String password, String id, String token,String urlStr){
            try {
                        URL url = new URL("http://192.168.43.161/androidData");
                        //httpconnection 열기
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        //아래의 Content-Type이  key-value&key=value.. 이런 형식으로 전달된다.
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                        //post로 보내겠다.
                        try {
                            conn.setRequestMethod("POST");
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                            Log.d("---",  e.toString());
            }

            //5초 안에 연결하겠다.
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            //conn.setDoInput(true);

                //outputstream을 통해서 id와 password, token을 보내겠다. type은 utf-8
            OutputStream outputStream = null;
            String request = "id=" + id + "&password=" + password + "&token=" + token;


            //보낸고 outputstream을 닫는다.
            try {
                outputStream = conn.getOutputStream();
                outputStream.write(request.getBytes("UTF-8"));
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("---",  e.toString());

            } finally {
                try {
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("---",  e.toString());

                }
            }


            try {//이코드 안써주면 실행이 안된다.
                int respondsecode = conn.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("---",  e.toString());
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        Log.d("---",  e.toString());

    } catch (IOException e) {
        e.printStackTrace();
        Log.d("---",  e.toString());
    }
}


    public void setGpsConfigSettings(){

        final Context context =  MainActivity.this;//mainactivity의 context를 가져온다.

        //gps에 관한 세팅 alert를 준다.
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

// Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
// Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
// On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

// on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
// Showing Alert Message
        alertDialog.show();

    }

    public void setRequestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        },0);
    }

    public void getLocation(View view){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            mWebView.loadUrl("javascript:setAndroidGps('"+latitude+"','"+longitude+"')");
            Log.d("getLocation", "getLocation");
            //여기서 web front로 데이터를 보낸다.
        }else{
            gpsTracker.showSettingsAlert();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setGpsConfigSettings();
        //setRequestPermission();


        myButton = (Button)findViewById(R.id.buttonId);

        //버튼은 숨김 상태이다. main이나 owner main에 들어가면 버튼이 생긴다.
        myButton.setVisibility(View.GONE);





        //안드로이드에서 current gps position 가져오기
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        //경도 위도 가져오기




        //logcat에서 tag 검색하기 위해서 설정함
        final String TAG = "---";

        //google firebase의 토큰 받기
        final String token2 = FirebaseInstanceId.getInstance().getToken();


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
                        Log.d(TAG, "token is " + token2);
                        Log.d(TAG, "token is " + token);
                        myToken = token;
                        // Log and toast
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });


        // 웹뷰 시작
        mWebView = (WebView) findViewById(R.id.webView);

                //아래에서 web의 로그인 페이지에서 performclick을 누르면 strl을 받아온다.
                //
        mWebView.getSettings().setJavaScriptEnabled(true);//이거 까먹어서 시간 너무 썼다. 이걸 써줘야지 아래 addJavascriptInterface가 작동한다.

        mWebView.addJavascriptInterface(new Object() {
                        @JavascriptInterface           // For API 17+
                        public void performClick(String strl) {
                            Log.d("idpassword", strl);
                            //여기서 토큰이랑 id , 비번을 보낸다.

                    idPassword = strl;
                }
            }, "ok");

        //main.html이나 ownermain.html으로 가면 id, 비번, 토큰을 전달한다.


        //owner도 일반 유저도 안드로이드 어플을 사용 할 수 있다.
        mWebView.setWebViewClient(new WebViewClient() {

            //특정 url 이 열렸을 때
            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(mWebView, url);

                //아래 둘중에 한개의 페이지에 들어오면 androidData로 id, password, 토큰을 쪼개서 보낸다.
                if (url.equals("http://172.30.1.60/main.html") || url.equals("http://172.30.1.60/ownermain.html")) {


                    if(idPassword == null){//id 세션이 남아있어서 자동로그인 된경우

                    }else if(idPassword != null){//처음 들어간 경우
                        final String[] tokens = idPassword.split("&");
                        Log.d("idpassword", idPassword);
                        Log.d("---1", "yes");
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        //request(tokens[0], tokens[1], myToken, "http:192.168.43.161:80/androidData");
                                        request2(tokens[0], tokens[1], myToken,"http://172.30.1.60/androidData");
                                    }
                                }
                        ).start();

                    }

                    //여기 url에 들어오면 gps button을 띄우고
                    if(url.equals("http://172.30.1.60/main.html")){
                        myButton.setVisibility(View.VISIBLE);

                    }

                } else {
                    //위의 url의 조건에 맞지 않으면 gps button을 지운다.

                }
            }
        }); // 클릭시 새창 안뜨게

        //gps 사용하겠다.  아직 몰라 아래는
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }
        });



        mWebSettings = mWebView.getSettings(); //세부 세팅 등록


        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부 ----> 웹에서 쓴 데이터 읽기


        출처: https://soulduse.tistory.com/59 [프로그래밍좀비]
        mWebView.loadUrl("http://172.30.1.60"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작   aws : 54.193.173.207

        /*
        mWebView.addJavascriptInterface(new Object()
        {
            @JavascriptInterface           // For API 17+
            public void performClick(String strl)
            {
                Log.d("idpassword", strl);
                //여기서 토큰이랑 id , 비번을 보낸다.
            }
        }, "ok");
        mWebSettings = mWebView.getSettings(); //html에서 자바스크립트와 상호작용
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부
        mWebView.loadUrl("http://192.168.43.161:80"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작// 나중에 aws 주소
        */
    }
}
