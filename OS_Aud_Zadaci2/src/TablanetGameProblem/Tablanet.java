package TablanetGameProblem;

import FILES.ProblemExecution;
import FILES.TemplateThread;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class Tablanet {

    public static Semaphore seats;
    public static Semaphore lock;
    public static Semaphore canPlay;
    public static Semaphore playerHere;
    public static Semaphore playerFinished;
    public static Semaphore nextCycle;
    public static int count;
    public static int total;

    public static void init() {

        seats = new Semaphore(4);
        lock = new Semaphore(1);
        canPlay = new Semaphore(0);
        playerHere = new Semaphore(0);
        playerFinished = new Semaphore(0);
        nextCycle = new Semaphore(0);
        count = 0;
        total = 0;

    }

    public static class Dealer extends TemplateThread {

        public Dealer(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            playerHere.acquire();

            state.dealCards();

            canPlay.release(4);

            playerFinished.acquire();
            state.nextGroup();
            seats.release(4);
        }
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
            if(count==4){
                playerHere.release();
            }
            lock.release();

            canPlay.acquire();
            state.play();

            lock.acquire();
            count--;
            if(count==0){
                playerFinished.release();
                total += 4;
            }
            if(total == 20){
                total = 0;
                state.endCycle();
                nextCycle.release(20);
            }
            lock.release();

            nextCycle.acquire();

        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    static TablanetState state = new TablanetState();

    public static void run() {
        try {
            int numCycles = 10;
            int numIterations = 20;

            HashSet<Thread> threads = new HashSet<Thread>();

            Dealer d = new Dealer(50);
            threads.add(d);
            for (int i = 0; i < numIterations; i++) {
                Player c = new Player(numCycles);
                threads.add(c);
            }

            init();

            ProblemExecution.start(threads, state);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
