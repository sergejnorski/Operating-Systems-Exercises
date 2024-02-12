package PokerGameProblem;

import FILES.ProblemExecution;
import FILES.TemplateThread;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class Poker {

    public static Semaphore seats;
    public static Semaphore lock;
    public static Semaphore canPlay;
    public static Semaphore newCycle;
    public static int count;
    public static int total;

    private static int groupNo = 0;
    private static int totalNo = 0;

    public static void init() {
        seats = new Semaphore(5);
        lock = new Semaphore(1);
        canPlay = new Semaphore(0);
        newCycle = new Semaphore(0);
        count = 0;
        total = 0;
    }

    public static class Player extends TemplateThread {

        public Player(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            seats.acquire();
            state.playerSeat();

            lock.acquire();
            count++;
            if(count == 5){
                canPlay.release(5);
            }
            lock.release();

            canPlay.acquire();

            state.play();

            lock.acquire();
            count--;
            if(count==0){
                state.endRound();
                total += 5;
                seats.release(5);
            }
            if(total == 15){
                state.endCycle();
                newCycle.release(15);
                total = 0;
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

    static PokerState state = new PokerState();

    public static void run() {
        try {
            int numRuns = 20;
            int numIterations = 15;

            HashSet<Thread> threads = new HashSet<Thread>();

            for (int i = 0; i < numIterations; i++) {
                Player c = new Player(numRuns);
                threads.add(c);
            }

            init();

            ProblemExecution.start(threads, state);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}