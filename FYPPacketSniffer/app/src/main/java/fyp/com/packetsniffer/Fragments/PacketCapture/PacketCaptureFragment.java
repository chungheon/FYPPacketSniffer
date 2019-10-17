package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class PacketCaptureFragment extends Fragment {

    private final String TAG = "PCapFragment";
    private View view;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;

    private RecyclerView output;
    private EditText editText;
    private CmdExec cmdRunnable;
    private Button sniffBtn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_packet_capture, container, false);
        initView();
        return view;
    }

    private void initView(){
        this.output = (RecyclerView) view.findViewById(R.id.output_content);
        this.sniffBtn = (Button) view.findViewById(R.id.sniffBtn);
        this.editText = (EditText) view.findViewById(R.id.cmd_prompt);
        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
        output.setAdapter(mRVAdapter);
        output.setLayoutManager(mLayoutManager);

        sniffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> cmds = new ArrayList<>();
                cmds.add("tcpdump --version");
                String[] cmd = editText.getText().toString().split(":");
                for(String str: cmd){
                    cmds.add(str);
                }

                if(cmdRunnable != null){
                    cmdRunnable.stopRun();
                    final ArrayList<String> allCmd = cmds;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
                            output.setAdapter(mRVAdapter);
                            runCmd(allCmd);
                        }
                    });
                }else{
                    runCmd(cmds);
                }
            }
        });
    }

    private void runCmd(ArrayList<String> args){
        cmdRunnable = new CmdExec(this, args);
        Thread test = new Thread(cmdRunnable);
        test.start();
    }

    public void printToast(String message){
        Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG);
    }

    public void printResult(final ArrayList<String> result){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int prevSize = mRVAdapter.getItemCount();
                mRVAdapter.bindList(result);
                mRVAdapter.notifyItemRangeInserted(prevSize, 5);
                /*mRVAdapter =
                        new RecyclerViewPageAdapter(getContext().getApplicationContext(), result);
                output.setAdapter(mRVAdapter);
                output.setNestedScrollingEnabled(true);
                output.setLayoutManager(mLayoutManager);*/
            }
        });
    }
}