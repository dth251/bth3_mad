package com.example.quanlynhansu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NhanSuAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final ArrayList<NhanSu> people = new ArrayList<>();

    public NhanSuAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void updateData(ArrayList<NhanSu> newPeople) {
        people.clear();
        people.addAll(newPeople);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return people.size(); }
    @Override public NhanSu getItem(int position) { return people.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_nhan_su, parent, false);
            holder = new ViewHolder();
            holder.avatar = convertView.findViewById(R.id.imgAvatar);
            holder.id = convertView.findViewById(R.id.tvEmployeeId);
            holder.name = convertView.findViewById(R.id.tvName);
            holder.department = convertView.findViewById(R.id.tvDepartment);
            holder.salary = convertView.findViewById(R.id.tvSalary);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        NhanSu person = getItem(position);
        holder.id.setText(person.getMaNhanSu());
        holder.name.setText(person.getHoTen());
        holder.department.setText(person.getPhongBan());
        holder.salary.setText(person.getLuongHienThi());
        PhotoHelper.display(holder.avatar, person.getAnh());
        return convertView;
    }

    private static class ViewHolder {
        ImageView avatar;
        TextView id, name, department, salary;
    }
}
