package org.mian.gitnex.clients;

import android.content.Context;
import android.util.Log;
import com.google.gson.GsonBuilder;
import org.gitnex.tea4j.v2.apis.AdminApi;
import org.gitnex.tea4j.v2.apis.IssueApi;
import org.gitnex.tea4j.v2.apis.MiscellaneousApi;
import org.gitnex.tea4j.v2.apis.NotificationApi;
import org.gitnex.tea4j.v2.apis.OrganizationApi;
import org.gitnex.tea4j.v2.apis.PackageApi;
import org.gitnex.tea4j.v2.apis.RepositoryApi;
import org.gitnex.tea4j.v2.apis.SettingsApi;
import org.gitnex.tea4j.v2.apis.UserApi;
import org.gitnex.tea4j.v2.apis.custom.CustomApi;
import org.gitnex.tea4j.v2.apis.custom.OTPApi;
import org.gitnex.tea4j.v2.apis.custom.WebApi;
import org.gitnex.tea4j.v2.auth.ApiKeyAuth;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FilesData;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import java.io.File;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Author M M Arif
 */

public class RetrofitClient {

	private static final Map<String, ApiInterface> apiInterfaces = new ConcurrentHashMap<>();
	private static final Map<String, WebApi> webInterfaces = new ConcurrentHashMap<>();

	private static Retrofit createRetrofit(Context context, String instanceUrl, boolean cacheEnabled, String token) {

		TinyDB tinyDB = TinyDB.getInstance(context);

//		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//		logging.setLevel(HttpLoggingInterceptor.Level.BODY);

		try {

			SSLContext sslContext = SSLContext.getInstance("TLS");

			MemorizingTrustManager memorizingTrustManager = new MemorizingTrustManager(context);
			sslContext.init(null, new X509TrustManager[]{ memorizingTrustManager }, new SecureRandom());

			ApiKeyAuth auth = new ApiKeyAuth("header", "Authorization");
			auth.setApiKey(token);
			OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
//				.addInterceptor(logging)
				.addInterceptor(auth)
				.sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
				.hostnameVerifier(memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));

			if(cacheEnabled) {

				int cacheSize = FilesData.returnOnlyNumberFileSize(tinyDB.getString("cacheSizeStr", context.getString(R.string.cacheSizeDataSelectionSelectedText))) * 1024 * 1024;
				Cache cache = new Cache(new File(context.getCacheDir(), "responses"), cacheSize);

				okHttpClient.cache(cache).addInterceptor(chain -> {

					Request request = chain.request();

					request = AppUtil.hasNetworkConnection(context) ?
						request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build() :
						request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 30).build();

					return chain.proceed(request);

				});
			}

			return new Retrofit.Builder()
				.baseUrl(instanceUrl)
				.client(okHttpClient.build())
				.addConverterFactory(ScalarsConverterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
					.create()))
				.build();

		}
		catch(Exception e) {

			Log.e("onFailureRetrofit", e.toString());
		}

		return null;
	}

	public static ApiInterface getApiInterface(Context context) {
		return getApiInterface(context, ((BaseActivity) context).getAccount().getAccount().getInstanceUrl(),
			((BaseActivity) context).getAccount().getAuthorization());
	}

	public static WebApi getWebInterface(Context context) {

		String instanceUrl = ((BaseActivity) context).getAccount().getAccount().getInstanceUrl();
		instanceUrl = instanceUrl.substring(0, instanceUrl.lastIndexOf("api/v1/"));

		return getWebInterface(context, instanceUrl, ((BaseActivity) context).getAccount().getWebAuthorization());

	}

	public static ApiInterface getApiInterface(Context context, String url, String token) {

		String key = token.hashCode() + "@" + url;
		if(!apiInterfaces.containsKey(key)) {
			synchronized(RetrofitClient.class) {
				if(!apiInterfaces.containsKey(key)) {

					ApiInterface apiInterface = createRetrofit(context, url, true, token).create(ApiInterface.class);
					apiInterfaces.put(key, apiInterface);

					return apiInterface;
				}
			}
		}

		return apiInterfaces.get(key);

	}

	public static WebApi getWebInterface(Context context, String url, String token) {

		String key = token.hashCode() + "@" + url;
		if(!webInterfaces.containsKey(key)) {
			synchronized(RetrofitClient.class) {
				if(!webInterfaces.containsKey(key)) {

					WebApi webInterface = createRetrofit(context, url, false, token).create(WebApi.class);
					webInterfaces.put(key, webInterface);

					return webInterface;
				}
			}
		}

		return webInterfaces.get(key);

	}

	public interface ApiInterface extends AdminApi, OrganizationApi, IssueApi, RepositoryApi, MiscellaneousApi, NotificationApi,
		UserApi, SettingsApi, OTPApi, CustomApi, PackageApi {}

}
