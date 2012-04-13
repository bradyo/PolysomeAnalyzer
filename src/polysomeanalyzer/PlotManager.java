/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package polysomeanalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 *
 * @author brady
 */
public class PlotManager {

    private ArrayList plots = new ArrayList();
    
    // current x alignment
    static final int X_ALIGN_NONE = 0;
    static final int X_ALIGN_LEFT = 1;
    static final int X_ALIGN_RIGHT = 2;
    static final int X_ALIGN_BOTH = 3;
    
    // current y alignment
    static final int Y_ALIGN_NONE = 0;
    static final int Y_ALIGN_TOP = 1;
    static final int Y_ALIGN_BOTTOM = 2;
    static final int Y_ALIGN_BOTH = 3;
    static final int Y_ALIGN_NORMAL = 4;
    
    // x parameters of unaligned plots used for transforming
    private double xMaxLeftOffset = 0;
    private double xMaxRightOffset = 0;
    private double xMaxSpan = 0;
    private double xMaxScaledLeftOffset = 0;
    
    // y parameters of unaligned plots used for transforming
    private double yMaxBottomOffset = 0;
    private double yMaxTopOffset = 0;
    private double yMaxSpan = 0;
    private double yMaxScaledBottomOffset = 0;
    
    
    PlotManager() {
    }
    PlotManager(String filename) {
        loadFromFile(filename);
    }
       
    public void loadFromFile(String filename) {
        ArrayList groupNames = new ArrayList();
        ArrayList plotNames = new ArrayList();
        ArrayList[] xs = null; // xs[plot][point] = x
        ArrayList[] ys = null; // ys[plot][point] = y
        String line = null;
        int nPlots = 0;
        double iPoint = 0;
        
        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader inputStream = new BufferedReader(fileReader);
            StringTokenizer tokenizer = null;
            try {
                // read the plot headers
                line = inputStream.readLine();
                if (line == null) {
                    System.err.println("File does not contain header line");
                    return;
                }
                tokenizer = new StringTokenizer(line, "\t");
                while (tokenizer.hasMoreTokens()) {
                    groupNames.add(tokenizer.nextToken());
                }
                
                // read the plot names 
                line = inputStream.readLine();
                if (line == null) {
                    System.err.println("File does not contain plot names line");
                    return;
                }
                tokenizer = new StringTokenizer(line, "\t");
                while (tokenizer.hasMoreTokens()) {
                    plotNames.add(tokenizer.nextToken());
                }
                
                // set the number of plots (numbers should agree)
                nPlots = groupNames.size();
                if (plotNames.size() < nPlots)
                    nPlots = plotNames.size();
                if (groupNames.size() != plotNames.size()) {
                    System.err.println("Group names = " + groupNames.size() +
                            " but plot names = " + plotNames.size() +
                            ". Using " + nPlots);
                }

                // pre-allocate array lists to hold data
                xs = new ArrayList[nPlots];
                ys = new ArrayList[nPlots];
                for (int i = 0; i < nPlots; i++) {
                    xs[i] = new ArrayList();
                    ys[i] = new ArrayList();
                }

                // read data from file until end
                iPoint = 0;
                while ((line = inputStream.readLine()) != null) { 
                    if (line.length() == 0) // skip blank lines
                        continue;
                    
                    tokenizer = new StringTokenizer(line, "\t");
                    if (tokenizer.countTokens() == 0)
                        continue;
                    
                    for (int iPlot = 0; iPlot < nPlots; iPlot++) {
                        if (tokenizer.hasMoreElements()) {
                            String token = tokenizer.nextToken();
                            double x = (double)xs[iPlot].size();
                            double y = Double.parseDouble(token);
                            xs[iPlot].add(new Double(x));
                            ys[iPlot].add(new Double(y));
                        }
                    }
                    iPoint++;
                }
            } catch (IOException e) {
                System.err.println("Error reading " + filename + ": " +
                       e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Error reading file at line " + iPoint + 
                        "," + e.getMessage() + 
                        ". Any folling entries ignored.");
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to open " + filename + ":");
            System.err.println(e.getMessage());
        }
        
        // create new plots with data
        plots.clear();
        for (int iPlot = 0; iPlot < nPlots; iPlot++) {
            int nPoints = xs[iPlot].size();
            String groupName = (String)groupNames.get(iPlot);
            String plotName = (String)plotNames.get(iPlot);
            double[] xValues = new double[nPoints];
            double[] yValues = new double[nPoints];
            ArrayList xArray = (ArrayList)xs[iPlot];
            ArrayList yArray = (ArrayList)ys[iPlot];
            for (int i = 0; i < nPoints; i++) {
                Double x = (Double)xArray.get(i);
                Double y = (Double)yArray.get(i);
                xValues[i] = x.doubleValue();
                yValues[i] = y.doubleValue();
            }
            Plot plot = new Plot(groupName, plotName, xValues, yValues);
            plots.add(plot);
        }  
        
        // get initial tranformation parameters
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            double xLeft = 0;
            if (plot.getDetergentPeak() != null)
                xLeft = plot.getDetergentPeak().x;
            if (xLeft > xMaxLeftOffset)
                xMaxLeftOffset = xLeft;
            
            double xRight = 0;
            if (plot.getEndPeak() != null)
                xRight = plot.getEndPeak().x;
            if (xRight > xMaxRightOffset)
                xMaxRightOffset = xRight;
            
            double xSpan = xRight - xLeft;
            if (xSpan > xMaxSpan)
                xMaxSpan = xSpan;
            
            double yBaseline = plot.yBaseline;
            if (yBaseline > yMaxBottomOffset)
                yMaxBottomOffset = yBaseline;
            
            double yTop = 0;
            if (plot.getDetergentPeak() != null)
                yTop = plot.getDetergentPeak().y;
            if (yTop > yMaxTopOffset)
                yMaxTopOffset = yTop;
            
            double ySpan = yTop - yBaseline;
            if (ySpan > yMaxSpan)
                yMaxSpan = ySpan;
        }
        
