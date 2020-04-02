package org.mian.gitnex.clients;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.squareup.picasso.Cache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * Author anonTree1417
 */

public class PicassoCache implements Cache {
	private static final int CACHE_SIZE = 999;

	private File cachePath;
	private HashMap<String, String> cacheMap;

	public PicassoCache(File cachePath) {
		this.cachePath = cachePath;
		cacheMap = new HashMap<>();
	}

	@Override
	public Bitmap get(String key) {

		try {

			FileInputStream fileInputStream = new FileInputStream(new File(cachePath, Objects.requireNonNull(cacheMap.get(key))));

			Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
			fileInputStream.close();

			return bitmap;
		}
		catch(IOException e) {
			e.printStackTrace();
		}


		return null;
	}

	private String generateRandomFilename() {
		return UUID.randomUUID().toString();
	}

	@Override
	public void set(String key, Bitmap bitmap) {
		try {

			String uuid = generateRandomFilename();

			File file = new File(cachePath, uuid);
			file.createNewFile();

			FileOutputStream fileOutputStream = new FileOutputStream(file, false);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

			fileOutputStream.flush();
			fileOutputStream.close();

			cacheMap.put(key, uuid);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int size() {

		return cacheMap.size();
	}

	@Override
	public int maxSize() {

		return CACHE_SIZE;
	}

	@Override
	public void clear() {

		File[] files = cachePath.listFiles();

		assert files != null;
		for(File file : files) {
			file.delete();
		}
	}

	@Override
	public void clearKeyUri(String keyPrefix) {
		// Whats that for?

		for(String key : cacheMap.keySet()) {

			int len = Math.min(keyPrefix.length(), key.length());
			boolean match = true;

			for(int i=0; i<len; i++) {

				if(key.charAt(i) != keyPrefix.charAt(i)) {
					match = false;
					break;
				}
			}

			if(match) {

				new File(cachePath, Objects.requireNonNull(cacheMap.get(key))).delete();
				cacheMap.remove(key);
			}
		}
	}

}
