package edu.cmu.cc.warmup.ebs;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by tianhaowang on 5/31/16.
 */
public class SingleThreadFileHeater {

    private RandomAccessFile raf;
    private long fileLength;
//    private ArrayList<Long> timeSaver;
    private ArrayList<posTimePair> timeSaver;
    public class posTimePair{
        long pos, time;
        public posTimePair(long posIn, long timeIn)
        {
            pos = posIn;
            time = timeIn;
        }
    }

    //Constructor, open the file and get the file length.
    public SingleThreadFileHeater(String filename)
    {
        try {
            raf = new RandomAccessFile(filename, "r"); //java.io has this class RandomAccessFile(filename, mode)
            fileLength = raf.length();
            timeSaver = new ArrayList<>();
            System.out.println("File length is "+fileLength);
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
            raf.read(tempBuffer); //Reads up to tempBuffer.length bytes of data from this file into an array of bytes.
            endTime = System.nanoTime();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return endTime-startTime;
    }

    //Read the file with certain blocksize sequentially. The step is the same as blocksize
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

    //Randomly read the file with certain blocksize to readtime.
    public void randomReadThrough(int blockSize, int readTime)
    {
        Random randomPosGenerator = new Random();
        for(int i=0; i < readTime;i++)
        {
            long pos = Math.abs(randomPosGenerator.nextLong())%(fileLength-blockSize);
            //System.out.println(pos);
            timeSaver.add(new posTimePair(pos,blockReadNanoTime(blockSize, pos)));
        }
    }

    //print the testName line by line
    public void printEachReadTime()
    {
        for(int i=0; i < timeSaver.size() ; i++ )
        {
            //System.out.println(i);
            System.out.println(timeSaver.get(i).pos + "\t" + timeSaver.get(i).time);
        }
    }

    //Save the test result content to a file
    public void saveLastArrayToFile(String fileName)
    {
        try {
            String temp;
            BufferedReader out = null;
            try{
                FileWriter fstream = new FileWriter(fileName, false);
                for(int i=0; i < timeSaver.size() ; i++ )
                {
                    //System.out.println(i);
                    fstream.write(timeSaver.get(i).pos + "\t" + timeSaver.get(i).time + "\n");
                }
                //System.out.println("Wrote experiment data to log file: "+ fileName);
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
    public void randomMultiTests(int blockSize, int readTime, int testTime, String fileNamePrefix)
    {
        for(int i = 0; i < testTime ; i++)
        {
            randomReadThrough(blockSize, readTime);
            saveLastArrayToFile(fileNamePrefix + "_" + Integer.toString(i));
            clearArray();
        }
    }


    //Test multiTimes randomly, the read size if BlockSize, for each test, read readTime times, totally run testTime times.
    //the result will be save to file fileNamePrefix_0 fileNamePrefix_1 ..... fileNamePrefix_(testTime-1)
    public void sequenceMultiTests(int blockSize, long step, int testTime, String fileNamePrefix)
    {
        for(int i = 0; i < testTime ; i++)
        {
            sequentialReadThrough(blockSize, step);
            saveLastArrayToFile(fileNamePrefix + "_" + Integer.toString(i));
            clearArray();
        }
    }

    //Clear the test result array
    public void clearArray()
    {
        timeSaver.clear();
    }

    // Argument: filename rand/seq blockSize step/readTimes testTimes saveFileNamePrefix
    public static void main(String args[])
    {
        try{
            SingleThreadFileHeater newFile = new SingleThreadFileHeater(args[0]);
            if(args[1].equals("rand")){
                newFile.randomMultiTests(Integer.parseInt(args[2]), Integer.parseInt(args[3]),
                        Integer.parseInt(args[4]), args[5]);
            }
            else if(args[1].equals("seq"))
            {
                newFile.sequenceMultiTests(Integer.parseInt(args[2]), Long.parseLong(args[3]),
                        Integer.parseInt(args[4]), args[5]);
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
