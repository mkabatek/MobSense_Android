package com.streamn.mobilesense;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.streamn.mobilesense.widgets.AnimatedExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bots on 12/6/14.
 */
public class ExpandableListAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter{

    private Context context;
    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List> listDataChild;

    private ImageView mDrawerIcon;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }




    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {

        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosititon);


    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ArrayList<Integer> mDrawerIconArray = new ArrayList<>();
        mDrawerIconArray.add(R.drawable.home);
        mDrawerIconArray.add(R.drawable.settings);
        mDrawerIconArray.add(R.drawable.playcircle);


        String headerTitle = (String) getGroup(groupPosition);

        if(convertView == null) {

            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = infalInflater.inflate(R.layout.drawer_list_group, null);
        }


        ImageView iv = (ImageView)convertView.findViewById(R.id.drawer_icon);
        iv.setImageResource((Integer)mDrawerIconArray.get(groupPosition));
        iv.setVisibility(View.VISIBLE);//textView.setText(choice.name);

        TextView lbListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);

        lbListHeader.setTypeface(Typeface.DEFAULT_BOLD);

        lbListHeader.setText(headerTitle);

        return convertView;
    }

//    @Override
//    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//        return null;
//    }

//    @Override
//    public int getRealChildrenCount(int groupPosition) {
//        return 0;
//    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final ResolveInfo child = (ResolveInfo) getChild(groupPosition, childPosition);
        final String childText = child.activityInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
        final Drawable childImage = child.activityInfo.applicationInfo.loadIcon(context.getPackageManager());


        if(convertView == null) {

            LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = infalInflater.inflate(R.layout.linearlistview, null);
        }

        TextView label = (TextView) convertView.findViewById(R.id.text1);
        label.setText(childText);
        ImageView image = (ImageView) convertView.findViewById(R.id.logo2);
        image.setImageDrawable(childImage);


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
