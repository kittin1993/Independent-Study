package edu.cmu.cc.warmup.ebs;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
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

        //Argument: filename thread_num rand/seq blockSize step/readTimes saveFileName

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
                int statusFlag = 0;
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
                    ReadFileThread thread = new ReadFileThread(statusFlag, queue, fstream, args[0], Integer.parseInt(args[3]), readTime,
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

            }

            //Arguments: filename totalthread seq/rand blockSize step outfile startPos endPos
            else if(args[2].equals("seq"))
            {
                // Multiple thread
                //long startTime = System.currentTimeMillis();
                //System.out.println("startTime:"+startTime);

                int statusFlag = 1;
                String filename = args[0];
                int totalThread = Integer.parseInt(args[1]);


                RandomAccessFile raf = new RandomAccessFile(filename, "r"); //java.io has this class RandomAccessFile(filename, mode)
                long fileLength = raf.length();
                long eachLength = fileLength/totalThread;

                //System.out.println("first eachlength:"+eachLength);
                int blockSize = Integer.parseInt(args[3])*1024;
                long step = Long.parseLong(args[4])*1024;
                String out_file = args[5];
                long startPos = Long.parseLong(args[6])*1024;
                long endPos = Long.parseLong(args[7])*1024;
                //FileWriter fstream = new FileWriter(out_file, false);

                System.out.println("thread Number:"+totalThread);
                // Create an arraylist
                //ArrayList<LinkedBlockingQueue> queueList = new ArrayList<LinkedBlockingQueue>();
                for (int j= 0; j< totalThread; j++){

                    ReadFileThread thread = new ReadFileThread(statusFlag, j, eachLength,filename,blockSize, step,
                            out_file);
                    thread.start();

                }

                //System.out.println("time:"+(endTime-startTime));
                /*
                long count = eachLength/step;
                for (int m=0; m<queueList.size(); m++){
                    int items_num = 1;
                    while (items_num <= count){
                        //System.out.println("Items Number:"+items_num);
                        ReadFileThread.posTimePair newpair = new ReadFileThread.posTimePair();
                        newpair.put((ReadFileThread.posTimePair) queueList.get(m).take());
                        fstream.write((newpair.pos/1024) + "\t" + newpair.time + "\n");
                        items_num++;
                    }
                }
                //System.out.println("Finished");
                fstream.close();
                */
            }
            else
            {
                System.out.println("Only Support rand/seq mode. Check your input");
            }
            //System.out.println("Finished");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }



}
