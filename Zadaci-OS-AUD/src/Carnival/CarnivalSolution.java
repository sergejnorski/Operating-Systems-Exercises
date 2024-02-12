package Carnival;

import FILES.TemplateThread;
import FILES.AbstractState;
import FILES.PointsException;
import FILES.ProblemExecution;
import FILES.Switcher;
import FILES.BoundCounterWithRaceConditionCheck;

import java.util.HashSet;
import java.util.concurrent.Semaphore;

/*
Link od zadaca:
https://github.com/ristes/synchronization-examples/tree/master/src/mk/ukim/finki/os/synchronization/exam16/k1/g2
*/

public class CarnivalSolution {

    static Semaphore seats;
    static Semaphore lock;
    static Semaphore canPlay;
    static Semaphore endCycle;

    static int count;
    static int total;

    public static void init() {
        seats = new Semaphore(10);
        lock = new Semaphore(1);
        canPlay = new Semaphore(0);
        endCycle = new Semaphore(0);

        total = 0;
        count = 0;
    }

    public static class Participant extends TemplateThread {

        public Participant(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            seats.acquire();    //dobiva mesto na bina

            state.participantEnter();   //se kacuva na bina

            //kriticen region mora da go zaklucime
            lock.acquire();
            count++;        //zgolemuvame broj na ljuge na bina
            if(count==10){  //proveruvame dali site 10 ljuge se na bina za da mozat da pocnat od ednas
                canPlay.release(10);
            }
            lock.release();

            canPlay.acquire();  //gi blokira bidjeki canPlay e 0 inicjalno ie blokrian se dodeka ne se kacat site na bina, linija 54 go odblokira
            state.present();    //pocnuva celata grupa od 10 ljuge

            lock.acquire();
            count--;        //za sekoj korisnik kako vleguva znaeme deka svirel i se namaluva count do 0
            if(count==0){
                total+=10;
                state.endGroup();   //koga ke zavsat site posledniot korisnik oznacuva deka zavrsila celata grupa
                seats.release(10);  //se osloboduva binata za 10 novi ljuge
            }
            if(total==30){      //proveruva dali site 30 ljuge zavrsile
                total = 0;
                state.endCycle();
                endCycle.release(30);   //gi oslboduva 30te ljuge
            }
            lock.release();

            endCycle.acquire(); // go blokira proceost da ne moze vo naredna grupa pak da odi
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