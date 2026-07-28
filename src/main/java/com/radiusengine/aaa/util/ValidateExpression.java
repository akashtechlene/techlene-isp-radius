//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.aaa.util;

import com.radiusengine.aaa.data.customerData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.packet.RadiusPacket;

public class ValidateExpression {
    Logger log = LoggerFactory.getLogger("radiuslog");
    public static final String NEO_PREFIX_USERATTR = "NEO-USERATTR-";
    public static final String NEO_PREFIX_ATTR = "NEO-ATTR-";
    public static final String NEO_PREFIX_CUSTOMIZATION = "NEO-CUSTOMIZATION-";
    public static final String NEO_USERATTR_STATICIPALLOTTED = "NEO-USERATTR-STATICIPALLOTED";
    public static final String NEO_USERATTR_NETWORKTYPE = "NEO-USERATTR-NETWORKTYPE";
    public static final String NEO_USERATTR_PARTNERNAME = "NEO-USERATTR-PARTNERNAME";
    public static final String NEO_USERATTR_POOLNAME = "NEO-USERATTR-POOLNAME";
    public static final String NEO_USERATTR_SERVICETYPE = "NEO-USERATTR-SERVICETYPE";
    public static final String NEO_ATTR_CLIENTGROUP = "NEO-ATTR-CLIENTGROUP";
    public String PACKET_IDENTIFIER = "";

    public boolean checkExpression(String strExpression, RadiusPacket request) {
        return this.checkExpression(strExpression, request, (RadiusPacket)null, (customerData)null);
    }

    public ValidateExpression() {
    }

    public ValidateExpression(String packetIdentifier) {
        this.PACKET_IDENTIFIER = packetIdentifier;
    }

    public boolean checkExpression(String strExpression, RadiusPacket request, RadiusPacket response, customerData customerData) {
        boolean bExprMatches = false;
        this.log.debug(this.PACKET_IDENTIFIER + "In checkExpression, start validating Expression:" + strExpression);

        try {
            if (strExpression != null && !strExpression.equalsIgnoreCase("")) {
                Pattern pattern = Pattern.compile("[&]");
                String[] conditions = pattern.split(strExpression);
                List<HashMap<String, String>> conditionList = new ArrayList();
                HashMap<String, String> condiKeyValMap = null;
                Pattern patternkeyValue = Pattern.compile("[=]");
                String[] var11 = conditions;
                int var12 = conditions.length;

                for(int var13 = 0; var13 < var12; ++var13) {
                    String s = var11[var13];
                    String[] condWords = patternkeyValue.split(s);
                    if (condWords != null && condWords.length == 2) {
                        condiKeyValMap = new HashMap();
                        condiKeyValMap.put(condWords[0].trim(), condWords[1].trim().replaceAll("\"", "").replaceAll("'", ""));
                        conditionList.add(condiKeyValMap);
                    }
                }

                label69:
                for(int i = 0; i < conditionList.size(); ++i) {
                    HashMap<String, String> regMap = (HashMap)conditionList.get(i);
                    Iterator it = regMap.entrySet().iterator();

                    while(true) {
                        while(it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            this.log.debug(this.PACKET_IDENTIFIER + pair.getKey() + " = " + pair.getValue());
                            if (!pair.getKey().toString().startsWith("NEO")) {
                                try {
                                    if (null != request.getAttribute(pair.getKey().toString()) && pair.getValue().toString().equalsIgnoreCase(request.getAttribute(pair.getKey().toString()).getAttributeValue())) {
                                        this.log.debug(this.PACKET_IDENTIFIER + "In checkExpression" + pair.getKey() + " is matched with Radius Request");
                                        bExprMatches = true;
                                    } else {
                                        this.log.debug(this.PACKET_IDENTIFIER + "In checkExpression" + pair.getKey() + " not matched with Radius Request");
                                        bExprMatches = false;
                                    }
                                } catch (Exception var16) {
                                    bExprMatches = false;
                                    this.log.error(this.PACKET_IDENTIFIER + "In checkExpression Error in checkExpression():" + var16, var16);
                                }
                            } else if (pair.getKey().toString().startsWith("NEO") && pair.getKey() != null) {
                                bExprMatches = this.validateNeoExpresson(pair.getKey().toString(), pair.getValue().toString(), request, response, customerData);
                                if (bExprMatches) {
                                    this.log.debug(this.PACKET_IDENTIFIER + "In checkExpression" + pair.getKey() + " is matched with Neo Attributes");
                                } else {
                                    this.log.debug(this.PACKET_IDENTIFIER + "In checkExpression" + pair.getKey() + " not matched with Neo Attributes");
                                }
                            } else {
                                bExprMatches = false;
                                this.log.debug(this.PACKET_IDENTIFIER + "In checkExpression" + pair.getKey() + " is supported");
                            }
                        }

                        if (!bExprMatches) {
                            this.log.debug(this.PACKET_IDENTIFIER + "In checkExpression Dropped the condition as one of the condition match failed");
                            break label69;
                        }
                        break;
                    }
                }
            } else {
                bExprMatches = true;
            }
        } catch (Exception var17) {
            Exception e = var17;
            this.log.error(this.PACKET_IDENTIFIER + "Error in checkExpression():" + e, e);
            bExprMatches = false;
        }

        this.log.debug(this.PACKET_IDENTIFIER + "In checkExpression, validating Expression:" + strExpression + ",Result:" + bExprMatches);
        return bExprMatches;
    }

