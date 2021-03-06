package fyp.com.packetsniffer.Fragments.DevicesConnected;

/*RecyclerView History Adapter Class
User Defined Class for RecyclerView to display results from previous scans
 */


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class RecyclerViewHistoryAdapter extends RecyclerView.Adapter<RecyclerViewHistoryAdapter.ViewHolder>{

    //User-Defined Interface for OnItemClickListener
    public interface OnItemClickListener{
        void onItemClick(Pair<String, String> item);
    }
    private ArrayList<Pair<String, String>> wifiSSIDs;
    private Context mContext;
    private OnItemClickListener listener;

    public RecyclerViewHistoryAdapter(ArrayList<Pair<String, String>> wifiSSIDs, Context context, OnItemClickListener listener){
        this.wifiSSIDs = wifiSSIDs;
        this.mContext = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = null;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_scan_history_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String wifiSSID = (String) wifiSSIDs.get(i).first;
        final int position = i;
        viewHolder.wifiSSID.setText(wifiSSID);
        viewHolder.viewBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                listener.onItemClick(wifiSSIDs.get(position));
            }
        });
    }

    private class ItemOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int itemPosition = 0;
            String item = wifiSSIDs.get(itemPosition).first;
            Toast.makeText(mContext, item, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return wifiSSIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView wifiSSID;
        ImageButton viewBtn;
        ImageButton saveBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wifiSSID = (TextView) itemView.findViewById(R.id.wifiSSID);
            viewBtn = (ImageButton) itemView.findViewById(R.id.view_history_btn);
        }

    }
}
