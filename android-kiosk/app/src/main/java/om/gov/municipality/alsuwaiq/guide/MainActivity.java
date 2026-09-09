package om.gov.municipality.alsuwaiq.guide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME_URL = "https://municipality2026-hue.github.io/municipal-guide/?v=44";
    private static final long IDLE_TIMEOUT = 180_000L;
    private final Handler idleHandler = new Handler(Looper.getMainLooper());
    private WebView webView;
    private LinearLayout returnBar;
    private final Runnable returnHome = () -> {
        if (webView != null) webView.loadUrl(HOME_URL);
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        returnBar = new LinearLayout(this);
        returnBar.setGravity(Gravity.CENTER_VERTICAL);
        returnBar.setPadding(dp(16), dp(10), dp(16), dp(10));
        returnBar.setBackgroundColor(Color.rgb(8, 79, 61));

        Button home = new Button(this);
        home.setText("↩  العودة إلى دليل الخدمات البلدية");
        home.setTextSize(18);
        home.setTextColor(Color.rgb(8, 79, 61));
        home.setAllCaps(false);
        home.setOnClickListener(v -> webView.loadUrl(HOME_URL));
        returnBar.addView(home, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(58)));

        TextView title = new TextView(this);
        title.setText("منصة تجاوب");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, dp(58), 1);
        titleParams.setMarginStart(dp(16));
        returnBar.addView(title, titleParams);
        returnBar.setVisibility(View.GONE);

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (isAllowed(uri)) return false;
                Toast.makeText(MainActivity.this, "هذا الرابط غير متاح في شاشة الخدمات", Toast.LENGTH_SHORT).show();
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                returnBar.setVisibility(isTajawob(url) ? View.VISIBLE : View.GONE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                returnBar.setVisibility(isTajawob(url) ? View.VISIBLE : View.GONE);
            }
        });

        root.addView(returnBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
        webView.loadUrl(HOME_URL);
        resetIdleTimer();
    }

    private boolean isAllowed(Uri uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (!"https".equalsIgnoreCase(scheme) || host == null) return false;
        return host.equals("municipality2026-hue.github.io") || host.equals("tajawob.om") || host.endsWith(".tajawob.om");
    }

    private boolean isTajawob(String url) {
        try {
            String host = Uri.parse(url).getHost();
            return host != null && (host.equals("tajawob.om") || host.endsWith(".tajawob.om"));
        } catch (Exception ignored) { return false; }
    }

    private void resetIdleTimer() {
        idleHandler.removeCallbacks(returnHome);
        idleHandler.postDelayed(returnHome, IDLE_TIMEOUT);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        resetIdleTimer();
        return super.dispatchTouchEvent(event);
    }

    @Override public void onBackPressed() {
        if (isTajawob(webView.getUrl())) webView.loadUrl(HOME_URL);
        else if (webView.canGoBack()) webView.goBack();
        else webView.loadUrl(HOME_URL);
    }

    @Override protected void onResume() {
        super.onResume();
        resetIdleTimer();
    }

    @Override protected void onDestroy() {
        idleHandler.removeCallbacks(returnHome);
        if (webView != null) webView.destroy();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
