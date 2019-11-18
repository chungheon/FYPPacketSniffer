package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class NetworkUtilsFragment extends Fragment {

    private final String pingGuide = "Enter an IP address or website. " +
            "Eg. 192.168.1.1 or www.google.com. Click Start to begin.";
    private final String traceGuide = "Enter an IP address or website. " +
            "Eg. 192.168.1.1 or www.google.com. Click Start to begin.";
    private View view;
    private ListView networkUtils;
    private TextView optionTitle;
    private TextView guideText;
    private EditText inputText;
    private Button startBtn;
    private ArrayList<String> options;
    private final String[] utilOptions = {"Ping", "Trace Route"};

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
        this.optionTitle = (TextView) view.findViewById(R.id.network_utils_option_text);
        this.guideText = (TextView) view.findViewById(R.id.network_utils_guide_text);
        this.inputText = (EditText) view.findViewById(R.id.network_utils_input_edittext);
        this.startBtn = (Button) view.findViewById(R.id.network_utils_start_btn);

        OptionsListAdapter optionsListAdapter = new OptionsListAdapter(getContext().getApplicationContext());
        networkUtils.setAdapter(optionsListAdapter);
        optionTitle.setText("Ping");
        guideText.setHint(pingGuide);
    }

    private void initListener(){
        this.networkUtils.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0: optionTitle.setText("Ping");
                        guideText.setHint(pingGuide);
                        break;
                    case 1: optionTitle.setText("Trace Route");
                        guideText.setHint(traceGuide);
                        break;
                }
            }
        });

        this.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputText.getText().toString().equals("")){
                    Toast.makeText(getContext().getApplicationContext(), "Please enter a IP/Web address", Toast.LENGTH_SHORT).show();
                }else{
                    Bundle args = new Bundle();
                    args.putString("host", inputText.getText().toString());
                    if(optionTitle.getText().toString().equals("Ping")){
                        PingActivity pingActivity = new PingActivity();
                        pingActivity.setArguments(args);
                        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Ping");
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, pingActivity, "Ping")
                                .addToBackStack(null)
                                .commit();
                    }else if(optionTitle.getText().toString().equals("Trace Route")){
                        TraceActivity traceActivity = new TraceActivity();
                        traceActivity.setArguments(args);
                        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Trace Route");
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, traceActivity, "TraceRoute")
                                .addToBackStack(null)
                                .commit();
                    }
                }
            }
        });

    }

    public void hideSoftwareKeyboard(EditText currentEditText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    public class OptionsListAdapter extends BaseAdapter {

        private Context context;

        public OptionsListAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            return utilOptions.length;
        }

        public String getItem(int position) {
            return utilOptions[position];
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
