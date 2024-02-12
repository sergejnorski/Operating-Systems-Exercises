package KindergartenShowProblem;

import FILES.ProblemExecution;
import FILES.TemplateThread;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class KindergartenShow {

    public static Semaphore seats;
    public static Semaphore lock;
    public static Semaphore canPlay;
    public static Semaphore newCycle;
    public static int count;
    public static int total;

    private static int sumPermits = 0;
    private static int numExecutions = 0;
    private static int sumQueue = 0;

    public static void init() {

        seats = new Semaphore(6);
        lock = new Semaphore(1);
        canPlay = new Semaphore(0);
        newCycle = new Semaphore(0);
        count = 0;
        total = 0;

    }

    public static class Child extends TemplateThread {

        public Child(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            seats.acquire();
            state.participantEnter();

            lock.acquire();
            count++;
            if(count == 6){
                canPlay.release(6);
            }
            lock.release();

            canPlay.acquire();
            state.present();

            lock.acquire();
            count--;
            if(count == 0){
                state.endGroup();
                seats.release(6);
                total += 6;
            }
            if(total == 24){
                total = 0;
                state.endCycle();
                newCycle.release(24);
            }
            lock.release();

            newCycle.acquire();

        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    static KindergartenShowState state = new KindergartenShowState();

    public static void run() {
        try {
            int numRuns = 24;
            int numIterations = 24;
            numExecutions = 0;
            sumPermits = 0;
            sumQueue = 0;

            HashSet<Thread> threads = new HashSet<Thread>();

            for (int i = 0; i < numIterations; i++) {
                Child c = new Child(numRuns);
                threads.add(c);
            }

            init();

            ProblemExecution.start(threads, state);
            System.out.println(((double) sumPermits) / numExecutions);
            System.out.println(((double) sumQueue) / numExecutions);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
