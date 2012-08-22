package au.org.ala.fielddata.mobile.validation;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Record;

public class DateBinder implements Binder, OnClickListener, DatePickerDialog.OnDateSetListener {

	private Attribute attribute;
	private Record record;
	private Validator validator;
	private DateFieldHolder holder;
	private Context ctx;
	private Date date;
	
	static class DateFieldHolder {
		public Button button;
		public TextView text;
		public DateFieldHolder(View container) {
			text = (TextView)container.findViewById(R.id.dateDisplay);
			button = (Button)container.findViewById(R.id.changeButton);
		}
	}
	
	public DateBinder(Context ctx, View view, Attribute attribute, Record record,
			Validator validator) {
		
		
		this.ctx = ctx;
		this.attribute = attribute;
		this.validator = validator;
		this.record = record;
		
		holder = new DateFieldHolder(view);
		holder.button.setOnClickListener(this);
		
		date = record.getDate(attribute);
		updateDisplay();
	}
	
	private void updateDisplay() {
		if (date != null) {
			holder.text.setText(DateFormat.getMediumDateFormat(ctx).format(date));
		}
		else {
			holder.text.setText("");
		}
	}
	
	public void onClick(View v) {
		final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
		new DatePickerDialog(ctx, this, year, month, day).show();
	}
	
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		
		date = calendar.getTime();
		updateDisplay();
		
		
	}
	
	public boolean validate() {

		boolean valid = true;
		if (validator != null) {
//			valid = validator.validate(nullSafeText());
//			if (!valid) {
//				View selected = view.getSelectedView();
//				if (selected instanceof TextView) {
//					((TextView) selected).setError("Uhoh");
//				}
//			}
		}
		return valid;

	}

	public void bind() {
		record.setValue(attribute, date);
	}
}
