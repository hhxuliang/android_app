/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huewu.pla.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.dodola.model.DuitangInfo;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.example.android.bitmapfun.util.Utils;
import com.zoom.ZoomImageView;
import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ImageView mImageView;
    private ImageFetcher mImageFetcher;
    private TextView mTextView;
    private String message;
    private ListView mListView;
    private Handler mHandler;
    private DuitangInfo  mDuitangInfo;

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     *
     * @param mInfo The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragment newInstance(DuitangInfo mInfo) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, mInfo.getIsrc());
        args.putString("message",mInfo.getMsg());
        f.setArguments(args);
        f.mDuitangInfo = mInfo;
        System.out.println("the size is:"+mInfo.getCommentList().size());

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {}

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageDetailFragment#newInstance(com.dodola.model.DuitangInfo)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
        message = getArguments() != null ? getArguments().getString("message") : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        mTextView = (TextView) v.findViewById(R.id.message);
        mListView = (ListView) v.findViewById(R.id.shop);

        //setView
        mTextView.setText(message);
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setDrawingCacheEnabled(true);
                Bitmap bitmap = v.getDrawingCache();
                ZoomImageView zoom = new ZoomImageView(getActivity(), bitmap);
                zoom.showZoomView();
            }
        });
        ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
        
        for(int i = 0; i < mDuitangInfo.getCommentList().size();i++){
        	HashMap<String,String> map = new HashMap<String,String>();
            map.put("name",mDuitangInfo.getCommentList().get(i).getUsername());
	        map.put("address",mDuitangInfo.getCommentList().get(i).getMessage());
	        System.out.println("111111111111:" + mDuitangInfo.getCommentList().get(i).getUsername());
	        list.add(map);
        }
        
        VpAdapter adapter = new VpAdapter(getActivity(),list,mHandler);
        mListView.setAdapter(adapter);

        return v;
    }

    private void setHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (!Thread.currentThread().isInterrupted()) {
                    switch (msg.what) {
                        case 100:
                            Log.d("myTag", "sucess");
                            break;
                        case -1:
                            Log.d("myTag","fail");
                            break;
                        case 1://评论
                            break;
                        case 2://到这里去
                            break;
                        case 3://收藏
                            break;
                        default:
                            break;
                    }
                }
            }
        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (ImageDetailActivity.class.isInstance(getActivity())) {
            mImageFetcher = ((ImageDetailActivity) getActivity()).getImageFetcher();
            mImageFetcher.loadImage(mImageUrl, mImageView);
        }

        // Pass clicks on the ImageView to the parent activity to handle
        if (OnClickListener.class.isInstance(getActivity()) && Utils.hasHoneycomb()) {
//            mImageView.setOnClickListener((OnClickListener) getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
    }
}
