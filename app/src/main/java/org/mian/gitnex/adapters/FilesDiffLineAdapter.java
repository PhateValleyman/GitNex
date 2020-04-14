package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FilesDiffLineAdapter extends BaseAdapter {
	private Context context;
	private String[] lines;

	public FilesDiffLineAdapter(Context context, String[] lines) {
		this.context = context;
		this.lines = lines;
	}

	@Override
	public int getCount() {
		return lines.length;
	}

	@Override
	public Object getItem(int position) {
		return lines[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		/*
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate();
		}
		 */


		return null;
	}

}
