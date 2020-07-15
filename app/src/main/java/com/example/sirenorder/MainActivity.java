package com.example.sirenorder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
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


class HttpClient {
    private static final String WWW_FORM = "application/x-www-form-urlencoded";
    private int httpStatusCode;
    private String body;

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getBody() {
        return body;
    }

    private Builder builder;

    private void setBuilder(Builder builder) {
        this.builder = builder;
    }

    public void request() {
        HttpURLConnection conn = getConnection();
        setHeader(conn);
        setBody(conn);
        httpStatusCode = getStatusCode(conn);
        body = readStream(conn);
        conn.disconnect();
    }

    private HttpURLConnection getConnection() {
        try {
            URL url = new URL(builder.getUrl());
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setHeader(HttpURLConnection connection) {
        setContentType(connection);
        setRequestMethod(connection);

        connection.setConnectTimeout(5000);
        connection.setDoOutput(true);
        connection.setDoInput(true);
    }

    private void setContentType(HttpURLConnection connection) {
        connection.setRequestProperty("Content-Type", WWW_FORM);
    }

    private void setRequestMethod(HttpURLConnection connection) {
        try {
            connection.setRequestMethod(builder.getMethod());
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
    }

    private void setBody(HttpURLConnection connection) {

        String parameter = builder.getParameters();
        if (parameter != null && parameter.length() > 0) {
            OutputStream outputStream = null;
            try {
                outputStream = connection.getOutputStream();
                outputStream.write(parameter.getBytes("UTF-8"));
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private int getStatusCode(HttpURLConnection connection) {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -10;
    }

    private String readStream(HttpURLConnection connection) {
        String result = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
            }
        }

        return result;
    }

    public static class Builder {

        private Map<String, String> parameters;
        private String method;
        private String url;

        public String getMethod() {
            return method;
        }

        public String getUrl() {
            return url;
        }

        public Builder(String method, String url) {
            if (method == null) {
                method = "GET";
            }
            this.method = method;
            this.url = url;
            this.parameters = new HashMap<String, String>();
        }

        public void addOrReplace(String key, String value) {
            this.parameters.put(key, value);
        }

        public void addAllParameters(Map<String, String> param) {
            this.parameters.putAll(param);
        }

        public String getParameters() {
            return generateParameters();
        }

        public String getParameter(String key) {
            return this.parameters.get(key);
        }

        private String generateParameters() {
            StringBuffer parameters = new StringBuffer();

            Iterator<String> keys = getKeys();

            String key = "";
            while (keys.hasNext()) {
                key = keys.next();
                parameters.append(String.format("%s=%s", key, this.parameters.get(key)));
                parameters.append("&");
            }

            String params = parameters.toString();
            if (params.length() > 0) {
                params = params.substring(0, params.length() - 1);
            }

            return params;
        }

        private Iterator<String> getKeys() {
            return this.parameters.keySet().iterator();
        }

        public HttpClient create() {
            HttpClient client = new HttpClient();
            client.setBuilder(this);
            return client;
        }

    }

}


public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private WebSettings mWebSettings;
    private String idPassword;
    private String myToken;

    public void request2(String password, String id, String token,String urlStr){
        try {
            URL url = new URL("http:192.168.43.161:80/androidData");
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
    public void request(String password, String id, String token, String urlStr) {
        HttpClient.Builder http = new HttpClient.Builder("POST", "http:192.168.43.161:80/androidData");

        Map<String, String> maps = new HashMap<String, String>();
        maps.put("token", token);
        maps.put("id", id);
        maps.put("password", password);


        http.addAllParameters(maps);
        HttpClient post = http.create();
        post.request();




    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String TAG = "---";
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
                if (url.equals("http://192.168.43.161/main.html") || url.equals("http://192.168.43.161/ownermain.html")) {
                    final String[] tokens = idPassword.split("&");

                    Log.d("---1", "yes");
                    new Thread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    //request(tokens[0], tokens[1], myToken, "http:192.168.43.161:80/androidData");
                                    request2(tokens[0], tokens[1], myToken,"http:192.168.43.161:80/androidData");
                                }
                            }
                    ).start();
                } else {
                }
            }
        }); // 클릭시 새창 안뜨게

        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
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

        mWebView.loadUrl("http://192.168.43.161"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작


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