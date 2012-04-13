
package polysomeanalyzer;

import java.util.ArrayList;

/*
 * Plot.java
 * Created on April 18, 2007, 10:39 PM
 */
import java.util.Iterator;

/**
 */
public class Plot 
{
    public String name = "plot";
    public String group = "group";
    public double[] xValues = new double[0];
    public double[] yValues = new double[0];
    public double xStart = 0; 
    public double xEnd = 0;
    public double yBaseline = 0;
    public ArrayList peaks = new ArrayList();
    private double[] sigmas = new double[0];
    
    // initial values saved for revert
    private double[] xValuesInitial = new double[0];
    private double[] yValuesInitial = new double[0];
    private double xStartInitial = 0;
    private double xEndInitial = 0;
    private double yBaselineInitial = 0;
    
    
    public Plot() {}
    
    public Plot(String group, String newName, double[] xValues, double[] yValues) {
        this.group = group;
        this.name = newName;
        
        // make sure x and y values are of the same size, if not use smallest
        int minLength = xValues.length;
        if (xValues.length < yValues.length)
           minLength = xValues.length;

        xValuesInitial = new double[minLength];
        yValuesInitial = new double[minLength];
        this.xValues = new double[minLength];
        this.yValues = new double[minLength];
        for (int i = 0; i < minLength; i++) {
            xValuesInitial[i] = xValues[i];
            yValuesInitial[i] = yValues[i];
            this.xValues[i] = xValues[i];
            this.yValues[i] = yValues[i];
        }
        
        initialize();
    }
    
    public void initialize() {
        // get detergent peak index. assume its the peak with max voltage
        this.peaks = getAllPeaks();
        Peak maxPeak = getDetergentPeak();
        if (maxPeak != null) {
            xStartInitial = maxPeak.xRight;
            this.xStart = xStartInitial;
        } else {
            System.err.println("Failed to find start for " + name);
        }

        // get baseline, assume its absolute min between max and last peaks
        Peak endPeak = getEndPeak();
        if (maxPeak != null && endPeak != null) {
            xEndInitial = getBaselineX(maxPeak.x, endPeak.x);
            this.xEnd = xEndInitial;
            yBaselineInitial = getY(this.xEnd);
            this.yBaseline = yBaselineInitial;
        } else {
            System.err.println("Failed to get end and baseline for " + name);
            this.yBaseline = 0;
            this.xEnd = xValues[xValues.length-1];
            this.xEndInitial = this.xEnd;
        }
    }
    
    public void guessNames() {
        String[] peakNames = {"40S", "60S", "80S", "2R", "3R", "4R", "5R"};
        ArrayList boundPeaks = getBoundPeaks();
        for (int iPeak = 0; iPeak < boundPeaks.size(); iPeak++) {
            Peak peak = (Peak)boundPeaks.get(iPeak);
            if (iPeak < peakNames.length) {
                peak.name = peakNames[iPeak];
            }
        }
        // rename last bound peak
        if (boundPeaks.size() > 2) {
            Peak lastPeak = (Peak)boundPeaks.get(boundPeaks.size()-1);
            Peak prevPeak = (Peak)boundPeaks.get(boundPeaks.size()-2);
            lastPeak.name = ">" + prevPeak.name;
        }
    }
    
    public void resetPeakNames() {
        ArrayList allPeaks = getAllPeaks();
        for (int iPeak = 0; iPeak < allPeaks.size(); iPeak++) {
            Peak peak = (Peak)allPeaks.get(iPeak);
            peak.name = "peak" + iPeak;
        }
    }
    
    public String toString() {
        return this.name;
    }
    
    public double[] getXValues() {
        return xValues;
    }
    public double[] getYValues() {
        return yValues;
    }
    
    public ArrayList getPeaks() {
        return peaks;
    }
    
    public Peak addPeak(double x) {
        Peak peak = new Peak("peak" + peaks.size(), x, getY(x),
                getNextXMin(x, "left"), getNextXMin(x, "right"));
        peaks.add(peak);
        return peak;
    }
    
  
    public Peak getDetergentPeak() {
        if (this.peaks.size() == 0)
            return null;
        Peak maxPeak = (Peak)this.peaks.get(0);
        for (int i = 1; i < this.peaks.size(); i++) {
            Peak currentPeak = (Peak)this.peaks.get(i);
            if (currentPeak.y > maxPeak.y) {
                maxPeak = currentPeak;
            }
        }
        return maxPeak;
    }
    
    public Peak getEndPeak() {
        // take last peak with apreciable area
        double y0 = yValues[yValues.length-1];
        for (int iPeak = peaks.size()-1; iPeak >= 0; iPeak--) {
            Peak peak = (Peak)peaks.get(iPeak);
            double area = getArea(peak.xLeft, peak.xRight, y0);
            if (area > 0.1)
                return peak;
        }
        return null;
    }
    
