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
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.MapDefaults;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.model.SurveyViewModel.TempValue;
import au.org.ala.fielddata.mobile.service.LocationServiceHelper;
import au.org.ala.fielddata.mobile.service.LocationServiceHelper.LocationServiceConnection;
import au.org.ala.fielddata.mobile.service.StorageManager;
import au.org.ala.fielddata.mobile.ui.MultiSpinner;
import au.org.ala.fielddata.mobile.ui.SpeciesSelectionListener;
import au.org.ala.fielddata.mobile.ui.ValidatingViewPager;
import au.org.ala.fielddata.mobile.validation.Binder;
import au.org.ala.fielddata.mobile.validation.DateBinder;
import au.org.ala.fielddata.mobile.validation.ImageBinder;
import au.org.ala.fielddata.mobile.validation.LocationBinder;
import au.org.ala.fielddata.mobile.validation.MultiSpinnerBinder;
import au.org.ala.fielddata.mobile.validation.RecordValidator.RecordValidationResult;
import au.org.ala.fielddata.mobile.validation.SingleCheckboxBinder;
import au.org.ala.fielddata.mobile.validation.SpeciesBinder;
import au.org.ala.fielddata.mobile.validation.SpinnerBinder;
import au.org.ala.fielddata.mobile.validation.TextViewBinder;

import com.actionbarsherlock.app.ActionBar.LayoutParams;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * The CollectSurveyData activity presents a survey form to the user to fill
 * out.
 */
