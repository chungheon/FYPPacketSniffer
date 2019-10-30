package fyp.com.packetsniffer.Fragments.DevicesConnected;

/*RecyclerView Device Adapter Class
User Defined Class for RecyclerView to display Device information on a RecyclerView
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class RecyclerViewDeviceAdapter extends RecyclerView.Adapter<RecyclerViewDeviceAdapter.ViewHolder> {
    private static final String TAG = "RecyclerViewDevAdapter"; //Tag used for debugging purposes

    private ArrayList<DeviceInformation> devices; //ArrayList of DeviceInformation to populate the RecyclerView

    public RecyclerViewDeviceAdapter(ArrayList<DeviceInformation> devices){
        this.devices = devices;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = null;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_devicelistitem, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: called");
        String hostname = devices.get(i).getHostName();
        String ipaddr = devices.get(i).getIpAddrs();
        String vendor = devices.get(i).getMacVendor();
        String mac = devices.get(i).getMacAddrs();
        viewHolder.hostname.setText(hostname);
        viewHolder.ipaddr.setText(ipaddr);
        viewHolder.vendor.setText(vendor);
        viewHolder.mac.setText(mac);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView hostname;
        TextView ipaddr;
        TextView vendor;
        TextView mac;
        LinearLayout parentLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hostname = (TextView) itemView.findViewById(R.id.hostname);
            ipaddr = (TextView) itemView.findViewById(R.id.ipaddr);
            vendor = (TextView) itemView.findViewById(R.id.vendor);
            mac = (TextView) itemView.findViewById(R.id.mac);
            parentLayout = (LinearLayout) itemView.findViewById(R.id.parent_layout_device);
        }

    }
}
