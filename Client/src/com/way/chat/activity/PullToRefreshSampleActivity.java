package com.way.chat.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dodola.model.DuitangInfo;
import com.dodowaterfall.Helper;
import com.dodowaterfall.widget.ScaleImageView;
import com.example.android.bitmapfun.util.ImageFetcher;

import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.AsyncTask.Status;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Androidʵ�����һ���ָ��Ч��
 * @Description: Androidʵ�����һ���ָ��Ч��

 * @File: MyGuideViewActivity.java

 * @Package com.test.guide

 * @Author Hanyonglu

 * @Date 2012-4-6 ����11:15:18

 * @Version V1.0
 */
public class PullToRefreshSampleActivity extends FragmentActivity implements IXListViewListener{
	 private ViewPager viewPager;  
	 private ArrayList<View> pageViews;  
	 private ImageView imageView;  
	 private ImageView[] imageViews; 
	 // ��������ͼƬLinearLayout
	 private ViewGroup main;
	 // ����СԲ���LinearLayout
	 private ViewGroup group;
	 private XListView tv1;
	 private XListView tv2;
	 private XListView tv3;
	 private XListView tv4;
	 private TextView tv5;
	 private TextView tv6;
	 private ImageFetcher mImageFetcher;
     private XListView[] mAdapterView;
     private StaggeredAdapter[] mAdapter;
     private int currentPage = 0;
     ContentTask[] task;
     
     private class ContentTask extends AsyncTask<String, Integer, List<DuitangInfo>> {

         private Context mContext;
         private int mType = 1;
         private XListView mAdapterView_t = null;
         private StaggeredAdapter mAdapter_t = null;

         public ContentTask(Context context, int type,XListView av,StaggeredAdapter sa) {
             super();
             mContext = context;
             mType = type;
             mAdapterView_t = av;
             mAdapter_t = sa;
         }

