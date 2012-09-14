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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.MapDefaults;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.model.SurveyViewModel.TempValue;
import au.org.ala.fielddata.mobile.service.StorageManager;
import au.org.ala.fielddata.mobile.ui.MenuHelper;
import au.org.ala.fielddata.mobile.ui.MultiSpinner;
import au.org.ala.fielddata.mobile.ui.SpeciesSelectionListener;
import au.org.ala.fielddata.mobile.ui.ValidatingViewPager;
import au.org.ala.fielddata.mobile.validation.Binder;
import au.org.ala.fielddata.mobile.validation.DateBinder;
import au.org.ala.fielddata.mobile.validation.ImageBinder;
import au.org.ala.fielddata.mobile.validation.LocationBinder;
import au.org.ala.fielddata.mobile.validation.MultiSpinnerBinder;
import au.org.ala.fielddata.mobile.validation.SingleCheckboxBinder;
import au.org.ala.fielddata.mobile.validation.SpeciesBinder;
import au.org.ala.fielddata.mobile.validation.SpinnerBinder;
import au.org.ala.fielddata.mobile.validation.TextViewBinder;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * The CollectSurveyData activity presents a survey form to the user to
 * fill out.
 */
public class CollectSurveyData extends SherlockFragmentActivity implements
		SpeciesSelectionListener {

	public static final String SURVEY_BUNDLE_KEY = "SurveyIdKey";
	public static final String RECORD_BUNDLE_KEY = "RecordIdKey";
	public static final String SPECIES = "species";

	/**
	 * Used to identify a request to the LocationSelectionActivity when a result
	 * is returned
	 */
	public static final int SELECT_LOCATION_REQUEST = 1;

	/** Used to identify a request to the Camera when a result is returned */
	public static final int TAKE_PHOTO_REQUEST = 10000;

	/**
	 * Used to identify a request to the Image Gallery when a result is returned
	 */
	public static final int SELECT_FROM_GALLERY_REQUEST = 3;

	private SurveyViewModel surveyViewModel;

	private SurveyPagerAdapter pagerAdapter;
	private ValidatingViewPager pager;
	private List<ImageBinder> imageBinders;
	private Species selectedSpecies;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_collect_survey_data);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(new SurveyModelHolder(), "model")
					.commit();
		}

		pagerAdapter = new SurveyPagerAdapter(getSupportFragmentManager());
		pager = (ValidatingViewPager) findViewById(R.id.surveyPager);
		pager.setAdapter(pagerAdapter);
		
		Intent i = getIntent();
		int speciesId = i.getIntExtra(CollectSurveyData.SPECIES, 0);
		if (speciesId > 0) {
			GenericDAO<Species> speciesDao = new GenericDAO<Species>(this);
			selectedSpecies = speciesDao.load(Species.class, speciesId);
		}
		
	}

	public void setViewModel(SurveyViewModel model) {
		this.surveyViewModel = model;
		getSupportActionBar().setTitle(Utils.bold(model.getSurvey().name));
		getSupportActionBar().setSubtitle(model.getSurvey().description);

		if (surveyViewModel.getPageCount() > 1) {
			TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
			titleIndicator.setViewPager(pager);
			titleIndicator.setOnPageChangeListener(pagerAdapter);
			titleIndicator.setVisibility(View.VISIBLE);
		}
		if (selectedSpecies != null) {
			onSpeciesSelected(selectedSpecies);
		}
	}

	public SurveyViewModel getViewModel() {
		return surveyViewModel;
	}


	public void onSpeciesSelected(Species selectedSpecies) {

		surveyViewModel.speciesSelected(selectedSpecies);
		pager.setCurrentItem(1);
		SpannableString title = new SpannableString(
				selectedSpecies.scientificName);
		title.setSpan(new StyleSpan(Typeface.ITALIC), 0, title.length(), 0);
		getSupportActionBar().setTitle(title);
		getSupportActionBar().setSubtitle(selectedSpecies.commonName);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.common_menu_items, menu);
		inflater.inflate(R.menu.activity_mobile_field_data, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.save) {
			int firstInvalidPage = surveyViewModel.validate();
			if (firstInvalidPage < 0) {
				new SaveRecordTask(this).execute(surveyViewModel.getRecord());
			}
			else {
				pager.setCurrentItem(firstInvalidPage);
			}
			return true;
		}
		else if (item.getItemId() == R.id.cancel) {
			finish();
		}
		return new MenuHelper(this).handleMenuItemSelection(item);
	}
	
	public void addImageListener(ImageBinder binder) {
		if (imageBinders == null) {
			imageBinders = new ArrayList<ImageBinder>(8);
		}
		imageBinders.add(binder);
	}

	static class BinderManager {
		private List<Binder> binders;

		private SurveyViewModel surveyViewModel;
		private CollectSurveyData ctx;

		public BinderManager(CollectSurveyData activity) {
			this.ctx = activity;
			binders = new ArrayList<Binder>();
			surveyViewModel = activity.getViewModel();
		}

		public void configureBindings(View view, Attribute attribute) {

			Binder binder = null;
			// Some attribute types require special bindings.
			switch (attribute.getType()) {
			case WHEN:
			case TIME:
				binder = new DateBinder(ctx, view, attribute, surveyViewModel);
				break;
			case POINT:
				binder = new LocationBinder(ctx, view, attribute, surveyViewModel);
				break;
			case IMAGE:
				binder = new ImageBinder(ctx, attribute, view);
				break;
			case SPECIES_P:
				binder = new SpeciesBinder(ctx, view, surveyViewModel);
				break;
			case SINGLE_CHECKBOX:
				binder = new SingleCheckboxBinder(ctx, (CheckBox) view, attribute, surveyViewModel);
				break;
			case MULTI_CHECKBOX:
				binder = new MultiSpinnerBinder(ctx, (MultiSpinner) view, attribute, surveyViewModel); 
				break;
			default:
				binder = bindByViewClass(view, attribute);
				break;
			}

			add(attribute, binder);
			
		}
		
		private void add(Attribute attribute, Binder binder) {
			if (binder != null) {
				binders.add(binder);
				surveyViewModel.setAttributeChangeListener(binder, attribute);
			}
		}

		private Binder bindByViewClass(View view, Attribute attribute) {
			
			Binder binder = null;
			if (view instanceof TextView) {
				binder = new TextViewBinder(ctx, (TextView) view, attribute, surveyViewModel);

			} else if (view instanceof Spinner) {
				binder = new SpinnerBinder(ctx, (Spinner) view, attribute, surveyViewModel);
			} 
			return binder;
		}

		public void bindAll() {
			for (Binder binder : binders) {
				binder.bind();
			}
		}

		public void clearBindings() {
			for (Binder binder : binders) {
				surveyViewModel.removeAttributeChangeListener(binder);
			}
			binders.clear();
		}

	}

	class SurveyPagerAdapter extends FragmentPagerAdapter implements OnPageChangeListener {

		private BinderManager[] binders;
		
		public SurveyPagerAdapter(FragmentManager manager) {
			super(manager);
			
			//binders = new BinderManager[getCount()];
		}

		@Override
		public Fragment getItem(int page) {

			SurveyPage surveyPage = new SurveyPage();
			Bundle args = new Bundle();
			args.putInt("pageNum", page);
			surveyPage.setArguments(args);
			return surveyPage;
		}

		@Override
		public int getCount() {
			return surveyViewModel.getPageCount();
		}

		@Override
		public String getPageTitle(int page) {
			return "Page " + (page + 1);
		}

		public void onPageScrollStateChanged(int arg0) {
			Log.d("Paging", "Scroll state changed, page: "+arg0);
			
			if (arg0 == ViewPager.SCROLL_STATE_DRAGGING) {
				int page = pager.getCurrentItem();
				//boolean valid = binders[page].validateAll(); 
				//Log.d("Paging", "Validating page: "+page+", result: "+valid);
				
				
			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}

		public void onPageSelected(int arg0) {
			Log.d("Paging", "Page selected, page: "+arg0);
			//pager.setPagingEnabled(false);
			
		}

	}

	/**
	 * Launches the default camera application to take a photo and store
	 * the result for the supplied attribute.
	 * @param attribute the attribute the photo relates to.
	 */
	public void takePhoto(Attribute attribute) {
		if (StorageManager.canWriteToExternalStorage()) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Uri fileUri = StorageManager
					.getOutputMediaFileUri(StorageManager.MEDIA_TYPE_IMAGE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			// Unfortunately, this URI isn't being returned in the
			// result as expected so we have to save it somewhere it can 
			// survive an activity restart.
			surveyViewModel.setTempValue(attribute, fileUri.toString());
			startActivityForResult(intent, CollectSurveyData.TAKE_PHOTO_REQUEST);
		}
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Cannot take photo").
			    setMessage("Please ensure you have mounted your SD card and it is writable").
				setPositiveButton("OK", null).show();
		}
	}
	
	/**
	 * Launches the default gallery application to allow the user to select
	 * an image to be attached to the supplied attribute.
	 * @param attribute the attribute the image is being selected for.
	 */
	public void selectFromGallery(Attribute attribute) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		// Just saving the attribute we are working with.
		surveyViewModel.setTempValue(attribute, "");
		startActivityForResult(Intent.createChooser(intent, "Select Photo"),
				CollectSurveyData.SELECT_FROM_GALLERY_REQUEST);
	}
	
	public void selectLocation() {
		Intent intent = new Intent(this, LocationSelectionActivity.class);
		Location location = surveyViewModel.getLocation();
		MapDefaults defaults = surveyViewModel.getSurvey().map;
		intent.putExtra(LocationSelectionActivity.MAP_DEFAULTS_BUNDLE_KEY, defaults);
		if (location != null) {
			intent.putExtra(LocationSelectionActivity.LOCATION_BUNDLE_KEY, location);
			
		}
		startActivityForResult(intent, CollectSurveyData.SELECT_LOCATION_REQUEST );
	}
	
	
	/**
	 * Callback made to this activity after the camera, gallery or map 
	 * activity has finished.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_LOCATION_REQUEST) {
			if (resultCode == RESULT_OK) {
				Location location = (Location) data.getExtras().get(
						LocationSelectionActivity.LOCATION_BUNDLE_KEY);
				surveyViewModel.locationSelected(location);
			}
		} else if (requestCode == TAKE_PHOTO_REQUEST) {
			if (resultCode == RESULT_OK) {
				Log.d("CollectSurveyData", "Returned ok from photo request");
				surveyViewModel.persistTempValue();
			}
			else {
				surveyViewModel.clearTempValue();
			}
		}
		else if (requestCode == SELECT_FROM_GALLERY_REQUEST) {
			TempValue value = surveyViewModel.clearTempValue();
			if (resultCode == RESULT_OK) {
				Uri selected = data.getData();
				if (selected != null) {
					surveyViewModel.setValue(value.getAttribute(), selected.toString());
				}
				else {
					Log.e("CollectSurveyData", "Null data returned from gallery intent!"+data);
				}
			}
		}
	}

	public static class SurveyPage extends Fragment {

		private int pageNum;
		private SurveyViewModel viewModel;
		private BinderManager binder;
		private CollectSurveyData ctx;

		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			pageNum = getArguments() != null ? getArguments().getInt("pageNum")
					: 0;

		}

		@Override
		public void onAttach(Activity activity) {

			super.onAttach(activity);
			Log.d("SurveyDataCollection", "Attaching to activity for page: "
					+ pageNum);

			ctx = (CollectSurveyData) activity;
			
		}
		
		@Override
		public void onActivityCreated(Bundle bundle) {
			super.onActivityCreated(bundle);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			viewModel = ctx.getViewModel();
			binder = new BinderManager(ctx);
			Log.d("SurveyDataCollection", "Creating view for page: " + pageNum);
			View view = inflater.inflate(R.layout.survey_data_page, container,
					false);
			buildSurveyForm(view);
			return view;
		}

		@Override
		public void onPause() {
			Log.d("SurveyDataCollection", "Pausing view for page: "
					+ pageNum);
			binder.bindAll();
			binder.clearBindings();
			super.onPause();
		}

		private void buildSurveyForm(View page) {
			SurveyBuilder builder = new SurveyBuilder(getActivity(), viewModel);

			TableLayout tableLayout = (TableLayout) page.findViewById(R.id.surveyGrid);
			List<Attribute> pageAttributes = viewModel.getPage(pageNum);
			
			int rowCount = pageAttributes.size();
			for (int i = 0; i < rowCount; i++) {
				TableRow row = new TableRow(getActivity());
				Attribute attribute = pageAttributes.get(i);

				View inputView = builder.buildFields(attribute, row);
				//View inputView = builder.buildInput(attribute, row);
				binder.configureBindings(inputView, attribute);
				//row.addView(inputView);

				TableRow.LayoutParams params = new TableRow.LayoutParams();
				params.setMargins(5, 5, 10, 10);
				params.width = TableRow.LayoutParams.MATCH_PARENT;
				params.height = TableRow.LayoutParams.WRAP_CONTENT;
				tableLayout.addView(row, params);
			}

		}

	}

	static class SaveRecordTask extends AsyncTask<Record, Void, Boolean> {

		private CollectSurveyData ctx;

		public SaveRecordTask(CollectSurveyData ctx) {
			this.ctx = ctx;
		}

		@Override
		protected Boolean doInBackground(Record... params) {
			boolean success = true;
			try {

				GenericDAO<Record> recordDao = new GenericDAO<Record>(
						ctx.getApplicationContext());
				recordDao.save(ctx.getViewModel().getRecord());


			} catch (Exception e) {
				success = false;
				Log.e("SurveyUpload", "Upload failed", e);
			}
			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			
			ctx.finish();
		}

	}
}
