package com.streamn.mobilesense;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;

/**
 * Created by bots on 12/3/14.
 */
public class NavDrawerAdapter extends ArrayAdapter<NavDrawerItem>
{
    private final Context context;
    private final int layoutResourceId;
    //private NavDrawerItem data[] = null;
    ExpandableListAdapter expListAdapter;

    private ImageView mDrawerIcon;


    public NavDrawerAdapter(Context context, int layoutResourceId, NavDrawerItem [] data)
    {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        //this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        View v = inflater.inflate(layoutResourceId, parent, false);





        //TextView textView = (TextView) v.findViewById(R.id.navDrawerTextView);
        ExpandableListView expandableListView = (ExpandableListView) v.findViewById(R.id.left_drawer);
        //NavDrawerItem choice = data[position];





        expandableListView.setAdapter(expListAdapter);

        return v;
    }
}
