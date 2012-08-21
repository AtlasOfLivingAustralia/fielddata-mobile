package au.org.ala.fielddata.mobile.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import au.org.ala.fielddata.mobile.R;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Supplies the Settings and About menu items to any Activity that includes
 * this fragment.
 */
public class MenuFragment extends SherlockFragment {
	
	/**
	 * Fragments are required to create a View, so even though this fragement
	 * is not visible, we create a dummy view and return it.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		return new View(getActivity());
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.common_menu_items, menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		return new MenuHelper(getActivity()).handleMenuItemSelection(item);
    }
}
