//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.test;

import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusClient;

public class TestClient {
   public TestClient() {
   }

   public static void main(String[] args) throws Exception {
      if (args.length != 4) {
         System.out.println("Usage: TestClient hostName sharedSecret userName password");
         System.exit(1);
      }

      String host = args[0];
      String shared = args[1];
      String user = args[2];
      String pass = args[3];
      RadiusClient rc = new RadiusClient(host, shared);
      rc.setSocketTimeout(100000000);
      AccountingRequest acc = new AccountingRequest("mw", 1);
      acc.addAttribute("Acct-Session-Id", "1234567892");
      acc.addAttribute("A-AL-AAA", "3333");
      acc.addAttribute("Cisco-AVPair", "4444");
      System.out.println(acc + "\n");
      RadiusPacket response = rc.account(acc);
      System.out.println("Response: " + response);
      Thread.sleep(1000L);
      acc = new AccountingRequest("mw", 2);
      acc.addAttribute("Acct-Session-Id", "1234567892");
      acc.addAttribute("A-AL-AAA", "3333");
      acc.addAttribute("Cisco-AVPair", "4444");
      System.out.println(acc + "\n");
      response = rc.account(acc);
      System.out.println("Response: " + response);
      rc.close();
   }
}
