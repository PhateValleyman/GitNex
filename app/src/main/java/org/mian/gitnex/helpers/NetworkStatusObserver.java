package org.mian.gitnex.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import androidx.annotation.NonNull;
import java.util.concurrent.atomic.AtomicBoolean;
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;

/**
 * @author opyale
 */
public class NetworkStatusObserver {

	private static NetworkStatusObserver networkStatusObserver;

	private final AtomicBoolean hasNetworkConnection = new AtomicBoolean();
	private boolean hasInitialized = false;

	private final Object mutex = new Object();

	private NetworkStatusObserver(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkRequest networkRequest = new NetworkRequest.Builder()
			.addCapability(NET_CAPABILITY_INTERNET)
			.build();

		cm.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {

			@Override
			public void onAvailable(@NonNull Network network) {
				hasNetworkConnection.set(true);
				checkInitialized();
			}

			@Override
			public void onUnavailable() {
				hasNetworkConnection.set(false);
				checkInitialized();
			}

			private void checkInitialized() {

				if(!hasInitialized) {
					synchronized(mutex) {
						hasInitialized = true;
						mutex.notify();
					}
				}
			}

		});

		try {
			synchronized(mutex) {
				mutex.wait();
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
