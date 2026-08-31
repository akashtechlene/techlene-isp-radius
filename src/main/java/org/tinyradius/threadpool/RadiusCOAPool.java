package org.tinyradius.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusCOAPool {
   private static RadiusCOAPool coaPool = null;
   private ExecutorService coaExecutor;
   private Logger logger = LoggerFactory.getLogger("coalog");
   private int coaMinThread = 10;
   private int coaMaxThread = 100;
   private int threadBenchTime = 0;
   private int coaSleepTime = 50;

   public static RadiusCOAPool getCoaPool() {
      return coaPool;
   }

   public static void setCoaPool(RadiusCOAPool coaPool) {
      RadiusCOAPool.coaPool = coaPool;
   }

   public int getCoaMinThread() {
      return this.coaMinThread;
   }

   public void setCoaMinThread(int coaMinThread) {
      this.coaMinThread = coaMinThread;
   }

   public int getCoaMaxThread() {
      return this.coaMaxThread;
   }

   public void setCoaMaxThread(int coaMaxThread) {
      this.coaMaxThread = coaMaxThread;
   }

   public ExecutorService getCOAExecutor() {
      if (this.coaExecutor == null) {
         this.logger.info("RadiusCOAPool Initialized: min:" + this.coaMinThread + ",Max:" + this.coaMaxThread);
         this.coaExecutor = new ThreadPoolExecutor(this.coaMinThread, this.coaMaxThread, (long)this.threadBenchTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), (new ThreadFactoryBuilder()).setNameFormat("RadiusCOAPool-%d").build());
      }

      return this.coaExecutor;
   }

   public static RadiusCOAPool getCoaRespPool() {
      return coaPool;
   }

   public int getThreadBenchTime() {
      return this.threadBenchTime;
   }

   public void setThreadBenchTime(int threadBenchTime) {
      this.threadBenchTime = threadBenchTime;
   }

   public int getCoaSleepTime() {
      return this.coaSleepTime;
   }

   public void setCoaSleepTime(int coaSleepTime) {
      this.coaSleepTime = coaSleepTime;
   }

   static {
      try {
         coaPool = new RadiusCOAPool();
      } catch (Exception var1) {
      }

   }
}
