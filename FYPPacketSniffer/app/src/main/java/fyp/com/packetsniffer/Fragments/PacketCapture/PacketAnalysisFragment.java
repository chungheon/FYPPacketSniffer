package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
import fyp.com.packetsniffer.Fragments.CmdExecNormal;
import fyp.com.packetsniffer.Fragments.DevicesConnected.ScanHistoryFragment;
import fyp.com.packetsniffer.Fragments.FileChooser;
import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class PacketAnalysisFragment extends Fragment implements CmdExecInterface {

    private final String TAG = "PCapFragment";
    private View view;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;

    private RecyclerView output;
    private Button jumpBtn;
    private EditText pageJump;
    private TextView pageText;
    private Button analyseBtn;
    private TextView selectedText;
    private Button selectFileBtn;
    private RadioGroup filters;
    private CardView filterMenu;
    private ImageButton expandFilters;
    private RadioGroup hexFilters;
    private Button reportBtn;
    private TextView numPacket;

    private long movement = 1050;
    private String architect = "";
    private String filter = "";
    private String numOfPackets = "";
    private String grep = "";
    private String hex = "";
    private int mode = 0;
    private boolean inProgress;
    private CmdExecNormal cmdRunnable;
    private FileChooser fileChooser;
    private String fileName = "";
    private String captureName = "-";
    private long seconds = new Long(1);
    private boolean http = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_packet_analysis, container, false);
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.output = (RecyclerView) view.findViewById(R.id.output_content);
        this.analyseBtn = (Button) view.findViewById(R.id.analyse_btn);
        this.jumpBtn = (Button) view.findViewById(R.id.jump_btn);
        this.pageJump = (EditText) view.findViewById(R.id.jump_page);
        this.pageText = (TextView) view.findViewById(R.id.num_page);
        this.selectedText = (TextView) view.findViewById(R.id.analyse_selected_file);
        this.selectFileBtn = (Button) view.findViewById(R.id.analyse_select_file);
        this.filterMenu = (CardView) view.findViewById(R.id.analyse_filters);
        this.expandFilters = (ImageButton) view.findViewById(R.id.analyse_view_filters_btn);
        this.filters = (RadioGroup) view.findViewById(R.id.analyse_filter_options);
        this.hexFilters = (RadioGroup) view.findViewById(R.id.analyse_hex_filters);
        this.reportBtn = (Button) view.findViewById(R.id.analyse_get_report);
        this.numPacket = (TextView) view.findViewById(R.id.num_packets);

        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<byte[]>());

        output.setAdapter(mRVAdapter);
        output.setLayoutManager(mLayoutManager);
        this.pageText.setText("0/0");
        this.inProgress = false;
        this.fileChooser = new FileChooser(getActivity());
        jumpBtn.setVisibility(View.GONE);
        pageJump.setVisibility(View.GONE);
        output.setVisibility(View.GONE);
        pageText.setVisibility(View.GONE);
        numPacket.setVisibility(View.GONE);
        showAllUI();
    }

    private void initListener(){

        analyseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> cmds = new ArrayList<>();
                File file = new File(fileName);

                if(!file.exists()){
                    printToast("Please select a valid file");
                    return;
                }

                if(grep.equals(" | grep -e \'Host:\' -e \'User-Agent:\' -e \'Set-Cookie\' - e \'Cookie\'")){
                    http = true;
                }else{
                    http = false;
                }
                captureName = selectedText.getText().toString();

                String cmd = "tcpdump " + filter + "-ttttvvvv" + hex + " -r " + fileName + grep;

                cmds.add(cmd);

                if(cmdRunnable != null){
                    if(inProgress){
                        cmdRunnable.stopRun();
                        inProgress = false;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                analyseBtn.setText("Review");
                                showAllUI();
                            }
                        });
                    }else{
                        final ArrayList<String> allCmd = cmds;
                        ((MainActivity) getActivity()).clearResult();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(),
                                        new ArrayList<byte[]>());
                                output.setAdapter(mRVAdapter);
                                pageText.setText("1/1");
                                analyseBtn.setText("Stop Review");
                                hideAllUI();
                            }
                        });
                        movement = 1050;
                        runCmd(allCmd);
                        inProgress = true;

                    }
                }else{
                    ((MainActivity) getActivity()).clearResult();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pageText.setText("0/0");
                            analyseBtn.setText("Stop Review");
                            hideAllUI();
                        }
                    });
                    runCmd(cmds);
                    inProgress = true;
                }
            }
        });

        output.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    if(pageInfo.length >= 2){
                        long totalpages = Integer.parseInt(pageInfo[1]) * 1050;
                        if(movement <= totalpages && pageInfo.length >= 2){
                            movement += dx;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pageText.setText((((movement + 200)/1050)) + "/" + pageInfo[1]);
                                }
                            });
                        }
                        if(movement > totalpages){
                            movement = totalpages;
                        }
                    }
                } else {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    if((movement - 1050) <= (dx * -1)){
                        movement = 1050;
                    }else{
                        movement += dx;
                        if(pageInfo.length >= 2){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pageText.setText((((movement + 600)/1050)) + "/" + pageInfo[1]);
                                }
                            });
                        }
                    }

                    if(movement == 1050 && pageInfo.length >= 2){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText("1/" + pageInfo[1]);
                            }
                        });
                    }
                }
            }
        });

        jumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pageNum = pageJump.getText().toString();
                String[] totalPage = pageText.getText().toString().split("/");
                try{
                    int num = Integer.parseInt(pageNum);
                    if(totalPage.length >= 2){
                        int totalPages = Integer.parseInt(totalPage[1]);
                        if(totalPages < num){
                            Toast.makeText(getContext().getApplicationContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }else{
                        return;
                    }
                    if(num <= 0){
                        Toast.makeText(getContext().getApplicationContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }else{
                        output.scrollToPosition(num - 1);
                        movement = (num) * 1050;
                    }

                }catch (NumberFormatException e){
                    Toast.makeText(getContext().getApplicationContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                selectedText.setText(file.getName());
                fileName = file.getPath();
            }
        });

        selectFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileChooser.showDialog();
            }
        });



        expandFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filters.isEnabled()){
                    filters.setEnabled(false);
                    filters.setVisibility(View.GONE);
                    hexFilters.setVisibility(View.GONE);
                }else{
                    filters.setEnabled(true);
                    filters.setVisibility(View.VISIBLE);
                    hexFilters.setVisibility(View.VISIBLE);
                }
            }
        });

        hexFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String selected = "";
                switch (checkedId){
                    case R.id.analyse_hex_1: hex = "x";
                        selected = "Hex Enabled";
                        break;

                    case R.id.analyse_hex_2: hex = "xx";
                        selected = "Hex Enabled (With ethernet header)";
                        break;

                    case R.id.analyse_hex_3: hex = "X";
                        selected = "Hex & ASCII Enabled";
                        break;

                    case R.id.analyse_hex_4: hex = "XX";
                        selected = "Hex & ASCII Enabled (With ethernet header)";
                        break;

                    case R.id.analyse_hex_5: hex = "x";
                        selected = "No Hex/ASCII";
                        break;
                }
                showMessage("SELECTED " + selected);
            }
        });

        filters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String selected = "";
                switch (checkedId){
                    case R.id.filter_1: filter = "";
                        grep = "";
                        selected = "No Filter";
                        mode = 0;
                        break;
                    //FTP Traffic
                    case  R.id.filter_2: filter = "-s0 ";
                        grep = " port 21";
                        selected = "FTP Traffic";
                        mode = 2;
                        break;
                    //HTTP Host Headers
                    case R.id.filter_3: filter = "-s0 ";
                        grep = " | grep -e \'Host:\' -e \'User-Agent:\' -e \'Set-Cookie\' - e \'Cookie\'";
                        selected = "HTTP Information";
                        mode = 2;
                        break;
                    //NTP Traffic
                    case R.id.filter_4: filter = "";
                        filter = "-s0 port 123 ";
                        grep = "";
                        selected = "NTP Traffic";
                        mode = 1;
                        break;
                    //DNS Traffic
                    case R.id.filter_5: filter = "-s0 port 53 ";
                        grep = "";
                        selected = "DNS Traffic";
                        mode = 1;
                        break;
                    //TCP Traffic
                    case R.id.filter_6: filter = "tcp ";
                        grep = "";
                        selected = "TCP Traffic";
                        mode = 0;
                        break;
                    //UDP Traffic
                    case R.id.filter_7: filter = "udp ";
                        grep = "";
                        selected = "UDP Traffic";
                        mode = 0;
                        break;
                    //ICMP Traffic
                    case R.id.filter_8: filter = "icmp ";
                        grep = "";
                        selected = "ICMP Traffic";
                        mode = 0;
                        break;
                    //ARP Traffic
                    case R.id.filter_9: filter = "arp ";
                        grep = "";
                        selected = "ARP Traffic";
                        mode = 0;
                        break;
                }
                showMessage("SELECTED " + selected);
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRVAdapter != null && !mRVAdapter.getData().isEmpty() && !http) {

                    String packets = numPacket.getText().toString();
                    numOfPackets = packets;
                    packets = packets.replace(" packets", "");
                    Long numPackets;
                    try{
                        numPackets = Long.parseLong(packets);
                    }catch (NumberFormatException e){
                        numPackets = new Long(0);
                    }
                    ((MainActivity) getActivity()).storeResult(mRVAdapter.getData(), numPackets);
                    Bundle args = new Bundle();
                    args.putString("capturefile", captureName);
                    args.putString("numpackets", packets);
                    AnalysisReportFragment nFrag = new AnalysisReportFragment();
                    nFrag.setArguments(args);
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle("Analysis Report");
                    ((MainActivity) getActivity()).enableViews(true, 1, "Packet Analysis", "");
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, nFrag, "AnalysisReport")
                            .addToBackStack(null)
                            .commit();
                } else if (((MainActivity) getActivity()).getResult() != null && !((MainActivity) getActivity()).getResult().isEmpty()){
                    long packets = ((MainActivity) getActivity()).getNumPackets();
                    Bundle args = new Bundle();
                    args.putString("capturefile", captureName);
                    args.putString("numpackets", packets + "");
                    AnalysisReportFragment nFrag = new AnalysisReportFragment();
                    nFrag.setArguments(args);
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle("Analysis Report");
                    ((MainActivity) getActivity()).enableViews(true, 1, "Packet Analysis", "");
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, nFrag, "AnalysisReport")
                            .addToBackStack(null)
                            .commit();
                }else if(http){
                    printToast("HTTP Data has no information to report");
                }else{
                    printToast("No packets have been read");
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(),
                            new ArrayList<byte[]>());
                    File file = new File(fileName);
                    if(file.exists()){
                        selectedText.setText(file.getName());
                    }else{
                        fileName = "";
                    }
                    output.setAdapter(mRVAdapter);
                }
            });
        }
    }

    private void hideAllUI(){
        filters.setVisibility(View.GONE);
        filterMenu.setVisibility(View.GONE);
        hexFilters.setVisibility(View.GONE);
        selectFileBtn.setVisibility(View.GONE);
        selectedText.setVisibility(View.GONE);
        reportBtn.setVisibility(View.GONE);
        jumpBtn.setVisibility(View.VISIBLE);
        pageJump.setVisibility(View.VISIBLE);
        output.setVisibility(View.VISIBLE);
        pageText.setVisibility(View.VISIBLE);
        numPacket.setVisibility(View.VISIBLE);
    }

    private void showAllUI(){
        filterMenu.setVisibility(View.VISIBLE);
        filters.setVisibility(View.GONE);
        hexFilters.setVisibility(View.GONE);
        reportBtn.setVisibility(View.VISIBLE);
        filters.setEnabled(false);
        selectFileBtn.setVisibility(View.VISIBLE);
        selectedText.setVisibility(View.VISIBLE);
    }

    private void runCmd(ArrayList<String> args){
        cmdRunnable = new PacketViewThread(this, args, mode);
        cmdRunnable.start();
    }

    public void printToast(final String message){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showMessage(final String message){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void printResult(final String result, final long numOfPackets){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int prevSize = mRVAdapter.getItemCount();
                    mRVAdapter.addPage(result);
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    String text = pageInfo[0] + "/" + mRVAdapter.getItemCount();
                    if(numOfPackets <= 0){
                        numPacket.setText("-");
                    }else{
                        numPacket.setText(numOfPackets + " packets");
                    }
                    mRVAdapter.notifyItemRangeInserted(prevSize, 1);
                    pageText.setText(text);
                }
            });
        }
    }

    @Override
    public void cmdDone() {
        inProgress = false;
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    analyseBtn.setText("Review");
                    if(mRVAdapter.getItemCount() == 0){
                        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(),
                                "No packets available");
                        output.setAdapter(mRVAdapter);
                    }
                    showAllUI();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if(cmdRunnable != null){
            cmdRunnable.stopRun();
        }
        if(getActivity() != null){
            ((MainActivity) getActivity()).clearResult();
        }
        super.onDestroy();
    }
}