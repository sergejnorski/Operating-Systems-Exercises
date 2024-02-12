package CarnivalProblem;

import FILES.ProblemExecution;
import FILES.TemplateThread;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class CarnivalSolution {

    public static Semaphore seats;
    public static Semaphore lock;
    public static Semaphore canPlay;
    public static Semaphore newCycle;
    public static int count;
    public static int total;

    private static int groupNo = 0;
    private static int totalNo = 0;

    public static void init() {

        seats = new Semaphore(10);
        lock = new Semaphore(1);
        canPlay = new Semaphore(0);
        newCycle = new Semaphore(0);
        count = 0;
        total = 0;

    }

    public static class Participant extends TemplateThread {

        public Participant(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            seats.acquire();
            state.participantEnter();

            lock.acquire();
            count++;
            if(count == 10){
                canPlay.release(10);
            }
            lock.release();

            canPlay.acquire();

            state.present();

            lock.acquire();
            count--;
            if(count == 0){
                state.endGroup();
                total += 10;
                seats.release(10);
            }
            if(total == 30){
                total = 0;
                state.endCycle();
                newCycle.release(30);
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

    static CarnivalState state = new CarnivalState();

    public static void run() {
        try {
            int numRuns = 15;
            int numThreads = 30;

            HashSet<Thread> threads = new HashSet<Thread>();

            for (int i = 0; i < numThreads; i++) {
                Participant c = new Participant(numRuns);
                threads.add(c);
            }

            init();

            ProblemExecution.start(threads, state);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
