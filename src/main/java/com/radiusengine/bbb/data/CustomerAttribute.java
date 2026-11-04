/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.bbb.data;

import java.io.Serializable;

public class CustomerAttribute implements Serializable {
    private int custattrid;
    private int custid;
    private String attributeValue;
    private String attributeName;

    public CustomerAttribute() {
    }

    public int getCustattrid() {
        return this.custattrid;
    }

    public void setCustattrid(int custattrid) {
        this.custattrid = custattrid;
    }

    public int getCustid() {
        return this.custid;
    }

    public void setCustid(int custid) {
        this.custid = custid;
    }

    public String getAttributeValue() {
        return this.attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String toString() {
        return "CustomerAttribute{custattrid=" + this.custattrid + ", custid=" + this.custid + ", attributeValue='" + this.attributeValue + '\'' + ", attributeName='" + this.attributeName + '\'' + '}';
    }
}
