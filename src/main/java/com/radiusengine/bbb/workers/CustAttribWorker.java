//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.radiusengine.bbb.workers;

import com.radiusengine.bbb.data.CustomerAttribute;
import com.radiusengine.bbb.db.DBAuthenticationDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustAttribWorker implements Runnable {
    Logger logger = LoggerFactory.getLogger("radiuslog");
    long lngStartTime = System.currentTimeMillis();
    String PACKET_IDENTIFIER;
    String MODULE = "[CustAttribWorker]";
    CustomerAttribute customerAttribute;

    public CustAttribWorker(String PACKET_IDENTIFIER, CustomerAttribute customerAttribute) {
        this.customerAttribute = customerAttribute;
        this.PACKET_IDENTIFIER = PACKET_IDENTIFIER + this.MODULE;
    }

    public void run() {
        try {
            this.logger.info("{} Started..." + this.PACKET_IDENTIFIER);
            if (this.customerAttribute != null) {
                DBAuthenticationDriver driver = new DBAuthenticationDriver();
                if (this.customerAttribute.getCustattrid() > 0) {
                    driver.updateCustomerArribute(this.customerAttribute);
                } else {
                    driver.insertCustomerArribute(this.customerAttribute);
                }
            }
        } catch (Exception var3) {
            Exception e = var3;
            this.logger.error("{} ERROR in {}:{} {}", new Object[]{this.PACKET_IDENTIFIER, this.MODULE, e.getMessage(), e});
        }

        long endTime = System.currentTimeMillis();
        this.logger.info("{} Ended. Time to Final Respond:{}", endTime - this.lngStartTime);
    }
}
