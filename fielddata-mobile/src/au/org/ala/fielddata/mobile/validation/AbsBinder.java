package au.org.ala.fielddata.mobile.validation;

import android.view.View;
import au.org.ala.fielddata.mobile.model.Attribute;

public abstract class AbsBinder implements Binder {

	protected Attribute attribute;
	protected View view;
	
	public AbsBinder(Attribute attribute, View view) {
		this.attribute = attribute;
		this.view = view;
	}
	
	public View getView() {
		return view;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
}
