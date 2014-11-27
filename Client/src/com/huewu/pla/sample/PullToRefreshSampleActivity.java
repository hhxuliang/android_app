package com.huewu.pla.sample;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.dodola.model.Comment;
import com.dodola.model.DuitangInfo;
import com.dodowaterfall.Helper;
import com.dodowaterfall.widget.ScaleImageView;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.huewu.pla.AsyncImageLoader;
import com.huewu.pla.lib.internal.PLA_AdapterView;
import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;
import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PullToRefreshSampleActivity extends FragmentActivity implements IXListViewListener {
    private static final String STATE_ACTIVE_POSITION = "com.huewu.pla.sample.PullToRefreshSampleActivity.activePosition";
    private ImageFetcher mImageFetcher;
    private XListView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private int currentPage = 0;
    ContentTask task = new ContentTask(this, 2);
    private MenuDrawer mMenuDrawer;
    private int mActivePosition = -1;
    private ListView mList;
    private MenuAdapter mMenuAdapter;
    private String albumId = "60917958";
    private AsyncImageLoader mAsyncImageLoader;

    private class ContentTask extends AsyncTask<String, Integer, List<DuitangInfo>> {

        private Context mContext;
        private int mType = 1;

        public ContentTask(Context context, int type) {
            super();
            mContext = context;
            mType = type;
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

                mAdapter.addItemTop(result);
                mAdapter.notifyDataSetChanged();
                mAdapterView.stopRefresh();

            } else if (mType == 2) {
                mAdapterView.stopLoadMore();
                mAdapter.addItemLast(result);
                mAdapter.notifyDataSetChanged();
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
                        JSONArray commentJson = newsInfoLeftObject.getJSONArray("cmts");
                        newsInfo1.setCommentList(new ArrayList<Comment>());
                        for (int j = 0; j < commentJson.length(); j++) {
                        	JSONObject commentObject = commentJson.getJSONObject(j);
                        	Comment c = new Comment(); 
                        	c.setID(commentObject.isNull("id") ? "" : commentObject.getString("id"));
                        	c.setMessage(commentObject.isNull("cont") ? "" : commentObject.getString("cont"));
                        	c.setUsername(commentObject.isNull("name") ? "" : commentObject.getString("name"));
                        	newsInfo1.getCommentList().add(c);
                        }
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
    private void AddItemToContainer(int pageindex, int type, String albumId) {
        if (task.getStatus() != Status.RUNNING) {
            String url = "http://www.duitang.com/album/"+albumId+"/masn/p/" + pageindex + "/24/";
            Log.d("MainActivity", "current url:" + url);
            ContentTask task = new ContentTask(this, type);
            task.execute(url);

        }else if (task != null && task.getStatus() != AsyncTask.Status.FINISHED){
            task.cancel(true);
            String url = "http://www.duitang.com/album/"+albumId+"/masn/p/" + pageindex + "/24/";
            Log.d("MainActivity", "current url:" + url);
            ContentTask task = new ContentTask(this, type);
            task.execute(url);
        }
    }

    public class StaggeredAdapter extends BaseAdapter {
        private Context mContext;

        public LinkedList<DuitangInfo> getmInfos() {
            return mInfos;
        }

        private LinkedList<DuitangInfo> mInfos;
        private XListView mListView;

        public StaggeredAdapter(Context context, XListView xListView) {
            mContext = context;
            mInfos = new LinkedList<DuitangInfo>();
            mListView = xListView;
            mAsyncImageLoader = AsyncImageLoader.getInstance();
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
//            Bitmap bitmap = mAsyncImageLoader.decodeSampledBitmapFromResource(
//                    duitangInfo.getIsrc(), duitangInfo.getWidth());
//            mAsyncImageLoader.addBitmapToMemoryCache(duitangInfo.getIsrc(), bitmap);
//            holder.imageView.setImageBitmap(bitmap);
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

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);

        if (inState != null) {
            mActivePosition = inState.getInt(STATE_ACTIVE_POSITION);
        }

        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_CONTENT, Position.RIGHT);
        mMenuDrawer.setContentView(R.layout.act_pull_to_refresh_sample);

        List<Object> items = new ArrayList<Object>();
        items.add(new Category("全部图片"));
        items.add(new Item("热门", R.drawable.ic_action_refresh_dark));
        items.add(new Item("最新", R.drawable.ic_action_refresh_dark));
        items.add(new Category("我们学校"));
        items.add(new Item("热门", R.drawable.ic_action_select_all_dark));
        items.add(new Item("最新", R.drawable.ic_action_select_all_dark));
        items.add(new Category("我们班级"));
        items.add(new Item("热门", R.drawable.ic_action_select_all_dark));
        items.add(new Item("最新", R.drawable.ic_action_select_all_dark));
        items.add(new Category("我的发布"));
        items.add(new Item("全部", R.drawable.ic_action_select_all_dark));
        items.add(new Item("相机", R.drawable.camera));

        // A custom ListView is needed so the drawer can be notified when it's scrolled. This is to update the position
        // of the arrow indicator.
        mList = new ListView(this);
        mMenuAdapter = new MenuAdapter(items);
        mList.setAdapter(mMenuAdapter);
        mList.setOnItemClickListener(mItemClickListener);
        mList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mMenuDrawer.invalidate();
            }
        });
        mList.setBackgroundResource(R.color.red);
        mMenuDrawer.setMenuView(mList);

