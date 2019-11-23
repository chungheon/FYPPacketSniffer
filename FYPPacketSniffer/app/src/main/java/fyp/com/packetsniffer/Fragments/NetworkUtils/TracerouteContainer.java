package fyp.com.packetsniffer.Fragments.NetworkUtils;

/*
This file is part of the project TraceroutePing, which is an Android library
implementing Traceroute with ping under GPL license v3.
Copyright (C) 2013  Olivier Goutay

TraceroutePing is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

TraceroutePing is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with TraceroutePing.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.io.Serializable;

/**
 *
 * @author Olivier Goutay
 *
 */
public class TracerouteContainer {

    private String hostname;
    private String ip;
    private float ms;

    public TracerouteContainer(String hostname, String ip, float ms) {
        this.hostname = hostname;
        this.ip = ip;
        this.ms = ms;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public float getMs() {
        return ms;
    }

    public void setMs(float ms) {
        this.ms = ms;
    }

    @Override
    public String toString() {
        return "Traceroute : \nHostname : " + hostname + "\nip : " + ip + "\nMilliseconds : " + ms;
    }

}
