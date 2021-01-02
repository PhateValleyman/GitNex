package org.mian.gitnex.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.annotation.NonNull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author opyale
 */
public class NetworkStatusObserver {

	private static NetworkStatusObserver networkStatusObserver;

	private final AtomicBoolean hasNetworkConnection = new AtomicBoolean(false);
	private boolean hasInitialized = false;

	private final Object mutex = new Object();

	private NetworkStatusObserver(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkRequest networkRequest = new NetworkRequest.Builder()
			.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
			.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
			.build();

		cm.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {

			@Override
			public void onAvailable(@NonNull Network network) {
				hasNetworkConnection.set(true);
				checkInitialized();
			}

			@Override
			public void onLost(@NonNull Network network) {
				hasNetworkConnection.set(false);
				checkInitialized();
			}

			private void checkInitialized() {

				if(!hasInitialized) {
					hasInitialized = true;
					synchronized(mutex) {
						mutex.notify();
					}
				}
			}

		});

		try {
			synchronized(mutex) {
				// This is actually not the recommended way to do this, but there
				// is no other option besides upgrading to API level 26
				// in order to use the built-in timeout functionality of {@code requestNetwork()}
				// which in turn gives us access to {@code onUnavailable()} .
				mutex.wait(5);
			}
		} catch(InterruptedException ignored) {}
	}

	public boolean hasNetworkConnection() {
		return hasNetworkConnection.get();
	}

	public static NetworkStatusObserver getInstance(Context context) {
		if(networkStatusObserver == null) {
			networkStatusObserver = new NetworkStatusObserver(context);
		}

		return networkStatusObserver;
	}

}
