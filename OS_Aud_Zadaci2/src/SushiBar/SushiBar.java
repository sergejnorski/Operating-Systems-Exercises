package SushiBar;

import FILES.ProblemExecution;
import FILES.TemplateThread;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class SushiBar {

    public static Semaphore seats;
    public static Semaphore lock;
    public static Semaphore canEat;
    public static Semaphore eatingDone;
    public static int count;

    public static void init() {
        seats = new Semaphore(6);
        lock = new Semaphore(1);
        canEat = new Semaphore(0);
        eatingDone = new Semaphore(0);
        count = 0;
    }


    public static class Customer extends TemplateThread {

        public Customer(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            seats.acquire();
            state.customerSeat();

            lock.acquire();
            count++;
            if(count == 6){
                state.callWaiter();
                canEat.release(6);
            }
            lock.release();

            canEat.acquire();

            state.customerEat();

            lock.acquire();
            count--;
            if(count==0){
                state.eatingDone();
                eatingDone.release(6);
                seats.release(6);
            }
            lock.release();

            eatingDone.acquire();

        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    static SushiBarState state = new SushiBarState();

    public static void run() {
        try {
            int numRuns = 1;
            int numIterations = 1200;

            HashSet<Thread> threads = new HashSet<Thread>();

            for (int i = 0; i < numIterations; i++) {
                Customer c = new Customer(numRuns);
                threads.add(c);
            }

            init();

            ProblemExecution.start(threads, state);
            // System.out.println(new Date().getTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}