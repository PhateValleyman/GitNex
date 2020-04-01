package org.mian.gitnex.interfaces;

import com.google.gson.JsonElement;
import org.mian.gitnex.models.AddEmail;
import org.mian.gitnex.models.Branches;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.Commits;
import org.mian.gitnex.models.CreateIssue;
import org.mian.gitnex.models.CreateLabel;
import org.mian.gitnex.models.Emails;
import org.mian.gitnex.models.ExploreRepositories;
import org.mian.gitnex.models.Files;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.models.IssueComments;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.models.MergePullRequest;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.models.NewFile;
import org.mian.gitnex.models.OrgOwner;
import org.mian.gitnex.models.Organization;
import org.mian.gitnex.models.OrganizationRepository;
import org.mian.gitnex.models.Permission;
import org.mian.gitnex.models.PullRequests;
import org.mian.gitnex.models.Releases;
import org.mian.gitnex.models.Teams;
import org.mian.gitnex.models.UpdateIssueAssignees;
import org.mian.gitnex.models.UpdateIssueState;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.models.UserOrganizations;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.models.UserSearch;
import org.mian.gitnex.models.UserTokens;
import org.mian.gitnex.models.WatchRepository;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Author M M Arif
 */

public interface WebInterface {

    @GET("{owner}/{repo}/pulls/{index}.diff") // get pull diff file contents
    Call<ResponseBody> getPullDiffContent(@Path("owner") String owner, @Path("repo") String repo, @Path("index") String pullIndex);

}
