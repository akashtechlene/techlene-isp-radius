/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.bbb.data;

import java.io.Serializable;

public class ServerConfData implements Serializable {
    private String attributeName;
    private String attributeValue;
    private String preservWithCustomer;
    private String ensureUnique;

    public ServerConfData() {
    }

    public String getPreservWithCustomer() {
        return this.preservWithCustomer;
    }

    public void setPreservWithCustomer(String preservWithCustomer) {
        this.preservWithCustomer = preservWithCustomer;
    }

    public String getEnsureUnique() {
        return this.ensureUnique;
    }

    public void setEnsureUnique(String ensureUnique) {
        this.ensureUnique = ensureUnique;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return this.attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String toString() {
        return "DATA : Atttibute:" + this.attributeName + ":Value:" + this.attributeValue + ":";
    }
}
