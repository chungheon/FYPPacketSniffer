package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class PacketCaptureFragment extends Fragment implements PacketCaptureInterface {
    private static final String TAG = "PacketCapture";
    private View view;
    private EditText numPacket;
    private TextView pageText;
    private Button captureBtn;
    private RecyclerView captureOutput;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;

    private CmdExec cmdRunnable;
    private long movement = 1050;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_packet_capture, container, false);
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.numPacket = (EditText) view.findViewById(R.id.capture_num);
        this.captureOutput = (RecyclerView) view.findViewById(R.id.capture_output);
        this.captureBtn = (Button) view.findViewById(R.id.capture_btn);
        this.pageText = (TextView) view.findViewById(R.id.capture_num_page);
        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
        captureOutput.setAdapter(mRVAdapter);
        captureOutput.setLayoutManager(mLayoutManager);
        ViewCompat.setNestedScrollingEnabled(captureOutput, true);
        this.pageText.setText("0/0");
    }

    private void initListener(){

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> cmds = new ArrayList<>();
                long numOfPacket = 0;
                try{
                    numOfPacket = Long.parseLong(numPacket.getText().toString());

                    if(numOfPacket < 0){
                        throw new NumberFormatException();
                    }
                }catch (NumberFormatException e){
                    printToast("Please choose a valid number of packets eg. 1000");
                    return;
                }

                cmds.add("tcpdump -tttt -c " + numOfPacket);

                if(cmdRunnable != null){
                    cmdRunnable.stopRun();
                    final ArrayList<String> allCmd = cmds;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
                            captureOutput.setAdapter(mRVAdapter);
                            ViewCompat.setNestedScrollingEnabled(captureOutput, true);
                            runCmd(allCmd);
                        }
                    });
                }else{
                    runCmd(cmds);
                }
            }
        });

        captureOutput.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    long totalpages = Integer.parseInt(pageInfo[1]) * 1050;
                    if(movement <= totalpages && pageInfo.length == 3){
                        movement += dx;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText((((movement + 200)/1050)) + "/" + pageInfo[1] + "/" + pageInfo[2]);
                            }
                        });
                    }
                    if(movement > totalpages){
                        movement = totalpages;
                    }
                } else {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    if((movement-1050)<= (dx * -1)){
                        movement = 1050;
                    }else{
                        movement += dx;
                        if(pageInfo.length == 3){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pageText.setText((((movement + 600)/1050)) + "/" + pageInfo[1] + "/" + pageInfo[2]);
                                }
                            });
                        }
                    }

                    if(movement == 1050 && pageInfo.length == 3){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText("1/" + pageInfo[1] + "/" + pageInfo[2]);
                            }
                        });
                    }

                }
            }
        });
    }

    private void runCmd(ArrayList<String> args){
        cmdRunnable = new CmdExec(this, args);
        Thread test = new Thread(cmdRunnable);
        test.start();
    }

    @Override
    public void printResult(final ArrayList<String> result,final long numOfPackets) {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int prevSize = mRVAdapter.getItemCount();
                    mRVAdapter.bindList(result);
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    mRVAdapter.notifyItemRangeInserted(prevSize, 5);
                    pageText.setText(pageInfo[0] + "/" + result.size() + "/" + numOfPackets + " packets");

                }
            });
        }
    }

    @Override
    public void printToast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
