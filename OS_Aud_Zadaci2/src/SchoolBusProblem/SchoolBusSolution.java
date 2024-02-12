package SchoolBusProblem;

import FILES.ProblemExecution;
import FILES.TemplateThread;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

public class SchoolBusSolution {

    public static Semaphore studentSeats;
    public static Semaphore driverSeats;
    public static Semaphore lock;
    public static Semaphore driverCanDepart;
    public static Semaphore busArrive;
    public static Semaphore driverCanLeave;
    public static int count;

    public static void init() {
        studentSeats = new Semaphore(0);
        driverSeats = new Semaphore(1);
        lock = new Semaphore(1);
        driverCanDepart = new Semaphore(0);
        busArrive = new Semaphore(0);
        driverCanLeave = new Semaphore(0);
        count = 0;

    }

    public static class Driver extends TemplateThread {

        public Driver(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            driverSeats.acquire();
            state.driverEnter();

            studentSeats.release(50);

            driverCanDepart.acquire();

            state.busDeparture();
            state.busArrive();

            busArrive.release(50);
            driverCanLeave.acquire(1);

            state.driverLeave();

            driverSeats.release(1);
        }
    }

    public static class Student extends TemplateThread {

        public Student(int numRuns) {
            super(numRuns);
        }

        @Override
        public void execute() throws InterruptedException {
            studentSeats.acquire();
            state.studentEnter();

            lock.acquire();
            count++;
            if(count == 50){
                driverCanDepart.release(1);
            }
            lock.release();

            busArrive.acquire();

            state.studentLeave();

            lock.acquire();
            count--;
            if(count == 0){
                driverCanLeave.release(1);
            }
            lock.release();

        }
    }
    static SchoolBusState state = new SchoolBusState();

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            run();
        }
    }

    public static void run() {
        try {
            int numRuns = 1;
            int numScenarios = 1000;
            HashSet<Thread> threads = new HashSet<Thread>();

            for (int i = 0; i < numScenarios; i++) {
                Student p = new Student(numRuns);
                threads.add(p);
                if (i % 50 == 0) {
                    Driver c = new Driver(numRuns);
                    threads.add(c);
                }
            }

            init();

            ProblemExecution.start(threads, state);
            System.out.println(new Date().getTime());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
