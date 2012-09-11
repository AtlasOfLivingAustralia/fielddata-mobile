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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.service.UploadService;
import au.org.ala.fielddata.mobile.ui.MenuHelper;
import au.org.ala.fielddata.mobile.ui.SavedRecordHolder;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ViewSavedRecordsActivity extends SherlockListActivity {

	private List<Record> records;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

			viewHolder.populate(getItem(position));
			return row;
		}

	}
}
