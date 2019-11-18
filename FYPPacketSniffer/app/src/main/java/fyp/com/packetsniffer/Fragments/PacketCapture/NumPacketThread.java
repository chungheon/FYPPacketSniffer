package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Pair;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class NumPacketThread implements Runnable{
    public Thread thread;
    AnalysisReportFragment mFragment;
    private ArrayList<byte[]> data;
    private ArrayList<byte[]> dataSend;
    private boolean exit = false;

    public NumPacketThread(AnalysisReportFragment fragment, ArrayList<byte[]> data){
        this.data = data;
        this.mFragment = fragment;
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        gatherIPAddrStats();
    }
    private void gatherIPAddrStats(){
        HashMap<String, Double> numSend = new HashMap<>();
        HashMap<String, Double> numRecv = new HashMap<>();
        String ipSend = "";
        String ipRecv = "";
        for(byte[] data: this.data){
            String[] allPacket = new String(data).split("\n");
            for(String packet: allPacket){
                String[] dataInfo = packet.split("\\|");
                if(dataInfo.length >= 2){
                    String process = dataInfo[1];
                    String[] packetInfo = process.split(":");
                    String[] processInfo = packetInfo[0].split(">");
                    if(processInfo.length >= 2){
                        String[] temp = processInfo[0].split("\\.");
                        ipSend = temp[0] + "." + temp[1] + "." + temp[2] + "." + temp[3];
                        ipSend = ipSend.replace(" ", "");
                        temp = processInfo[1].split("\\.");
                        ipRecv = temp[0] + "." + temp[1] + "." + temp[2] + "." + temp[3];
                        ipRecv = ipRecv.replace(" ", "");
                        if(ipSend.matches("\\d*\\.\\d*\\.\\d*\\.\\d*")){
                            if(numSend.containsKey(ipSend)){
                                numSend.put(ipSend, numSend.get(ipSend) + new Double(1));
                            }else{
                                numSend.put(ipSend, new Double(1));
                            }
                        }
                        if(ipRecv.matches("\\d*\\.\\d*\\.\\d*\\.\\d*")){
                            if(numRecv.containsKey(ipRecv)){
                                numRecv.put(ipRecv, numRecv.get(ipRecv) + new Double(1));
                            }else{
                                numRecv.put(ipRecv, new Double(1));
                            }
                        }
                    }
                }
                if(this.exit){
                    return;
                }
            }
            List<Pair<String, Double>> update = updateInfo(numSend);
            mFragment.printNumSend(update);
            update = updateInfo(numRecv);
            mFragment.printNumRecv(update);
        }
    }
    private List<Pair<String, Double>> updateInfo(HashMap<String, Double> numPackets){
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

    public void stopRun(){
        this.exit = true;
    }
}
