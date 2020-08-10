package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.fragments.UserAccountsFragment;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;
import io.mikael.urlbuilder.UrlBuilder;

/**
 * Author M M Arif
 */

public class UserAccountsNavAdapter extends RecyclerView.Adapter<UserAccountsNavAdapter.UserAccountsViewHolder> {

	private static DrawerLayout drawer;
	private List<UserAccount> userAccountsList;
	private Context mCtx;

	public UserAccountsNavAdapter(Context mCtx, List<UserAccount> userAccountsListMain, DrawerLayout drawerLayout) {

		this.mCtx = mCtx;
		this.userAccountsList = userAccountsListMain;
		drawer = drawerLayout;
	}

	static class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		private ImageView userAccountAvatar;

		private UserAccountsViewHolder(View itemView) {

			super(itemView);

			userAccountAvatar = itemView.findViewById(R.id.userAccountAvatar);

			itemView.setOnClickListener(item -> {


				AppCompatActivity activity = (AppCompatActivity) itemView.getContext();
				activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserAccountsFragment()).commit();
				drawer.closeDrawers();


			});

		}

	}

	@NonNull
	@Override
	public UserAccountsNavAdapter.UserAccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_user_accounts, parent, false);
		return new UserAccountsViewHolder(v);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onBindViewHolder(@NonNull UserAccountsNavAdapter.UserAccountsViewHolder holder, int position) {

		UserAccount currentItem = userAccountsList.get(position);

		String url = UrlBuilder.fromString(currentItem.getInstanceUrl())
			.withPath("/")
			.toString();

		PicassoService
			.getInstance(mCtx).get().load(url + "img/favicon.png").placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.userAccountAvatar);
	}

	@Override
	public int getItemCount() {

		return userAccountsList.size();
	}

}
