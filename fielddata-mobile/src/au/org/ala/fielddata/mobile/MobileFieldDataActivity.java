package au.org.ala.fielddata.mobile;

import android.app.AlertDialog;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * Base class for the activities that make up the FieldData mobile client.
 * It provides "settings" and "about" menu items to the activity.
 */
public abstract class MobileFieldDataActivity extends SherlockActivity {

	public MobileFieldDataActivity() {
		super();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater menuInflater = new MenuInflater(this);
		menuInflater.inflate(getMenuId(), menu);
		menuInflater.inflate(R.menu.common_menu_items, menu);
			
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			
			new AlertDialog.Builder(this).setMessage(R.string.aboutMessage).setPositiveButton(R.string.ok, null).create().show();
			return true;
		
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * @return the resource id of the menu for this activity.  The menu
	 * must contain an item with id R.id.about.
	 */
	protected abstract int getMenuId();
}