//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.aaa.data;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class AcctProfileData implements Serializable {
    private String name;
    private String checkitem;
    private String accountcdrstatus;
    private String sessionstatus;
    private int mappingmasterid;
    private int priority;
    ConcurrentHashMap dbFieldMapping = new ConcurrentHashMap();

    public AcctProfileData() {
    }

    public ConcurrentHashMap getDbFieldMapping() {
        return this.dbFieldMapping;
    }

    public void setDbFieldMapping(ConcurrentHashMap dbFieldMapping) {
        this.dbFieldMapping = dbFieldMapping;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCheckitem() {
        return this.checkitem;
    }

    public void setCheckitem(String checkitem) {
        this.checkitem = checkitem;
    }

    public String getAccountcdrstatus() {
        return this.accountcdrstatus;
    }

    public void setAccountcdrstatus(String accountcdrstatus) {
        this.accountcdrstatus = accountcdrstatus;
    }

    public String getSessionstatus() {
        return this.sessionstatus;
    }

    public void setSessionstatus(String sessionstatus) {
        this.sessionstatus = sessionstatus;
    }

    public int getMappingmasterid() {
        return this.mappingmasterid;
    }

    public void setMappingmasterid(int mappingmasterid) {
        this.mappingmasterid = mappingmasterid;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
