package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import fyp.com.packetsniffer.R;

public class TraceActivity extends Fragment{

    public static final String tag = "TraceroutePing";
    public static final String INTENT_TRACE = "INTENT_TRACE";

    private Button buttonLaunch;
    private EditText editTextPing;
    private ProgressBar progressBarPing;
    private ListView listViewTraceroute;
    private TraceListAdapter traceListAdapter;

    private List<TracerouteContainer> traces;
    private View view;
    private TraceActivity traceActivity = this;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_network_trace_route, container, false);
        this.traces = new ArrayList<TracerouteContainer>();
        this.buttonLaunch = (Button) view.findViewById(R.id.trace_route_btn);
        this.editTextPing = (EditText) view.findViewById(R.id.trace_route_edit_text);
        this.listViewTraceroute = (ListView) view.findViewById(R.id.trace_route_result_list);
        this.progressBarPing = (ProgressBar) view.findViewById(R.id.trace_route_progress_bar);

        initView();
        return view;
    }
    /**
     * initView, init the main view components (action, adapter...)
     */
    private void initView() {
        buttonLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPing.getText().length() == 0) {
                    Toast.makeText(getActivity(), getString(R.string.no_text), Toast.LENGTH_SHORT).show();
                } else {
                    traces.clear();
                    traceListAdapter.notifyDataSetChanged();
                    startProgressBar();
                    hideSoftwareKeyboard(editTextPing);
                    TraceRouteShell traceRouteShell = new TraceRouteShell("www.google.com", traceActivity);
                    traceRouteShell.start();
                }
            }
        });

        traceListAdapter = new TraceListAdapter(getContext().getApplicationContext());
        listViewTraceroute.setAdapter(traceListAdapter);
    }

    public void printResult(TracerouteContainer trace) {
        final TracerouteContainer fTrace = trace;
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    traces.add(fTrace);
                    traceListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void refreshList(){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(traces.isEmpty()){
                        Toast.makeText(getContext().getApplicationContext(),
                                "Host is unreachable",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * The adapter of the listview (build the views)
     */
    public class TraceListAdapter extends BaseAdapter {

        private Context context;

        public TraceListAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            return traces.size();
        }

        public TracerouteContainer getItem(int position) {
            return traces.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            // first init
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.layout_item_list_trace, null);

                TextView textViewNumber = (TextView) convertView.findViewById(R.id.textViewNumber);
                TextView textViewIp = (TextView) convertView.findViewById(R.id.textViewIp);
                TextView textViewTime = (TextView) convertView.findViewById(R.id.textViewTime);

                // Set up the ViewHolder.
                holder = new ViewHolder();
                holder.textViewNumber = textViewNumber;
                holder.textViewIp = textViewIp;
                holder.textViewTime = textViewTime;

                // Store the holder with the view.
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            TracerouteContainer currentTrace = getItem(position);

            holder.textViewNumber.setText((position + 1) + "");
            holder.textViewIp.setText(currentTrace.getHostname() + " (" + currentTrace.getIp() + ")");
            holder.textViewTime.setText(currentTrace.getMs() + "ms");

            return convertView;
        }

        // ViewHolder pattern
        class ViewHolder {
            TextView textViewNumber;
            TextView textViewIp;
            TextView textViewTime;
            ImageView imageViewStatusPing;
        }
    }

    /**
     * Hides the keyboard
     *
     * @param currentEditText
     *            The current selected edittext
     */
    public void hideSoftwareKeyboard(EditText currentEditText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void startProgressBar() {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBarPing.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void stopProgressBar() {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBarPing.setVisibility(View.GONE);
                }
            });
        }
    }
}
