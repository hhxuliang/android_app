package com.huewu.pla.sample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.way.chat.activity.R;
import com.way.chat.activity.R.id;
import com.way.chat.activity.R.layout;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Huyf
 */
public class VpAdapter extends BaseAdapter {

    private ArrayList<HashMap<String,String>> items=new ArrayList<HashMap<String,String>>();
    private Context context;
//    private TaskCollectAction taskCollectAction;

    private boolean checkFlag;
    private boolean scrollFlag;

    public HashMap<Integer, Boolean> getSelected() {
        return isSelected;
    }

    private HashMap<Integer, Boolean> isSelected = null;//�洢checkbox״̬
    private HashMap<Integer,View> lmap = new HashMap<Integer,View>();
    private Handler statusHandler = null;//����AllCheckBox��handler

    private View view1;
    private View view2;

    private Handler handler;
    private int mLastMotionX, mLastMotionY;

    private long mFirstDownTime = -1;
    //�Ƿ��ƶ���
    private boolean isMoved;
    //������runnable
//    private Runnable mLongPressRunnable;
    //�ƶ�����ֵ
    private static final int TOUCH_SLOP = 20;
    private int pos;

    private class ViewHolder {
        TextView address, billNo, state,time,breakout,union,xfTime;
        ImageView ivSg;
        CheckBox cbSelect;
        ViewPager vp;
        View view1,view2;
    }

    public VpAdapter(Context context, ArrayList<HashMap<String, String>> items, final Handler statusHandler){
        this.context = context;
        isSelected = new HashMap<Integer, Boolean>();
        this.checkFlag = false;
        this.scrollFlag = false;
        this.items = items;
        this.statusHandler = statusHandler;
//        taskCollectAction = new TaskCollectAction();
        initSelectAll(false);//��ʼ��ȫ��checkBox��״̬
        //�¼�����
        handler=new Handler();
//        mTapRunnable = new Runnable() {
//
//            @Override
//            public void run() {
//                sendMessageToUI(TaskCollectFragment.LONG_CLICK_CONFIRM);
//            }
//        };
    }

    private void sendMessageToUI(int type) {
        Message msg = statusHandler.obtainMessage();
        msg.what = type;
        Bundle data = new Bundle();
        data.putInt("pos", pos);
        msg.setData(data);
        statusHandler.sendMessage(msg);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            view1 = LayoutInflater.from(context).inflate(R.layout.worklist, null);
            view2 = LayoutInflater.from(context).inflate(R.layout.view2, null);
            convertView = LayoutInflater.from(context).inflate(R.layout.vp_item, null);
            viewHolder = new ViewHolder();
            viewHolder.address = (TextView) view1.findViewById(R.id.tl_address);
            viewHolder.billNo = (TextView) view1.findViewById(R.id.tl_billNo);
            viewHolder.cbSelect = (CheckBox) view1.findViewById(R.id.tl_checkBox);

            viewHolder.breakout = (TextView) view2.findViewById(R.id.breakout);
            viewHolder.union = (TextView) view2.findViewById(R.id.union);
            viewHolder.xfTime = (TextView) view2.findViewById(R.id.xftime);

            viewHolder.vp = (ViewPager) convertView.findViewById(R.id.tabcontent_vp);
            viewHolder.view1 = view1;
            viewHolder.view2 = view2;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.vp.setAdapter(new TabAdapter(viewHolder));
        viewHolder.vp.setCurrentItem(0);
        viewHolder.breakout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendMessageToUI(TaskCollectFragment.GD_BREAK_OUT);
            }
        });