    public void initializePeakBoundaries(Peak peak) {
        peak.setXLeft(getNextXMin(peak.x, "left"));
        peak.setXRight(getNextXMin(peak.x, "right"));
    }
    
    private ArrayList getAllPeaks() {
        ArrayList allPeaks = new ArrayList();
        
        // first find all peak locations
        boolean isIncreasing = true;
        int nUnchanging = 0;
        for (int i = 0; i < this.yValues.length-1; i++) {
            if (isIncreasing) {
                if (yValues[i] > yValues[i+1]) {
                    isIncreasing = false;

                    // seek left for where yMax is reached
                    int iPeakRight = i;
                    int j = i-1;
                    while (j > 0 && this.yValues[j] == yValues[i]) {
                        j--;
                    }
                    int iPeakLeft = j + 1;
                    
                    double x = xValues[i];
                    int nPoints = iPeakRight - iPeakLeft + 1;
                    if (nPoints <= 1) {
                        x = this.xValues[iPeakRight];
                    } else if (nPoints % 2 == 0) { // even
                        // average the two middle x values
                        int iLeftMiddle = iPeakLeft + nPoints / 2 - 1;
                        int iRightMiddle = iPeakLeft + nPoints / 2;
                        double xLeft = this.xValues[iLeftMiddle];
                        double xRight = this.xValues[iRightMiddle];
                        x = (xLeft + xRight) / 2;
                    } else { // odd
                        // take the middle x value
                        int iMiddle = iPeakLeft + (nPoints - 1) / 2;
                        x = this.xValues[iMiddle];
                    }                        
          
                    Peak peak = new Peak("peak" + allPeaks.size(), x, getY(x),
                            getNextXMin(x, "left"), getNextXMin(x, "right"));
                    allPeaks.add(peak);
                }
                
            } else {
                if (yValues[i] < yValues[i+1]) {
                    isIncreasing = true;
                }
            }
            
            if (peaks.size() > 0 && yValues[i] == yValues[i+1])
                nUnchanging++;
            else
                nUnchanging = 0;
            if (nUnchanging > 5)
                i = this.yValues.length;
        }
        return allPeaks;
    }

    // interpolate to get the y value at a given x
    public double getY(double x) {
        if (x > this.xValues[this.xValues.length-2])
            return 0;
        if (x < this.xValues[1])
            return 0;
        
        // find index where x crosses x(i). assume x increases with i
        for (int i = 0; i < this.xValues.length; i++) {
            if (xValues[i] == x) {
                return yValues[i];
            } else if (this.xValues[i] > x) {
                double x1 = this.xValues[i-1];
                double y1 = this.yValues[i-1];
                double x2 = this.xValues[i];
                double y2 = this.yValues[i];
                
                // interpolate y value 
                double m = (y2 - y1) / (x2 - x1);
                return m * (x - x1) + y1;
            } 
        }
        return 0;
    }
    
    public int getXIndex(double x) {
        for (int i = 0; i < this.xValues.length; i++) {
            if (this.xValues[i] >= x) {
                return i;
            } 
        }
        return 0;
    }

    // gets the x value of the nearest min (left or right)
    private double getNextXMin(double xStart, String direction) {
        if (xStart <= xValues[0])
            return 0;
        if (xStart >= xValues[xValues.length-1])
            return 0;

        if (direction.equals("left"))
            return getNextXMinLeft(xStart);
        else
            return getNextXMinRight(xStart);
    }
    
    private double getNextXMinLeft(double xStart) {
        // scan left until decreasing
        int i = getXIndex(xStart);
        while (i > 0 && yValues[i-1] >= yValues[i])
            i--;
        
        // scan left for min
        while (i > 0 && yValues[i-1] <= yValues[i])
            i--;
        int iMinLeft = i;
        
        // scan back to where y min value was first found
        while (i < yValues.length-1 && yValues[i+1] == yValues[i])
            i++;
        int iMinRight = i;
        
        // calculate x between these left and right indexes
        double xMin = this.xValues[iMinLeft];
        int nPoints = iMinRight - iMinLeft + 1;
        if (nPoints > 1) {
            if (nPoints % 2 == 0) { // even
                // average the two middle x values
                int iLeftMiddle = iMinLeft + nPoints / 2 - 1;
                int iRightMiddle = iMinLeft + nPoints / 2;
                double xLeft = this.xValues[iLeftMiddle];
                double xRight = this.xValues[iRightMiddle];
                xMin = (xLeft + xRight) / 2;
            } else { // odd
                // take the middle x value
                int iMiddle = iMinLeft + (nPoints - 1) / 2;
                xMin = this.xValues[iMiddle];
            }
        }
        return xMin;
    }
    
