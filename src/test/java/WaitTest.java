import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class WaitTest {
    /**
     * 待定所有线程执行完毕
     *1.countDownLatch 初始化一个数值，可以对数值进行i--操作，await()会阻塞并等待i=0，
     * 2.Semaphone  release()进行一定数量许可的颁发，acquire进行阻塞操作
     * @param args
     */
    private static int COUNT=5;
    private static CountDownLatch LATCH=new CountDownLatch(COUNT);
    private static Semaphore SEMAPHORE=new Semaphore(0);

    public static void main(String[] args) throws InterruptedException {
        for(int i=0;i<COUNT;i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName());
                    //LATCH.countDown();//i--
                    SEMAPHORE.release();//颁发一定数量的许可证
                }
            }).start();
        }
        //LATCH.await();//await()会阻塞并一直等待，直到LATCH=0
        SEMAPHORE.acquire(5);//无参代表请求资源数量为1，也可以请求指定资源数量
        System.out.println(Thread.currentThread().getName());
    }
}
