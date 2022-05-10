package com.google.zxing.qrcode.detector;

public class FinderPatternFinderUtils {

    /**
     * Given a count of black/white/black/white/black pixels just seen and an end position,
     * figures the location of the center of this run.
     */
    static float centerFromEnd(int[] stateCount, int end) {
      return (end - stateCount[4] - stateCount[3]) - stateCount[2] / 2.0f;
    }

    /**
     * Get square of distance between a and b.
     */
    static double squaredDistance(FinderPattern a, FinderPattern b) {
      double x = a.getX() - b.getX();
      double y = a.getY() - b.getY();
      return x * x + y * y;
    }
    
}
