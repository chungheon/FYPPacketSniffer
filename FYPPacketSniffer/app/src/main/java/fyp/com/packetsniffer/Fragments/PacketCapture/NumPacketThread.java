package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.util.Pair;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class NumPacketThread implements Runnable{
    private Thread thread;
    AnalysisReportFragment mFragment;
    private ArrayList<String> data;
    private HashMap<String, Double> numPackets;
    private ArrayList<String> dataSend;

    public NumPacketThread(AnalysisReportFragment fragment, ArrayList<String> data){
        this.data = data;
        this.mFragment = fragment;
        numPackets = new HashMap<>();
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        gatherIPAddrStats();
    }
    private void gatherIPAddrStats(){
        for(String data: this.data){
            String d = data.replace("|", "");
            String[] allPacket = data.split("\n");
            for(String packet: allPacket){
                String[] dataInfo = packet.split("\\|");
                if(dataInfo.length >= 2){
                    String process = dataInfo[1];
                    String[] processInfo = process.split(":")[0].split("\\.");
                    if(processInfo.length >= 4){
                        String ip = processInfo[0] + "." + processInfo[1] + "." + processInfo[2] + "." + processInfo[3];
                        ip = ip.replace(" ", "");
                        if(ip.matches("\\d*\\.\\d*\\.\\d*\\.\\d*")){
                            if(numPackets.containsKey(ip)){
                                numPackets.put(ip, numPackets.get(ip) + new Double(1));
                            }else{
                                numPackets.put(ip, new Double(1));
                            }
                        }
                    }
                }
            }
            List<Pair<String, Double>> update = updateInfo();
            mFragment.printIPNumPacket(update);
        }
    }
    private List<Pair<String, Double>> updateInfo(){
        List<Pair<String, Double>> dataArr = new ArrayList<>();
        for(String key: numPackets.keySet()){
            Pair<String, Double> pair = new Pair<>(key,numPackets.get(key));
            dataArr.add(pair);
        }
        Collections.sort(dataArr, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return (int) (o2.second - o1.second);
            }
        });
        return dataArr;
    }
}