        // get initial scale parameters
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);    

            if (plot.getDetergentPeak() == null || plot.getEndPeak() == null) {
                xMaxScaledLeftOffset = xMaxLeftOffset;
                yMaxScaledBottomOffset = yMaxBottomOffset;
                continue;
            }
            
            double xSpan = plot.getEndPeak().x - plot.getDetergentPeak().x;
            double xStretch = xMaxSpan / xSpan;
            double xLeft = xStretch * plot.getDetergentPeak().x;
            if (xLeft > xMaxScaledLeftOffset)
                xMaxScaledLeftOffset = xLeft;
            
            double ySpan = plot.getDetergentPeak().y - plot.yBaseline;
            double yStretch = yMaxSpan / ySpan;
            double yBaseline = yStretch * plot.yBaseline;
            if (yBaseline > yMaxScaledBottomOffset)
                yMaxScaledBottomOffset = yBaseline;
        }           
    }
    
    
    public String getOutput() {
        return getOutput(null);
    }
  
    public String getOutput(String group) {
        ArrayList groupNames = new ArrayList();
        if (group != null)
            groupNames.add(group);
        else
            groupNames = getGroupNames(); // all
        
        String output = "";
        for (int iGroup = 0; iGroup < groupNames.size(); iGroup++) {
            String groupName = (String)groupNames.get(iGroup);
            ArrayList groupPlots = getGroupPlots(groupName);
            
            output += "Group:\t" + groupName + "\t";
            output += "Plots:\t" + groupPlots.size() + "\n";
            output += "\n";
            
            // output areas
            output += "Raw Areas:\n";
            for (int iPlot = 0; iPlot < groupPlots.size(); iPlot++) {
                Plot plot = (Plot)groupPlots.get(iPlot);
                ArrayList peaks = plot.getBoundPeaks();
                               
                // output headers
                output += "Plot\tTotal\t";
                for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
                    Peak peak = (Peak)peaks.get(iPeak);
                    output += peak.name + "\t";
                }
                output += "\n";
                
                // output raw
                double normalArea = plot.getNormalArea();
                output += plot.name + "\t" + normalArea + "\t";
                for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
                    Peak peak = (Peak)peaks.get(iPeak);
                    output += plot.getArea(peak.xLeft, peak.xRight) + "\t";
                }
                output += "\n";
                
                // output fractions
                output += plot.name + "\t" + 1 + "\t";
                for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
                    Peak peak = (Peak)peaks.get(iPeak);
                    double normalPeakArea = plot.getArea(peak.xLeft, peak.xRight)
                            / normalArea;
                    output += normalPeakArea + "\t";
                }       
                output += "\n";
                
                // output percents
                output += plot.name + "\t" + 100 + "\t";
                for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
                    Peak peak = (Peak)peaks.get(iPeak);
                    double normalPeakArea = plot.getArea(peak.xLeft, peak.xRight)
                            / normalArea;
                    output += normalPeakArea * 100 + "\t";
                }       
                output += "\n";
                
                // output peak info
                output += plot.name + "\tpeak x\t";
                for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
                    Peak peak = (Peak)peaks.get(iPeak);
                    output += peak.x + "\t";
                }       
                output += "\n";
                
                output += plot.name + "\tpeak left\t";
                for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
                    Peak peak = (Peak)peaks.get(iPeak);
                    output += peak.xLeft + "\t";
                }    
                output += "\n";
                
                output += plot.name + "\tpeak right\t";
                for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
                    Peak peak = (Peak)peaks.get(iPeak);
                    output += peak.xRight + "\t";
                }  
                output += "\n";
                
                // skip a line between plots
                output += "\n";
            }
        }
        
        return output;
    }
    
    public String getAnalysisOutput(String groupName) {
        ArrayList commonPeakNames = getCommonPeakNames(groupName);
        
        // output headers
        String output = "";
        output += "Plot\t";
        for (int iName = 0; iName < commonPeakNames.size(); iName++) {
            output += commonPeakNames.get(iName) + "\t";
        }
        output += "\n";
        
        // output peak percents for each common peak
        ArrayList groupPlots = getGroupPlots(groupName);

        double[][] values = 
                new double[groupPlots.size()][commonPeakNames.size()]; 
        for (int iPlot = 0; iPlot < groupPlots.size(); iPlot++) {
            Plot plot = (Plot)groupPlots.get(iPlot);
            output += plot.name + "\t";
            double normalArea = plot.getNormalArea();

            for (int iPeak = 0; iPeak < commonPeakNames.size(); iPeak++) {
                String targetPeakName = (String)commonPeakNames.get(iPeak);
                ArrayList allPeaks = plot.getBoundPeaks();
                for (int jPeak = 0; jPeak < allPeaks.size(); jPeak++) {
                    Peak peak = (Peak)allPeaks.get(jPeak);
                    if (peak.name.equals(targetPeakName)) {
                        double percentArea = plot.getArea(peak.xLeft, peak.xRight)
                            / normalArea * 100;
                        values[iPlot][iPeak] = percentArea;
                        output += percentArea + "\t";
                        break;
                    }
                }
            }

            output += "\n";
        }
        // output mean and standard deviation lines for group
        output += "Mean\t";
        for (int iPeak = 0; iPeak < commonPeakNames.size(); iPeak++) {
            double sum = 0;
            for (int iPlot = 0; iPlot < groupPlots.size(); iPlot++) {
                sum += values[iPlot][iPeak];
            }
            double mean = sum / groupPlots.size();
            output += mean + "\t";
        }
        output += "\n";

        output += "Std Dev\t";
        for (int iPeak = 0; iPeak < commonPeakNames.size(); iPeak++) {
            double sum = 0;
            for (int iPlot = 0; iPlot < groupPlots.size(); iPlot++) {
                sum += values[iPlot][iPeak];
            }
            double mean = sum / groupPlots.size();

            double ssd = 0;
            for (int iPlot = 0; iPlot < groupPlots.size(); iPlot++) {
                ssd += Math.pow(values[iPlot][iPeak] - mean, 2.0);
            }
            double stdev = Math.sqrt(ssd / (groupPlots.size() - 1.0));
            output += stdev + "\t";
        }
        output += "\n";

        return output;
    }
    
    public String getAnalysisOutput() {
        String output = "";
        int nGroups = getGroupNames().size();
        for (int iGroup = 0; iGroup < nGroups; iGroup++) {
            String groupName = (String)getGroupNames().get(iGroup);
            output += getAnalysisOutput(groupName);
            output += "\n";
        }
        return output;
    }
    
    public ArrayList getCommonPeakNames(String groupName) {
        ArrayList groupPlots = getGroupPlots(groupName);
        int nPlots = groupPlots.size();
        if (nPlots < 1)
            return new ArrayList();
           
        ArrayList names = new ArrayList();
        Plot plot = (Plot)groupPlots.get(0);
        ArrayList peaks = plot.getBoundPeaks();
        for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
            Peak peak = (Peak)peaks.get(iPeak);
            names.add(peak.name);
        }
        
        ArrayList commonNames = new ArrayList();
        int nNames = names.size();
        for (int iName = 0; iName < nNames; iName++) {
            // if one peak doesnt have the name it should be removed
            boolean isCommon = true;
            for (int iPlot = 1; iPlot < nPlots; iPlot++) {
                boolean isFound = false;
                plot = (Plot)groupPlots.get(iPlot);
                peaks = plot.getBoundPeaks();
                for (int iPeak = 0; iPeak < peaks.size(); iPeak++) {
                    Peak peak = (Peak)peaks.get(iPeak);
                    if (peak.name.equals(names.get(iName))) {
                        isFound = true;
                    }
                }
                if (!isFound) {
                    isCommon = false;
                    break;
                }
            }

            if (isCommon)
                commonNames.add(names.get(iName));
        }
        return commonNames;
    }

    public ArrayList getPlots() {
        return plots;
    }
    
    public Plot getPlot(String name) {
        Iterator i = plots.iterator();
        while (i.hasNext()) {
            Plot plot = (Plot)i.next();
            if (plot.name.equals(name))
                return plot;
        }
        return null;
    }
    
    public void setPlots(ArrayList plots) {
        this.plots = plots;
    }
    
    public ArrayList getGroupNames() {
        ArrayList groupNames = new ArrayList();
        Iterator i = plots.iterator();
        while (i.hasNext()) {
            Plot plot = (Plot)i.next();
            if (!groupNames.contains(plot.group))
                groupNames.add(plot.group);
        }
        return groupNames;
    }
    
    public ArrayList getPlotNames() {
        ArrayList plotNames = new ArrayList();
        Iterator i = plots.iterator();
        while (i.hasNext()) {
            Plot plot = (Plot)i.next();
            plotNames.add(plot.name);
        }
        return plotNames;
    }
    
    public ArrayList getPlotNames(String group) {
        ArrayList plotNames = new ArrayList();
        Iterator i = plots.iterator();
        while (i.hasNext()) {
            Plot plot = (Plot)i.next();
            if (plot.group.equals(group))
                plotNames.add(plot.name);
        }
        return plotNames;
    }
    
    public ArrayList getGroupPlots(String group) {
        if (group.equals("All"))
            return plots;
        
        ArrayList groupPlots = new ArrayList();
        Iterator i = plots.iterator();
        while (i.hasNext()) {
            Plot plot = (Plot)i.next();
            if (plot.group.equals(group))
                groupPlots.add(plot);
        }
        return groupPlots;
    }
    
    public double getXMax() {
        return getXMax("All");
    }
    
    public double getXMax(String groupName) {
        ArrayList groupPlots = getGroupPlots(groupName);
        int nPlots = plots.size();
        double xMax = 0;
        for (int iPlot = 0; iPlot < nPlots; iPlot++) {
            Plot plot = (Plot)groupPlots.get(iPlot);
            double plotXMax = plot.getXMax();
            if (plotXMax > xMax)
                xMax = plotXMax;
        }
        return xMax;
    }
    
    public double getYMax() {
        return getYMax("All");
    }
    
    public double getYMax(String groupName) {
        ArrayList groupPlots = getGroupPlots(groupName);
        int nPlots = plots.size();
        double yMax = 0;
        for (int iPlot = 0; iPlot < nPlots; iPlot++) {
            Plot plot = (Plot)groupPlots.get(iPlot);
            double plotYMax = plot.getYMax();
            if (plotYMax > yMax)
                yMax = plotYMax;
        }
        return yMax;
    }
    
    

    public void setXAlign(int newXAlign) {
        revertXAlign();       
        double xOffset = 0.0;
        double xScale = 1.0;
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            switch (newXAlign) {
                case X_ALIGN_LEFT:
                    xOffset = xMaxLeftOffset - plot.getDetergentPeak().x;
                    break;
                    
                case X_ALIGN_RIGHT:
                    xOffset = xMaxRightOffset - plot.getEndPeak().x;
                    break;
                    
                case X_ALIGN_BOTH:
                    double xSpan = plot.getEndPeak().x 
                            - plot.getDetergentPeak().x;
                    xScale = xMaxSpan / xSpan;
                    xOffset = xMaxScaledLeftOffset 
                            - xScale * plot.getDetergentPeak().x;
                    break;
            }
            plot.xTransform(xOffset, xScale);
        }
    }
    
    public void revertXAlign() {
        // revert x values to initial values (prior to transforms)
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            plot.xRevert();
        }        
    }
    
    public void setYAlign(int yAlignType) {
        revertYAlign();
        double yOffset = 0.0;
        double yScale = 1.0;
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            switch (yAlignType) {
                case Y_ALIGN_TOP:
                    yOffset = yMaxTopOffset - plot.getDetergentPeak().y;
                    break;

                case Y_ALIGN_BOTTOM:
                    yOffset = yMaxBottomOffset - plot.yBaseline;
                    break;

                case Y_ALIGN_BOTH:
                    double ySpan = plot.getDetergentPeak().y - plot.yBaseline;
                    yScale = yMaxSpan / ySpan;
                    yOffset = yMaxScaledBottomOffset - yScale * plot.yBaseline;
                    break;
                    
                case Y_ALIGN_NORMAL:
                    yScale = 1.0 /  plot.getNormalArea();
                    yOffset = yMaxScaledBottomOffset - yScale * plot.yBaseline;
                    break;
            }
            plot.yTransform(yOffset, yScale);
        }        
    }
    
    public void revertYAlign() {
        // revert each y values to initial (prior transformation)
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            plot.yRevert();
        }
    }
    
 }