    private double getNextXMinRight(double xStart) {
        // scan right until decreasing
        int i = getXIndex(xStart);
        while (i < yValues.length-1 && yValues[i] <= yValues[i+1])
            i++;
        
        // scan right for min
        while (i < yValues.length-1 && yValues[i] >= yValues[i+1])
            i++;
        int iMinRight = i;
        
        // scan back to where ymin was first found
        while (i > 0 && yValues[i] == yValues[i-1])
            i--;
        int iMinLeft = i;
        
        // calculate x between these left and right indexes
        double xMin = this.xValues[iMinLeft];
        int nPoints = iMinRight - iMinLeft + 1;
        if (nPoints > 1) {
            if (nPoints % 2 == 0) { // even
                // average the two middle x values
                int iLeftMiddle = iMinLeft + nPoints / 2 - 1;
                int iRightMiddle = iMinLeft + nPoints / 2;
                double xLeft = this.xValues[iLeftMiddle];
                double xRight = this.xValues[iRightMiddle];
                xMin = (xLeft + xRight) / 2;
            } else { // odd
                // take the middle x value
                int iMiddle = iMinLeft + (nPoints - 1) / 2;
                xMin = this.xValues[iMiddle];
            }
        }
        return xMin;
    }
    
    private double getBaselineX(double xStart, double xEnd) {
        // scan right for min
        int iStart = getXIndex(xStart) + 1;
        int iEnd = getXIndex(xEnd) - 1;
        int iMinLeft = getXIndex(xStart) + 1;
        for (int i = iStart; i < iEnd; i++) {
            if (yValues[i] < yValues[iMinLeft]) {
                iMinLeft = i;
            }
        }

        // scan a bit further if y doesnt change
        int iMinRight = iMinLeft;
        while (iMinRight < iEnd && yValues[iMinRight+1] == yValues[iMinRight]) {
            iMinRight++;
        }
        
        // calculate x between these left and right indexes
        double xMin = this.xValues[iMinLeft];
        int nPoints = iMinRight - iMinLeft + 1;
        if (nPoints > 1) {
            if (nPoints % 2 == 0) { // even
                // average the two middle x values
                int iLeftMiddle = iMinLeft + nPoints / 2 - 1;
                int iRightMiddle = iMinLeft + nPoints / 2;
                double xLeft = this.xValues[iLeftMiddle];
                double xRight = this.xValues[iRightMiddle];
                xMin = (xLeft + xRight) / 2;
            } else { // odd
                // take the middle x value
                int iMiddle = iMinLeft + (nPoints - 1) / 2;
                xMin = this.xValues[iMiddle];
            }
        }
        
        return xMin;
    }
    
    public double getNormalArea() {
        return getArea(xStart, xEnd, yBaseline);
    }
    
    public double getArea(double x1, double x2) {
        return getArea(x1, x2, yBaseline);
    }
    
    public double getArea(double x1, double x2, double y0) {
        if (x1 == x2)
            return 0;
        if (x1 > x2) {
            double temp = x1;
            x1 = x2;
            x2 = temp;
        }
        double area = 0;
        
        // get area from x1 to first discrete x point
        int iStart = getXIndex(x1);
        if (x1 < xValues[iStart]) {           
            // calculate area from x1 to x(iStart)
            double y2 = getY(xValues[iStart]) - y0;
            double y1 = getY(x1) - y0;
            double avgY = (y2 + y1) / 2;
            double dx = xValues[iStart] - x1;
            area += avgY * dx;
        }
        
        // get area from first discrete x point to last
        int iEnd = getXIndex(x2) - 1; // point left of x2
        for (int i = iStart; i < iEnd; i++) {
            // calculate area from i to i+1
            double y2 = getY(xValues[i+1]) - y0;
            double y1 = getY(xValues[i]) - y0;
            double avgY = (y2 + y1) / 2;
            double dx = xValues[i+1] - xValues[i];
            area += avgY * dx;
        }
    
        // get area from last point to x2
        if (x2 > xValues[iEnd]) {
            // calculate area from x(iEnd) to x2
            double y1 = getY(xValues[iEnd]) - y0;
            double y2 = getY(x2) - y0;
            double avgY = (y2 + y1) / 2;
            double dx = x2 - xValues[iEnd];
            area += avgY * dx;
        }
        return area;
    }

