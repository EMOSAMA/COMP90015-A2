package COMM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ThreadPool<Job extends Runnable>{

    // max mun of workers
    private static final int MAX_WORKER_NUMBERS = 15;
    // default num of workers
    private static final int DEFAULT_WORKER_NUMBERS = 5;
    // min num of workers
    private static final int MIN_WORKER_NUMBERS = 1;
    // list of jobs
    private final LinkedList<Job> jobs = new LinkedList<Job>();
    // list of workers
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());
    // num of worker
    private int workerNum;
    // create id for each thread
    //private AtomicLong threadNum = new AtomicLong();

    private Boolean isScalable = false;
    private int scalaUpValue = 0;
    private int scalaDownValue = 0;

    public void autoScalaThreadPool(int scalableUpValue,int scalableDownValue){
        this.isScalable = true;
        this.scalaUpValue = scalableUpValue;
        this.scalaDownValue = scalableDownValue;
    }
    public void stopAutoScalaThreadPool(){
        this.isScalable = false;
    }

    //constructor
    public ThreadPool() {
        this.workerNum = DEFAULT_WORKER_NUMBERS;
        initializeWorkers(this.workerNum);
    }

    public ThreadPool(int num) {
        if (num > MAX_WORKER_NUMBERS) {
            this.workerNum =DEFAULT_WORKER_NUMBERS;
        } else {
            this.workerNum = num;
        }
        initializeWorkers(this.workerNum);
    }

    //init thread pool
    private void initializeWorkers(int num) {
        for (int i = 0; i < num; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            Thread thread = new Thread(worker);
            thread.start();
        }
    }

    public void execute(Job job) {
        if (job != null) {
            synchronized (jobs) {
                jobs.addLast(job);
                jobs.notify();
                if(this.isScalable){
                    if(jobs.size()> scalaUpValue){
                        System.out.println("Current num of jobs is: "+jobs.size());
                        if(workerNum < MAX_WORKER_NUMBERS){
                            System.out.println("Scala up 1 thread into pool!");
                            addWorkers(1);
                        }else{
                            System.out.println("The System is on high loan! And the num of thread reach the max num!");
                        }
                    }else if(jobs.size() < scalaDownValue){
                        if(workerNum>DEFAULT_WORKER_NUMBERS){
                            System.out.println("Current num of jobs is: "+jobs.size());
                            removeWorker(1);
                            System.out.println("Scala down 1 thread from pool!");
                            System.out.println("Current num of thread is: "+workerNum);
                        }
                    }
                }
            }
        }
    }
    //close thread pool
    public void shutdown() {
        for (Worker w : workers) {
            w.shutdown();
        }
    }
    //add worker thread
    public void addWorkers(int num) {
        synchronized (jobs) {
            if (num + this.workerNum > MAX_WORKER_NUMBERS) {
                num = MAX_WORKER_NUMBERS - this.workerNum;
            }
            initializeWorkers(num);
            this.workerNum += num;
        }
    }
    //reduce worker thread
    public void removeWorker(int num) {
        synchronized (jobs) {
            if(num>=this.workerNum){
                throw new IllegalArgumentException("over the total num of current workers");
            }
            for (int i = 0; i < num; i++) {
                Worker worker = workers.get(i);
                if (worker != null) {
                    worker.shutdown();
                    workers.remove(i);
                }
            }
            this.workerNum -= num;
        }
    }

    public int getJobSize() {
        return workers.size();
    }
    class Worker implements Runnable {
        private volatile boolean running = true;

        public void run() {
            while (running) {
                Job job = null;
                synchronized (jobs) {
                    if (jobs.isEmpty()) {
                        try {
                            jobs.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    //job = jobs.getFirst();
                    job = jobs.removeFirst();
                }
                if (job != null) {
                    job.run();
                }
            }
        }
        public void shutdown() {
            running = false;
        }
    }
}
