package org.mian.gitnex.clients;

import android.content.Context;
import android.util.Log;
import org.gitnex.tea4j.v2.apis.*;
import org.gitnex.tea4j.v2.apis.custom.WebApi;
import org.gitnex.tea4j.v2.auth.ApiKeyAuth;
import org.gitnex.tea4j.v2.models.AccessToken;
import org.gitnex.tea4j.v2.models.ContentsResponse;
import org.gitnex.tea4j.v2.models.CreateAccessTokenOption;
import org.gitnex.tea4j.v2.models.ServerVersion;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FilesData;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import java.io.File;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.*;

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
			// TODO support OTP
			auth.setApiKey(token); // TODO allow custom token to use in LoginActivity and AddNewAccountActivity
			OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
//				.addInterceptor(logging)
				.addInterceptor(auth)
				.sslSocketFactory(sslContext.getSocketFactory(), memorizingTrustManager)
				.hostnameVerifier(memorizingTrustManager.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));

			if(cacheEnabled) {

				int cacheSize = FilesData.returnOnlyNumber(tinyDB.getString("cacheSizeStr", context.getString(R.string.cacheSizeDataSelectionSelectedText))) * 1024 * 1024;
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
				.addConverterFactory(GsonConverterFactory.create())
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

		if(!apiInterfaces.containsKey(url)) {
			synchronized(RetrofitClient.class) {
				if(!apiInterfaces.containsKey(url)) {

					ApiInterface apiInterface = createRetrofit(context, url, true, token).create(ApiInterface.class);
					apiInterfaces.put(url, apiInterface);

					return apiInterface;
				}
			}
		}

		return apiInterfaces.get(url);

	}

	public static WebApi getWebInterface(Context context, String url, String token) {

		if(!webInterfaces.containsKey(url)) {
			synchronized(RetrofitClient.class) {
				if(!webInterfaces.containsKey(url)) {

					WebApi webInterface = createRetrofit(context, url, false, token).create(WebApi.class);
					webInterfaces.put(url, webInterface);

					return webInterface;
				}
			}
		}

		return webInterfaces.get(url);

	}

	public interface ApiInterface extends AdminApi, OrganizationApi, IssueApi, RepositoryApi, MiscellaneousApi, NotificationApi, UserApi, SettingsApi {

		// custom methods to use with OTP tokens
		@GET("version")
		Call<ServerVersion> getVersion(@Header("X-Gitea-OTP") int otp);

		@GET("users/{username}/tokens")
		Call<List<AccessToken>> userGetTokens(
			@Header("X-Gitea-OTP") int otp,
			@Path("username") String username,
			@Query("page") Integer page,
			@Query("limit") Integer limit);

		@DELETE("users/{username}/tokens/{token}")
		Call<Void> userDeleteAccessToken(
			@Header("X-Gitea-OTP") int otp,
			@Path("username") String username, @Path("token") String token);

		@Headers({"Content-Type:application/json"})
		@POST("users/{username}/tokens")
		Call<AccessToken> userCreateToken(
			@Header("X-Gitea-OTP") int otp,
			@Path("username") String username,
			@Body CreateAccessTokenOption body);

		@GET("repos/{owner}/{repo}/contents/{filepath}")
		Call<List<ContentsResponse>> repoGetContentsList(
			@Path("owner") String owner,
			@Path("repo") String repo,
			@Path("filepath") String filepath,
			@Query("ref") String ref);

	}

	public interface WebApi extends org.gitnex.tea4j.v2.apis.custom.WebApi {

		@GET("{owner}/{repo}/git/commit/{sha}.{diffType}")
		Call<String> repoDownloadCommitDiffOrPatch(
			@retrofit2.http.Path("owner") String owner,
			@retrofit2.http.Path("repo") String repo,
			@retrofit2.http.Path("sha") String sha,
			@retrofit2.http.Path("diffType") String diffType);

	}
}
