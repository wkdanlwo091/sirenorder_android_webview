package com.example.sirenorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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

    public void request2(String password, String id, String token,String urlStr){
            try {
                        URL url = new URL("http://54.193.173.207/androidData");
                        //httpconnection 열기
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        try {
                            conn.setRequestMethod("POST");
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                            Log.d("---",  e.toString());
            }
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            //conn.setDoInput(true);
            OutputStream outputStream = null;
            String request = "id=" + id + "&password=" + password + "&token=" + token;
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

    public void setGpsSettings(){

        /*
         * location permission setting으로 가는 부분
         *
         * */
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //gps가 안 켜진 경우 gps를 키기;
        setGpsSettings();


        //이 앱을 킬 때마다 위치 허용 물어보기
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        },0);




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


        /*
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
                public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                //이 함수는 url이 변할 때마다 call 된다.
                //
                    super.doUpdateVisitedHistory(view, url, isReload);
                    Log.d(TAG,"setWebViewClient changed ");
                }
            }
        );
         */

                mWebView = (WebView) findViewById(R.id.webView);
                mWebView.addJavascriptInterface(new Object() {
                    @JavascriptInterface           // For API 17+
                    public void performClick(String strl) {

                        Log.d("idpassword", strl);
                //여기서 토큰이랑 id , 비번을 보낸다.

                idPassword = strl;

            }
        }, "ok");

        //main.html이나 ownermain.html으로 가면 id, 비번, 토큰을 전달한다.

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(mWebView, url);

                //아래 둘중에 한개면 send한다.
                if (url.equals("http://54.193.173.207/main.html") || url.equals("http://54.193.173.207/ownermain.html")) {
                    final String[] tokens = idPassword.split("&");

                    Log.d("---1", "yes");
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    //request(tokens[0], tokens[1], myToken, "http:192.168.43.161:80/androidData");
                                    request2(tokens[0], tokens[1], myToken,"http://54.193.173.207/androidData");
                                }
                            }
                    ).start();
                } else {
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
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부


        출처: https://soulduse.tistory.com/59 [프로그래밍좀비]
        mWebView.loadUrl("http://54.193.173.207"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작   aws : 54.193.173.207

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
