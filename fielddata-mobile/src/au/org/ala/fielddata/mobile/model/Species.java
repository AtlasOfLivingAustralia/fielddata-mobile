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
package au.org.ala.fielddata.mobile.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Species extends Persistent implements Parcelable {

	public String scientificName;
	public String commonName;
	public String imageFileName;
	
	public int profileImagePath;

	private List<ProfileElement> infoItems;
	
	public static class ProfileElement {
		String content;
		String type;
	}
	
	public Species(String scientificName, String commonName, int fileName) {
		this.scientificName = scientificName;
		this.commonName = commonName;
		this.profileImagePath = fileName;
		infoItems = new ArrayList<Species.ProfileElement>();
	}
	
	public String getImageFileName() {
		if (infoItems != null) {
			for (ProfileElement element : infoItems) {
				if ("thumb".equals(element.type)) {
					return element.content;
				}
			}
		}
		return null;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		int tmpId = -1;
		if (getId() != null) {
			tmpId  = getId();
		}
		dest.writeInt(tmpId);
		dest.writeInt(server_id);
		dest.writeString(scientificName);
		dest.writeString(commonName);
	}

	public static final Parcelable.Creator<Species> CREATOR = new Parcelable.Creator<Species>() {
		public Species createFromParcel(Parcel in) {
			return new Species(in);
		}

		public Species[] newArray(int size) {
			return new Species[size];
		}
	};
	
	private Species(Parcel in) {
		setId(in.readInt());
		server_id = in.readInt();
		scientificName = in.readString();
		commonName = in.readString();
	}

}
