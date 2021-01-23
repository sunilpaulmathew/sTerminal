package com.sunilpaulmathew.terminal.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.terminal.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 05, 2020
 */

public class LicenceFragment extends Fragment {

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_webview, container, false);

        AppCompatImageButton mBack = mRootView.findViewById(R.id.back);
        MaterialTextView mCancel = mRootView.findViewById(R.id.cancel_button);
        SwipeRefreshLayout mSwipeRefreshLayout = mRootView.findViewById(R.id.swipe_layout);
        WebView mWebView = mRootView.findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("https://www.gnu.org/licenses/gpl-3.0-standalone.html");
        mWebView.setWebViewClient(new WebViewClient());

        mCancel.setOnClickListener(v -> requireActivity().finish());
        mBack.setOnClickListener(v -> requireActivity().finish());

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mWebView.reload();
            mWebView.setWebViewClient(new WebViewClient() {
                public void onPageFinished(WebView view, String url) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    requireActivity().finish();
                }
            }
        });

        return mRootView;
    }

}