//        viewHolder.union.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendMessageToUI(TaskCollectFragment.GD_UNION);
//            }
//        });
//        viewHolder.xfTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendMessageToUI(TaskCollectFragment.XF_TIME);
//            }
//        });

        viewHolder.vp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (((ViewPager)v).getCurrentItem()==0){
                            Log.d("qxgd","mFirstDownTime"+mFirstDownTime);
//                            handler.postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());
                            mLastMotionX = x;
                            mLastMotionY = y;
                            isMoved = false;
                            pos = position;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(isMoved) break;
                        if(Math.abs(mLastMotionX-x) > TOUCH_SLOP
                                || Math.abs(mLastMotionY-y) > TOUCH_SLOP) {
                            //�ƶ�������ֵ�����ʾ�ƶ���
                            isMoved = true;
//                            handler.removeCallbacks(mLongPressRunnable);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("qxgd","event.getDownTime()"+event.getDownTime());
                        //˫����ִ��
                        if (!isMoved&&((ViewPager)v).getCurrentItem()==0 && (event.getDownTime() - mFirstDownTime > ViewConfiguration.getDoubleTapTimeout()|| mFirstDownTime == -1)){
                            Log.d("qxgd","event.getEventTime()"+event.getEventTime());
                            if((event.getEventTime() -event.getDownTime())<ViewConfiguration.getTapTimeout()){//&&mFirstDownTime!=-1) {//����С����ֵ�����ʾ������ ���Ҳ���˫��
//                                sendMessageToUI(TaskCollectFragment.CLICK_CONFIRM);
                            }
                            //�ͷ���
                            mFirstDownTime = event.getDownTime();
//                            handler.removeCallbacks(mLongPressRunnable);
                        }
                        break;
                }
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        viewHolder.cbSelect.setClickable(false);//ʹ��item�ĵ��
//                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView,
//                                                 boolean isChecked) {
//                        select(position, isChecked);
////                        sendMessageToUI(TaskCollectFragment.CLICK_CONFIRM);
//                    }
//                });
        Boolean isChecked = isSelected.get(position);
        if (null == isChecked) {
            isChecked = false;
        }
        HashMap<String,String> hashMap = items.get(position);
//        viewHolder.state.setText(taskCollectAction.getStateName("SYS_DICT_STATE",gongDanList.getWfstatus()));
//        viewHolder.ivSg.setVisibility(null!= gongDanList.getSgFlag() && gongDanList.getSgFlag().equals("1")?View.VISIBLE:View.GONE);
        viewHolder.address.setText(hashMap.get("address"));
        viewHolder.billNo.setText(hashMap.get("name"));
//
//        try {
//            viewHolder.time.setText(gongDanList.getHandleTime().trim().length() == 14 ? DateUtil.format_date_list(gongDanList.getHandleTime()) : gongDanList.getHandleTime());
//        } catch (ParseException e) {
//            viewHolder.time.setText(gongDanList.getHandleTime());
//        }

        viewHolder.cbSelect.setChecked(isChecked);
        viewHolder.cbSelect.setVisibility(checkFlag?View.VISIBLE:View.GONE);

        //����ɫ
//        if(position%2==1){
////            convertView.setBackgroundColor(0x15fe8fff);
//            viewHolder.view1.setBackgroundColor(Color.parseColor("#afdfe4"));
//        }

        return convertView;
    }

    public void initSelectAll(boolean isChecked) {
        //To change body of created methods use File | Settings | File Templates.
        isSelected.clear();
        for (int i = 0; i < items.size(); i++) {
            isSelected.put(i, isChecked);
        }
    }

    private boolean isAllSelected() {
        for (int i = 0; i < isSelected.size(); i++) {
            if (!isSelected.get(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isOneSelected() {
        for (int i = 0; i < isSelected.size(); i++) {
            if (isSelected.get(i)) {
                return true;
            }
        }
        return false;
    }

    public void selectAll(boolean isChecked){
        for (int i = 0; i < items.size(); i++) {
            isSelected.put(i, isChecked);
        }
        notifyDataSetChanged();
    }

    public void select(int potision, boolean isChecked) {
        isSelected.put(potision, isChecked);//��¼�µ�ǰcheckBox��״̬
        Message msg = statusHandler.obtainMessage();
        if (isChecked) {
            if (isAllSelected()) {
//                msg.what = TaskCollectFragment.ALL_SELECTED_CHECK;//��AllCheckbox״̬��Ϊѡ��
            } else {
//                msg.what = TaskCollectFragment.NOT_ALL_SELECTED_CHECK;//��AllCheckBox�ĳ�δѡ��
            }
        } else {
//            msg.what = TaskCollectFragment.NOT_ALL_SELECTED_CHECK;//��AllCheckBox�ĳ�δѡ��
        }
        statusHandler.sendMessage(msg);

        Message msgPanel = statusHandler.obtainMessage();
        if(isOneSelected()){
//            msgPanel.what = TaskCollectFragment.EXIST_SELECTED_CHECK;//��������һ��checkBox��ѡ��
            statusHandler.sendMessage(msgPanel);
        }else{
//            msgPanel.what = TaskCollectFragment.NOEXIST_SELECTED_CHECK;//������checkBox��ѡ��
            statusHandler.sendMessage(msgPanel);
        }
    }

    public void addSelected(int count) {
        int num = isSelected.size();
        for(int i = 0; i < count; i++){
            isSelected.put(num+i,false);
        }
        for(int i = isSelected.size()-1; i >= 0; i--){
            if(isSelected.get(i)){
                isSelected.put(i+count,true);
                isSelected.put(i,false);
            }
        }
    }

    public void setCheckFlag(boolean checkFlag) {
        this.checkFlag = checkFlag;
        notifyDataSetChanged();
    }

    public void setScrollFlag(boolean scrollFlag) {
        this.scrollFlag = scrollFlag;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    private class TabAdapter extends PagerAdapter {

        private  ViewHolder viewHolder;

        public TabAdapter(ViewHolder viewHolder) {
            //To change body of created methods use File | Settings | File Templates.
            this.viewHolder = viewHolder;
        }

        @Override
        public int getCount() {
            return scrollFlag?1:2;
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            Log.i("VpAdapter", "shit");
            if (arg1 == 0) {
                ((ViewPager) arg0).addView(viewHolder.view1);
                Log.i("VpAdapter", "view1 == null? :" + (viewHolder.view1 == null));
                return viewHolder.view1;
            } else {
                if(!scrollFlag)((ViewPager) arg0).addView(viewHolder.view2);
                Log.i("VpAdapter", "view2 == null? :" + (viewHolder.view2 == null));
                return viewHolder.view2;
            }
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }

        @Override
        public void finishUpdate(View arg0) {

        }
    }

}
