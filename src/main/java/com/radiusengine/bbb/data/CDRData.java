/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.bbb.data;

import java.io.Serializable;

public class CDRData implements Serializable {
    private String username;
    private String nasip;
    private String framedip;
    private String sessionid;
    private double totalActualBytes;
    private double totalActualTime;
    private String createdate;
    private String lastmodifiedddate;

    public CDRData() {
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNasip() {
        return this.nasip;
    }

    public void setNasip(String nasip) {
        this.nasip = nasip;
    }

    public String getSessionid() {
        return this.sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public double getTotalActualBytes() {
        return this.totalActualBytes;
    }

    public void setTotalActualBytes(double totalActualBytes) {
        this.totalActualBytes = totalActualBytes;
    }

    public double getTotalActualTime() {
        return this.totalActualTime;
    }

    public void setTotalActualTime(double totalActualTime) {
        this.totalActualTime = totalActualTime;
    }

    public String getFramedip() {
        return this.framedip;
    }

    public void setFramedip(String framedip) {
        this.framedip = framedip;
    }

    public String getCreatedate() {
        return this.createdate;
    }

    public void setCreatedate(String createdate) {
        this.createdate = createdate;
    }

    public String getLastmodifiedddate() {
        return this.lastmodifiedddate;
    }

    public void setLastmodifiedddate(String lastmodifiedddate) {
        this.lastmodifiedddate = lastmodifiedddate;
    }
}