         @Override
         protected List<DuitangInfo> doInBackground(String... params) {
             try {
                 return parseNewsJSON(params[0]);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return null;
         }

         @Override
         protected void onPostExecute(List<DuitangInfo> result) {
             if (mType == 1) {

                 mAdapter_t.addItemTop(result);
                 mAdapter_t.notifyDataSetChanged();
                 mAdapterView_t.stopRefresh();

             } else if (mType == 2) {
                 mAdapterView_t.stopLoadMore();
                 mAdapter_t.addItemLast(result);
                 mAdapter_t.notifyDataSetChanged();
             }

         }

         @Override
         protected void onPreExecute() {
         }

         public List<DuitangInfo> parseNewsJSON(String url) throws IOException {
             List<DuitangInfo> duitangs = new ArrayList<DuitangInfo>();
             String json = "";
             if (Helper.checkConnection(mContext)) {
                 try {
                     json = Helper.getStringFromUrl(url);

                 } catch (IOException e) {
                     Log.e("IOException is : ", e.toString());
                     e.printStackTrace();
                     return duitangs;
                 }
             }
             Log.d("MainActiivty", "json:" + json);

             try {
                 if (null != json) {
                     JSONObject newsObject = new JSONObject(json);
                     JSONObject jsonObject = newsObject.getJSONObject("data");
                     JSONArray blogsJson = jsonObject.getJSONArray("blogs");

                     for (int i = 0; i < blogsJson.length(); i++) {
                         JSONObject newsInfoLeftObject = blogsJson.getJSONObject(i);
                         DuitangInfo newsInfo1 = new DuitangInfo();
                         newsInfo1.setAlbid(newsInfoLeftObject.isNull("albid") ? "" : newsInfoLeftObject.getString("albid"));
                         newsInfo1.setIsrc(newsInfoLeftObject.isNull("isrc") ? "" : newsInfoLeftObject.getString("isrc"));
                         newsInfo1.setMsg(newsInfoLeftObject.isNull("msg") ? "" : newsInfoLeftObject.getString("msg"));
                         newsInfo1.setHeight(newsInfoLeftObject.getInt("iht"));
                         duitangs.add(newsInfo1);
                     }
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             return duitangs;
         }
     }   
     
     /**
      * 添加内容
      * 
      * @param pageindex
      * @param type
      *            1为下拉刷新 2为加载更多
      */
     private void AddItemToContainer(int pageindex, int type) {
    	 
         if (task[0].getStatus() != Status.RUNNING) {
        	 String url = "http://www.duitang.com/album/1733789/masn/p/" + pageindex + "/24/";
             Log.d("MainActivity", "current url:" + url);
             ContentTask task = new ContentTask(this, type,mAdapterView[0],mAdapter[0]);
             task.execute(url);
         }
         if (task[1].getStatus() != Status.RUNNING) {
        	 String url = "http://www.duitang.com/album/1733789/masn/p/" + (pageindex+4) + "/24/";
             Log.d("MainActivity", "current url:" + url);
             ContentTask task1 = new ContentTask(this, type,mAdapterView[1],mAdapter[1]);
             task1.execute(url);
         }
         if (task[2].getStatus() != Status.RUNNING) {
        	 String url = "http://www.duitang.com/album/1733789/masn/p/" + (pageindex+8) + "/24/";
             Log.d("MainActivity", "current url:" + url);
        	 ContentTask task2 = new ContentTask(this, type,mAdapterView[2],mAdapter[2]);
             task2.execute(url);
         }
         if (task[3].getStatus() != Status.RUNNING) {
        	 String url = "http://www.duitang.com/album/1733789/masn/p/" + (pageindex+10) + "/24/";
             Log.d("MainActivity", "current url:" + url);
        	 ContentTask task3 = new ContentTask(this, type,mAdapterView[3],mAdapter[3]);
             task3.execute(url);
         }
         
     }

     public class StaggeredAdapter extends BaseAdapter {
         private Context mContext;
         private LinkedList<DuitangInfo> mInfos;
         private XListView mListView;

         public StaggeredAdapter(Context context, XListView xListView) {
             mContext = context;
             mInfos = new LinkedList<DuitangInfo>();
             mListView = xListView;
         }

         @Override
         public View getView(int position, View convertView, ViewGroup parent) {

             ViewHolder holder;
             DuitangInfo duitangInfo = mInfos.get(position);

             if (convertView == null) {
                 LayoutInflater layoutInflator = LayoutInflater.from(parent.getContext());
                 convertView = layoutInflator.inflate(R.layout.infos_list, null);
                 holder = new ViewHolder();
                 holder.imageView = (ScaleImageView) convertView.findViewById(R.id.news_pic);
                 holder.contentView = (TextView) convertView.findViewById(R.id.news_title);
                 convertView.setTag(holder);
             }

             holder = (ViewHolder) convertView.getTag();
             holder.imageView.setImageWidth(duitangInfo.getWidth());
             holder.imageView.setImageHeight(duitangInfo.getHeight());
             holder.contentView.setText(duitangInfo.getMsg());
             mImageFetcher.loadImage(duitangInfo.getIsrc(), holder.imageView);
             return convertView;
         }

         class ViewHolder {
             ScaleImageView imageView;
             TextView contentView;
             TextView timeView;
         }

         @Override
         public int getCount() {
             return mInfos.size();
         }

         @Override
         public Object getItem(int arg0) {
             return mInfos.get(arg0);
         }

         @Override
         public long getItemId(int arg0) {
             return 0;
         }

         public void addItemLast(List<DuitangInfo> datas) {
             mInfos.addAll(datas);
         }

         public void addItemTop(List<DuitangInfo> datas) {
             for (DuitangInfo info : datas) {
                 mInfos.addFirst(info);
             }
         }
     }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // �����ޱ��ⴰ��
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater = getLayoutInflater();  
        
        View v1 = inflater.inflate(R.layout.item01, null);
        View v2 = inflater.inflate(R.layout.item02, null);
        View v3 = inflater.inflate(R.layout.item03, null);
        View v4 = inflater.inflate(R.layout.item04, null);
        
        pageViews = new ArrayList<View>();  
        pageViews.add(v1);
        pageViews.add(v2);
        pageViews.add(v3);  
        pageViews.add(v4);   
        
        imageViews = new ImageView[pageViews.size()];  
        mAdapterView = new XListView[4];
        mAdapter = new StaggeredAdapter[4];
        task = new ContentTask[4];
        main = (ViewGroup)inflater.inflate(R.layout.main_for_pic, null);  
        
        group = (ViewGroup)main.findViewById(R.id.viewGroup);  
        viewPager = (ViewPager)main.findViewById(R.id.guidePages);
        
        for (int i = 0; i < pageViews.size(); i++) {  
            imageView = new ImageView(PullToRefreshSampleActivity.this);  
            imageView.setLayoutParams(new LayoutParams(20,20));  
            imageView.setPadding(20, 0, 20, 0);  
            imageViews[i] = imageView;  
            
            if (i == 0) {  
                //Ĭ��ѡ�е�һ��ͼƬ
                imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused);  
            } else {  
                imageViews[i].setBackgroundResource(R.drawable.page_indicator);  
            }  
            
            group.addView(imageViews[i]);  
        }  
        
        setContentView(main);
        
        // ������ҳ���¼�
        tv1 = (XListView)v1.findViewById(R.id.list);
        //tv1.setOnClickListener(new TextViewOnClickListener());
        tv2 = (XListView)v2.findViewById(R.id.list1);
        //tv2.setOnClickListener(new TextViewOnClickListener());
        tv3 = (XListView)v3.findViewById(R.id.list2);
        //tv3.setOnClickListener(new TextViewOnClickListener());
        tv4 = (XListView)v4.findViewById(R.id.list3);
        
        
        viewPager.setAdapter(new GuidePageAdapter());  
        viewPager.setOnPageChangeListener(new GuidePageChangeListener());  
        
        mAdapterView[0] = (XListView) v1.findViewById(R.id.list);
        mAdapterView[0].setPullLoadEnable(true);
        mAdapterView[0].setXListViewListener(this);
        mAdapter[0] = new StaggeredAdapter(this, mAdapterView[0]);
        task[0] = new ContentTask(this, 2,mAdapterView[0],mAdapter[0]);
        
        mAdapterView[1] = (XListView) v2.findViewById(R.id.list1);
        mAdapterView[1].setPullLoadEnable(true);
        mAdapterView[1].setXListViewListener(this);
        mAdapter[1] = new StaggeredAdapter(this, mAdapterView[1]);
        task[1] = new ContentTask(this, 2,mAdapterView[1],mAdapter[1]);

        mAdapterView[2] = (XListView) v3.findViewById(R.id.list2);
        mAdapterView[2].setPullLoadEnable(true);
        mAdapterView[2].setXListViewListener(this);
        mAdapter[2] = new StaggeredAdapter(this, mAdapterView[2]);
        task[2] = new ContentTask(this, 2,mAdapterView[2],mAdapter[2]);

        mAdapterView[3] = (XListView) v4.findViewById(R.id.list3);
        mAdapterView[3].setPullLoadEnable(true);
        mAdapterView[3].setXListViewListener(this);
        mAdapter[3] = new StaggeredAdapter(this, mAdapterView[3]);
        task[3] = new ContentTask(this, 2,mAdapterView[3],mAdapter[3]);

        
        

        mImageFetcher = new ImageFetcher(this, 240);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
    }
    
    
    
