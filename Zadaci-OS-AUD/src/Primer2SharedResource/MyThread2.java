package Primer2SharedResource;

import java.util.concurrent.Semaphore;

public class MyThread2 extends  Thread{

    //3 nacini na pravenje na Shared Resource:
    //1. static int kaj Threads
    //2. Primer2SharedResource.SharedResource klasa so non-static int, istiot objekt go prakjate to site threads
    //3. razlicni Primer2SharedResource.SharedResource objekti so static int

    private SharedResource sharedResource;
    private static Semaphore semaphore = new Semaphore(1);

    public MyThread2(SharedResource sharedResource){
        this.sharedResource = sharedResource;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            for(int i=0;i<100000;i++){
                sharedResource.count++;
            }
            semaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SharedResource sharedResource = new SharedResource();
        MyThread2 myThread1 = new MyThread2(sharedResource);
        MyThread2 myThread2 = new MyThread2(sharedResource);

        myThread1.start();
        myThread2.start();

        myThread1.join();
        myThread2.join();

        System.out.println(sharedResource.count);
    }
}
