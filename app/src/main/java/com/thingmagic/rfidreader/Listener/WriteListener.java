package com.thingmagic.rfidreader.Listener;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.thingmagic.ReaderException;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.TMConstants;
import com.thingmagic.TagFilter;
import com.thingmagic.Gen2;
import com.thingmagic.TagProtocol;
import com.thingmagic.rfidreader.DatabaseOperation.EpcDatabase;
import com.thingmagic.rfidreader.DatabaseOperation.ProductInfo;
import com.thingmagic.rfidreader.R;
import com.thingmagic.rfidreader.ReaderActivity;
import com.thingmagic.util.LoggerUtil;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import vn.gpay.epccoding.TCCoding;

public class WriteListener implements View.OnClickListener {

	private static String TAG = "WriteListener";
	private RadioButton syncReadRadioButton = null;
	private RadioButton asyncReadSearchRadioButton = null;
	private RadioButton writeSelection1 = null;
	private RadioButton writeSelection2 = null;
	private static Button writeButton = null;
	private static Button readCurrentEPC = null;
	private static Button searchButton = null;
	private static EditText currentEPC = null;
	private static EditText productCode = null;
	private static ReaderActivity mReaderActivity;
	private static TagFilter tagFilter = null;
	private static Spinner writeTagList = null;
	AlertDialog.Builder alertDialogBuilder = null;


	public WriteListener(ReaderActivity readerActivity) {
		mReaderActivity = readerActivity;
		findAllViewsById();
	}

	private void findAllViewsById() {
		asyncReadSearchRadioButton = (RadioButton) mReaderActivity.findViewById(R.id.AsyncRead_radio_button);
		syncReadRadioButton = (RadioButton) mReaderActivity.findViewById(R.id.SyncRead_radio_button);
		writeSelection1 = (RadioButton) mReaderActivity.findViewById(R.id.write_selection1);
		writeSelection2 = (RadioButton) mReaderActivity.findViewById(R.id.write_selection2);
		currentEPC = (EditText) mReaderActivity.findViewById(R.id.current_epc);
		productCode = (EditText) mReaderActivity.findViewById(R.id.productcode);
		readCurrentEPC = (Button) mReaderActivity.findViewById(R.id.Read_current_epc);
		searchButton = (Button) mReaderActivity.findViewById(R.id.Search_button);
		writeButton = (Button) mReaderActivity.findViewById(R.id.Write_button);
		writeTagList = (Spinner) mReaderActivity
				.findViewById(R.id.writeTagList);
	}

