/**
 * @author Akash Gaur
 * @created year 2026
 */

package com.radiusengine.aaa.snmp;

import java.util.Date;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPTrapGenerator {
    public SNMPTrapGenerator() {
    }

    public static void main(String[] args) {
        SNMPTrapGenerator trapV2 = new SNMPTrapGenerator();
        trapV2.sendTrap_Version2("public", "127.0.0.1", 162, "Trap", ".1.3.6.1.2.1.1.8");
        trapV2.clearTrap_Version2("public", "127.0.0.1", 162, "Clear", ".1.3.6.1.2.1.1.9");
    }

    public void sendTrap_Version2(String strCommunity, String strIpaddress, int intPort, String strMsg, String strOid) {
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();
            CommunityTarget cTarget = new CommunityTarget();
            cTarget.setCommunity(new OctetString(strCommunity));
            cTarget.setVersion(1);
            cTarget.setAddress(new UdpAddress(strIpaddress + "/" + intPort));
            cTarget.setRetries(2);
            cTarget.setTimeout(5000L);
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString((new Date()).toString())));
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(strOid)));
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress(strIpaddress)));
            pdu.add(new VariableBinding(new OID(strOid), new OctetString(strMsg)));
            pdu.setType(-89);
            Snmp snmp = new Snmp(transport);
            snmp.send(pdu, cTarget);
            snmp.close();
        } catch (Exception var10) {
            Exception e = var10;
            e.printStackTrace();
        }

    }

    public void clearTrap_Version2(String strCommunity, String strIpaddress, int intPort, String strMsg, String strOid) {
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();
            CommunityTarget cTarget = new CommunityTarget();
            cTarget.setCommunity(new OctetString(strCommunity));
            cTarget.setVersion(1);
            cTarget.setAddress(new UdpAddress(strIpaddress + "/" + intPort));
            cTarget.setRetries(2);
            cTarget.setTimeout(5000L);
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString((new Date()).toString())));
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(strOid)));
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress(strIpaddress)));
            pdu.add(new VariableBinding(new OID(strOid), new OctetString(strMsg)));
            pdu.setType(-89);
            Snmp snmp = new Snmp(transport);
            snmp.send(pdu, cTarget);
            snmp.close();
        } catch (Exception var10) {
            Exception e = var10;
            e.printStackTrace();
        }

    }
}
