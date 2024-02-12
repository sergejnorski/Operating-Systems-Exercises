package CalciumNitride;

import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/*
Link do zadaca:
https://github.com/ristes/synchronization-examples/tree/master/src/mk/ukim/finki/os/synchronization/exam14/march/ca3n2
*/

public class CalciumNitride {
    static Semaphore ca;
    static Semaphore n;
    static Semaphore lock;
    static Semaphore nHere;
    static Semaphore ready;
    static Semaphore bondingDone;
    static Semaphore canLeave;

    static int count;

    public static void init() {
        //so 3 i 2 dozovli zasto ni trebaat po 3 i 2 atomi
        ca = new Semaphore(3);
        n = new Semaphore(2);
        lock = new Semaphore(1);
        nHere = new Semaphore(0);
        ready = new Semaphore(0);
        bondingDone = new Semaphore(0);
        canLeave = new Semaphore(0);

        count = 0;
    }

    public static class Calcium extends TemplateThread {

        public Calcium(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            ca.acquire();
            lock.acquire();
            count++;
            if(count==3){
                //tuka vleguva posledniot atom na CA odnosno KORDINATOROT
                //ako vlezeme vo IF znaeme deka site 3 atomi na CA se tuka
                count=0;
                lock.release();
                nHere.acquire(2);
                ready.release(4);
                state.bond();
                bondingDone.acquire(4);
                state.validate();
                canLeave.release(4);
                ca.release();
            }
            else {
                //tuka vleguvaat prvite 2 atoma na CA
                lock.release();
                ready.acquire();
                state.bond();
                bondingDone.release();
                canLeave.acquire();
                ca.release();
            }
        }

    }

    public static class Nitrogen extends TemplateThread {

        public Nitrogen(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            n.acquire();
            nHere.release();
            ready.acquire();
            state.bond();
            bondingDone.release();
            canLeave.acquire();
            n.release();
        }

    }

    static CalciumNitrideState state = new CalciumNitrideState();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    public static void run() {
        try {
            Scanner s = new Scanner(System.in);
            int numRuns = 1;
            int numIterations = 100;
            s.close();

            HashSet<Thread> threads = new HashSet<Thread>();

            for (int i = 0; i < numIterations; i++) {
                Nitrogen n = new Nitrogen(numRuns);
                threads.add(n);
                Calcium ca = new Calcium(numRuns);
                threads.add(ca);
                ca = new Calcium(numRuns);
                threads.add(ca);
                n = new Nitrogen(numRuns);
                threads.add(n);
                ca = new Calcium(numRuns);
                threads.add(ca);
            }

            init();

            ProblemExecution.start(threads, state);
            System.out.println(new Date().getTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}