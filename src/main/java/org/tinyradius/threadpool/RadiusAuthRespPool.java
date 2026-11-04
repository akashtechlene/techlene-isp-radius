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

public class RadiusAuthRespPool {
   private static RadiusAuthRespPool authRespPool = null;
   private ExecutorService authRespExecutor;
   private ExecutorService acctRespExecutor;
   private Logger logger = LoggerFactory.getLogger("radiuslog");
   private int authMinThreads = 10;
   private int authMaxThreads = 100;
   private int acctMinThreads = 10;
   private int acctMaxThreads = 100;
   private int threadBenchTime = 200;

   public RadiusAuthRespPool() {
   }

   public ExecutorService getAuthRespExecutor() {
      if (this.authRespExecutor == null) {
         this.authRespExecutor = new ThreadPoolExecutor(this.authMinThreads, this.authMaxThreads, (long)this.threadBenchTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), (new ThreadFactoryBuilder()).setNameFormat("RadiusAuthRespPool-%d").build());
      }

      return this.authRespExecutor;
   }

   public ExecutorService getAcctRespExecutor() {
      if (this.acctRespExecutor == null) {
         this.acctRespExecutor = new ThreadPoolExecutor(this.acctMinThreads, this.acctMaxThreads, (long)this.threadBenchTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), (new ThreadFactoryBuilder()).setNameFormat("RadiusAcctRespPool-%d").build());
      }

      return this.acctRespExecutor;
   }

   public static RadiusAuthRespPool getRadiusAuthRespPool() {
      return authRespPool;
   }

   public int getAuthMinThreads() {
      return this.authMinThreads;
   }

   public void setAuthMinThreads(int authMinThreads) {
      this.authMinThreads = authMinThreads;
   }

   public int getAuthMaxThreads() {
      return this.authMaxThreads;
   }

   public void setAuthMaxThreads(int authMaxThreads) {
      this.authMaxThreads = authMaxThreads;
   }

   public int getThreadBenchTime() {
      return this.threadBenchTime;
   }

   public void setThreadBenchTime(int threadBenchTime) {
      this.threadBenchTime = threadBenchTime;
   }

   public int getAcctMinThreads() {
      return this.acctMinThreads;
   }

   public void setAcctMinThreads(int acctMinThreads) {
      this.acctMinThreads = acctMinThreads;
   }

   public int getAcctMaxThreads() {
      return this.acctMaxThreads;
   }

   public void setAcctMaxThreads(int acctMaxThreads) {
      this.acctMaxThreads = acctMaxThreads;
   }

   static {
      try {
         authRespPool = new RadiusAuthRespPool();
      } catch (Exception var1) {
      }

   }
}
