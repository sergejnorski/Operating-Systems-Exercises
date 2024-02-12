package Kindergardten;

import FILES.TemplateThread;
import FILES.AbstractState;
import FILES.PointsException;
import FILES.ProblemExecution;
import FILES.Switcher;
import FILES.BoundCounterWithRaceConditionCheck;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

/*
Link od zadacu:
https://github.com/ristes/synchronization-examples/tree/master/src/mk/ukim/finki/os/synchronization/exam16/k1/g1
*/

public class KindergartenShow {
    //Ako sakame da blokirame -> semafor so 0 dozvoli
    //Ako sakame da zastitme kriticen region -> semafor so 1 dozvola

    static Semaphore seats;
    static Semaphore lock;
    static Semaphore canPlay;   //ne dozvoluvame da prezentiraat decata se dodeka ne se kacat site na bina
    static Semaphore newCycle;

    static int count;   //broime kolku deca ima na bina
    static int total;

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
            if (count==6) {
                canPlay.release(6);
            }
            lock.release();

            canPlay.acquire();  //gi blokira site se dodeka ne se kacat 6 deca na bina
            state.present();

            lock.acquire();
            count--;
            if (count==0){
                state.endGroup();
                seats.release(6);
                total+=6;
            }
            if(total==24){
                total=0;
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