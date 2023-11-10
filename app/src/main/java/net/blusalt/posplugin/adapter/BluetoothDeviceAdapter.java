package net.blusalt.posplugin.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.blusalt.posplugin.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

    public BluetoothDeviceAdapter(Context context) {
        super(context, 0, new ArrayList<>());
    }

    @Override
    public void add(BluetoothDevice object) {
        for (int i = 0; i < getCount(); i++) {
            BluetoothDevice item = getItem(i);
            if (item != null && item.getAddress().equalsIgnoreCase(object.getAddress())) {
                return;
            }
        }

        super.add(object);
    }

    @Override
    public void addAll(@NotNull Collection<? extends BluetoothDevice> collection) {
        for (int i = 0; i < getCount(); i++) {
            BluetoothDevice item = getItem(i);
            if (item != null && collection.contains(item)) {
                return;
            }
        }

        super.addAll(collection);
    }

    @SuppressLint("MissingPermission")
    @Override
    @NotNull
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bluetooth_list_item, parent, false);
        }

        BluetoothDevice device = getItem(position);

        TextView name = convertView.findViewById(R.id.bt_name_text);
        TextView address = convertView.findViewById(R.id.bt_address_text);

        if (device != null && device.getName() != null) {
            name.setText(device.getName());
        }

        if (device != null && device.getAddress() != null) {
            address.setText(device.getAddress());
        }

        return convertView;
    }
}

