package org.mian.gitnex.helpers;

/**
 * @author M M Arif
 */

public class FilesData {

	public static int returnOnlyNumberFileSize(String fileSize) {

		final int i = Integer.parseInt(fileSize.substring(0, fileSize.indexOf(" ")));
		if(fileSize.substring(fileSize.lastIndexOf(" ") + 1).equals("GB")) {
			return i * 1000;
		}
		else {
			return i;
		}
	}

}
