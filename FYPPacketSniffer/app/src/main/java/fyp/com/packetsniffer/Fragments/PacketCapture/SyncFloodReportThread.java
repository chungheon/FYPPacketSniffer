package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.icu.util.LocaleData;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.util.Pair;
import android.widget.LinearLayout;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class SyncFloodReportThread implements Runnable{
    public Thread thread;
    AnalysisReportFragment mFragment;
    private ArrayList<byte[]> data;
    private ArrayList<byte[]> dataSend;
    private boolean exit = false;

    public SyncFloodReportThread(AnalysisReportFragment fragment, ArrayList<byte[]> data){
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
        HashMap<String, Double> numPackets = new HashMap<>();
        ArrayList<Pair<Calendar, Pair<Long, Long>>> packetMin = new ArrayList<>();
        int skipTimeCheck = 0;
        DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        Calendar currentTime = Calendar.getInstance();
        String allPackets = "";
        long syncCounter = 0;
        long ackCounter = 0;
        for(byte[] data: this.data){
            String[] allPacket = new String(data).split("\n");
            for(String packet: allPacket){
                String[] dataInfo = packet.split("\\|");
                if(dataInfo.length >= 2){
                    String process = dataInfo[1];
                    String[] packetInfo = process.split(":");
                    String[] processInfo = packetInfo[0].split("\\.");
                    if(processInfo.length >= 4){
                        String ip = processInfo[0] + "." + processInfo[1] + "." + processInfo[2] + "." + processInfo[3];
                        ip = ip.replace(" ", "");
                        if(ip.matches("\\d*\\.\\d*\\.\\d*\\.\\d*")){
                            /*String date = dataInfo[0].substring(0, 19);
                            if(skipTimeCheck == 1){
                                try{
                                    startTime.setTime(timeFormat.parse(date));
                                    startTime = PacketDate.getMinRange(1, startTime);
                                    endTime = PacketDate.getMaxRange(1, startTime);
                                    skipTimeCheck = 0;
                                } catch (ParseException e) { }
                            }else if(skipTimeCheck != 3){
                                try{
                                    startTime.setTime(timeFormat.parse(date));
                                    startTime = PacketDate.getMinRange(1, startTime);
                                    endTime = PacketDate.getMaxRange(1, startTime);
                                    skipTimeCheck = 3;
                                } catch (ParseException e) { skipTimeCheck = 1; }
                            }*/
                            if(numPackets.containsKey(ip)){
                                numPackets.put(ip, numPackets.get(ip) + new Double(1));
                            }else{
                                numPackets.put(ip, new Double(1));
                            }

                            /*if(skipTimeCheck == 3){
                                try{
                                    currentTime.setTime(timeFormat.parse(date));
                                    if(currentTime.before(endTime)){
                                        if(packetInfo[0].matches(".*Flags \\[S\\.?\\].*")){
                                            syncCounter++;
                                        }
                                    }else{
                                        try{
                                            startTime.setTime(timeFormat.parse(date));
                                            startTime = PacketDate.getMinRange(1, startTime);
                                            endTime = PacketDate.getMaxRange(1, startTime);
                                            skipTimeCheck = 3;
                                        } catch (ParseException e) { skipTimeCheck = 1; }
                                    }
                                } catch (ParseException e) { }
                                allPackets += packet;
                            }
                            packetMin.add(new Pair<>(endTime, new Pair<>(syncCounter, ackCounter)));
                            if(syncCounter > new Long(100) && ackCounter < new Long(20)){
                                dataSend.add(allPackets.getBytes());
                                allPackets = "";
                            }*/
                        }
                    }
                }
                if(this.exit){
                    return;
                }
            }
            List<Pair<String, Double>> update = updateInfo(numPackets);
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
