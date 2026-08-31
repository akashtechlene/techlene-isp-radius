package org.tinyradius.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import jersey.repackaged.com.google.common.util.concurrent.ThreadFactoryBuilder;
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonThreadPool {
   private static CommonThreadPool commonPool = null;
   private ExecutorService executor;
   private Logger logger = LoggerFactory.getLogger("radiuslog");
   private int minThread = 10;
   private int maxThread = 100;
   private int threadBenchTime = 0;
   private int sleepTime = 50;

   public static CommonThreadPool getCommonPool() {
      return commonPool;
   }

   public static void setCommonPool(CommonThreadPool commonPool) {
      CommonThreadPool.commonPool = commonPool;
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

   public ExecutorService getExecutor() {
      if (this.executor == null) {
         this.logger.info("CommonThreadPool Initialized: min:{},max:{}", this.minThread, this.maxThread);
         this.executor = new ThreadPoolExecutor(this.minThread, this.maxThread, (long)this.threadBenchTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), (new ThreadFactoryBuilder()).setNameFormat("CommonThreadPool-%d").build());
      }

      return this.executor;
   }

   public static CommonThreadPool getInstance() {
      return commonPool;
   }

   public int getThreadBenchTime() {
      return this.threadBenchTime;
   }

   public void setThreadBenchTime(int threadBenchTime) {
      this.threadBenchTime = threadBenchTime;
   }

   public int getSleepTime() {
      return this.sleepTime;
   }

   public void setSleepTime(int sleepTime) {
      this.sleepTime = sleepTime;
   }

   static {
      try {
         commonPool = new CommonThreadPool();
      } catch (Exception var1) {
      }

   }
}
