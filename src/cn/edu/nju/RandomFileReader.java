package cn.edu.nju;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Read several files at the same time. The readNext method provides the same line of each file.
 */
public class RandomFileReader {
    public String[] fileNameList;
    public BufferedReader[] readerList;
    public int currentLine; //this is not the index of the array, but the actual line number, so it starts at 1.
    public int totalLine;
    public String[] currentContent;

    public final int maximumCachedLines = 1000;
    public boolean allCached = false;
    public String[][] cachedLines = null;

    boolean loop = false;
    public static final int oneLineCertificate = 1231;

    /**
     * Initialized by giving the list of file names to read.
     */
    public RandomFileReader(String[] fileNameList) {
        this.fileNameList = fileNameList;
        currentLine = 0;
        totalLine = countLineNumber();
        if (totalLine < maximumCachedLines) {
            readInAllLines();
            allCached = true;
        } else
            openFile();
    }

    /**
     * Initialized by giving the list of file names to read.
     */
    public RandomFileReader(String[] fileNameList, boolean loop) {
        this.fileNameList = fileNameList;
        currentLine = 0;
        totalLine = countLineNumber();
        if (totalLine < maximumCachedLines) {
            readInAllLines();
            allCached = true;
        } else
            openFile();
        this.loop = loop;
    }

    /**
     * this is a bad initialization...
     */
    public RandomFileReader(String[] oneLine, int oneLineCertificate) {
        if (oneLineCertificate == RandomFileReader.oneLineCertificate) {
            currentLine = 1;
            totalLine = 1;
            currentContent = oneLine;
            cachedLines = new String[1][];
            cachedLines[0] = currentContent;
        }
    }

    /**
     * Count the number of lines for each file. The number is used for ramdom access of the file.
     */
    protected int countLineNumber() {
        int count = 0;
        openFile();
        try {
            while (true) {
                boolean end = false;
                for (int i = 0; i < readerList.length; i++) {
                    String content = readerList[i].readLine();
                    if (content == null)
                        end = true;
                }
                if (end)
                    break;
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeFile();
        return count;
    }

    /**
     * Open several files at the same time.
     */
    protected void openFile() {
        readerList = new BufferedReader[fileNameList.length];
        try {
            for (int i = 0; i < readerList.length; i++) {
                readerList[i] = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameList[i]), "UTF-8"));
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentLine = 0;
    }

    protected void closeFile() {
        if (readerList == null)
            return;
        try {
            for (int i = 0; i < readerList.length; i++) {
                readerList[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read all lines into a string array. This is used when the file is small.
     */
    protected void readInAllLines() {
        openFile();
        cachedLines = new String[totalLine][readerList.length];
        try {
            int count = 0;
            while (true) {
                String[] content = new String[readerList.length];
                boolean end = false;
                for (int i = 0; i < readerList.length; i++) {
                    content[i] = readerList[i].readLine();
                    if (content[i] == null)
                        end = true;
                }
                if (end)
                    break;
                cachedLines[count] = content;
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeFile();
    }

    /**
     * get the immediate next lines of each file.
     */

    public String[] readNext() {
        if (currentLine == totalLine)//already last line
        {
            return null;
        }
        if (allCached) {
            currentLine++;
            currentContent = cachedLines[currentLine - 1];
            return currentContent;
        }
        currentContent = new String[readerList.length];
        try {
            boolean newContent = true;
            for (int i = 0; i < readerList.length; i++) {
                currentContent[i] = readerList[i].readLine();
                if (currentContent[i] == null)
                    newContent = false; //at least one of the files are not ready
            }
            if (!newContent)//at least one line come to the end
            {
                closeFile();
                if (loop)
                    openFile();
                return readNext();
            } else {
                currentLine++;
                return currentContent;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Jump to any given line number.
     */
    public String[] readGivenLineAt(int lineNumber) {
        if (lineNumber > totalLine || lineNumber <= 0 || lineNumber == currentLine)
            return currentContent;
        if (allCached) {
            currentLine = lineNumber;
            currentContent = cachedLines[lineNumber - 1];
            return currentContent; //pay attention to this lineNumber-1, indexes start at 0, but line number starts at 1.
        }
        if (lineNumber < currentLine) {
            closeFile();
            openFile();
        }
        for (; currentLine < lineNumber - 1; )
            readNext();
        return readNext();
    }

    public boolean hasNext() {
        return currentLine < totalLine;
    }

    public void close() {
        for (int i = 0; i < readerList.length; i++)
            try {
                readerList[i].close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
}