	@Override
	public void onClick(View arg0) {
		try {
			String current_EPC = null;
			int status;
			if(writeSelection1.isChecked()){
				current_EPC = currentEPC.getText().toString();
			}
			else if(writeSelection2.isChecked()){
				current_EPC = writeTagList.getSelectedItem().toString();
			}
			String product_Code = productCode.getText().toString();
			TCCoding coding = new TCCoding();
			String new_EPC = coding.encode(product_Code.toUpperCase());
			byte[] mask = Hex.decodeHex(current_EPC.toCharArray());
			tagFilter = new Gen2.Select(false, Gen2.Bank.EPC, 32, 128, mask);
			String readerModel=(String) mReaderActivity.reader.paramGet("/reader/version/model");
			if(readerModel.equalsIgnoreCase("M6e")
					|| readerModel.equalsIgnoreCase("sargas")){
			 // SimpleReadPlan  simplePlan = new SimpleReadPlan(new int[] {1,2}, TagProtocol.GEN2);
				SimpleReadPlan  simplePlan = new SimpleReadPlan(new int[] {1,2}, TagProtocol.GEN2, tagFilter, 1000);
				mReaderActivity.reader.paramSet("/reader/read/plan", simplePlan);
			}else if(readerModel.equalsIgnoreCase("M6e Nano")){
				SimpleReadPlan  simplePlan = new SimpleReadPlan(new int[] {1,2}, TagProtocol.GEN2);
				mReaderActivity.reader.paramSet("/reader/read/plan", simplePlan);
			}
			mReaderActivity.reader.paramSet(TMConstants.TMR_PARAM_TAGOP_ANTENNA, 1);
			status = encodeTag(current_EPC, new_EPC);
			if(status == -1){
				Toast toast=Toast.makeText(mReaderActivity,"Null Value for old EPC or new EPC", Toast.LENGTH_SHORT);
				toast.show();
			} else if (status == -2){
				Toast toast=Toast.makeText(mReaderActivity,"Invalid length of EPC", Toast.LENGTH_SHORT);
				toast.show();
			} else if (status == -3){
				Toast toast=Toast.makeText(mReaderActivity,"No tags found", Toast.LENGTH_SHORT);
				toast.show();
			}
			else if (status == -4){
				Toast toast=Toast.makeText(mReaderActivity,"Gen2 memory overrun - bad PC.", Toast.LENGTH_SHORT);
				toast.show();
			} else if (status == 0){
				EpcDatabase db = new EpcDatabase(mReaderActivity);
				ProductInfo productInfo = new ProductInfo(product_Code, new_EPC);
				if (db.findAssetInfo(product_Code) == null) {
					db.addAssetInfo(productInfo);
				} else {
					db.addAssetInfo(productInfo);
				}
				Toast toast=Toast.makeText(mReaderActivity,"Write EPC successfully to tag!", Toast.LENGTH_SHORT);
				toast.show();
			}
			InputMethodManager imm = (InputMethodManager) mReaderActivity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(productCode.getWindowToken(), 0);
			readCurrentEPC.setClickable(true);
			searchButton.setClickable(true);
		} catch (Exception ex) {
			LoggerUtil.error(TAG, "Exception", ex);
		}


	}

	public int encodeTag(String oldEPC, String newEPC ) {
		//reader.executeTagOp(tagOP, target);
		if(oldEPC == null || newEPC == null) {
			return -1;
		}
		byte[] oldEPC_b;
		byte[] newEPC_b;
		try {
			oldEPC_b = Hex.decodeHex(oldEPC.toCharArray());
			newEPC_b = Hex.decodeHex(newEPC.toCharArray());
			if(newEPC_b.length != 12 && newEPC_b.length != 16) {
				throw new DecoderException("Invalid length of EPC");
			}
		} catch(DecoderException dex) {
			dex.printStackTrace();
			return -2;
		}
		byte[] newPC = null;
		if(newEPC_b.length == 16) {
			newPC = new byte[] {0x40, 0x00};
		} else {
			newPC = new byte[] {0x30, 0x00};
		}

		Gen2.TagData writeEPC = new Gen2.TagData(newEPC_b, newPC);
//		TagData oEPC = new Gen2.TagData(oldEPC_b);
		//if(debug) System.err.println(AppResources.DEBUG_READER_SELECT_EPC + oEPC.epcString());//"-e- selectEPC:"
//		TagFilter target = oEPC;
		TagFilter target = new Gen2.Select(false, Gen2.Bank.EPC, 32, 128, oldEPC_b );
		Gen2.WriteTag tagop = new Gen2.WriteTag(writeEPC);
		//if(debug) System.err.println(AppResources.DEBUG_READER_WRITE_EPC + writeEPC.epcString());//"-e- writeEPC:
		try {
			mReaderActivity.reader.executeTagOp(tagop, target);
		} catch(ReaderException rex) {
			String msgErr = rex.getMessage();
			//if(debug) System.err.println(AppResources.DEBUG_READER_ERRMSG_IN_WRITEOP + msgErr);
			if("No tags found.".equals(msgErr)) {
				return -3;
			} else if("Gen2 memory overrun - bad PC.".equals(msgErr)) {
				return -4;
			} else {
				return -1;
			}
		}
		return 0;
	}

}

