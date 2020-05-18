package org.mian.gitnex.helpers;

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.regex.Pattern;

/**
 * Author M M Arif
 */

public class VersionNew {

	private static final String TAG = "VersionNew";
	private String raw;
	private String finalVersionStr;

	public VersionNew(String value) {

		raw = value;
		this.init();

	}

	private void init() {

		final Pattern pattern_valid = Pattern.compile("^[v,V]?(\\d+)+(\\.(\\d+))*([_,\\-,+][\\w,\\d,_,\\-,+]*)?$");

		if(!pattern_valid.matcher(raw).find()) {
			Log.e(TAG, "Invalid version format");
		}

		if(raw.charAt(0) == 'v' || raw.charAt(0) == 'V') {
			raw = raw.substring(1);
		}

		String[] split = raw.split("[.]");

		String raw1 = split[0];
		String raw2 ="00";
		String raw3 ="00";
		String raw3_1 = "00";

		if(raw1.length() == 1) {
			raw1 = "0" + raw1;
		}

		finalVersionStr = raw1;

		if(split.length > 1) {

			raw2 = split[1];

			if(raw2.length() == 1) {
				raw2 = "0" + raw2;
			}

			finalVersionStr = finalVersionStr.concat(raw2);

			if(split.length > 2) {

				raw3 = split[2];

				String[] raw3_ = raw3.split("[+.]");
				raw3_1 = raw3_[0];

				if(raw3_1.length() == 1) {
					raw3_1 = "0" + raw3_1;
				}

				finalVersionStr = finalVersionStr.concat(raw3_1);

			}
			else {
				finalVersionStr = finalVersionStr.concat(raw3);
			}

		}
		else {
			finalVersionStr = finalVersionStr.concat(raw2);
		}

	}

	public boolean higher(String value) {

		return this.higher(new VersionNew(value));

	}

	private boolean higher(@NonNull VersionNew v) {

		return Integer.parseInt(this.finalVersionStr) > Integer.parseInt(v.finalVersionStr);

	}

	public boolean higherOrEqual(String value) {

		return this.higherOrEqual(new VersionNew(value));

	}

	private boolean higherOrEqual(@NonNull VersionNew v) {

		return Integer.parseInt(this.finalVersionStr) >= Integer.parseInt(v.finalVersionStr);

	}

	public boolean less(String value) {

		return this.higher(new VersionNew(value));

	}

	private boolean less(@NonNull VersionNew v) {

		return Integer.parseInt(this.finalVersionStr) < Integer.parseInt(v.finalVersionStr);

	}

	public boolean lessOrEqual(String value) {

		return this.higherOrEqual(new VersionNew(value));

	}

	private boolean lessOrEqual(@NonNull VersionNew v) {

		return Integer.parseInt(this.finalVersionStr) <= Integer.parseInt(v.finalVersionStr);

	}

	public boolean equal(String value) {

		return this.higherOrEqual(new VersionNew(value));

	}

	private boolean equal(@NonNull VersionNew v) {

		return Integer.parseInt(this.finalVersionStr) == Integer.parseInt(v.finalVersionStr);

	}

}
