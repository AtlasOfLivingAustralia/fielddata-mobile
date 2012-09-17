/*******************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *  
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *  
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.fielddata.mobile;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.service.UploadService;
import au.org.ala.fielddata.mobile.ui.MenuHelper;
import au.org.ala.fielddata.mobile.ui.SavedRecordHolder;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Allows the user to view records that have been created but not
 * yet uploaded to the FieldData server.
 */
public class ViewSavedRecordsActivity extends SherlockListActivity implements ActionMode.Callback {

	private List<Record> records;
	private ActionMode actionMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(UploadService.UPLOAD_FAILED);
		filter.addAction(UploadService.UPLOADED);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (UploadService.UPLOAD_FAILED.equals(intent.getAction())) {
					Toast.makeText(getApplicationContext(), "Upload failed!", Toast.LENGTH_SHORT)
							.show();
				}
				refresh();
			}
		}, filter);
		refresh();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.saved_records_layout, menu);
		inflater.inflate(R.menu.common_menu_items, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.upload) {
			Intent intent = new Intent(this, UploadService.class);
			startService(intent);
		}
		
		return new MenuHelper(this).handleMenuItemSelection(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		int recordId = records.get(position).getId();
		Intent intent = new Intent(this, CollectSurveyData.class);
		intent.putExtra(CollectSurveyData.RECORD_BUNDLE_KEY, recordId);
		startActivity(intent);
	}

	private void refresh() {
		new GetRecordsTask().execute();
	}

	class GetRecordsTask extends AsyncTask<Void, Void, List<Record>> {

		protected List<Record> doInBackground(Void... ignored) {
			GenericDAO<Record> dao = new GenericDAO<Record>(getApplicationContext());

			records = new ArrayList<Record>();
			try {
				records.addAll(dao.loadAll(Record.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return records;
		}

		protected void onPostExecute(List<Record> records) {
			RecordAdapter adapter = new RecordAdapter(ViewSavedRecordsActivity.this, records);

			setListAdapter(adapter);
		}
	}

	public static class RecordAdapter extends ArrayAdapter<Record> {
		public RecordAdapter(Context ctx, List<Record> records) {
			super(ctx, R.layout.saved_records_layout, R.id.record_description_species, records);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View row = super.getView(position, convertView, parent);
			SavedRecordHolder viewHolder = (SavedRecordHolder) row.getTag();
			if (viewHolder == null) {
				viewHolder = new SavedRecordHolder(row);
				row.setTag(viewHolder);
			}
			boolean checked = ((ListView)parent).getCheckedItemPositions().get(position);
			viewHolder.checkbox.setChecked(checked);
			viewHolder.checkbox.setTag(position);
			viewHolder.populate(getItem(position));
			return row;
		}

	}
	
	public void onCheckboxClicked(View view) {
		CheckBox checkBox = (CheckBox)view;
		Log.d("ViewSavedRecordsActivity", "Checkbox at position "+checkBox.getTag()+" is "+checkBox.isChecked());
		getListView().setItemChecked((Integer)view.getTag(), checkBox.isChecked());
		
		int count = countSelected();
		if (count > 0) {
			if (actionMode == null) {
				startActionMode(this);
			}
			else {
				actionMode.setTitle(count+" selected");
			}
		}
		else {
			if (actionMode != null) {
				actionMode.finish();
				actionMode = null;
			}
		}
		
		Log.d("ViewSavedRecordsActivity", "OnCheckboxClicked:"+countSelected());
	}
	
	private int countSelected() {
		
		int count = 0;
		SparseBooleanArray selected = getListView().getCheckedItemPositions();
		for (int i=0; i<selected.size(); i++) {
			if (selected.valueAt(i)) {
				count++;
			}
		}
		return count;
	}
	
	
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		this.actionMode = mode;
		mode.setTitle(countSelected() + " selected");
		
		menu.add("Delete").setIcon(android.R.drawable.ic_menu_delete);
		menu.add("Upload").setIcon(android.R.drawable.ic_menu_upload);
		
		return true;
	}

	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		
		return false;
	}

	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		if ("Delete".equals(item.getTitle())) {
			deleteSelectedRecords();
		}
		else if ("Upload".equals(item.getTitle())) {
			uploadSelectedRecords();
		}
		return true;
	}

	public void onDestroyActionMode(ActionMode mode) {
		
		for (int i=0; i<getListAdapter().getCount(); i++) {
			getListView().setItemChecked(i, false);
		}
	}
	
	
	private void deleteSelectedRecords() {
		GenericDAO<Record> recordDao = new GenericDAO<Record>(this);
		SparseBooleanArray selected = getListView().getCheckedItemPositions();
		int deleteCount = 0;
		for (int i=0; i<selected.size(); i++) {
			if (selected.valueAt(i) == true) {
				Record record = (Record)getListAdapter().getItem(i);
				recordDao.delete(Record.class, record.getId());
				deleteCount++;
			}
		}
		if (deleteCount > 0) {
			Toast.makeText(this, deleteCount + " records deleted", Toast.LENGTH_SHORT).show();	
				
		}
		
		actionMode.finish();
		refresh();
	}
	
	private void uploadSelectedRecords() {
		SparseBooleanArray selected = getListView().getCheckedItemPositions();
		int count = countSelected();
		int index = 0;
		int[] recordIds = new int[count];
		for (int i=0; i<selected.size(); i++) {
			if (selected.valueAt(i) == true) {
				Record record = (Record)getListAdapter().getItem(i);
				recordIds[index++] = record.getId();
			}
		}
		
		Intent intent = new Intent(this, UploadService.class);
		intent.putExtra(UploadService.RECORD_IDS_EXTRA, recordIds);
		startService(intent);
		
		actionMode.finish();
	}
	
	
}
