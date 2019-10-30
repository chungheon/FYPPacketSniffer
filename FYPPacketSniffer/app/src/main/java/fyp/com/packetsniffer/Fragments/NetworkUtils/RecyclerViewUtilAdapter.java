package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class RecyclerViewUtilAdapter extends RecyclerView.Adapter<RecyclerViewUtilAdapter.ViewHolder> {
    private ArrayList<String> utilOptions;

    public RecyclerViewUtilAdapter(ArrayList<String> utilOptions){
        this.utilOptions = utilOptions;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = null;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.layout_network_utils_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView networkUtil;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            networkUtil = itemView.findViewById(R.id.network_util_recyclerview);
        }
    }
}
