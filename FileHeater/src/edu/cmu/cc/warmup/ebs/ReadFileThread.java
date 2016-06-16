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
    ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    ReentrantReadWriteLock rwl1 = new ReentrantReadWriteLock();
    private RandomAccessFile raf;

    private int blockSize;
    private int readTime;
    //private int testTime;
    private String filename;
    private String fileNamePrefix;
    private FileWriter fstream;
    private LinkedBlockingQueue queue;

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
    public ReadFileThread(LinkedBlockingQueue queue, FileWriter fstream, String filename,int blockSize, int readTime, String fileNamePrefix)
    {
        try {
            //this.filename = filename;

            //raf = new RandomAccessFile(filename, "r"); //java.io has this class RandomAccessFile(filename, mode)
            //fileLength = raf.length();
            timeSaver = new ArrayList<>();
           // System.out.println("File length is "+fileLength);
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

    //Read certain blocksize of file from indicated position.
    //Return the execution time in nanoSeconds.
    private long blockReadNanoTime(int readBytes, long position)
    {
        long startTime = 0, endTime = 0;
        try {
            byte[] tempBuffer = new byte[readBytes];
            raf.seek(position); //set the position-pointer offset
            startTime = System.nanoTime();
            rwl.readLock().lock();
            raf.read(tempBuffer); //Reads up to tempBuffer.length bytes of data from this file into an array of bytes.
            rwl.readLock().unlock();
            endTime = System.nanoTime();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return endTime-startTime;
    }

    //Read the file with certain blocksize sequentially. The step is the same as blocksize
    /*
    public void sequentialReadThrough(int blockSize)
    {
        long count;
        count = this.fileLength/blockSize;
        System.out.println(count);
        for( long i=0; i<count; i++ ) {
            timeSaver.add(new posTimePair(i*blockSize, blockReadNanoTime(blockSize, i * blockSize)));
        }
    }

    //Read the file with certain blocksize for each step sequentially.
    public void sequentialReadThrough(int blockSize, long step)
    {
        long count;
        count = this.fileLength/step;
        System.out.println(count);
        for( long i=0; i<count; i++ ) {
            timeSaver.add(new posTimePair(i*step, blockReadNanoTime(blockSize, i*step)));
        }
    }
    */
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

    //Test multiTimes randomly, the read size if BlockSize, for each test, read readTime times, totally run testTime times.
    //the result will be save to file fileNamePrefix_0 fileNamePrefix_1 ..... fileNamePrefix_(testTime-1)
    public void randomMultiTests(String filename, int blockSize, int readTime, String fileNamePrefix)
    {


            //System.out.println("filename:"+filename);
            long fileLength = 10000;
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
    public void sequenceMultiTests(int blockSize, long step, int testTime, String fileNamePrefix)
    {
        for(int i = 0; i < testTime ; i++)
        {
            //sequentialReadThrough(blockSize, step);
            saveLastArrayToFile(fileNamePrefix + "_" + Integer.toString(i));
            clearArray();
        }
    }

    //Clear the test result array
    public void clearArray()
    {
        timeSaver.clear();
    }

    //multithread sum of readtime -> total readtime

    public void run(){

        //System.out.println("Run a new thread");
        randomMultiTests(filename, blockSize, readTime, fileNamePrefix);
        //rwl.readLock().unlock();
    }

    /*public void start(){

    }*/
}
