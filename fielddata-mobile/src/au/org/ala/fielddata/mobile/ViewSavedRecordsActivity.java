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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.service.UploadService;
import au.org.ala.fielddata.mobile.ui.SavedRecordHolder;
import au.org.ala.fielddata.mobile.ui.SavedRecordHolder.RecordView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Allows the user to view records that have been created but not
 * yet uploaded to the FieldData server.
 */
public class ViewSavedRecordsActivity extends SherlockListFragment implements ActionMode.Callback, OnClickListener {

	private List<Record> records;
	private ActionMode actionMode;
	private List<Survey> surveys;
	private BroadcastReceiver uploadReceiver;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}
	

	@Override
	public void onResume() {
		super.onResume();
		
		refresh();
		
		uploadReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				refresh();
			}
		};
	
		IntentFilter filter = new IntentFilter();
		filter.addAction(UploadService.UPLOAD_FAILED);
		filter.addAction(UploadService.UPLOADED);
		
		
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(uploadReceiver, filter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(uploadReceiver);
		uploadReceiver = null;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		inflater.inflate(R.menu.saved_records_layout, menu);
	}

	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.upload) {
			
			Intent intent = new Intent(getActivity(), UploadService.class);
			getActivity().startService(intent);
			return true;
		}
		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		int recordId = records.get(position).getId();
		Intent intent = new Intent(getActivity(), CollectSurveyData.class);
		intent.putExtra(CollectSurveyData.RECORD_BUNDLE_KEY, recordId);
		startActivity(intent);
	}

	private void refresh() {
		new GetRecordsTask().execute();
	}
	
	class GetRecordsTask extends AsyncTask<Void, Void, List<RecordView>> {

		protected List<RecordView> doInBackground(Void... ignored) {
			
			GenericDAO<Record> dao = new GenericDAO<Record>(getActivity().getApplicationContext());

			records = dao.loadAll(Record.class);
			
			GenericDAO<Survey> surveyDao = new GenericDAO<Survey>(getActivity().getApplicationContext());
			surveys = surveyDao.loadAll(Survey.class);
			
			List<RecordView> recordViews = new ArrayList<RecordView>();
			for (Record record : records) {
				
				Survey survey = null;
				for (Survey tmpSurvey : surveys) {
					if (tmpSurvey.server_id.equals(record.survey_id)) {
						survey = tmpSurvey;
						break;
					}
				}
				
				recordViews.add(new RecordView(record, survey));
				
			}
			
			
			return recordViews;
		}

		protected void onPostExecute(List<RecordView> records) {
		
			RecordAdapter adapter = new RecordAdapter(ViewSavedRecordsActivity.this, records);
			setListAdapter(adapter);
		}
	}

	public static class RecordAdapter extends ArrayAdapter<RecordView> {
		
		private ViewSavedRecordsActivity fragment;
		
		public RecordAdapter(ViewSavedRecordsActivity fragment, List<RecordView> records) {
			super(fragment.getActivity(), R.layout.saved_records_layout, R.id.record_description_species, records);
			this.fragment = fragment;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View row = super.getView(position, convertView, parent);
			SavedRecordHolder viewHolder = (SavedRecordHolder) row.getTag();
			if (viewHolder == null) {
				viewHolder = new SavedRecordHolder(row);
				viewHolder.checkbox.setOnClickListener(fragment);
				row.setTag(viewHolder);
				
			}
			boolean checked = ((ListView)parent).getCheckedItemPositions().get(position);
			viewHolder.checkbox.setChecked(checked);
			viewHolder.checkbox.setTag(position);
			viewHolder.populate(getItem(position));
			return row;
		}

	}
	
	public void onClick(View view) {
		CheckBox checkBox = (CheckBox)view;
		Log.d("ViewSavedRecordsActivity", "Checkbox at position "+checkBox.getTag()+" is "+checkBox.isChecked());
		getListView().setItemChecked((Integer)view.getTag(), checkBox.isChecked());
		
		int count = countSelected();
		if (count > 0) {
			if (actionMode == null) {
				getSherlockActivity().startActionMode(this);
			}
			else {
				actionMode.setTitle(count+" selected");
			}
		}
		else {
			finishActionMode();
		}
		
		Log.d("ViewSavedRecordsActivity", "OnCheckboxClicked:"+countSelected());
	}
	
	private void finishActionMode() {
		if (actionMode != null) {
			actionMode.finish();
			actionMode = null;
		}
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
		GenericDAO<Record> recordDao = new GenericDAO<Record>(getActivity());
		SparseBooleanArray selected = getListView().getCheckedItemPositions();
		int deleteCount = 0;
		for (int i=0; i<selected.size(); i++) {
			if (selected.valueAt(i) == true) {
				Record record = ((RecordView)getListAdapter().getItem(i)).record;
				recordDao.delete(Record.class, record.getId());
				deleteCount++;
			}
		}
		if (deleteCount > 0) {
			Toast.makeText(getActivity(), deleteCount + " records deleted", Toast.LENGTH_SHORT).show();	
				
		}
		
		finishActionMode();
		
		refresh();
		
	}
	
	private void uploadSelectedRecords() {
		
		SparseBooleanArray selected = getListView().getCheckedItemPositions();
		int count = countSelected();
		int index = 0;
		int[] recordIds = new int[count];
		for (int i=0; i<selected.size(); i++) {
			if (selected.valueAt(i) == true) {
				Record record = ((RecordView)getListAdapter().getItem(i)).record;
				recordIds[index++] = record.getId();
			}
		}
		
		Intent intent = new Intent(getActivity(), UploadService.class);
		intent.putExtra(UploadService.RECORD_IDS_EXTRA, recordIds);
		getActivity().startService(intent);
		
		finishActionMode();
	}


	/**
	 * Because this fragment is managed by a ViewPager, the lifecycle
	 * callbacks aren't a reliable indication of the fragment being visible.
	 * 
	 * This callback though seems to do the trick, we can use it to dismiss
	 * the action mode if this view is paged away.
	 */
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		
		super.setMenuVisibility(menuVisible);
		if (!menuVisible) {
			finishActionMode();
		}
	}
	
	
	
	
}
