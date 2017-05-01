package cn.edu.nju;

import java.util.ArrayList;
//edit by Shujian 2010-11-12 ensure every function works well with sure/possible links

/**
 * Data structure of an alignment between source and target sentences.
 * The alignment is represented by a m*n boolean matrix.
 * Another matrix is used for annotation of sure and possible links.
 * The class provides all kinds of functions for accessing the alignment.
 */
public class AlignmentTable {
    public int rowNum;
    public int colNum;
    public boolean[][] matrix;
    public boolean[][] sureLinks;//By default all links are sure;
    int linkCount = 0;

    //    int sureCount = 0;
    //create an empty table
    public AlignmentTable(int r, int c) {
        rowNum = r;
        colNum = c;
        matrix = new boolean[r][c];
        sureLinks = new boolean[r][c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                sureLinks[i][j] = true;
                matrix[i][j] = false;
            }
        }
    }

    public AlignmentTable(int rowNum, int colNum, boolean[][] matrix,
                          boolean[][] sureLinks, int linkCount) {
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.matrix = matrix;
        this.sureLinks = sureLinks;
        this.linkCount = linkCount;
    }

    //copy constructor
    public AlignmentTable(AlignmentTable x) {
        rowNum = x.rowNum;
        colNum = x.colNum;
        linkCount = x.linkCount;
//        sureCount = x.sureCount;
        matrix = new boolean[rowNum][colNum];
        sureLinks = new boolean[rowNum][colNum];
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                sureLinks[i][j] = x.sureLinks[i][j];
                matrix[i][j] = x.matrix[i][j];
            }
        }
    }

    /////////////////////////////////////////////
    //Methods for processing multiple tables
    public boolean SameSize(AlignmentTable x) {
        if (rowNum == x.rowNum && colNum == x.colNum) {
            return true;
        } else {
            return false;
        }
    }

    public boolean intersection(AlignmentTable at) {
        if (!SameSize(at))
            return false;
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (IsAligned(i, j) && !at.IsAligned(i, j)) {
                    matrix[i][j] = false;
                    linkCount--;
                }
            }
        }
        return true;
    }

    public static AlignmentTable intersection(ArrayList<AlignmentTable> list) {
        AlignmentTable at = new AlignmentTable(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            if (!at.intersection(list.get(i)))
                return null;
        }
        return at;
    }

    public boolean union(AlignmentTable at) {
        if (!SameSize(at))
            return false;
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (!IsAligned(i, j) && at.IsAligned(i, j)) {
                    matrix[i][j] = true;
                    linkCount++;
                }
            }
        }
        return true;
    }

    public static AlignmentTable union(ArrayList<AlignmentTable> list) {
        AlignmentTable at = new AlignmentTable(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            if (!at.union(list.get(i)))
                return null;
        }
        return at;
    }

    /////////////////////////////////////////////
    //Methods for input and output from file
    //For the format of binary alignment table
    //read in the alignment table
    public void FillMatrix_Aligntable(String alignmentString) {
        if (alignmentString == null || alignmentString.equals(""))
            return;
        else
            alignmentString = alignmentString.trim();
        int srcid = -1;
        int tarid = -1;
        String srcitem = "";
        String taritem = "";

        String[] alignments = alignmentString.split(" ");
        for (int indexa = 0; indexa < alignments.length; indexa++) {
            if (alignments[indexa].equals(""))
                continue;
            //Shujian 2008-10-04 for Possible tag
            String pTagString = "";
            boolean isPossible = false;

            String s = alignments[indexa].trim();
            int index = s.indexOf("-");
            if (index == -1) {
                System.err.println("Format error in \"" + s + "\"!");
                continue;
            }
            srcitem = s.substring(0, index);
            taritem = s.substring(index + 1);

            //Shujian 2008-10-04 for Possible tag
            int pindex = taritem.indexOf("-");
            if (pindex != -1) {
                pTagString = taritem.substring(pindex); //should be "-P"
                taritem = taritem.substring(0, pindex);
            }
            try {
                srcid = Integer.parseInt(srcitem);
            } catch (Exception e) {
                System.err.println("Format error in \"" + s + "\"!");
                continue;
            }
            try {
                tarid = Integer.parseInt(taritem);
            } catch (Exception e) {
                System.err.println("Format error in \"" + s + "\"!");
                continue;
            }
            if (pTagString.equals("-P")) {
                isPossible = true;
            } else if (pTagString.equals("")) {
                isPossible = false;
            } else {
                System.err.println("Format error in \"" + s + "\"!");
                continue;
            }
            if (srcid < 0 || srcid >= rowNum || tarid < 0 || tarid >= colNum) {
                System.err.println("Index out of bounds : \"" + s + "\"! Max: " + (rowNum - 1) + " " + (colNum - 1) + "! ");
                continue;
            }
            if (!matrix[srcid][tarid])//in case of duplicate alignment Strings increase the link count
            {
                matrix[srcid][tarid] = true;
                linkCount++;
            }
            sureLinks[srcid][tarid] = !isPossible;
        }
    }

    //print on the console
    public void PrintMatrix() {
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (matrix[i][j]) {
                    if (sureLinks[i][j])
                        System.out.print("1  ");
                    else
                        System.out.print("1P ");
                } else {
                    System.out.print("0  ");
                }
            }
            System.out.println();
        }
    }

    public String GetAlignStr_Src() {
        return GetAlignStr_Src(0, rowNum - 1);
    }

    //Return the alignment in "sid-tid" format for the words between srcStartIndex and srcEndIndex (including srcStartIndex and srcEndIndex)
    //sid and tid in "sid-tid" format is the absolute index of words in the source sentence
    public String GetAlignStr_Src(int srcStartIndex, int srcEndIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = srcStartIndex; i <= srcEndIndex; i++) {
            for (int j = 0; j < colNum; j++) {
                if (matrix[i][j]) {
                    sb.append(' ');
                    sb.append(i);
                    sb.append('-');
                    sb.append(j);
                    if (!sureLinks[i][j]) {
                        sb.append('-');
                        sb.append('P');
                    }
                }
            }
        }
        String result = "";
        if (sb.length() > 0) {
            result = sb.substring(1);
        }

        return result;
    }

    //Return the alignment in "sid-tid" format for the words between srcStartIndex and srcEndIndex (including srcStartIndex and srcEndIndex)
    //sid and tid in "sid-tid" format is the relative index of words in the source sentence
    public String GetAlignStr_Src(int srcStartIndex, int srcEndIndex, int tarStartIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = srcStartIndex; i <= srcEndIndex; i++) {
            for (int j = 0; j < colNum; j++) {
                if (matrix[i][j]) {
                    sb.append(" ");
                    sb.append((i - srcStartIndex));
                    sb.append("-");
                    sb.append((j - tarStartIndex));
                    if (!sureLinks[i][j]) {
                        sb.append('-');
                        sb.append('P');
                    }
                }
            }
        }
        String result = "";
        if (sb.length() > 0) {
            result = sb.substring(1);
        }
        return result;
    }


    ///////////////////////////////////////////
    //Methods for accessing the table 
    public boolean IsSrcAligned(int index) {
        for (int i = 0; i < colNum; i++) {
            if (matrix[index][i]) {
                return true;
            }
        }
        return false;
    }

    public boolean IsTrgAligned(int index) {
        for (int i = 0; i < rowNum; i++) {
            if (matrix[i][index]) {
                return true;
            }
        }
        return false;
    }

    public boolean IsAligned(int i, int j) {
        if (i < rowNum && i >= 0 && j < colNum && j >= 0) {

            return matrix[i][j];
        }
        return false;
    }

    ///////////////////////////////////////////
    //Methods for accessing the table 
    //Shujian 2011-12-17
    public boolean IsSrcSureAligned(int index) {
        for (int i = 0; i < colNum; i++) {
            if (matrix[index][i] && sureLinks[index][i]) {
                return true;
            }
        }
        return false;
    }

    public boolean IsTrgSureAligned(int index) {
        for (int i = 0; i < rowNum; i++) {
            if (matrix[i][index] && sureLinks[i][index]) {
                return true;
            }
        }
        return false;
    }

    public boolean IsSureAligned(int i, int j) {
        if (i < rowNum && i >= 0 && j < colNum && j >= 0) {
            return matrix[i][j] && sureLinks[i][j];
        }
        return false;
    }

    //Shujian 2010-1-19
    public int GetFirstAlignWord_Src(int indexStart, int indexEnd) {
        int first = -1;
        for (int i = indexStart; i <= indexEnd; i++) {
            int cur_first = GetFirstAlignWord_Src(i);
            if (first == -1 || (first > cur_first && cur_first != -1)) {
                first = cur_first;
            }
        }
        return first;
    }

    public int GetLastAlignWord_Src(int indexStart, int indexEnd) {
        int last = -1;
        for (int i = indexStart; i <= indexEnd; i++) {
            int cur_last = GetLastAlignWord_Src(i);
            if (last == -1 || last < cur_last) {
                last = cur_last;
            }
        }
        return last;
    }

    //Shujian 2007-11-23 
    public int GetFirstAlignWord_Src(int index) {
        for (int i = 0; i < colNum; i++) {
            if (matrix[index][i]) {
                return i;
            }
        }
        return -1;
    }

    public int GetLastAlignWord_Src(int index) {
        for (int j = colNum - 1; j >= 0; j--) {
            if (matrix[index][j]) {
                return j;
            }
        }
        return -1;
    }

    //Shujian 2008-10-04
    public ArrayList<Integer> GetAlignWords_Src(int index) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (index < 0 || index >= rowNum) {
            return result;
        } else {
            for (int i = 0; i < colNum; i++) {
                if (matrix[index][i]) {
                    result.add(i);
                }
            }
        }
        return result;
    }

    public ArrayList<Integer> GetAlignWords_Trg(int index) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if (index < 0 || index >= colNum) {
            return result;
        } else {
            for (int i = 0; i < rowNum; i++) {
                if (matrix[i][index]) {
                    result.add(i);
                }
            }
        }
        return result;
    }

    public int GetLinkCount() {
        return linkCount;
    }

    public boolean AddLink(int i, int j, boolean isSure) {
        if (i < rowNum && i >= 0 && j < colNum && j >= 0) {
            if (matrix[i][j]) {
                System.err.println("Adding existing links " + i + "," + j);
                return false;
            }
            matrix[i][j] = true;
            sureLinks[i][j] = isSure;
            linkCount++;
            return true;
        }
        return false;
    }

    public boolean RemoveLink(int i, int j) {
        if (i < rowNum && i >= 0 && j < colNum && j >= 0) {
            matrix[i][j] = false;
            linkCount--;
            return true;
        }
        return false;
    }

    public boolean IsSureLink(int i, int j) {
        if (i < rowNum && i >= 0 && j < colNum && j >= 0) {
            return matrix[i][j] && sureLinks[i][j];
        } else {
            System.err.println("Index out of bounds: " + i + ", " + j);
            return false;
        }
    }

    public boolean minus(AlignmentTable a) {
        // TODO Auto-generated method stub
        if (!SameSize(a))
            return false;

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (a.IsAligned(i, j))
                    RemoveLink(i, j);
            }
        }
        return true;
    }

    public boolean sameAlign_src(int i, int j) {
        ArrayList<Integer> align_i = GetAlignWords_Src(i);
        ArrayList<Integer> align_j = GetAlignWords_Src(j);
        if (align_i.size() != align_j.size())
            return false;
        for (int k = 0; k < align_i.size(); k++) {
            if (align_i.get(k) != align_j.get(k))
                return false;
        }
        return true;
    }

    public boolean sameAlign_trg(int i, int j) {
        ArrayList<Integer> align_i = GetAlignWords_Trg(i);
        ArrayList<Integer> align_j = GetAlignWords_Trg(j);
        if (align_i.size() != align_j.size())
            return false;
        for (int k = 0; k < align_i.size(); k++) {
            if (align_i.get(k) != align_j.get(k))
                return false;
        }
        return true;
    }

    public void random() {
        //for each source word, pick a random alignment as it's alignment
        for (int i = 0; i < rowNum; i++) {
            int k = (int) Math.floor(Math.random() * (colNum + 1));
            if (k != colNum)
                this.AddLink(i, k, true);
        }
    }

    @Override
    public String toString() {
        return this.GetAlignStr_Src();
    }

    public AlignmentTable getReversedTable() {
        AlignmentTable at = new AlignmentTable(colNum, rowNum);
        for (int i = 0; i < rowNum; i++)
            for (int j = 0; j < colNum; j++)
                if (IsAligned(i, j))
                    at.AddLink(j, i, sureLinks[i][j]);
        return at;
    }

    public AlignmentTable removeRow(int index) {
        boolean[][] newMatrix = new boolean[rowNum - 1][colNum];
        boolean[][] newSureMatrix = new boolean[rowNum - 1][colNum];

        for (int i = 0; i < index; i++)
            for (int j = 0; j < colNum; j++) {
                newMatrix[i][j] = matrix[i][j];
                newSureMatrix[i][j] = sureLinks[i][j];
            }
        for (int i = index; i < rowNum - 1; i++)
            for (int j = 0; j < colNum; j++) {
                newMatrix[i][j] = matrix[i + 1][j];
                newSureMatrix[i][j] = sureLinks[i + 1][j];
            }
        return new AlignmentTable(rowNum - 1, colNum, newMatrix, newSureMatrix, linkCount - GetAlignWords_Src(index).size());
    }

    public AlignmentTable removeColumn(int index) {
        boolean[][] newMatrix = new boolean[rowNum][colNum - 1];
        boolean[][] newSureMatrix = new boolean[rowNum][colNum - 1];
        for (int j = 0; j < index; j++)
            for (int i = 0; i < rowNum; i++)

            {
                newMatrix[i][j] = matrix[i][j];
                newSureMatrix[i][j] = sureLinks[i][j];
            }
        for (int j = index; j < colNum - 1; j++)
            for (int i = 0; i < rowNum; i++)

            {
                newMatrix[i][j] = matrix[i][j + 1];
                newSureMatrix[i][j] = sureLinks[i][j + 1];
            }
        return new AlignmentTable(rowNum, colNum - 1, newMatrix, newSureMatrix, linkCount - GetAlignWords_Trg(index).size());
    }

    /**
     * TODO this methods does not work with possible alignment strings.
     */
    public static String getReverseAlignStr(String align) {
        String revAli = "";
        String[] cells = align.trim().split("\\s+");
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].length() == 0)
                continue;
            String[] fields = cells[i].split("-");
            revAli += fields[1] + "-" + fields[0] + " ";
        }
        revAli = revAli.trim();
        return revAli;
    }
}

