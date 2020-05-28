package task;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileScanner {
//1.核心线程数，始终运行的线程数量
    //2.最大线程数，有新任务，并且当前运行线程数小于最大线程数，会创建新的线程来处理任务
    //3-4.超过3这个数量，4这个时间单位，2-1（最大线程数-核心线程数）这些线程就会关闭
    //5.工作阻塞队列
    //6.如果超出工作队列的长度，任务要处理的方式
//    private ThreadPoolExecutor POOL=new ThreadPoolExecutor(
//            3,3,0, TimeUnit.MICROSECONDS,
//             new LinkedBlockingDeque<>(),new ThreadPoolExecutor.AbortPolicy()
//    );//之前多线程是快捷创建的方式
    private ExecutorService POOL= Executors.newFixedThreadPool(4);

    private volatile AtomicInteger count=new AtomicInteger();
    private Object lock=new Object();//方法1，synchronized（lock）进行wait等待

    private CountDownLatch latch=new CountDownLatch(1);//方法2：await()阻塞等待直到latch等于0
    private Semaphore semaphore=new Semaphore(0);//方法3，


    private ScanCallback callback;
    public FileScanner(ScanCallback callback) {
        this.callback=callback;
    }

    /**
     * 扫描文件目录，
     * 最开始不知道有多少级目录
     * 不知道启动多少个线程
     * @param path
     */
    public  void scan(String path) {
        count.incrementAndGet();//启动子文件夹计数器扫描任务，++i
       doScan(new File(path));
    }

    /**
     *
     * @param dir
     */
    private void doScan(File dir ){
        POOL.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.callback(dir);//文件保存操作
                    File[] children = dir.listFiles();
                    if (children != null) {
                        for (File child : children) {
                            if (child.isDirectory()) {
                              //  System.out.println("文件夹：" + child.getPath());
                                count.incrementAndGet();//启动子文件夹计数器扫描任务，++
                                System.out.println("当前任务数："+count.get());
                                doScan(child);
                          }
//                            else {
//                                //TODO
//                                //System.out.println("文件：" + child.getPath());
//                            }
                        }
                    }
                }finally {
                    int r=count.decrementAndGet();
                    if(r==0){
                        //方法1
//                        synchronized (lock){
//                           lock.notify();
//                        }
                        //方法2
                        //latch.countDown();
                        //方法3
                        semaphore.release();
                    }
                }
            }
        });

    }
    /**
     * 等待扫描任务结束scan();
     * 多线程的任务 等待：thread.start();
     * 1.jion():需要使用线程Thread类的引用对象
     * 2.wait()线程间的等待，
     */
    public void waitFinsh() throws InterruptedException {
// 方法1
//   synchronized (lock){
//            lock.wait();
//        }
        try {
            //latch.await();
            semaphore.acquire();
        } finally {

            shutdown();//阻塞等待，直到任务完成，完成后需要关闭线程池
        }
    }
    public void shutdown(){
        System.out.println("关闭线程池……");
        //POOL.shutdown();//内部实现原理是通过内部Thread.interruot()来中断
       POOL.shutdownNow();//内部实现原理是通过内部Thread.stop()来中断
    }
    public static void main(String[] args) throws InterruptedException {

        Object obj=new Object();
        Thread t2=new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());
                synchronized (obj) {
                    obj.notify();
                }
            }
        });
        t2.start();
        synchronized (obj){
            obj.wait();
        }
        //把jion方法改造成线程等待，改为使用wait()实现
        System.out.println(Thread.currentThread().getName());
    }
}
