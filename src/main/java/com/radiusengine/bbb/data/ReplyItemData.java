/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.bbb.data;

import java.io.Serializable;

public class ReplyItemData implements Serializable {
    private String attribute;
    private String attributeValue;

    public ReplyItemData() {
    }

    public String getAttribute() {
        return this.attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getAttributeValue() {
        return this.attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String toString() {
        return "DATA : Atttibute:" + this.attribute + ":Value:" + this.attributeValue + ":";
    }
}