    // ָ��ҳ������������
    private class GuidePageAdapter extends PagerAdapter {  
  	  
        @Override  
        public int getCount() {  
            return pageViews.size();  
        }  
  
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {  
            return arg0 == arg1;  
        }  
  
        @Override  
        public int getItemPosition(Object object) {  
            // TODO Auto-generated method stub  
            return super.getItemPosition(object);  
        }  
  
        @Override  
        public void destroyItem(View arg0, int arg1, Object arg2) {  
            // TODO Auto-generated method stub  
            ((ViewPager) arg0).removeView(pageViews.get(arg1));  
        }  
  
        @Override  
        public Object instantiateItem(View arg0, int arg1) {  
            // TODO Auto-generated method stub  
            ((ViewPager) arg0).addView(pageViews.get(arg1));  
            return pageViews.get(arg1);  
        }  
  
        @Override  
        public void restoreState(Parcelable arg0, ClassLoader arg1) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public Parcelable saveState() {  
            // TODO Auto-generated method stub  
            return null;  
        }  
  
        @Override  
        public void startUpdate(View arg0) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public void finishUpdate(View arg0) {  
            // TODO Auto-generated method stub  
  
        }  
    } 
    
    // ָ��ҳ������¼�������
    private class GuidePageChangeListener implements OnPageChangeListener {  
    	  
        @Override  
        public void onPageScrollStateChanged(int arg0) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public void onPageScrolled(int arg0, float arg1, int arg2) {  
            // TODO Auto-generated method stub  
  
        }  
  
        @Override  
        public void onPageSelected(int arg0) {  
            for (int i = 0; i < imageViews.length; i++) {  
                imageViews[arg0].setBackgroundResource(R.drawable.page_indicator_focused);
                
                if (arg0 != i) {  
                    imageViews[i].setBackgroundResource(R.drawable.page_indicator);  
                }  
            }
        }  
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapterView[0].setAdapter(mAdapter[0]);
        mAdapterView[1].setAdapter(mAdapter[1]);
        mAdapterView[2].setAdapter(mAdapter[2]);
        mAdapterView[3].setAdapter(mAdapter[3]);
        AddItemToContainer(currentPage, 2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRefresh() {
        AddItemToContainer(++currentPage, 1);

    }

    @Override
    public void onLoadMore() {
        AddItemToContainer(++currentPage, 2);

    }
}