    public boolean validateNeoExpresson(String key, String value, RadiusPacket request, RadiusPacket response, customerData customerData) {
        boolean blnReply = false;

        try {
            if (key == null || value == null) {
                return false;
            }

            if (key.startsWith("NEO-ATTR-")) {
                switch (key) {
                    case "NEO-ATTR-CLIENTGROUP":
                        String radiusAttribute = "Connect-Info";
                        this.log.debug(this.PACKET_IDENTIFIER + "In validateNeoExpresson,request:" + request);
                        if (request != null && null != request.getAttribute(radiusAttribute) && value.equalsIgnoreCase(request.getAttribute(radiusAttribute).getAttributeValue())) {
                            blnReply = true;
                        }
                }
            } else if (key.startsWith("NEO-USERATTR-")) {
                switch (key) {
                    case "NEO-USERATTR-STATICIPALLOTED":
                        boolean isIPSet = customerData != null && customerData.getIpaddress() != null && customerData.getIpaddress().length() > 0;
                        this.log.debug(this.PACKET_IDENTIFIER + "In validateNeoExpresson,isIPSet:" + isIPSet);
                        if (value.equalsIgnoreCase("true") && isIPSet) {
                            blnReply = true;
                        } else if (value.equalsIgnoreCase("false") && !isIPSet) {
                            blnReply = true;
                        }
                        break;
                    case "NEO-USERATTR-NETWORKTYPE":
                        if (customerData != null && customerData.getNetworktype() != null && !"".equalsIgnoreCase(customerData.getNetworktype()) && customerData.getNetworktype().equalsIgnoreCase(value)) {
                            blnReply = true;
                        }
                        break;
                    case "NEO-USERATTR-PARTNERNAME":
                        if (customerData != null && customerData.getPartnerName() != null && !"".equalsIgnoreCase(customerData.getPartnerName()) && customerData.getPartnerName().equalsIgnoreCase(value)) {
                            blnReply = true;
                        }
                        break;
                    case "NEO-USERATTR-SERVICETYPE":
                        if (customerData != null && customerData.getServicetype() != null && !"".equalsIgnoreCase(customerData.getServicetype()) && customerData.getServicetype().equalsIgnoreCase(value)) {
                            blnReply = true;
                        }
                        break;
                    case "NEO-USERATTR-POOLNAME":
                        if (customerData != null && customerData.getPoolname() != null && !"".equalsIgnoreCase(customerData.getPoolname()) && customerData.getPoolname().equalsIgnoreCase(value)) {
                            blnReply = true;
                        }
                }
            }
        } catch (Exception var10) {
            Exception e = var10;
            this.log.error(this.PACKET_IDENTIFIER + "Error in validateNeoExpresson():" + e, e);
            blnReply = false;
        }

        return blnReply;
    }
}
