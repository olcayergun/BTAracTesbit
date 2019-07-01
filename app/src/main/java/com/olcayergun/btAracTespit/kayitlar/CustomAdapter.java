package com.olcayergun.btAracTespit.kayitlar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.olcayergun.btAracTespit.R;
import com.olcayergun.btAracTespit.jsonObjects.Kayit;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private Context context;
    public static ArrayList<Kayit> kayitArrayList;

    public CustomAdapter(Context context, ArrayList<Kayit> kayitArrayList) {
        this.context = context;
        CustomAdapter.kayitArrayList = kayitArrayList;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return kayitArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return kayitArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater != null ? inflater.inflate(R.layout.lv_item, null, true) : null;

            holder.cbItem = convertView.findViewById(R.id.cbItem);
            holder.tvGonderildi = convertView.findViewById(R.id.tvGonderildi);
            holder.tvPlaka = convertView.findViewById(R.id.tvPlaka);
            holder.tvUrun = convertView.findViewById(R.id.tvUrun);
            holder.tvDepo = convertView.findViewById(R.id.tvDepo);
            holder.tvZaman = convertView.findViewById(R.id.tvZaman);

            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvPlaka.setText(kayitArrayList.get(position).getPlaka());
        holder.tvUrun.setText(kayitArrayList.get(position).getUrun());
        holder.tvDepo.setText(kayitArrayList.get(position).getDepo());
        holder.tvZaman.setText(kayitArrayList.get(position).getZaman());

        holder.cbItem.setChecked(kayitArrayList.get(position).isSelected());
        if (kayitArrayList.get(position).isSend()) {
            holder.tvGonderildi.setText(R.string.GONDERILDI);
            holder.tvGonderildi.setTextColor(Color.GREEN);
        } else {
            holder.tvGonderildi.setText(R.string.GONDERILDI);
            holder.tvGonderildi.setTextColor(Color.MAGENTA);
        }


        holder.cbItem.setTag(R.integer.btnplusview, convertView);
        holder.cbItem.setTag(position);
        holder.cbItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //View tempview = (View) holder.cbItem.getTag(R.integer.btnplusview);
                //TextView tv = tempview.findViewById(R.id.tvPlaka);
                //Toast.makeText(context, "Checkbox " + pos + " clicked!".concat(tv.getText().toString()), Toast.LENGTH_SHORT).show();
                Integer pos = (Integer) holder.cbItem.getTag();
                if (kayitArrayList.get(pos).isSelected()) {
                    kayitArrayList.get(pos).setSelected(false);
                } else {
                    kayitArrayList.get(pos).setSelected(true);
                }
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private CheckBox cbItem;
        private TextView tvGonderildi, tvPlaka, tvDepo, tvUrun, tvZaman;
    }
}