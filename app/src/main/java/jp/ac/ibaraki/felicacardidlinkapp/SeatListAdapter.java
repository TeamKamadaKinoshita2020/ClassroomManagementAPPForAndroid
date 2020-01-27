package jp.ac.ibaraki.felicacardidlinkapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
/**
 * Created by 大学用 on 2017/11/15.
 */

public class SeatListAdapter extends BaseAdapter {
    static final String UNREGISTER_CARD_ID = "000000000000000";

    private Context context;
    private List<SeatInfo> list;
    private LayoutInflater layoutInflater = null;

    public SeatListAdapter(Context context, List<SeatInfo> list) {
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
        SeatInfo seatInfo = (SeatInfo) getItem(position);
        convertView = layoutInflater.inflate(R.layout.seat_item,null);
        TextView seatNum = (TextView) convertView.findViewById(R.id.textView1);
        seatNum.setText(seatInfo.getNum() + "番座席");
        TextView identityId = (TextView) convertView.findViewById(R.id.textView2);
        if(!seatInfo.getIdentityId().isEmpty()) {
            if(seatInfo.getCardId().equals(UNREGISTER_CARD_ID)){
                identityId.setText("カードが未登録です");
            }
            else {
                identityId.setText(seatInfo.getIdentityId());
            }
        }
        else{
            identityId.setText("カードが未登録です");
        }
        return convertView;
    }
}
