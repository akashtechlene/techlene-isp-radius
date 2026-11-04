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

public class RadiusAuthAuditPool {
   private static RadiusAuthAuditPool radiusAuthAuditPool = null;
   private ExecutorService radiusAuthAuditExecutor;
   private Logger logger = LoggerFactory.getLogger("radiuslog");
   private int radiusAuthAuditMinThread = 10;
   private int radiusAuthAuditMaxThread = 50;
   private int threadBenchTime = 0;

   private RadiusAuthAuditPool() {
   }

   public static RadiusAuthAuditPool getInstance() {
      return radiusAuthAuditPool;
   }

   public ExecutorService getExecutor() {
      if (this.radiusAuthAuditExecutor == null) {
         this.logger.info("RadiusAuthAuditPool Initialized: min:" + this.radiusAuthAuditMinThread + ",Max:" + this.radiusAuthAuditMaxThread);
         this.radiusAuthAuditExecutor = new ThreadPoolExecutor(this.radiusAuthAuditMinThread, this.radiusAuthAuditMaxThread, (long)this.threadBenchTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), (new ThreadFactoryBuilder()).setNameFormat("RadiusAuthAuditPool-%d").build());
      }

      return this.radiusAuthAuditExecutor;
   }

   public int getRadiusAuthAuditMinThread() {
      return this.radiusAuthAuditMinThread;
   }

   public void setRadiusAuthAuditMinThread(int radiusAuthAuditMinThread) {
      this.radiusAuthAuditMinThread = radiusAuthAuditMinThread;
   }

   public int getRadiusAuthAuditMaxThread() {
      return this.radiusAuthAuditMaxThread;
   }

   public void setRadiusAuthAuditMaxThread(int radiusAuthAuditMaxThread) {
      this.radiusAuthAuditMaxThread = radiusAuthAuditMaxThread;
   }

   public int getThreadBenchTime() {
      return this.threadBenchTime;
   }

   public void setThreadBenchTime(int threadBenchTime) {
      this.threadBenchTime = threadBenchTime;
   }

   static {
      try {
         radiusAuthAuditPool = new RadiusAuthAuditPool();
      } catch (Exception var1) {
      }

   }
}
