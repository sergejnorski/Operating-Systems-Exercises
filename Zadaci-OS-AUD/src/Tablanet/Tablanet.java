package Tablanet;

import FILES.TemplateThread;
import FILES.ProblemExecution;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

/*
Link od zadaca:
https://github.com/ristes/synchronization-examples/tree/master/src/mk/ukim/finki/os/synchronization/exam16/k1/g4
*/

public class Tablanet {
    static Semaphore seats;
    static Semaphore lock;
    static Semaphore canPlay;
    static Semaphore playerReady;
    static Semaphore newCycle;
    static Semaphore playerFinished;

    static int count;
    static int total;

    public static void init() {
        seats = new Semaphore(4);
        lock = new Semaphore(1);
        canPlay = new Semaphore(0);
        playerReady = new Semaphore(0);
        newCycle = new Semaphore(0);
        playerFinished = new Semaphore(0);

        count = 0;
        total = 0;
    }

    public static class Dealer extends TemplateThread {

        public Dealer(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            playerReady.acquire();

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
                playerReady.release();
            }
            lock.release();

            canPlay.acquire();

            state.play();

            lock.acquire();
            count--;
            if(count==0){
                playerFinished.release();
                total+=4;
            }
            if(total==20){
                state.endCycle();
                newCycle.release(20);
                total=0;
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