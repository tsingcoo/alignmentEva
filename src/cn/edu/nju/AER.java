package cn.edu.nju;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Implementation of alignment error rate.
 */
public class AER extends AbstractAlignmentMetric {

    public double precision; // |A and P|/|A|  precision=correct/output_length
    public double recall;// |A and S|/|S|  recall=correct/reference_length
    public double aer;
    public int tpPossible = 0; //true positive
    public int tpSure = 0;
    public int resultCount = 0;
    public int referenceSureCount = 0;

    public void addSentence(AlignmentTable result, AlignmentTable reference) {
        if (!result.SameSize(reference)) {
            System.err.println("Reference and result doesn't match!");
        }
        int truepositivePossible = 0;
        int truepositiveSure = 0;
        int sureCount = 0;
        for (int i = 0; i < reference.rowNum; i++) {
            for (int j = 0; j < reference.colNum; j++) {
                if (reference.IsAligned(i, j) && result.IsAligned(i, j)) {
                    truepositivePossible++;
                    if (reference.IsSureLink(i, j)) {
                        truepositiveSure++;
                    }
                }
                if (reference.IsAligned(i, j) && reference.sureLinks[i][j]) {
                    sureCount++;
                }
            }
        }
        this.tpPossible += truepositivePossible;
        this.tpSure += truepositiveSure;
        this.referenceSureCount += sureCount;

        this.resultCount += result.GetLinkCount();
    }

    public double computeScore() {
        precision = 1.0 * tpPossible / resultCount;
        recall = 1.0 * tpSure / referenceSureCount;
        aer = 1 - ((double) tpPossible + tpSure) / (resultCount + referenceSureCount);
        return aer;
    }

    public String printScores() {

        DecimalFormat df = new DecimalFormat("0.000");
        df.setRoundingMode(RoundingMode.HALF_UP);


        String output = "precision: " + df.format(precision) + "\n";
        output += "recall: " + df.format(recall) + "\n";
        output += "aer: " + df.format(aer);
        return output;
    }

    public static void main(String[] args) throws IOException {
        AER aer = new AER();
        //test file,reference file
        System.out.println(aer.computeScoreOfFile("/Users/wangqinglong/Library/Mobile Documents/com~apple~CloudDocs/WordAlign/data/infer.incre.align", "/Users/wangqinglong/Library/Mobile Documents/com~apple~CloudDocs/WordAlign/data/test.qin.align"));
    }
}

