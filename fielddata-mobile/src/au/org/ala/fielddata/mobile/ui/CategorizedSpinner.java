package au.org.ala.fielddata.mobile.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeOption;

import com.commonsware.cwac.merge.MergeAdapter;

public class CategorizedSpinner extends Spinner implements OnClickListener {

	private List<String> items;
	private boolean[] selected;
	private String defaultText;
	private MultiSpinnerListener listener;
	private MergeAdapter listAdapter;
	
	public CategorizedSpinner(Context context) {
		super(context);
	}

	public CategorizedSpinner(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
	}

	public CategorizedSpinner(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	//@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		if (isChecked)
			selected[which] = true;
		else
			selected[which] = false;
	}

	
	
	@Override
	public SpinnerAdapter getAdapter() {
		return listAdapter;
	}

	//@Override
	public void onCancel(DialogInterface dialog) {
		// refresh text on spinner
		StringBuffer spinnerBuffer = new StringBuffer();
		boolean someUnselected = false;
		for (int i = 0; i < items.size(); i++) {
			if (selected[i] == true) {
				spinnerBuffer.append(items.get(i));
				spinnerBuffer.append(", ");
			} else {
				someUnselected = true;
			}
		}
		String spinnerText;
		if (someUnselected) {
			spinnerText = spinnerBuffer.toString();
			if (spinnerText.length() > 2)
				spinnerText = spinnerText
						.substring(0, spinnerText.length() - 2);
		} else {
			spinnerText = defaultText;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
				android.R.layout.simple_spinner_item,
				new String[] { spinnerText });
		setAdapter(adapter);
		listener.onItemsSelected(selected);
	}

	@Override
	public boolean performClick() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		ListView list = new ListView(getContext());
		list.setAdapter(listAdapter);
		
		builder.setView(list);
		final Dialog dialog = builder.create();
		list.setOnItemClickListener(new OnItemClickListener() {
		
			
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dialog.dismiss();
			}
		});
		dialog.show();
		return true;
	}

	public void setItems(AttributeOption[] items) {
		
		listAdapter = new MergeAdapter();
		
		List<String> strings = new ArrayList<String>(items.length*2);
		String currentHeading = "";
		for (AttributeOption option : items) {
			
			String value = option.value == null ? "" : option.value;
			int hyphenPos = value.indexOf("-");
			
			if (hyphenPos >= 0) {
				String tmpHeading = value.substring(0, hyphenPos).trim();
				if (!tmpHeading.equals(currentHeading)) {
					currentHeading = tmpHeading;
					if (strings.size() > 0) {
						 listAdapter.addAdapter(new ArrayAdapter<String>(getContext(),
									android.R.layout.simple_list_item_1, strings));
						 strings = new ArrayList<String>(8);
					}
					listAdapter.addView(inflateHeader(currentHeading), false);
				}
				if (hyphenPos < value.length()-1) {
					value = value.substring(hyphenPos+1, value.length()).trim();
				}
				else {
					value = "";
				}
			}
			
			strings.add(value);
		}
		if (strings.size() > 0) {
			listAdapter.addAdapter(new ArrayAdapter<String>(getContext(),
					android.R.layout.simple_list_item_1, strings));
		}
	}
	
	private View inflateHeader(String headerText) {
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView header = (TextView)inflater.inflate(R.layout.category_header_view, null);
		header.setText(headerText);
		return header;
	}

	public boolean[] getSelected() {
		return selected;
	}

	public void setSelected(boolean[] selected) {
		this.selected = selected;
	}

	public interface MultiSpinnerListener {
		public void onItemsSelected(boolean[] selected);
	}
}
