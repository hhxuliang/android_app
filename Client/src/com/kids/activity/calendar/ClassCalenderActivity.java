package com.kids.activity.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.kids.activity.calendar.CalendarFragment.FragmentListener;
import com.kids.activity.chat.ChatMsgEntity;
import com.kids.activity.chat.ChatMsgViewAdapter;
import com.kids.activity.chat.MyActivity;
import com.kids.util.CalendarUtils;
import com.way.chat.activity.R;
import com.way.chat.common.bean.User;
import com.way.chat.common.util.MyDate;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 日历
 * 
 * @author 程科
 */
public class ClassCalenderActivity extends MyActivity implements FragmentListener {

	private ViewPager viewPager;
	private TextView tvMonth;
	private String month;
	private EventAdapter mAdapter;// 消息视图的Adapter
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组
	private ListView mListView;
	private User user;
	private int crowdid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_calender);
		crowdid=getIntent().getIntExtra("crowdid", -1);
		viewPager = (ViewPager) this.findViewById(R.id.viewpager);
		final ScreenSlidePagerAdapter screenSlidePagerAdapter = new ScreenSlidePagerAdapter(
				getFragmentManager());
		viewPager.setAdapter(screenSlidePagerAdapter);
		viewPager.setCurrentItem(500);
		tvMonth = (TextView) this.findViewById(R.id.tv_month);
		mListView = (ListView) findViewById(R.id.listView);
		month = Calendar.getInstance().get(Calendar.YEAR)
				+ "-"
				+ CalendarUtils.LeftPad_Tow_Zero(Calendar.getInstance().get(
						Calendar.MONTH) + 1);
		tvMonth.setText(month);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				Calendar calendar = CalendarUtils.getSelectCalendar(position);
				month = calendar.get(Calendar.YEAR)
						+ "-"
						+ CalendarUtils.LeftPad_Tow_Zero(calendar
								.get(Calendar.MONTH) + 1);
				tvMonth.setText(month);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		initData(MyDate.getDateENNoTime());
		//initData("2014-12-29");
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return CalendarFragment.create(position);
		}

		@Override
		public int getCount() {
			return 1000;
		}
	}

	/**
	 * 加载消息历史，从数据库中读出
	 */
	public void initData(String datestr) {
		user = userDB.getUserByID(crowdid);
		if(user==null)
			return;
		String wherestr;
		wherestr = " date LIKE '%" + datestr + "%' and isPic > 1 ";
		List<ChatMsgEntity> list = messageDB
				.getMsg(user.getId(), wherestr, 100);
		mDataArrays.clear();
		if (list.size() > 0) {
			for (ChatMsgEntity entity : list) {
				if (entity.getName() != null && entity.getName().equals("")) {
					entity.setName(user.getName());
				}
				if (entity.getImg() < 0) {
					entity.setImg(user.getImg());
				}
				mDataArrays.add(entity);
			}
			Collections.reverse(mDataArrays);
		}
		if (mAdapter == null) {
			mAdapter = new EventAdapter(this, mDataArrays);
			mListView.setAdapter(mAdapter);
		}
		mListView.setSelection(mAdapter.getCount() - 1);
		mAdapter.init(mDataArrays);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void dateUpdate(String str) {
		initData(str);
	}
}
