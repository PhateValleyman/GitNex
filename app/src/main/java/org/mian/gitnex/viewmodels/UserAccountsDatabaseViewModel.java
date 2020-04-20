package org.mian.gitnex.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.database.repository.UserAccountsRepository;

/**
 * Author M M Arif
 */

public class UserAccountsDatabaseViewModel extends ViewModel {

	private static LiveData<Integer> totalCount;

	public static LiveData<Integer> getCount(String accountName) {

		if (totalCount == null) {
			totalCount = UserAccountsRepository.getCount(accountName);
		}

		return totalCount;

	}

}
