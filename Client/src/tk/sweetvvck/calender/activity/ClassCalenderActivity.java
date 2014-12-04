package tk.sweetvvck.calender.activity;

import java.util.Calendar;

import com.way.chat.activity.R;
import tk.sweetvvck.calender.utils.Utils;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

/**
 * 日历
 * @author 程科
 */
public class ClassCalenderActivity extends FragmentActivity {

	private ViewPager viewPager;
	private TextView tvMonth;
	private String month;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_calender);
		viewPager = (ViewPager) this.findViewById(R.id.viewpager);
		final ScreenSlidePagerAdapter screenSlidePagerAdapter = new ScreenSlidePagerAdapter(
				getFragmentManager());
		viewPager.setAdapter(screenSlidePagerAdapter);
		viewPager.setCurrentItem(500);
		tvMonth = (TextView) this.findViewById(R.id.tv_month);
		month = Calendar.getInstance().get(Calendar.YEAR)
				+ "-"
				+ Utils.LeftPad_Tow_Zero(Calendar.getInstance().get(
						Calendar.MONTH) + 1);
		tvMonth.setText(month);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				Calendar calendar = Utils.getSelectCalendar(position);
				month = calendar.get(Calendar.YEAR)
						+ "-"
						+ Utils.LeftPad_Tow_Zero(calendar.get(Calendar.MONTH) + 1);
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
}
