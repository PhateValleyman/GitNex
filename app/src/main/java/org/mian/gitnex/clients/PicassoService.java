package org.mian.gitnex.clients;

import android.content.Context;
import android.util.Log;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Author anonTree1417
 */

public class PicassoService {

	private static PicassoService picassoService;
	private Picasso picasso;

	private PicassoService(Context context) {

		Picasso.Builder builder = new Picasso.Builder(context);

		int cacheSize = 100 * 1024 * 1024; // 100MB
		File httpCacheDirectory = new File(context.getCacheDir(), "responses");
		Cache cache = new Cache(httpCacheDirectory, cacheSize);

		try {

			SSLContext sslContext = SSLContext.getInstance("TLS");

			MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(context);
			sslContext.init(null, new X509TrustManager[]{memorizingTrustManager}, new SecureRandom());

			OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
					.cache(cache)
					.sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
					.hostnameVerifier(memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()))
					.addInterceptor(new Interceptor() {

						@NotNull
						@Override
						public Response intercept(@NotNull Chain chain) throws IOException {

							Response originalResponse = chain.proceed(chain.request());
							return originalResponse.newBuilder().header("Cache-Control", "public, max-stale=" + 60 * 60 * 24 * 30).build();
						}
					});

			builder.downloader(new OkHttp3Downloader(okHttpClient.build()));
			builder.listener((picasso, uri, exception) -> {

				//Log.e("PicassoService", Objects.requireNonNull(uri.toString()));
				//Log.e("PicassoService", exception.toString());

			});

			picasso = builder.build();

		}
		catch(Exception e) {

			Log.e("PicassoService", e.toString());
		}

	}

	public Picasso get() {

		return picasso;
	}

	public static synchronized PicassoService getInstance(Context context) {

		if(picassoService == null) {
			picassoService = new PicassoService(context);
		}

		return picassoService;
	}

}
