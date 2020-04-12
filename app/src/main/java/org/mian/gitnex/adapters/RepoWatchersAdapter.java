package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.TinyDB;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepoWatchersAdapter extends BaseAdapter {

    private List<UserInfo> watchersList;
    private Context mCtx;

    private static class ViewHolder {

        private ImageView memberAvatar;
        private TextView memberName;

        ViewHolder(View v) {
            memberAvatar  = v.findViewById(R.id.memberAvatar);
            memberName  = v.findViewById(R.id.memberName);
        }
    }

    public RepoWatchersAdapter(Context mCtx, List<UserInfo> membersListMain) {
        this.mCtx = mCtx;
        this.watchersList = membersListMain;
    }

    @Override
    public int getCount() {
        return watchersList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View finalView, ViewGroup parent) {

        RepoWatchersAdapter.ViewHolder viewHolder;

        if (finalView == null) {
            finalView = LayoutInflater.from(mCtx).inflate(R.layout.list_repo_watchers, null);
            viewHolder = new ViewHolder(finalView);
            finalView.setTag(viewHolder);
        }
        else {
            viewHolder = (RepoWatchersAdapter.ViewHolder) finalView.getTag();
        }

        initData(viewHolder, position);
        return finalView;

    }

    private void initData(RepoWatchersAdapter.ViewHolder viewHolder, int position) {

        UserInfo currentItem = watchersList.get(position);
        PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(180, 180).centerCrop().into(viewHolder.memberAvatar);

        final TinyDB tinyDb = new TinyDB(mCtx);
        Typeface myTypeface;

        switch(tinyDb.getInt("customFontId")) {

            case 0:
                myTypeface = Typeface.createFromAsset(mCtx.getAssets(), "fonts/roboto.ttf");
                break;

            case 2:
                myTypeface = Typeface.createFromAsset(mCtx.getAssets(), "fonts/sourcecodeproregular.ttf");
                break;

            default:
                myTypeface = Typeface.createFromAsset(mCtx.getAssets(), "fonts/manroperegular.ttf");
                break;

        }

        if(!currentItem.getFullname().equals("")) {
            viewHolder.memberName.setText(currentItem.getFullname());
            viewHolder.memberName.setTypeface(myTypeface);
        }
        else {
            viewHolder.memberName.setText(currentItem.getLogin());
            viewHolder.memberName.setTypeface(myTypeface);
        }

        if(tinyDb.getInt("themeId") == 1) { //light
            viewHolder.memberName.setTextColor(mCtx.getResources().getColor(R.color.lightThemeTextColor));
        }
        else { // dark
            viewHolder.memberName.setTextColor(mCtx.getResources().getColor(R.color.white));
        }

    }

}
