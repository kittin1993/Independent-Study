package edu.cmu.cc.warmup.ebs;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xym1993 on 6/15/16.
 */
public class ReadFileThread extends Thread{
    //ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private RandomAccessFile raf;
    private int statusFlag;
    private int blockSize;
    private int readTime;
    private long step;
    //private int testTime;
    private String filename;
    private String fileNamePrefix;
    private FileWriter fstream;
    private LinkedBlockingQueue queue;
    private int threadNum;
    private long eachLength;

    //    private ArrayList<Long> timeSaver;
    private ArrayList<posTimePair> timeSaver;



    public static class posTimePair{
        long pos, time;
        public posTimePair(long posIn, long timeIn)
        {
            pos = posIn;
            time = timeIn;
        }
        public posTimePair(){

        }
        public void put(posTimePair newpair){
            pos = newpair.pos;
            time = newpair.time;
        }
    }

    //Constructor, open the file and get the file length.
    public ReadFileThread(int statusFlag, LinkedBlockingQueue queue, FileWriter fstream, String filename,int blockSize, int readTime, String fileNamePrefix)
    {
        try {
            //this.filename = filename;

            //raf = new RandomAccessFile(filename, "r"); //java.io has this class RandomAccessFile(filename, mode)
            //fileLength = raf.length();
            timeSaver = new ArrayList<>();
           // System.out.println("File length is "+fileLength);
            this.statusFlag = statusFlag;
            this.filename = filename;
            this.queue = queue;
            this.fstream = fstream;
            this.blockSize = blockSize;
            this.readTime = readTime;
            this.fileNamePrefix = fileNamePrefix;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public ReadFileThread(int statusFlag, int threadNum, long eachLength, String filename,int blockSize,long step, String fileNamePrefix)
    {
        try {
            //this.filename = filename;

            //raf = new RandomAccessFile(filename, "r"); //java.io has this class RandomAccessFile(filename, mode)
            //fileLength = raf.length();
            timeSaver = new ArrayList<>();
            // System.out.println("File lenglong startPos,th is "+fileLength);
            this.statusFlag = statusFlag;
            this.threadNum = threadNum;
            this.eachLength = eachLength;
            this.filename = filename;
            //this.queue = queue;
            //this.fstream = fstream;
            this.blockSize = blockSize;
            this.step = step;
            this.fileNamePrefix = fileNamePrefix;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //Read certain blocksize of file from indicated position.
    //Return the execution time in nanoSeconds.
    private long blockReadNanoTime(int readBytes, long position)
    {
        long startTime = 0, endTime = 0;
        try {
            byte[] tempBuffer = new byte[readBytes];
            raf.seek(position); //set the position-pointer offset
            raf.read(tempBuffer); //Reads up to tempBuffer.length bytes of data from this file into an array of bytes.
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return endTime-startTime;
    }

    //Read the file with certain blocksize for each step sequentially.
    public void sequentialReadThrough(long startPos,long eachLength, int blockSize,long step)
    {
        long count;
        count = eachLength/step;

        //System.out.println("count:"+ count);
        long pos = startPos+262144;
        //System.out.println("startPos: "+ startPos);
        //System.out.println("pos: "+ (pos/1048576));
        for( long i=0; i<count; i++ ) {
            timeSaver.add(new posTimePair(pos+i*step, blockReadNanoTime(blockSize, pos+i*step)));
        }
    }

    //Randomly read the file with certain blocksize to readtime.
    public void randomReadThrough(String filename, long fileLength, int blockSize, int readTime)
    {
        Random randomPosGenerator = new Random();
        for(int i=0; i < readTime;i++)
        {
            long pos = Math.abs(randomPosGenerator.nextLong())%(fileLength-blockSize);
            //System.out.println(pos);
            timeSaver.add(new posTimePair(pos,blockReadNanoTime(blockSize, pos)));
        }
    }

    /*print the testName line by line
    public void printEachReadTime()
    {
        for(int i=0; i < timeSaver.size() ; i++ )
        {
            //System.out.println(i);
            System.out.println(timeSaver.get(i).pos + "\t" + timeSaver.get(i).time);
        }
    }
   */
    //Save the test result content to a file
    public void saveLastArrayToFile(String fileNamePrefix)
    {
        try {
            String temp;
            BufferedReader out = null;
            try{
                //FileWriter fstream = new FileWriter(fileName, false);
                for(int i=0; i < timeSaver.size() ; i++ )
                {
                    //System.out.println(i);
                    //rwl1.writeLock().lock();
                    //System.out.println("put data to queue:"+timeSaver.get(i).pos+" "+timeSaver.get(i).time );
                    queue.put(timeSaver.get(i));
                    //fstream.write(timeSaver.get(i).pos + "\t" + timeSaver.get(i).time + "\n");
                    //rwl1.writeLock().unlock();
                }
                //System.out.println("Wrote experiment data to log file: " + fileNamePrefix);
                //fstream.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void seqSaveLastArrayToFile(String fileNamePrefix, int threadNum)
    {
        try {
            String temp;
            BufferedReader out = null;
            try{
                FileWriter fstream = new FileWriter(fileNamePrefix+"_"+threadNum, false);
                for(int i=0; i < timeSaver.size() ; i++ )
                {
                    //System.out.println(i);
                    //System.out.println("put data to queue:"+timeSaver.get(i).pos+" "+timeSaver.get(i).time );
                    //queue.put(timeSaver.get(i));
                    fstream.write((timeSaver.get(i).pos) + "\t" + timeSaver.get(i).time + "\n");
                }
                //System.out.println("Wrote experiment data to log file: " + fileNamePrefix+"_"+threadNum);
                fstream.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //Test multiTimes randomly, the read size if BlockSize, for each test, read readTime times, totally run testTime times.
    //the result will be save to file fileNamePrefix_0 fileNamePrefix_1 ..... fileNamePrefix_(testTime-1)
    public void randomMultiTests(String filename, int blockSize, int readTime, String fileNamePrefix)
    {


            //System.out.println("filename:"+filename);
            long fileLength = 0;
            try{

                raf = new RandomAccessFile(filename, "r");
                fileLength = raf.length();
                System.out.println("File length is "+fileLength);
            }
            catch(Exception e){
                e.printStackTrace();
            }


            randomReadThrough(filename, fileLength, blockSize, readTime);
            saveLastArrayToFile(fileNamePrefix);
            clearArray();
        //}
    }


    //Test multiTimes randomly, the read size if BlockSize, for each test, read readTime times, totally run testTime times.
    //the result will be save to file fileNamePrefix_0 fileNamePrefix_1 ..... fileNamePrefix_(testTime-1)
    public void sequenceMultiTests(String filename, long eachLength, int threadNum, int blockSize, long step, String fileNamePrefix)
    {
        long fileLength = 0;
        long startPos = ((eachLength*threadNum)/1048576)*1048576;
        System.out.println("startPos:"+startPos);

        try{

            raf = new RandomAccessFile(filename, "r");
            //fileLength = raf.length();
            //System.out.println(filename+": File length is "+fileLength);
        }
        catch(Exception e){
            e.printStackTrace();
        }
            System.out.println("eachlength is "+eachLength);
            sequentialReadThrough(startPos, eachLength, blockSize, step);
            //seqSaveLastArrayToFile(fileNamePrefix, threadNum);
            //clearArray();

    }

    //Clear the test result array
    public void clearArray()
    {
        timeSaver.clear();
    }

    //multithread sum of readtime -> total readtime

    public void run(){

        //System.out.println("Run a new thread");
        if (statusFlag == 0){
            randomMultiTests(filename, blockSize, readTime, fileNamePrefix);
        }
        else{
            //System.out.println("seq mode");
            sequenceMultiTests(filename, eachLength, threadNum, blockSize, step, fileNamePrefix);
            //long endTime = System.currentTimeMillis();
            //System.out.println("endTime:"+endTime);
        }
        //rwl.readLock().unlock();
    }

    /*public void start(){

    }*/
}
