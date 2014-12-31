package com.kids.activity.chat;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.way.chat.activity.R;

public class WebActivity extends MyActivity implements OnClickListener {
	String url;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webactivity);
		url = getIntent().getStringExtra("url");
		String title = getIntent().getStringExtra("title");
		Button back = (Button) findViewById(R.id.back);
		TextView tv=(TextView)findViewById(R.id.pic_name);
		tv.setText(title);
		back.setOnClickListener(this);
		init();

	}

	private void init() {
		WebView webView = (WebView) findViewById(R.id.webView);
		// WebView加载web资源
		webView.loadUrl(url);
		// 覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				// 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				view.loadUrl(url);
				return true;
			}
		});
	}
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.back:
			finish();
			break;
		}
	}

}
