package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class NetworkUtilsFragment extends Fragment {

    private View view;
    private ListView networkUtils;
    private ArrayList<String> options;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_network_utils, container, false);
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.networkUtils = (ListView) view.findViewById(R.id.network_utils_listview);

    }

    private void initListener(){

    }
    public class OptionsListAdapter extends BaseAdapter {

        private Context context;

        public OptionsListAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            return options.size();
        }

        public String getItem(int position) {
            return options.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            OptionsListAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.layout_item_network_utils, null);

                TextView networkOption = (TextView) convertView.findViewById(R.id.network_util_option);

                holder = new OptionsListAdapter.ViewHolder();
                holder.networkOption = networkOption;

                convertView.setTag(holder);
            } else {
                holder = (OptionsListAdapter.ViewHolder) convertView.getTag();
            }

            String currentOption = getItem(position);

            holder.networkOption.setText(currentOption);

            return convertView;
        }

        // ViewHolder pattern
        class ViewHolder {
            TextView networkOption;
        }
    }
}
