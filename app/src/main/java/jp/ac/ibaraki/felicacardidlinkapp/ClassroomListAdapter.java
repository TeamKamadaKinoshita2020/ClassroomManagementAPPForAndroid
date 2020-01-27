package jp.ac.ibaraki.felicacardidlinkapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 大学用 on 2017/11/15.
 * 出席可能な講義を表示するリストの設定
 */

public class ClassroomListAdapter  extends BaseAdapter {
    private Context context;
    private List<ClassroomInfo> list;
    private LayoutInflater layoutInflater = null;

    public ClassroomListAdapter(Context context, List<ClassroomInfo> list) {
        super();
        this.context = context;
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClassroomInfo classroomInfo = (ClassroomInfo) getItem(position);
        convertView = layoutInflater.inflate(R.layout.classroom_item,null);
        TextView tv = (TextView) convertView.findViewById(R.id.textView1);
        //CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox1);
        tv.setText(classroomInfo.getName());
        return convertView;
    }
}
