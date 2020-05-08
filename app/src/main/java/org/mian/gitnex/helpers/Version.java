package org.mian.gitnex.helpers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author 6543
 */

public class Version {

	// the raw String
	private String raw;
	// the version numbers in its order (dot separated)
	private List<Integer> values;
	// if version git-annotated
	private String commit;
	// if its a dev release or a rc or stable ...
	private String type;

	/**
	 * regex used to identify the type of version
	 **/
	final private Pattern pattern_major_minor_patch_release = Pattern.compile("^[v,V]?(\\d+)\\.(\\d+)\\.(\\d+)");
	final private Pattern pattern_dev_release = Pattern.compile("^[v,V]?(\\d).(\\d+).(\\d+)(\\D)(.+)");

	/**
	 * public static Version check(String min, String last, String value) {
	 * <p>
	 * final Pattern pattern_stable_release = Pattern.compile("^(\\d)\\.(\\d+)\\.(\\d+)$");
	 * final Pattern pattern_dev_release = Pattern.compile("^(\\d).(\\d+).(\\d+)(\\D)(.+)");
	 * Matcher match;
	 * <p>
	 * if(!pattern_stable_release.matcher(min).find() || !pattern_stable_release.matcher(last).find()) {
	 * throw new IllegalArgumentException("VersionCheck: wrong format for min or last version given");
	 * }
	 * <p>
	 * match = pattern_stable_release.matcher(value);
	 * if(match.find()) {
	 * <p>
	 * switch(correlate(min, last, match.group())) {
	 * case 0:
	 * return UNSUPPORTED_OLD;
	 * case 1:
	 * return SUPPORTED_OLD;
	 * case 2:
	 * return SUPPORTED_LATEST;
	 * default:
	 * return UNSUPPORTED_NEW;
	 * }
	 * <p>
	 * }
	 * <p>
	 * match = pattern_dev_release.matcher(value);
	 * if(match.find()) {
	 * <p>
	 * match = Pattern.compile("^(\\d)\\.(\\d+)\\.(\\d+)").matcher(value);
	 * match.find();
	 * <p>
	 * if(correlate(min, last, match.group()) > 0) {
	 * return DEVELOPMENT;
	 * }
	 * else {
	 * return UNSUPPORTED_OLD;
	 * }
	 * <p>
	 * }
	 * <p>
	 * return UNKNOWN;
	 * <p>
	 * }
	 **/

	public Version(String value) {

		raw = value;
		this.init();

	}

	/**
	 * HELPER
	 **/

	/**
	 * init parse and store values for other functions of an Version instance
	 * it use the raw variable as base
	 *
	 * @return if parse was successfully
	 */
	private boolean init() {

		this.raw = raw;
		// ....
		return true;

	}

	/**
	 * @param min
	 * @param last
	 * @param value
	 * @return 0=less; 1=in-range; 2=max; 3=above
	 */
	private static int correlate(String min, String last, String value) {

		int min_check = compareVersion(value, min);
		int max_check = compareVersion(value, last);
		int range_check = compareVersion(min, last);

		switch(range_check) {
			case 2:
				throw new IllegalArgumentException("Minimum Version higher than Last Version");
			case 1: //min == last
				switch(min_check) {
					case 0:
						return 0;
					case 1:
						return 2;
					default:
						return 3;
				}
			default:
				if(max_check > 1) {
					return 3;
				}
				if(max_check == 1) {
					return 2;
				}
				if(min_check < 1) {
					return 0;
				}
				return 1;
		}

	}

	/**
	 * @param A doted formatted Versions
	 * @param B doted formatted Versions
	 * @return 0|1|2
	 * 0 = less
	 * 1 = same
	 * 2 = more
	 * @description compare doted formatted Versions
	 */
	public static int compareVersion(String A, String B) {

		final Pattern pattern_stable_release = Pattern.compile("^(\\d)\\.(\\d+)\\.(\\d+)");
		final Pattern pattern_dev_release = Pattern.compile("^(\\d).(\\d+).(\\d+)(\\D)(.+)");
		Matcher match;
		match = pattern_dev_release.matcher(A);
		if(match.find()) {
			match = pattern_stable_release.matcher(A);
			match.find();
			A = match.group();
		}
		match = pattern_dev_release.matcher(B);
		if(match.find()) {
			match = pattern_stable_release.matcher(B);
			match.find();
			B = match.group();
		}

		//throw new IllegalArgumentException
		if((!A.matches("[0-9]+(\\.[0-9]+)*")) || (!B.matches("[0-9]+(\\.[0-9]+)*"))) {
			throw new IllegalArgumentException("Invalid version format");
		}

		if(A.contains(".") || B.contains(".")) {
			// example 2 vs 1.3
			if(!(A.contains(".") && B.contains("."))) {
				if(A.contains(".")) {
					return compareVersion(A, B + ".0");
				}
				if(B.contains(".")) {
					return compareVersion(A + ".0", B);
				}
			}

			//normal compare
			int a = Integer.parseInt(A.substring(0, A.indexOf(".")));
			int b = Integer.parseInt(B.substring(0, B.indexOf(".")));
			if(a < b) {
				return 0;
			}
			if(a == b) {
				return compareVersion(A.substring(A.indexOf(".") + 1), B.substring(B.indexOf(".") + 1));
			}
			return 2; //if (a > b)
		}
		else {
			int a = Integer.parseInt(A);
			int b = Integer.parseInt(B);
			if(a < b) {
				return 0;
			}
			if(a == b) {
				return 1;
			}
			return 2; //if (a > b)
		}
	}

}