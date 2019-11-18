package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Trace;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
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

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class TraceActivity extends Fragment{

    public static final String TAG = "Traceroute";

    private TextView address;
    private ProgressBar progressBarPing;
    private ListView listViewTraceroute;
    private TraceListAdapter traceListAdapter;
    private String libPath;
    private String arch = "";

    private List<TracerouteContainer> traces;
    private View view;
    private TraceActivity traceActivity = this;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_network_trace_route, container, false);
        this.traces = new ArrayList<TracerouteContainer>();
        this.address = (TextView) view.findViewById(R.id.trace_route_address);
        this.listViewTraceroute = (ListView) view.findViewById(R.id.trace_route_result_list);
        this.progressBarPing = (ProgressBar) view.findViewById(R.id.trace_route_progress_bar);
        getArchitecture();
        if(arch.equals("aarch64")){
            installAsset("traceroute_arm", "traceroute",getActivity().getFilesDir().toString());
        }else if(arch.equals("x86")){
            installAsset("traceroute_x86", "traceroute",getActivity().getFilesDir().toString());
        }
        setPermissions();
        initView();
        initListener();
        return view;
    }

    private void initView() {
        startProgressBar();

        try{
            Bundle args = getArguments();
            String ip = args.getString("host");
            address.setText(ip);
            TraceRouteShell traceRouteShell = new TraceRouteShell(ip, traceActivity, libPath);
            traceRouteShell.start();

            traceListAdapter = new TraceListAdapter(getContext().getApplicationContext());
            listViewTraceroute.setAdapter(traceListAdapter);
        }catch (Exception e){
            Toast.makeText(getContext().getApplicationContext(),
                    "Host is unreachable",
                    Toast.LENGTH_SHORT).show();
            address.setText("Unknown");
        }
    }

    private void initListener(){
        this.view.setFocusableInTouchMode(true);
        this.view.requestFocus();
        this.view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    ((MainActivity)getActivity()).getSupportActionBar().setTitle("Network Utils");
                }
                return false;
            }
        });
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

    private void getArchitecture(){
        arch = System.getProperty("os.arch");
        if(arch.equals("") || arch == null){
            final String[] architectures = {"aarch64", "x86"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext().getApplicationContext());
            builder.setTitle("Pick device's architecture");
            builder.setItems(architectures, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0: arch = "aarch64";
                                break;
                        case 1: arch = "x86";
                                break;
                    }
                }
            });
            builder.show();
        }
        Log.d("ARCH", arch);
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

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.layout_item_list_trace, null);

                TextView textViewNumber = (TextView) convertView.findViewById(R.id.textViewNumber);
                TextView textViewIp = (TextView) convertView.findViewById(R.id.textViewIp);
                TextView textViewTime = (TextView) convertView.findViewById(R.id.textViewTime);

                holder = new ViewHolder();
                holder.textViewNumber = textViewNumber;
                holder.textViewIp = textViewIp;
                holder.textViewTime = textViewTime;

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

    private void setPermissions() {
        try {
            Process shell = Runtime.getRuntime().exec("chmod 777 " + libPath + "\n");
            shell.waitFor();
        } catch (IOException e) {
        } catch (InterruptedException e) { }
    }

    private void installAsset(String assetName, String fileName, String dirPath) {
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open(assetName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(dirPath + "/" + fileName);
            libPath = targetFile.getPath();
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();
        } catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