public class CollectSurveyData extends SherlockFragmentActivity implements
		SpeciesSelectionListener, OnPageChangeListener, LocationListener {

	public static final String SURVEY_BUNDLE_KEY = "SurveyIdKey";
	public static final String RECORD_BUNDLE_KEY = "RecordIdKey";
	public static final String SPECIES = "species";
	
	/** The accuracy required to auto-populate the location from GPS */
	private static final float ACCURACY_THESHOLD = 20f;

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
	private Species selectedSpecies;
	private View leftArrow;
	private View rightArrow;
	private Attribute autoScrollAttribute;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_collect_survey_data);

		buildCustomActionBar();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(new SurveyModelHolder(), "model")
					.commit();
		}

		pagerAdapter = new SurveyPagerAdapter(getSupportFragmentManager());
		pager = (ValidatingViewPager) findViewById(R.id.surveyPager);
		pager.setAdapter(pagerAdapter);
		leftArrow = findViewById(R.id.leftArrow);
		rightArrow = findViewById(R.id.rightArrow);
		Intent i = getIntent();
		int speciesId = i.getIntExtra(CollectSurveyData.SPECIES, 0);
		if (speciesId > 0) {
			GenericDAO<Species> speciesDao = new GenericDAO<Species>(this);
			selectedSpecies = speciesDao.load(Species.class, speciesId);
		}

	}
	
	private LocationServiceConnection locationServiceConnection;
	private void startLocationUpdates() {
		if (surveyViewModel.getLocation() == null) {
			locationServiceConnection = new LocationServiceConnection(this, ACCURACY_THESHOLD);
			Intent intent = new Intent(this, LocationServiceHelper.class);
			bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	private void stopLocationUpdates() {
		if (locationServiceConnection != null) {
			unbindService(locationServiceConnection);
			locationServiceConnection = null;
		}
	}
	
	public void onLocationChanged(Location location) {
		if (surveyViewModel.getLocation() == null) {
			Toast.makeText(this, R.string.locationSelectedByGPS, Toast.LENGTH_SHORT).show();
			surveyViewModel.setLocation(location);
		}
		stopLocationUpdates();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public void onProviderEnabled(String provider) {}

	public void onProviderDisabled(String provider) {}
	
	@Override
	public void onResume() {
		Log.i("GPSFragment", "onResume");
		super.onResume();
		startLocationUpdates();
	}
	
	@Override
	public void onPause() {
		Log.i("GPSFragment", "onPause");
		super.onPause();
		
		stopLocationUpdates();
	}

	private void buildCustomActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		View customNav = LayoutInflater.from(this).inflate(R.layout.cancel_done, null);

		customNav.findViewById(R.id.action_done).setOnClickListener(customActionBarListener);
		customNav.findViewById(R.id.action_cancel).setOnClickListener(customActionBarListener);

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, Gravity.FILL_HORIZONTAL);
		getSupportActionBar().setCustomView(customNav, params);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
	}

	public void setViewModel(SurveyViewModel model) {
		this.surveyViewModel = model;
		getSupportActionBar().setTitle(Utils.bold(model.getSurvey().name));
		getSupportActionBar().setSubtitle(model.getSurvey().description);

		if (surveyViewModel.getPageCount() > 1) {
			TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
			titleIndicator.setViewPager(pager);
			titleIndicator.setOnPageChangeListener(this);
			titleIndicator.setVisibility(View.VISIBLE);
			rightArrow.setVisibility(View.VISIBLE);
		}
		if (selectedSpecies != null) {
			onSpeciesSelected(selectedSpecies);
		}
	}

	public void onPageSelected(int page) {
		int leftVisiblity = View.VISIBLE;
		int rightVisibility = View.VISIBLE;
		if (page == 0) {
			leftVisiblity = View.GONE;
		}
		int count = pagerAdapter.getCount();
		if (page == count - 1) {
			rightVisibility = View.GONE;
		}

		leftArrow.setVisibility(leftVisiblity);
		rightArrow.setVisibility(rightVisibility);

		if (autoScrollAttribute != null) {
			Log.d("CollectSurveyData", "Invalid: " + autoScrollAttribute + ", Pager scrolling");
			final Attribute invalid = autoScrollAttribute;
			scrollTo(invalid);
			autoScrollAttribute = null;
		}

	}

	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	public void onPageScrollStateChanged(int arg0) {
	}

	public SurveyViewModel getViewModel() {
		return surveyViewModel;
	}

	public void scrollTo(final Attribute attribute) {
		pager.post(new Runnable() {
			public void run() {

				Binder binder = (Binder) surveyViewModel.getAttributeListener(attribute);
				View boundView = binder.getView();
				ViewParent parent = boundView.getParent();
				while (parent != null && !(parent instanceof ScrollView)) {
					parent = parent.getParent();
				}
				Log.d("CollectSurveyData", "Bound view; " + boundView + ", parent=" + parent);

				if (parent != null) {
					final ScrollView view = (ScrollView) parent;
					final Rect r = new Rect();

					view.offsetDescendantRectToMyCoords((View) boundView.getParent(), r);
					Log.d("CollectSurveyData", "Invalid: " + attribute + ", Pager scrolling to: "
							+ boundView.getBottom());
					view.post(new Runnable() {
						public void run() {
							view.scrollTo(0, r.bottom);

						}
					});

				}
			}
		});
	}

	public void onSpeciesSelected(Species selectedSpecies) {

		surveyViewModel.speciesSelected(selectedSpecies);
		pager.setCurrentItem(1);
		SpannableString title = new SpannableString(selectedSpecies.scientificName);
		title.setSpan(new StyleSpan(Typeface.ITALIC), 0, title.length(), 0);
		getSupportActionBar().setTitle(title);
		getSupportActionBar().setSubtitle(selectedSpecies.commonName);

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		onActionBarItemSelected(item.getItemId());
		return true;
	}

	public void onActionBarItemSelected(int itemId) {
		if (itemId == R.id.action_done) {
			RecordValidationResult result = surveyViewModel.validate();

			if (result.valid()) {
				new SaveRecordTask(this).execute(surveyViewModel.getRecord());
			} else {
				Toast.makeText(this, R.string.validationMessage, Toast.LENGTH_LONG).show();
				Attribute firstInvalid = result.invalidAttributes().get(0).getAttribute();
				int firstInvalidPage = surveyViewModel.pageOf(firstInvalid);
				if (pager.getCurrentItem() != firstInvalidPage) {
					pager.setCurrentItem(firstInvalidPage);
				}
				else {
					autoScrollAttribute = firstInvalid;
					scrollTo(autoScrollAttribute);
				}

			}
		} else if (itemId == R.id.action_cancel) {
			finish();
		}
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
				binder = new SpeciesBinder(ctx, attribute, view, surveyViewModel);
				break;
			case SINGLE_CHECKBOX:
				binder = new SingleCheckboxBinder(ctx, (CheckBox) view, attribute, surveyViewModel);
				break;
			case MULTI_CHECKBOX:
				binder = new MultiSpinnerBinder(ctx, (MultiSpinner) view, attribute,
						surveyViewModel);
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

		public View getView(Attribute attribute) {
			for (Binder binder : binders) {
				if (binder.getAttribute().equals(attribute)) {
					return binder.getView();
				}
			}
			return null;
		}
	}

	class SurveyPagerAdapter extends FragmentPagerAdapter {

		public SurveyPagerAdapter(FragmentManager manager) {
			super(manager);
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

	}

	/**
	 * Launches the default camera application to take a photo and store the
	 * result for the supplied attribute.
	 * 
	 * @param attribute
	 *            the attribute the photo relates to.
	 */
	public void takePhoto(Attribute attribute) {
		if (StorageManager.canWriteToExternalStorage()) {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Uri fileUri = StorageManager.getOutputMediaFileUri(StorageManager.MEDIA_TYPE_IMAGE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			// Unfortunately, this URI isn't being returned in the
			// result as expected so we have to save it somewhere it can
			// survive an activity restart.
			surveyViewModel.setTempValue(attribute, fileUri.toString());
			startActivityForResult(intent, CollectSurveyData.TAKE_PHOTO_REQUEST);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Cannot take photo")
					.setMessage("Please ensure you have mounted your SD card and it is writable")
					.setPositiveButton("OK", null).show();
		}
	}

	/**
	 * Launches the default gallery application to allow the user to select an
	 * image to be attached to the supplied attribute.
	 * 
	 * @param attribute
	 *            the attribute the image is being selected for.
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
		startActivityForResult(intent, CollectSurveyData.SELECT_LOCATION_REQUEST);
	}

	/**
	 * Callback made to this activity after the camera, gallery or map activity
	 * has finished.
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_LOCATION_REQUEST) {
			if (resultCode == RESULT_OK) {
				Location location = (Location) data.getExtras().get(
						LocationSelectionActivity.LOCATION_BUNDLE_KEY);
				surveyViewModel.setLocation(location);
			}
		} else if (requestCode == TAKE_PHOTO_REQUEST) {
			if (resultCode == RESULT_OK) {
				Log.d("CollectSurveyData", "Returned ok from photo request");
				surveyViewModel.persistTempValue();
			} else {
				surveyViewModel.clearTempValue();
			}
		} else if (requestCode == SELECT_FROM_GALLERY_REQUEST) {
			TempValue value = surveyViewModel.clearTempValue();
			if (resultCode == RESULT_OK) {
				Uri selected = data.getData();
				if (selected != null) {
					surveyViewModel.setValue(value.getAttribute(), selected.toString());
				} else {
					Log.e("CollectSurveyData", "Null data returned from gallery intent!" + data);
				}
			}
		}
	}

	public static class SurveyPage extends Fragment {

		private int pageNum;
		private SurveyViewModel viewModel;
		private BinderManager binder;
		private CollectSurveyData ctx;
		private ScrollView scroller;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			pageNum = getArguments() != null ? getArguments().getInt("pageNum") : 0;

		}

		@Override
		public void onAttach(Activity activity) {

			super.onAttach(activity);
			Log.d("SurveyDataCollection", "Attaching to activity for page: " + pageNum);

			ctx = (CollectSurveyData) activity;
		}

		@Override
		public void onActivityCreated(Bundle bundle) {
			super.onActivityCreated(bundle);

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Log.d("SurveyDataCollection", "onCreateView for page: " + pageNum);

			viewModel = ctx.getViewModel();
			binder = new BinderManager(ctx);
			Log.d("SurveyDataCollection", "Creating view for page: " + pageNum);
			View view = inflater.inflate(R.layout.survey_data_page, container, false);
			view.setTag(binder);
			scroller = (ScrollView) view.findViewById(R.id.tableScroller);
			scroller.setTag(binder);
			buildSurveyForm(view);

			return view;
		}

		@Override
		public void onDestroyView() {
			Log.d("SurveyDataCollection", "onDestroyView for page: " + pageNum);

			super.onDestroyView();

			binder.clearBindings();

		}

		private void buildSurveyForm(View page) {
			SurveyBuilder builder = new SurveyBuilder(getActivity(), viewModel);

			TableLayout tableLayout = (TableLayout) page.findViewById(R.id.surveyGrid);
			List<Attribute> pageAttributes = viewModel.getPage(pageNum);

			int rowCount = pageAttributes.size();
			if (pageNum == 0) {
				TableRow row = new TableRow(getActivity());
				builder.buildSurveyName(viewModel.getSurvey(), row);
				addRow(tableLayout, row);
			}
			for (int i = 0; i < rowCount; i++) {
				TableRow row = new TableRow(getActivity());

				Attribute attribute = pageAttributes.get(i);

				View inputView = builder.buildFields(attribute, row);
				// View inputView = builder.buildInput(attribute, row);
				binder.configureBindings(inputView, attribute);
				// row.addView(inputView);

				addRow(tableLayout, row);
			}

		}

		private void addRow(TableLayout tableLayout, TableRow row) {
			TableRow.LayoutParams params = new TableRow.LayoutParams();
			params.setMargins(5, 5, 10, 10);
			params.width = TableRow.LayoutParams.MATCH_PARENT;
			params.height = TableRow.LayoutParams.WRAP_CONTENT;
			tableLayout.addView(row, params);
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

				GenericDAO<Record> recordDao = new GenericDAO<Record>(ctx.getApplicationContext());
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

	private final View.OnClickListener customActionBarListener = new View.OnClickListener() {
		public void onClick(View v) {
			onActionBarItemSelected(v.getId());
		}
	};
}
