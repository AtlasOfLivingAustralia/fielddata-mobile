package au.org.ala.fielddata.mobile.ui;

import android.app.AlertDialog;
import android.content.Context;
import au.org.ala.fielddata.mobile.R;

import com.actionbarsherlock.view.MenuItem;

public class MenuHelper {

	private Context ctx;
	
	public MenuHelper(Context ctx) {
		this.ctx = ctx;
	}
	
	public boolean handleMenuItemSelection(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			
			new AlertDialog.Builder(ctx).setMessage(R.string.aboutMessage).setPositiveButton(R.string.ok, null).create().show();
			return true;
		
		}
		return false;
	}
	
}
