package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.content.Context;
import android.os.Build;
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
    private static final String TAG = "RecyclerViewDevAdapter";

    private ArrayList<String> hostnames = new ArrayList<String>();
    private ArrayList<String> ipaddrs = new ArrayList<String>();
    private ArrayList<String> vendors = new ArrayList<String>();
    private ArrayList<String> macs = new ArrayList<String>();
    private Context mContext;

    public RecyclerViewDeviceAdapter(ArrayList<String> hostnames, ArrayList<String> ipaddrs, ArrayList<String> vendors, ArrayList<String> macs, Context context){
        this.hostnames = hostnames;
        this.ipaddrs = ipaddrs;
        this.vendors = vendors;
        this.macs = macs;
        this.mContext = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = null;
        if(Build.VERSION.SDK_INT >= 23){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_devicelistitem, viewGroup, false);
        }else{
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_devicelistitemold, viewGroup, false);
        }
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: called");
        String hostname = hostnames.get(i);
        String ipaddr = ipaddrs.get(i);
        String vendor = vendors.get(i);
        String mac = macs.get(i);
        viewHolder.hostname.setText(hostname);
        viewHolder.ipaddr.setText(ipaddr);
        viewHolder.vendor.setText(vendor);
        viewHolder.mac.setText(mac);
    }

    @Override
    public int getItemCount() {
        return hostnames.size();
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
