package Primer1;

import java.util.concurrent.Semaphore;

public class MyThread extends Thread{

    //sakam da imam edna edinstvena promenila za site objekti od ovaa klasa toa go rpavime so static
    public static int count = 0;

    //Ako ima kriticen region sekogas koristime semafor so 1 dozvola
    //Semafor sekogas se pravi static
    public static Semaphore semaphore = new Semaphore(1);

    @Override
    public void run() {
        //kodot tuka sto ke go napisme,ke se izvrsuva za site threadovi

        try {
            //barame dozvola, acquire = wait (od teorija)
            semaphore.acquire();
            for(int i=0;i<100000;i++){
                //kriticen region
                count++;
            }
            //vrati 1 dozvola nazad, release = signal (od teorija)
            semaphore.release();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        //scheduler im dava CPU naizmenicno na threadovi
        MyThread myThread1 = new MyThread();
        MyThread myThread2 = new MyThread();

        //za da go startneme ovoj thread: (vo pozadina se povikuva run metodot)
        myThread1.start();
        myThread2.start();

        //Default na java e deka sekogas ke dodava sama + 1 thread koj sekogas se izvrsvua prv
        //Main thread e sozdaden thread od JAVA i toj sekogas ke se izvssuva prv
        //ako sakame main thread da se izvrsi posleden se povikuva metodot:
        myThread1.join();
        myThread2.join();

        System.out.println(count);
    }
}