//        setContentView(R.layout.act_pull_to_refresh_sample);
        mAdapterView = (XListView) findViewById(R.id.list);
        mAdapterView.setPullLoadEnable(true);
        mAdapterView.setXListViewListener(this);

        mAdapter = new StaggeredAdapter(this, mAdapterView);

        mImageFetcher = new ImageFetcher(this, 240);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);

        mAdapterView.setOnItemClickListener(new PLA_AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(PLA_AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent().setClass(PullToRefreshSampleActivity.this, ImageDetailActivity.class);
                in.putExtra(ImageDetailActivity.EXTRA_IMAGE,position-1);
                Bundle bu = new Bundle();
                ArrayList list = new ArrayList();
                list.add(mAdapter.getmInfos());
                bu.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) list);  //---
                in.putExtras(bu);
                startActivity(in);
            }
        });
    }
    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mActivePosition = position;
            mMenuDrawer.setActiveView(view, position);
            mMenuDrawer.closeMenu();
            System.out.println("currentPage is:"+currentPage);
            switch (position){
                case 0:
                    albumId = "60917958";
                    AddItemToContainer(++currentPage,1,albumId);
                    break;
                case 1:
                    albumId = "542468";
                    AddItemToContainer(++currentPage,1,albumId);
                    break;
                case 3:
                    albumId = "6760717";
                    AddItemToContainer(++currentPage,1,albumId);
                    break;
                case 4:
                    albumId = "4147732";
                    AddItemToContainer(++currentPage,1,albumId);
                    break;
            }
        }
    };

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
        mAdapterView.setAdapter(mAdapter);
        AddItemToContainer(currentPage, 2,albumId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRefresh() {
        AddItemToContainer(++currentPage, 1,albumId);

    }

    @Override
    public void onLoadMore() {
        AddItemToContainer(++currentPage, 2,albumId);

    }

    private static class Item {

        String mTitle;
        int mIconRes;

        Item(String title, int iconRes) {
            mTitle = title;
            mIconRes = iconRes;
        }
    }

    private static class Category {

        String mTitle;

        Category(String title) {
            mTitle = title;
        }
    }

    private class MenuAdapter extends BaseAdapter {

        private List<Object> mItems;

        MenuAdapter(List<Object> items) {
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position) instanceof Item ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position) instanceof Item;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Object item = getItem(position);

            if (item instanceof Category) {
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.menu_row_category, parent, false);
                }

                ((TextView) v).setText(((Category) item).mTitle);

            } else {
                if (v == null) {
                    v = getLayoutInflater().inflate(R.layout.menu_row_item, parent, false);
                }

                TextView tv = (TextView) v;
                tv.setText(((Item) item).mTitle);
                tv.setCompoundDrawablesWithIntrinsicBounds(((Item) item).mIconRes, 0, 0, 0);
            }

            v.setTag(R.id.mdActiveViewPosition, position);

            if (position == mActivePosition) {
                mMenuDrawer.setActiveView(v, position);
            }

            return v;
        }
    }
}// end of class
