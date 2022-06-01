package com.thingmagic.rfidreader.Listener;

/**
 * Created by SangDZ on 4/25/2018.
 */


import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;
import com.thingmagic.rfidreader.R;
import com.thingmagic.rfidreader.ReaderActivity;
import com.thingmagic.rfidreader.services.SettingsService;
import com.thingmagic.util.LoggerUtil;

public class CurrentEpcListener implements View.OnClickListener {

	private static String TAG = "CurrentEpcListener";
	private RadioGroup writeSelectionGroup = null;
	private RadioButton writeSelection1 = null;
	private RadioButton writeSelection2 = null;
	private static EditText currentEPC = null;
	private static TextView readTimeView = null;
	private static long queryStopTime = 0;

	private static ReaderActivity mReaderActivity;
	private static SettingsService mSettingsService;
	private static Spinner writeTagList = null;

	public CurrentEpcListener(ReaderActivity readerActivity) {
		mReaderActivity = readerActivity;
		mSettingsService = new SettingsService(mReaderActivity);
		findAllViewsById();
	}

	private void findAllViewsById() {
		writeSelectionGroup = (RadioGroup) mReaderActivity.findViewById(R.id.Radio_write_selection_group);
		writeSelection1 = (RadioButton) mReaderActivity.findViewById(R.id.write_selection1);
		writeSelection2 = (RadioButton) mReaderActivity.findViewById(R.id.write_selection2);
		currentEPC = (EditText) mReaderActivity.findViewById(R.id.current_epc);
		writeTagList = (Spinner) mReaderActivity
				.findViewById(R.id.writeTagList);
		readTimeView = (TextView) mReaderActivity.readOptions
				.findViewById(R.id.read_time_value);
	}

	@Override
	public void onClick(View arg0) {

		try {
			int id =0;
			int idx = 0;
			String readerModel=(String) mReaderActivity.reader.paramGet("/reader/version/model");
			if(readerModel.equalsIgnoreCase("M6e")
					|| readerModel.equalsIgnoreCase("sargas")){
			 SimpleReadPlan  simplePlan = new SimpleReadPlan(new int[] {1,2}, TagProtocol.GEN2);
//				SimpleReadPlan  simplePlan = new SimpleReadPlan(new int[] {1,2}, TagProtocol.GEN2, tagFilter, 1000);
				mReaderActivity.reader.paramSet("/reader/read/plan", simplePlan);
			}else if(readerModel.equalsIgnoreCase("M6e Nano")){
				SimpleReadPlan  simplePlan = new SimpleReadPlan(new int[] {1,2}, TagProtocol.GEN2);
				mReaderActivity.reader.paramSet("/reader/read/plan", simplePlan);
			}


			int timeOut = Integer.parseInt(readTimeView.getText().toString());
			TagReadData[] tagReads = mReaderActivity.reader.read(timeOut);
			queryStopTime = System.currentTimeMillis();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					mReaderActivity,
					android.R.layout.simple_spinner_dropdown_item);

			for (TagReadData tr : tagReads) {

				adapter.add(tr.getTag().epcString());
			}
//
//			for (id = 1; id < adapter.getCount(); id++) {
//				for (idx = 0; idx < id; idx++) {
//					if (adapter.getItem(id).toString().equalsIgnoreCase(adapter.getItem(idx).toString())) {
//						adapter.remove(adapter.getItem(id));
//					}
//				}
//			}
//			adapter.notifyDataSetChanged();

			if (writeSelection1.isChecked()){
				currentEPC.setText(tagReads[0].getTag().epcString());
				writeTagList.setVisibility(View.GONE);
				currentEPC.setVisibility(View.VISIBLE);

			}else if (writeSelection2.isChecked()){
				writeTagList.setAdapter(adapter);
				currentEPC.setVisibility(View.GONE);
				writeTagList.setVisibility(View.VISIBLE);
			}

		} catch (Exception ex) {
			LoggerUtil.error(TAG, "Exception", ex);
		}
	}
}