    public ArrayList getBoundPeaks() {
        ArrayList allPeaks = getPeaks();
        ArrayList boundPeaks = new ArrayList();
        for (int iPeak = 0; iPeak < allPeaks.size(); iPeak++) {
            Peak peak = (Peak)allPeaks.get(iPeak);
            if (peak.x > xStart && peak.x < xEnd)
                boundPeaks.add(peak);
        }
        return boundPeaks;
    }
    
    public void computeSigmas() {
        Peak p0 = new Peak("", xValues[0], 0);
        p0.sigma = 1;
        Peak pn = new Peak("", xValues[xValues.length-1], 0);
        pn.sigma = 1;
        
        for (int i = 0; i < peaks.size(); i++) {
            
        }
        
        
    }
    
    private double getD(Peak p0, Peak p1, Peak p2) {
        double x = (p0.x + p1.x) / 2;
        double yActual = getY(x);
        double yModel = 0;
        yModel += (getY(p0.x)) * Math.exp(-(x - p0.x)*(x - p0.x) / (2.0 * p0.sigma * p0.sigma));
        yModel += (getY(p1.x)) * Math.exp(-(x - p1.x)*(x - p1.x) / (2.0 * p1.sigma * p1.sigma));
        yModel += (getY(p2.x)) * Math.exp(-(x - p2.x)*(x - p2.x) / (2.0 * p2.sigma * p2.sigma));
        return Math.sqrt(Math.pow(yActual - yModel, 2));
    }
    
    public double[] getSigmas(Peak peak) {
        Peak[] p = new Peak[3];
        int nPeaks = peaks.size();
        for (int iPeak = 1; iPeak < nPeaks - 1; iPeak++) {
            if (peaks.get(iPeak) == peak) {
                p[0] = (Peak)peaks.get(iPeak - 1);
                p[1] = peak;
                p[2] = (Peak)peaks.get(iPeak + 1);
                break;
            }
        }
        if (p[0] == null || p[1] == null || p[2] == null)
            return new double[3];
        
        double[] sigmas = new double[3];
        sigmas[0] = p[0].sigma;
        sigmas[1] = p[1].sigma;
        sigmas[2] = p[2].sigma;
        return sigmas;
    }
  
    public double getXMin() {
        double xMin = Double.MAX_VALUE;
        for (int i = 0; i < xValues.length; i++) {
            double x = xValues[i];
            if (x < xMin)
                xMin = x;
        }
        return xMin;
    }
    
    public double getXMax() {
        double xMax = 0;
        for (int i = 0; i < xValues.length; i++) {
            double x = xValues[i];
            if (x > xMax)
                xMax = x;
        }
        return xMax;
    }
    
    public double getYMin() {
        double yMin = Double.MAX_VALUE;
        for (int i = 0; i < yValues.length; i++) {
            double y = yValues[i];
            if (y < yMin)
                yMin = y;
        }
        return yMin;
    }
    
    public double getYMax() {
        double yMax = 0;
        for (int i = 0; i < yValues.length; i++) {
            double y = yValues[i];
            if (y > yMax)
                yMax = y;
        }
        return yMax;
    }
    
    
    public void xTransform(double xOffset, double xScale) {
         // transform plot values
        for (int i = 0; i < xValues.length; i++) {
            xValues[i] = xValuesInitial[i] * xScale + xOffset;
        }
        xStart = xStartInitial * xScale + xOffset;
        xEnd = xEndInitial * xScale + xOffset;
        
        // transform peak values
        int nPeaks = peaks.size();
        for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
            Peak peak = (Peak)peaks.get(iPeak);
            peak.xTransform(xOffset, xScale);
        }
    }
    
    public void xRevert() {
        // revert plot values
        for (int i = 0; i < xValues.length; i++) {
            xValues[i] = xValuesInitial[i];
        }
        xStart = xStartInitial;
        xEnd = xEndInitial;
        
        // revert peak values
        int nPeaks = peaks.size();
        for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
            Peak peak = (Peak)peaks.get(iPeak);
            peak.xRevert();
        }
    }
    
    public void yTransform(double yOffset, double yScale) {
        // transform plot values
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = yValuesInitial[i] * yScale + yOffset;
            yBaseline = yBaselineInitial * yScale + yOffset;
        }
        // transform peak values
        int nPeaks = peaks.size();
        for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
            Peak peak = (Peak)peaks.get(iPeak);
            peak.yTransform(yOffset, yScale);
        }
    }
            
    public void yRevert() {
        // revert plot values
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = yValuesInitial[i];
        }
        yBaseline = yBaselineInitial;
        
        // revert peak values
        int nPeaks = peaks.size();
        for (int iPeak = 0; iPeak < nPeaks; iPeak++) {
            Peak peak = (Peak)peaks.get(iPeak);
            peak.yRevert();
        }
    }
    
}
