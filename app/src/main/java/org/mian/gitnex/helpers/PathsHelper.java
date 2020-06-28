package org.mian.gitnex.helpers;

/**
 * Author opyale
 */
public class PathsHelper {

	public static String join(String... paths) {

		StringBuilder stringBuilder = new StringBuilder();

		for(String path : paths) {

			if(!path.trim().isEmpty()) {

				if(path.endsWith("/")) {

					path = path.substring(0, path.lastIndexOf("/"));
				}

				if(!path.startsWith("/")) {

					stringBuilder.append("/");
				}

				stringBuilder.append(path);

			}

		}

		return stringBuilder.append("/").toString();

	}

}
