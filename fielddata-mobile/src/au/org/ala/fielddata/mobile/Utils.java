package au.org.ala.fielddata.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

public class Utils {

	public static CharSequence bold(CharSequence text) {
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), 0);
		return builder;
	}
	
	
}
