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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.ui.MenuHelper;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ViewSavedRecordsActivity extends SherlockListActivity {

	private List<Record> records;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new GetRecordsTask().execute();
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
		int recordId = records.get(position).id;
		Intent intent = new Intent(this, CollectSurveyData.class);
		intent.putExtra(CollectSurveyData.RECORD_BUNDLE_KEY, recordId);
		startActivity(intent);
	}



	class GetRecordsTask extends AsyncTask<Void, Void, List<Record>> {

		protected List<Record> doInBackground(Void... ignored) {
			GenericDAO<Record> dao = new GenericDAO<Record>(
					getApplicationContext());

			records = new ArrayList<Record>();
			try {
				records.addAll(dao.loadAll(Record.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return records;
		}

		protected void onPostExecute(List<Record> records) {

			List<RecordView> recordViews = new ArrayList<RecordView>();
			for (Record record : records) {
				recordViews.add(new RecordView(record));
			}
			ArrayAdapter<RecordView> adapter = new ArrayAdapter<RecordView>(
					ViewSavedRecordsActivity.this,
					android.R.layout.simple_list_item_1, recordViews);

			setListAdapter(adapter);
		}
	}

}

class RecordView {

	private Record record;

	public RecordView(Record record) {
		this.record = record;
	}
	
	public int getId() {
		return record.id;
	}

	public String toString() {
		StringBuilder out = new StringBuilder();
		Date created = new Date(record.created);
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		out.append(format.format(created));
		out.append(" ");
		out.append(record.scientificName);

		return out.toString();
	}
}
