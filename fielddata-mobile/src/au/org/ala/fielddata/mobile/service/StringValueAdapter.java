package au.org.ala.fielddata.mobile.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import util.Base64;
import android.net.Uri;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Record.StringValue;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;


/**
 * This class is responsible for writing the StringValue type to json format.
 * It exists to allow the application to store a URI representing a file
 * as an attribute value, but then upload the actual file when the
 * Record is serialized to json.
 */
public class StringValueAdapter extends TypeAdapter<Record.StringValue> {

	public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
		@SuppressWarnings("unchecked")
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (type.getRawType() == Object.class) {
				return (TypeAdapter<T>) new StringValueAdapter();
			}
			return null;
		}
	};

	public StringValue read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		String valueStr = reader.nextString();
		StringValue value = new StringValue();
		value.value = valueStr;

		return value;
	}

	public void write(JsonWriter writer, StringValue stringValue) throws IOException {
		if (stringValue == null) {
			writer.nullValue();
			return;
		}
		
		if (stringValue.uri && stringValue.value != null && !"".equals(stringValue.value)) {
			Uri uri = Uri.parse(stringValue.value);
			File file = new File(uri.getEncodedPath());
			
			JsonWriterWrapper wrapper = new JsonWriterWrapper(writer);
			
			wrapper.value(new FileInputStream(file));
		}
		else {
			writer.value(stringValue.value);
		}
		
	}

	/**
	 * This is a hack to allow large files (specifically photos) to be
	 * streamed during upload rather than converted to a several megabyte
	 * string in memory first.
	 * It is very brittle as it exposes private methods of the JsonWriter
	 * to achieve this aim.
	 */
	private static class JsonWriterWrapper {

		private JsonWriter writer;

		public JsonWriterWrapper(JsonWriter writer) {
			this.writer = writer;
		}

		public void nullValue() throws IOException {
			writer.nullValue();
		}

		public void value(InputStream in) throws IOException {

			if (in == null) {
				nullValue();
			}
			writeDeferredName();
			beforeValue(false);
			
			Writer writer = getWriter();
			writer.write('\"');
			// Buffer size needs to be divisible by 3 to avoid padding 
			// during base64 encoding
			byte[] buffer = new byte[9126];
			
			int nRead;
			while ((nRead = in.read(buffer, 0, buffer.length)) != -1) {
				
				if (nRead != buffer.length) {
					byte[] tmp = new byte[nRead];
					System.arraycopy(buffer, 0, tmp, 0, nRead);
					buffer = tmp;
				}
				writer.write(Base64.encodeToChar(buffer, true));
				writer.flush();
			}
			writer.write('\"');
		}

		private void writeDeferredName() throws IOException {

			try {
				Method method = JsonWriter.class.getDeclaredMethod("writeDeferredName",
						(Class<?>[]) null);
				method.setAccessible(true);
				method.invoke(writer);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		private void beforeValue(boolean value) throws IOException {
			try {

				Method method = JsonWriter.class.getDeclaredMethod("beforeValue", boolean.class);
				method.setAccessible(true);

				method.invoke(writer, value);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		private Writer getWriter() throws IOException {
			try {
				Field out = JsonWriter.class.getDeclaredField("out");
				out.setAccessible(true);
				return (Writer) out.get(writer);
			} catch (Exception e) {
				throw new IOException(e);

			}
		}
	}
}
