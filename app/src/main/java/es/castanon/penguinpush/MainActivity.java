package es.castanon.penguinpush;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.os.Build;
import android.window.OnBackInvokedDispatcher;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/** Offline native host. All game content is served from the bundled assets. */
public final class MainActivity extends Activity {
    private static final String HOST = "penguinpush.local";
    private static final String HOME = "https://" + HOST + "/index.html";
    private static final Set<String> FILES = new HashSet<>(Arrays.asList("index.html", "style.css", "assets/penguins.png", "assets/icon.svg", "js/levels.js", "js/engine.js", "js/audio.js", "js/renderer.js", "js/app.js"));
    private WebView webView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openGame();
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::confirmExit);
        }
    }

    private void openGame() {
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String path = uri.getPath();
                if (!"https".equals(uri.getScheme()) || !HOST.equals(uri.getHost()) || path == null || !path.startsWith("/")) return denied();
                String file = path.substring(1);
                if (!FILES.contains(file)) return denied();
                String mime = file.endsWith(".png") ? "image/png" : file.endsWith(".svg") ? "image/svg+xml" : file.endsWith(".css") ? "text/css" : file.endsWith(".js") ? "text/javascript" : "text/html";
                try {
                    return new WebResourceResponse(mime, file.endsWith(".png") ? null : "UTF-8", getAssets().open("game/" + file));
                } catch (IOException exception) { return denied(); }
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !HOME.equals(request.getUrl().toString());
            }
            @Override public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
                view.destroy();
                webView = null;
                showError();
                return true;
            }
        });
        // Android 15+ draws edge-to-edge: keep controls clear of system bars.
        webView.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        setContentView(webView);
        webView.requestApplyInsets();
        webView.loadUrl(HOME);
    }

    private static WebResourceResponse denied() {
        return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", Collections.emptyMap(), new ByteArrayInputStream("Not Found".getBytes(StandardCharsets.UTF_8)));
    }

    private void showError() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        TextView message = new TextView(this);
        message.setText(R.string.error_loading);
        Button retry = new Button(this);
        retry.setText(R.string.retry);
        retry.setOnClickListener(view -> openGame());
        layout.addView(message);
        layout.addView(retry);
        setContentView(layout);
    }

    @Override protected void onPause() {
        if (webView != null) {
            webView.evaluateJavascript("window.PenguinPush && window.PenguinPush.pause()", null);
            webView.onPause();
        }
        super.onPause();
    }
    @Override protected void onResume() { super.onResume(); if (webView != null) webView.onResume(); }
    @Override protected void onDestroy() { if (webView != null) { webView.destroy(); webView = null; } super.onDestroy(); }
    // Only the fallback for API 24-32. API 33+ uses the platform callback registered in onCreate.
    // Lint cannot associate that conditional registration with this legacy override.
    @SuppressLint("GestureBackNavigation")
    @SuppressWarnings("deprecation") @Override public void onBackPressed() { confirmExit(); }
    private void confirmExit() {
        new AlertDialog.Builder(this).setTitle("¿Salir de PenguinPush?").setMessage("Los niveles completados quedan guardados.")
                .setNegativeButton("Seguir jugando", null).setPositiveButton("Salir", (dialog, which) -> finish()).show();
    }
}
