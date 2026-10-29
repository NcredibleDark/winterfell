package de.winterfell.companion;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

/**
 * Winterfell Companion: zeigt die bestehende WinterfellStats-Website.
 * Login, Daten und Farmen bleiben unveraendert auf dem Minecraft-Server.
 */
public final class MainActivity extends Activity {
    private static final String WEBSITE = "http://winterfell-mc.com/";
    private static final int ICE_BACKGROUND = Color.rgb(237, 245, 250);
    private static final int ICE_BLUE = Color.rgb(65, 143, 189);

    private FrameLayout root;
    private WebView webView;
    private ProgressBar progress;
    private View errorPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ICE_BACKGROUND);
        getWindow().setNavigationBarColor(ICE_BACKGROUND);
        if (Build.VERSION.SDK_INT >= 26) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        root = new FrameLayout(this);
        root.setBackgroundColor(ICE_BACKGROUND);
        setContentView(root);

        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        configureWebView();

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(ICE_BLUE));
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(3), Gravity.TOP);
        root.addView(progress, progressParams);

        errorPanel = createErrorPanel();
        root.addView(errorPanel, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        errorPanel.setVisibility(View.GONE);

        if (savedInstanceState != null && webView.restoreState(savedInstanceState) != null) {
            // Vorherige WebView-Seite nach Rotation wiederherstellen.
        } else {
            webView.loadUrl(WEBSITE);
        }
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setGeolocationEnabled(false);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= 26) {
            settings.setSafeBrowsingEnabled(true);
        }

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, false);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                if (progress != null) {
                    progress.setProgress(newProgress);
                    progress.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
                }
            }
            @Override public void onPermissionRequest(PermissionRequest request) {
                request.deny();
            }
            @Override public void onGeolocationPermissionsShowPrompt(
                    String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, false, false);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                return handleNavigation(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(Uri.parse(url));
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                if (errorPanel != null) errorPanel.setVisibility(View.GONE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (progress != null) progress.setVisibility(View.GONE);
            }
            @Override public void onReceivedError(WebView view,
                    WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) showConnectionError();
            }
            @Override public void onReceivedHttpError(WebView view,
                    WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame() && response.getStatusCode() >= 500) {
                    showConnectionError();
                }
            }
            @Override public void onReceivedSslError(WebView view,
                    SslErrorHandler handler, SslError error) {
                handler.cancel();
                showConnectionError();
            }
        });
    }

    private boolean handleNavigation(Uri uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null) return true;
        scheme = scheme.toLowerCase(Locale.ROOT);
        if (scheme.equals("about")) return false;
        if (!scheme.equals("http") && !scheme.equals("https")) return true;
        if (host != null) {
            host = host.toLowerCase(Locale.ROOT);
            if (host.equals("winterfell-mc.com") || host.equals("www.winterfell-mc.com")
                    || host.equals("winterfell.server.live")) return false;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException ignored) {
        }
        return true;
    }

    private View createErrorPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(26), dp(25), dp(26), dp(25));
        panel.setBackgroundColor(ICE_BACKGROUND);

        TextView logo = new TextView(this);
        logo.setText("❄");
        logo.setTextSize(54);
        logo.setTextColor(ICE_BLUE);
        logo.setGravity(Gravity.CENTER);
        panel.addView(logo);

        TextView heading = new TextView(this);
        heading.setText(R.string.offline_title);
        heading.setTextSize(21);
        heading.setTextColor(Color.rgb(34, 62, 85));
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, dp(14), 0, dp(9));
        panel.addView(heading);

        TextView hint = new TextView(this);
        hint.setText(R.string.offline_description);
        hint.setTextSize(14);
        hint.setTextColor(Color.rgb(108, 140, 160));
        hint.setGravity(Gravity.CENTER);
        panel.addView(hint);

        Button retry = new Button(this);
        retry.setText(R.string.offline_retry);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = dp(20);
        panel.addView(retry, buttonParams);
        retry.setOnClickListener(v -> {
            errorPanel.setVisibility(View.GONE);
            webView.loadUrl(WEBSITE);
        });
        return panel;
    }

    private void showConnectionError() {
        if (errorPanel != null) errorPanel.setVisibility(View.VISIBLE);
        if (progress != null) progress.setVisibility(View.GONE);
    }

    private int dp(int dips) {
        return Math.round(getResources().getDisplayMetrics().density * dips);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override protected void onPause() {
        super.onPause();
        webView.onPause();
        CookieManager.getInstance().flush();
    }

    @Override protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (webView != null) {
            root.removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}