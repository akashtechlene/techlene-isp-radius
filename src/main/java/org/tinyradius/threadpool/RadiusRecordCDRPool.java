//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.tinyradius.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusRecordCDRPool {
   private static RadiusRecordCDRPool radiusRecordCDRPool = null;
   private ExecutorService radiusRecordCDRExecutor;
   private Logger logger = LoggerFactory.getLogger("radiuslog");
   private int minThread = 10;
   private int maxThread = 50;
   private int threadBenchTime = 0;

   private RadiusRecordCDRPool() {
   }

   public static RadiusRecordCDRPool getInstance() {
      return radiusRecordCDRPool;
   }

   public ExecutorService getExecutor() {
      if (this.radiusRecordCDRExecutor == null) {
         this.logger.info("RadiusRecordCDRPool Initialized: min:" + this.minThread + ",Max:" + this.maxThread);
         this.radiusRecordCDRExecutor = new ThreadPoolExecutor(this.minThread, this.maxThread, (long)this.threadBenchTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), (new ThreadFactoryBuilder()).setNameFormat("RadiusRecordCDRPool-%d").build());
      }

      return this.radiusRecordCDRExecutor;
   }

   public int getMinThread() {
      return this.minThread;
   }

   public void setMinThread(int minThread) {
      this.minThread = minThread;
   }

   public int getMaxThread() {
      return this.maxThread;
   }

   public void setMaxThread(int maxThread) {
      this.maxThread = maxThread;
   }

   public int getThreadBenchTime() {
      return this.threadBenchTime;
   }

   public void setThreadBenchTime(int threadBenchTime) {
      this.threadBenchTime = threadBenchTime;
   }

   static {
      try {
         radiusRecordCDRPool = new RadiusRecordCDRPool();
      } catch (Exception var1) {
      }

   }
}
