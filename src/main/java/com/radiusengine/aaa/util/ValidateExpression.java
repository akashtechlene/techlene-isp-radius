/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.util;

import com.radiusengine.aaa.data.customerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.packet.RadiusPacket;

public class ValidateExpression {
    private static final Logger LOG = LoggerFactory.getLogger("radiuslog");
    public static final String NEO_PREFIX_USERATTR = "NEO-USERATTR-";
    public static final String NEO_PREFIX_ATTR = "NEO-ATTR-";
    public static final String NEO_PREFIX_CUSTOMIZATION = "NEO-CUSTOMIZATION-";
    public static final String NEO_USERATTR_STATICIPALLOTTED = "NEO-USERATTR-STATICIPALLOTED";
    public static final String NEO_USERATTR_NETWORKTYPE = "NEO-USERATTR-NETWORKTYPE";
    public static final String NEO_USERATTR_PARTNERNAME = "NEO-USERATTR-PARTNERNAME";
    public static final String NEO_USERATTR_POOLNAME = "NEO-USERATTR-POOLNAME";
    public static final String NEO_USERATTR_SERVICETYPE = "NEO-USERATTR-SERVICETYPE";
    public static final String NEO_ATTR_CLIENTGROUP = "NEO-ATTR-CLIENTGROUP";
    private static final String NEO_KEY_PREFIX = "NEO";
    private static final String CLIENT_GROUP_REQUEST_ATTRIBUTE = "Connect-Info";
    public String PACKET_IDENTIFIER = "";

    public ValidateExpression() {
    }

    public ValidateExpression(String packetIdentifier) {
        this.PACKET_IDENTIFIER = packetIdentifier;
    }

    public boolean checkExpression(String strExpression, RadiusPacket request) {
        return this.checkExpression(strExpression, request, null, null);
    }

    /**
     * Evaluates an expression such as {@code Attr1=value1&NEO-USERATTR-POOLNAME=pool}. Every well-formed
     * {@code key=value} condition must match; conditions that are not exactly one key and one value are ignored.
     * An empty expression always matches, an expression without a single well-formed condition never does.
     */
    public boolean checkExpression(String strExpression, RadiusPacket request, RadiusPacket response, customerData customerData) {
        boolean exprMatches = false;
        LOG.debug("{}In checkExpression, start validating Expression:{}", this.PACKET_IDENTIFIER, strExpression);

        try {
            if (strExpression == null || strExpression.isEmpty()) {
                exprMatches = true;
            } else {
                for (String condition : strExpression.split("&")) {
                    String[] keyValue = condition.split("=");
                    if (keyValue.length != 2) {
                        continue;
                    }

                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim().replaceAll("\"", "").replaceAll("'", "");
                    LOG.debug("{}{} = {}", this.PACKET_IDENTIFIER, key, value);
                    exprMatches = key.startsWith(NEO_KEY_PREFIX)
                            ? this.matchesNeoAttribute(key, value, request, response, customerData)
                            : this.matchesRequestAttribute(key, value, request);
                    if (!exprMatches) {
                        LOG.debug("{}In checkExpression Dropped the condition as one of the condition match failed", this.PACKET_IDENTIFIER);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(this.PACKET_IDENTIFIER + "Error in checkExpression():" + e, e);
            exprMatches = false;
        }

        LOG.debug("{}In checkExpression, validating Expression:{},Result:{}", this.PACKET_IDENTIFIER, strExpression, exprMatches);
        return exprMatches;
    }

    private boolean matchesRequestAttribute(String key, String value, RadiusPacket request) {
        try {
            RadiusAttribute attribute = request.getAttribute(key);
            boolean matched = attribute != null && value.equalsIgnoreCase(attribute.getAttributeValue());
            LOG.debug("{}In checkExpression{} {} with Radius Request", this.PACKET_IDENTIFIER, key, matched ? "is matched" : "not matched");
            return matched;
        } catch (Exception e) {
            LOG.error(this.PACKET_IDENTIFIER + "In checkExpression Error in checkExpression():" + e, e);
            return false;
        }
    }

    private boolean matchesNeoAttribute(String key, String value, RadiusPacket request, RadiusPacket response, customerData customerData) {
        boolean matched = this.validateNeoExpresson(key, value, request, response, customerData);
        LOG.debug("{}In checkExpression{} {} with Neo Attributes", this.PACKET_IDENTIFIER, key, matched ? "is matched" : "not matched");
        return matched;
    }

    public boolean validateNeoExpresson(String key, String value, RadiusPacket request, RadiusPacket response, customerData customerData) {
        if (key == null || value == null) {
            return false;
        }

        try {
            switch (key) {
                case NEO_ATTR_CLIENTGROUP:
                    LOG.debug("{}In validateNeoExpresson,request:{}", this.PACKET_IDENTIFIER, request);
                    if (request == null) {
                        return false;
                    }

                    RadiusAttribute clientGroup = request.getAttribute(CLIENT_GROUP_REQUEST_ATTRIBUTE);
                    return clientGroup != null && value.equalsIgnoreCase(clientGroup.getAttributeValue());
                case NEO_USERATTR_STATICIPALLOTTED:
                    boolean isIPSet = customerData != null && customerData.getIpaddress() != null && customerData.getIpaddress().length() > 0;
                    LOG.debug("{}In validateNeoExpresson,isIPSet:{}", this.PACKET_IDENTIFIER, isIPSet);
                    return value.equalsIgnoreCase("true") && isIPSet || value.equalsIgnoreCase("false") && !isIPSet;
                case NEO_USERATTR_NETWORKTYPE:
                    return customerData != null && equalsNonEmpty(customerData.getNetworktype(), value);
                case NEO_USERATTR_PARTNERNAME:
                    return customerData != null && equalsNonEmpty(customerData.getPartnerName(), value);
                case NEO_USERATTR_SERVICETYPE:
                    return customerData != null && equalsNonEmpty(customerData.getServicetype(), value);
                case NEO_USERATTR_POOLNAME:
                    return customerData != null && equalsNonEmpty(customerData.getPoolname(), value);
                default:
                    return false;
            }
        } catch (Exception e) {
            LOG.error(this.PACKET_IDENTIFIER + "Error in validateNeoExpresson():" + e, e);
            return false;
        }
    }

    private static boolean equalsNonEmpty(String actual, String expected) {
        return actual != null && !actual.isEmpty() && actual.equalsIgnoreCase(expected);
    }
}
