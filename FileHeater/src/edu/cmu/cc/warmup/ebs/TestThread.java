package edu.cmu.cc.warmup.ebs;

import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * Created by xym1993 on 6/15/16.
 */
public class TestThread {

    public static void main(String args[]) {

        //Argument: filename thread_num rand/seq blockSize step/readTimes saveFileNamePrefix

        //set loop to create new thread(pass the parameter) and start thread
       /*
        ReadFileThread r1 = new ReadFileThread( "Thread-1");
        r1.start();
        ReadFileThread r2 = new ReadFileThread( "Thread-2");
        r2.start();
        */
        try{

            if(args[2].equals("rand")){

                // Multiple thread
                String out_file = args[5];
                int readTime = Integer.parseInt(args[4]);
                FileWriter fstream = new FileWriter(out_file, false);
                int thread_num = Integer.parseInt(args[1]);
                System.out.println("thread Number:"+thread_num);
                // Create an arraylist
                ArrayList<LinkedBlockingQueue> queueList = new ArrayList<LinkedBlockingQueue>();
                for (int j= 1; j<= thread_num; j++){
                    System.out.println("Create thread"+j);
                    LinkedBlockingQueue queue = new LinkedBlockingQueue();
                    queueList.add(queue);
                    ReadFileThread thread = new ReadFileThread(queue, fstream, args[0], Integer.parseInt(args[3]), readTime,
                            out_file);
                    thread.start();
                    //executor.execute(thread);

                }
                for (int m=0; m<queueList.size(); m++){
                    int items_num = 1;
                    while (items_num <= readTime){
                        //System.out.println("Items Number:"+items_num);
                        ReadFileThread.posTimePair newpair = new ReadFileThread.posTimePair();
                        newpair.put((ReadFileThread.posTimePair) queueList.get(m).take());
                        fstream.write(newpair.pos + "\t" + newpair.time + "\n");
                        items_num++;
                    }
                }
                //System.out.println("Finished");
                fstream.close();


                // One blocking queue
                /*
                LinkedBlockingQueue queue = new LinkedBlockingQueue();
                FileWriter fstream = new FileWriter(args[5], false);
                int thread_num = Integer.parseInt(args[1]);
                //ExecutorService executor = Executors.newFixedThreadPool(thread_num);

                for (int j=0; j< thread_num; j++){
                    System.out.println("Create thread"+j);
                    ReadFileThread thread = new ReadFileThread(queue, fstream, args[0], Integer.parseInt(args[3]), Integer.parseInt(args[4]),
                            args[5]);
                    thread.start();
                    //executor.execute(thread);


                }

                System.out.println("Wrote experiment data to log file: " + args[6]);
                int items_num = 1;
                int total_num = Integer.parseInt(args[4])*thread_num;
                System.out.println("total num:"+total_num);

                while(items_num<=total_num){
                    System.out.println("Items Number:"+items_num);
                    ReadFileThread.posTimePair newpair = new ReadFileThread.posTimePair();
                    newpair.put((ReadFileThread.posTimePair) queue.poll(5, TimeUnit.MILLISECONDS));
                    fstream.write(newpair.pos + "\t" + newpair.time + "\n");
                    items_num++;


                }
                System.out.println("Items Number:"+items_num);
                fstream.close();
             */
            }

            else if(args[2].equals("seq"))
            {
                LinkedBlockingQueue queue = new LinkedBlockingQueue();
                FileWriter fstream = new FileWriter(args[1], false);
                int thread_num = Integer.parseInt(args[0]);
                for (int j=0; j< thread_num; j++){
                    ReadFileThread thread = new ReadFileThread(queue, fstream, args[0], Integer.parseInt(args[3]), Integer.parseInt(args[4]),
                            args[5]);
                    thread.start();
                    /*
                    newFile.randomMultiTests(Integer.parseInt(args[2]), Integer.parseInt(args[3]),
                            Integer.parseInt(args[4]), args[5]);
                            */
                }
            }
            else
            {
                System.out.println("Only Support rand/seq mode. Check your input");
            }
            System.out.println("Finished");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }



}
