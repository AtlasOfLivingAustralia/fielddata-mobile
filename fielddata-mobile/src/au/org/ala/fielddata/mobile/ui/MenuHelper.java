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
