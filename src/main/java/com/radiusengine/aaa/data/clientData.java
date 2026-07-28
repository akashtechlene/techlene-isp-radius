//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.aaa.data;

import java.io.Serializable;

public class clientData implements Serializable {
    private String groupname;
    private String clintid;
    private String sharedkey;
    private String timeout;
    private String clientip;
    private int groupid;
    private String iptype;

    public clientData() {
    }

    public String getIptype() {
        return this.iptype;
    }

    public void setIptype(String iptype) {
        this.iptype = iptype;
    }

    public int getGroupid() {
        return this.groupid;
    }

    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }

    public String getClientip() {
        return this.clientip;
    }

    public void setClientip(String clientip) {
        this.clientip = clientip;
    }

    public String getGroupname() {
        return this.groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getClintid() {
        return this.clintid;
    }

    public void setClintid(String clintid) {
        this.clintid = clintid;
    }

    public String getSharedkey() {
        return this.sharedkey;
    }

    public void setSharedkey(String sharedkey) {
        this.sharedkey = sharedkey;
    }

    public String getTimeout() {
        return this.timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
}
