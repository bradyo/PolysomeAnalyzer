/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package polysomeanalyzer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author brady
 */
public class Plotter extends javax.swing.JPanel{
    
    private String title = "Polysome Gradient Plots";
    private String xAxisLabel = "Time (points)";
    private String yAxisLabel = "Voltage";
    private Font font = new Font("arial", Font.PLAIN, 12);
    private boolean showTrendLine = false;
        
    private double xMin = 0;
    private double xMax = 400;
    private double yMin = 0;
    private double yMax = 1.8;

    private int xMinLoc; 
    private int xMaxLoc;
    private int yMinLoc;
    private int yMaxLoc;  
    private int xMajorDivisions = 5;
    private int yMajorDivisions = 5;
    private int xMinorDivisions = 10;
    private int yMinorDivisions = 10;

    static final int MODE_NORMAL = 0;
    static final int MODE_GAUSSIAN = 1;
    public int mode = MODE_NORMAL;
    
    private ArrayList plots = new ArrayList();
    private Plot selectedPlot = null;
    private Peak selectedPeak = null;
    
    private Color majorDivisionColor = Color.LIGHT_GRAY;
    private Color minorDivisionColor = new Color(230, 230, 230);
    private Color plotColor = Color.BLACK;
    private Color plotBoundaryColor = new Color(255, 128, 0);
    private Color plotBoundaryFillColor = new Color(255, 255, 20, 50);
    private Color peakColor = Color.BLUE;
    private Color peakBoundaryColor = Color.BLUE;
    private Color peakFillColor = new Color(0, 0, 255, 25);
    
    private int xMouseLoc; // location of mouse, given by gui
    private int yMouseLoc;
    private int xMouseDragStart;
    private int yMouseDragStart;
    static final int DRAG_ACTION_NONE = 0;
    static final int DRAG_ACTION_ZOOM = 1;
    static final int DRAG_ACTION_PLOT_START = 2;
    static final int DRAG_ACTION_PLOT_END = 3;
    static final int DRAG_ACTION_PLOT_BASELINE = 4;
    static final int DRAG_ACTION_PEAK = 5;
    static final int DRAG_ACTION_PEAK_LEFT = 6;
    static final int DRAG_ACTION_PEAK_RIGHT = 7;
    private int dragAction = DRAG_ACTION_NONE;
    private boolean isControlDown = false;
   
    
    public Plotter() {
        super();
        this.setFocusable(true);
        this.setMinimumSize(new Dimension(200, 200));
        
        // initialzie mouse listeners
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                mousePressedHandler(evt);
            }
            public void mouseReleased(MouseEvent evt) {
                mouseReleasedHandler(evt);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {}
            public void mouseDragged(MouseEvent evt) {
                 mouseDraggedHandler(evt);
            }
        });
       
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                keyPressedHandler(e);
            }
            public void keyReleased(KeyEvent e) {
                keyReleasedHandler(e);
            }
        });        
    }
    
    public ArrayList getPlots() {
        return plots;
    }
    public void setPlots(ArrayList newPlots) {
        plots = newPlots;
        selectedPlot = null;
        selectedPeak = null;
    }
    
    public Plot getSelectedPlot() {
        return selectedPlot;
    }    
    public void setSelectedPlot(Plot plot) {
        selectedPlot = plot;
    }
    
    public Peak getSelectedPeak() {
        return selectedPeak;
    }
    
    public void setSelectedPeak(Peak peak) {
        selectedPeak = peak;
    }
    
    public ArrayList getSelectedPlotPeaks() {
        if (selectedPlot == null)
            return new ArrayList();
        return selectedPlot.getPeaks();
    }
 
    public void setMode(int newMode) {
        this.mode = newMode;
    }
    
    public void showTrendLine(boolean show) {
        this.showTrendLine = show;
    }
        
    private FontMetrics setFont(Graphics g, String name, int style, int size) {
        g.setFont(new Font(name, style, size));
        return g.getFontMetrics();
    }
    
    public double getXValue(int xLoc) {
        double valPerPixel = (xMax - xMin) / (xMaxLoc - xMinLoc);
        return xMin + (xLoc - xMinLoc) * valPerPixel;
    }
    
    public double getYValue(int yLoc) {
        double valPerPixel = (yMax - yMin) / (yMaxLoc - yMinLoc);
        return yMax - (yLoc - yMinLoc) * valPerPixel;
    }
    
    public int getXLoc(double xVal) { 
        double pixelsPerVal = (xMaxLoc - xMinLoc) / (xMax - xMin);
        return (int)((xVal - xMin) * pixelsPerVal + xMinLoc);
    }
    
    public int getYLoc(double yVal) {
        double pixelsPerVal = (yMaxLoc - yMinLoc) / (yMax - yMin);
        return (int)(yMaxLoc - (yVal - yMin) * pixelsPerVal);
    }
    
    private String getValueString(double value) {
        // represent as rounded decimal
        DecimalFormat numberFormat = new DecimalFormat("0.00");
        return numberFormat.format(value);

    }

    private Color getTransparentColor(Color color, double fraction) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int t = color.getAlpha();
        if (fraction >= 0 && fraction <= 1) 
            t *= fraction;
        Color alphaColor = new Color(r, g, b, t);
        return alphaColor;
    }
    
    public void setWindow(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }
    
    public void setWindowXMin(double xMin) {
        this.xMin = xMin;
    }
    
    public void setWindowXMax(double xMax) {
        this.xMax = xMax;
    }
    
    public void setWindowYMin(double yMin) {
        this.yMin = yMin;
    }
    
    public void setWindowYMax(double yMax) {
        this.yMax = yMax;
    }
    
    public void resetWindow() {
        setWindow(0, getXMax(), 0, getYMax());
        repaint();
    }
    
    public double getXMax() {
        int nPlots = plots.size();
        double xMax = 0;
        for (int iPlot = 0; iPlot < nPlots; iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            double plotXMax = plot.getXMax();
            if (plotXMax > xMax)
                xMax = plotXMax;
        }
        return xMax;
    }
    
    public double getYMax() {
        int nPlots = plots.size();
        double yMax = 0;
        for (int iPlot = 0; iPlot < nPlots; iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            double plotYMax = plot.getYMax();
            if (plotYMax > yMax)
                yMax = plotYMax;
        }
        return yMax;
    }
    
    private int fitXLocInWindow(int xLoc) {
        if (xLoc < xMinLoc)
            xLoc = xMinLoc;
        if (xLoc > xMaxLoc)
            xLoc = xMaxLoc;
        return xLoc;         
    }
    
    private int fitYLocInWindow(int yLoc) {
        if (yLoc < yMinLoc)
            yLoc = yMinLoc;
        if (yLoc > yMaxLoc)
            yLoc = yMaxLoc;
        return yLoc;
    }
    
    private int min(int x1, int x2) {
        return (x1 < x2) ? x1 : x2;
    }
    
    private int max(int x1, int x2) {
        return (x1 > x2) ? x1 : x2;
    }

    private int getMaxPlotN() {
        int maxN = 0;
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            int n = plot.getXValues().length;
            if (n > maxN)
                maxN = n;
        }
        return maxN;
    }  
    
    private double getMaxPlotX() {
        double maxX = 0;
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            double[] xVals = plot.getXValues();
            double maxPlotX = xVals[xVals.length-1];
            if (maxPlotX > maxX)
                maxX = maxPlotX;
        }
        return maxX;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        adjustWindowLoc();
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(g);
        
        drawXMinorLines(g);
        drawYMinorLines(g);
        drawXMajorLines(g);
        drawYMajorLines(g);
        drawXMajorValues(g);
        drawYMajorValues(g);
        
        if (selectedPlot != null) {
            drawSelectedPlotFill(g);
            drawSelectedPlot(g);
            drawSelectedPlotBoundaries(g);
            if (showTrendLine == true)
                drawSelectedPlotTrendLine(g);
            drawSelectedPlotPeaks(g);
            if (mode == MODE_NORMAL) {
                if (selectedPeak != null) {      
                    drawSelectedPeakFill(g);
                    drawSelectedPeakBoundaries(g);
                    drawSelectedPeak(g);
                } else {
                    drawSelectedPlotPeaks(g);
                }
            } else if (mode == MODE_GAUSSIAN) {
                drawSelectedPeakGaussian(g);
            } 
        } else {
            drawPlots(g);
        }
        
        if (dragAction == DRAG_ACTION_ZOOM)
            drawMouseDrag(g);
            
        drawWindow(g);
        drawLabels(g); // title + axis labels
    }
    
    private void adjustWindowLoc() {
        // set up screen rectangle for graph window, make it perfectly
        // divisible by the tick markers
        xMinLoc = 50;
        xMaxLoc = getWidth() - 20;
        yMinLoc = 30;
        yMaxLoc = getHeight() - 60;
        
        // adjust the window size so that each interval fits perfectly in
        // its pixel area (prevents rounding error in value -> pixel location)
        int xLocRange = xMaxLoc - xMinLoc;
        xMaxLoc = (int)(xLocRange / xMajorDivisions) *
                xMajorDivisions + xMinLoc;
        int yLocRange = yMaxLoc - yMinLoc;
        yMaxLoc = (int)(yLocRange / yMajorDivisions) *
                yMajorDivisions + yMinLoc;
    }
    
    private void drawBackground(Graphics g) {
        // fill background white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth()-1, getHeight()-1);
        
        // outline graph with a black line
        g.setColor(Color.BLACK);
        g.drawRect(0,0, getWidth()-1, getHeight()-1);
    }
    
    private void drawWindow(Graphics g) {
        // draw graph window outline (2 pixels wide)
        g.setColor(Color.BLACK);
        g.drawRect(xMinLoc, yMinLoc, xMaxLoc - xMinLoc, yMaxLoc - yMinLoc);
        g.drawRect(xMinLoc - 1, yMinLoc - 1,
                xMaxLoc - xMinLoc + 2, yMaxLoc - yMinLoc + 2);
    }
    
    private void drawXMinorLines(Graphics g) {
        // draw minor lines linearly
        for (int i = 0; i < xMajorDivisions; i++) {
            double xVal = xMin + (xMax - xMin) / xMajorDivisions * i;
            for (int j = 0; j <= xMinorDivisions; j++) {
                double dx = (xMax - xMin) / xMajorDivisions / 
                        xMinorDivisions * j;
                int xLoc = getXLoc(xVal + dx);
                g.setColor(minorDivisionColor);
                g.drawLine(xLoc, yMinLoc, xLoc, yMaxLoc);
            }
        }
    }

    private void drawYMinorLines(Graphics g) {
        for (int i = 0; i < yMajorDivisions; i++) {
            double yVal = yMin + (yMax - yMin) / yMajorDivisions * i;
            for (int j = 0; j <= yMinorDivisions; j++) {
                double dy = (yMax - yMin) / yMajorDivisions / 
                        yMinorDivisions * j;
                int yLoc = getYLoc(yVal + dy);
                g.setColor(minorDivisionColor);
                g.drawLine(xMinLoc, yLoc, xMaxLoc, yLoc);
            }
        }
    }
           
    private void drawXMajorLines(Graphics g) {
        for (int i = 0; i < xMajorDivisions + 1; i++) {
            int xLoc = xMinLoc + (xMaxLoc - xMinLoc) / xMajorDivisions * i;
            if (xLoc == getXLoc(0)) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(majorDivisionColor);
            }
            g.drawLine(xLoc, yMinLoc, xLoc, yMaxLoc);
        }
    }

    private void drawYMajorLines(Graphics g) {
        for (int i = 0; i < yMajorDivisions+1; i++) {            
            int yLoc = yMaxLoc - (yMaxLoc - yMinLoc) / yMajorDivisions * i;
            if (yLoc == getYLoc(0)) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(majorDivisionColor);
            }
            g.drawLine(xMinLoc, yLoc, xMaxLoc, yLoc);
        }
    }

    private void drawXMajorValues(Graphics g) {
        for (int i = 0; i < xMajorDivisions + 1; i++) {
            int xLoc = xMinLoc + (xMaxLoc - xMinLoc) / xMajorDivisions * i;
            double xVal = getXValue(xLoc);
            
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            Graphics2D g2d = (Graphics2D)g;
            g2d.setColor(Color.BLACK);
            g2d.rotate(270.0 * Math.PI / 180.0, 0, 0);
            g2d.drawString(Integer.toString((int)xVal),
                    - fm.stringWidth(getValueString(xVal)) - yMaxLoc + 10,
                    xLoc + 4);
            g2d.rotate(-270.0 * Math.PI / 180.0);
        }
    }
  
    private void drawYMajorValues(Graphics g) {
        for (int i = 0; i < yMajorDivisions+1; i++) {
            int yLoc = yMaxLoc - (yMaxLoc - yMinLoc) / yMajorDivisions * i;
            double yVal = getYValue(yLoc);
            
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            g.setColor(Color.BLACK);
            g.drawString(getValueString(yVal),
                    xMinLoc - fm.stringWidth(getValueString(yVal)) - 2,
                    yLoc + fm.getHeight() / 2 - 2);
        }
    }
    
    private void drawLabels(Graphics g) {
        // draw title
        g.setColor(Color.BLACK);
        g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
        int x= getWidth() / 2 - fm.stringWidth(title) / 2;
        int y = yMinLoc / 2 + fm.getHeight() / 2;
        g.drawString(title, x, y);
        
        // draw x axis
        x = getWidth() / 2 - fm.stringWidth(xAxisLabel) / 2;
        y = getHeight() - fm.getHeight();
        g.setColor(Color.BLACK);
        g.drawString(xAxisLabel, x, y);
        
        // draw y axis (rotated using g2d)
        x = -getHeight() / 2 - fm.stringWidth(yAxisLabel) / 2;
        y = fm.getHeight();
        Graphics2D g2d = (Graphics2D)g;
        g2d.rotate(270.0* Math.PI / 180.0);
        g2d.drawString(yAxisLabel, x, y);
        g2d.rotate(-270.0* Math.PI / 180.0);
    }

    private void drawPlots(Graphics g) {        
        int xLocWidth = xMaxLoc - xMinLoc;
        int n = xLocWidth / 2 + 1;
        int[] xLoc = new int[n+1];
        int[] yLoc = new int[n+1];
        for (int iPlot = 0; iPlot < plots.size(); iPlot++) {
            Plot plot = (Plot)plots.get(iPlot);
            for (int i = 0; i <= n; i++) {
                xLoc[i] = (int)((double)xLocWidth / n * i + xMinLoc);
                double x = getXValue(xLoc[i]);
                double y = plot.getY(x);
                yLoc[i] = getYLoc(y);
                
                xLoc[i] = fitXLocInWindow(xLoc[i]);
                yLoc[i] = fitYLocInWindow(yLoc[i]);
            }
            // set color (transparent if plot is selected);
            Color tColor = plotColor;
            if (selectedPlot != null)
                tColor = getTransparentColor(plotColor, 0.25);
            g.setColor(tColor);
            g.drawPolyline(xLoc, yLoc, n);
        }
    } 
    
    private void drawSelectedPlot(Graphics g) {
        if (selectedPlot == null) 
            return;
        
        // draw plot curve
        int xLocWidth = xMaxLoc - xMinLoc;
        int n = xLocWidth / 2 + 1;
        int[] xLoc = new int[n+1];
        int[] yLoc = new int[n+1]; 
        for (int i = 0; i <= n; i++) {
            xLoc[i] = (int)((double)xLocWidth / n * i + xMinLoc);
            double x = getXValue(xLoc[i]);
            double y = selectedPlot.getY(x);
            yLoc[i] = getYLoc(y);
            
            xLoc[i] = fitXLocInWindow(xLoc[i]);
            yLoc[i] = fitYLocInWindow(yLoc[i]);
        }
        g.setColor(plotColor);
        g.drawPolyline(xLoc, yLoc, n);
    }
        
    private void drawSelectedPlotFill(Graphics g) {
        if (selectedPlot == null)
            return;     

        // draw plot curve
        int xStart = getXLoc(selectedPlot.xStart);
        int xEnd = getXLoc(selectedPlot.xEnd);
        int xLocWidth = xEnd - xStart;
        int n = xLocWidth / 2 + 1;
        int[] xLoc = new int[n+4];
        int[] yLoc = new int[n+4]; 
        for (int i = 0; i <= n; i++) {
            xLoc[i] = (int)((double)xLocWidth / n * i + xStart);
            double x = getXValue(xLoc[i]);
            double y = selectedPlot.getY(x);
            yLoc[i] =  getYLoc(y);
            
            xLoc[i] = fitXLocInWindow(xLoc[i]);
            yLoc[i] = fitYLocInWindow(yLoc[i]);
        }
        xLoc[n+1] = xLoc[n];
        yLoc[n+1] = fitYLocInWindow(getYLoc(selectedPlot.yBaseline));
        xLoc[n+2] = xLoc[0];
        yLoc[n+2] = fitYLocInWindow(getYLoc(selectedPlot.yBaseline));
        xLoc[n+3] = fitXLocInWindow(xLoc[0]);
        yLoc[n+3] = fitYLocInWindow(yLoc[0]);
        
        g.setColor(plotBoundaryFillColor);
        g.fillPolygon(xLoc, yLoc, n+3);
    }  
    
    private void drawSelectedPlotTrendLine(Graphics g) {
        if (selectedPlot == null)
            return;
        
        // do a regression on peaks 2 (80S) --> plot right boundary
        ArrayList peaks = selectedPlot.getBoundPeaks();
        if (peaks.size() <= 2)
            return;
        
        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        int n = 0;
        for (int i = 2; i < peaks.size(); i++) {
            Peak peak = (Peak)peaks.get(i);
            System.err.println("x = " + peak.x + ", y = " + peak.y);
            if (peak.x > selectedPlot.xEnd)
                break;
            sumX += peak.x;
            sumY += peak.y;
            sumXX += peak.x * peak.x;
            sumXY += peak.x * peak.y;
            n++;
        }
        System.err.println();
        double m = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double b = (sumY - m * sumX) / n;
        
        double x1 = xMin;
        double x2 = xMax;
        double y1 = m * x1 + b;
        double y2 = m * x2 + b;
        g.setColor(Color.BLACK);
        drawDashedLine(g, getXLoc(x1), getYLoc(y1), getXLoc(x2), getYLoc(y2), 10, 10);
    }
    
    private void drawSelectedPlotBoundaries(Graphics g) {
        if (selectedPlot == null)
            return;
        
        // draw baseline
        g.setColor(plotBoundaryColor);
        int o = 0; // offset so you can see line off window
        int yLoc = getYLoc(selectedPlot.yBaseline);
        if (yLoc > yMaxLoc)
            drawDashedLine(g, xMinLoc, yMaxLoc+o, xMaxLoc, yMaxLoc+o, 5, 5);
        else if (yLoc < yMinLoc)
            drawDashedLine(g, xMinLoc, yMinLoc-o, xMaxLoc, yMinLoc-o, 5, 5);
        else
            drawDashedLine(g, xMinLoc, yLoc, xMaxLoc, yLoc, 5, 5);
        
        // draw start
        int xStart = getXLoc(selectedPlot.xStart);
        if (xStart > xMaxLoc)
            drawDashedLine(g, xMaxLoc+o, yMinLoc, xMaxLoc+o, yMaxLoc, 5, 5);
        else if (xStart < xMinLoc)
            drawDashedLine(g, xMinLoc-o, yMinLoc, xMinLoc-o, yMaxLoc, 5, 5);
        else
            drawDashedLine(g, xStart, yMinLoc, xStart, yMaxLoc, 5, 5);
 
        // draw end
        int xEnd = getXLoc(selectedPlot.xEnd);
        if (xEnd > xMaxLoc)
            drawDashedLine(g, xMaxLoc+o, yMinLoc, xMaxLoc+o, yMaxLoc, 5, 5);
        else if (xEnd < xMinLoc)
            drawDashedLine(g, xMinLoc-o, yMinLoc, xMinLoc-o, yMaxLoc, 5, 5);
        else
            drawDashedLine(g, xEnd, yMinLoc, xEnd, yMaxLoc, 5, 5);
        
        // draw area label
        Font bold = new Font(font.getName(), Font.BOLD, font.getSize());
        g.setFont(bold);
        FontMetrics fm = g.getFontMetrics(bold);
        g.setColor(Color.BLACK);
        double area = selectedPlot.getArea(selectedPlot.xStart, selectedPlot.xEnd);
        String str = "area = " + getValueString(area);
        int width = fm.stringWidth(str);
        int xLoc = (xEnd - xStart) / 2 + xStart - width / 2;
        yLoc = min(yMaxLoc, yLoc);
        if (yLoc == yMaxLoc) {
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(xLoc - 2, yLoc + 10, width + 4, fm.getAscent() + 4);
        }
        g.setColor(Color.BLACK);
        g.drawString(str, xLoc, yLoc + fm.getAscent() + 10);
    }
    
    private void drawSelectedPlotPeaks(Graphics g) {
        if (selectedPlot == null)
            return;
        
        // set color (transparent if peak is selected);
        Color tColor = peakColor;
        if (selectedPeak != null) {
            tColor = getTransparentColor(peakColor, 0.25);
        }
        FontMetrics fm = g.getFontMetrics(font);
        
        ArrayList peaks = selectedPlot.getPeaks();
        for (int i = 0; i < peaks.size(); i++) {
            Peak peak = (Peak)peaks.get(i);
            int xLoc = getXLoc(peak.x);
            int yLoc = getYLoc(peak.y);
            if (xLoc < xMinLoc || xLoc > xMaxLoc 
                    || yLoc < yMinLoc || yLoc > yMaxLoc)
                continue;
            
            // draw a circle at peak
            g.setColor(tColor);
            g.fillOval(xLoc-3, yLoc-3, 7, 7);
            g.setColor(Color.BLACK);
            g.drawOval(xLoc-3, yLoc-3, 7, 7);
            
            // draw label
            if (selectedPeak != null && selectedPeak == peak) {
                Font boldFont = new Font(font.getName(), Font.BOLD, 
                        font.getSize());
                g.setFont(boldFont);
            } else {
                g.setFont(font);
            }
            int x = -getYLoc(peak.y) + 5;
            int y = getXLoc(peak.x) + fm.getAscent()  /2;
            Graphics2D g2d = (Graphics2D)g;
            g.setColor(Color.BLACK);
            g2d.rotate(270.0* Math.PI / 180.0);
            g2d.drawString(peak.name, x, y);
            g2d.rotate(-270.0* Math.PI / 180.0);
        }
    }
   
    private void drawSelectedPeak(Graphics g) {
        if (selectedPeak == null)
            return;
        
        int xLoc = getXLoc(selectedPeak.x);
        int yLoc = getYLoc(selectedPeak.y);
        if (xLoc < xMinLoc || xLoc > xMaxLoc 
                || yLoc < yMinLoc || yLoc > yMaxLoc)
            return;
        g.setColor(peakColor);
        g.fillOval(xLoc-3, yLoc-3, 7, 7);
        g.setColor(Color.BLACK);
        g.drawOval(xLoc-3, yLoc-3, 7, 7);
    }
    
    private void drawSelectedPeakBoundaries(Graphics g) {
        if (selectedPeak == null)
            return;
        
        // draw left line
        g.setColor(peakBoundaryColor);
        int peakLeftLoc = getXLoc(selectedPeak.xLeft);
        if (peakLeftLoc > xMinLoc && peakLeftLoc < xMaxLoc)
            drawDashedLine(g, peakLeftLoc, yMinLoc, peakLeftLoc, yMaxLoc, 5, 5);
        
        // draw right line
        int peakRightLoc = getXLoc(selectedPeak.xRight);
        if (peakRightLoc > xMinLoc && peakRightLoc < xMaxLoc)
            drawDashedLine(g, peakRightLoc, yMinLoc, peakRightLoc, yMaxLoc, 5, 5);
        
        Font bold = new Font(font.getName(), Font.BOLD, font.getSize());
        g.setFont(bold);
        FontMetrics fm = g.getFontMetrics(bold);
        g.setColor(Color.BLACK);
        double area = selectedPlot.getArea(selectedPeak.xLeft, selectedPeak.xRight);
        String str = "area = " + getValueString(area);
        int width = fm.stringWidth(str);
        int xLoc = (peakRightLoc - peakLeftLoc) / 2 + peakLeftLoc;
        int yLoc = min(yMaxLoc, getYLoc(selectedPlot.yBaseline));
        g.drawString(str, xLoc - width/2, yLoc - fm.getDescent());
    }
    
    private void drawSelectedPeakFill(Graphics g) {
        if (selectedPeak == null)
            return;
        
        int xStart = getXLoc(selectedPeak.xLeft);
        int xEnd = getXLoc(selectedPeak.xRight);
        int xLocWidth = xEnd - xStart;
        int n = xLocWidth / 2 + 1;
        int[] xLoc = new int[n+4];
        int[] yLoc = new int[n+4]; 
        for (int i = 0; i <= n; i++) {
            xLoc[i] = (int)((double)xLocWidth / n * i + xStart);
            double x = getXValue(xLoc[i]);
            double y = selectedPlot.getY(x);
            yLoc[i] =  getYLoc(y);
            
            xLoc[i] = fitXLocInWindow(xLoc[i]);
            yLoc[i] = fitYLocInWindow(yLoc[i]);
        }
        xLoc[n+1] = xLoc[n];
        yLoc[n+1] = fitYLocInWindow(getYLoc(selectedPlot.yBaseline));
        xLoc[n+2] = xLoc[0];
        yLoc[n+2] = fitYLocInWindow(getYLoc(selectedPlot.yBaseline));
        xLoc[n+3] = fitXLocInWindow(xLoc[0]);
        yLoc[n+3] = fitYLocInWindow(yLoc[0]);
        
        g.setColor(peakFillColor);
        g.fillPolygon(xLoc, yLoc, n+3);
    }
    
    private void drawSelectedPeakGaussian(Graphics g) {
        if (selectedPeak == null)
            return;
        
        // draw left peak guassian
        ArrayList peaks = selectedPlot.getPeaks();
        for (int iPeak = 1; iPeak < peaks.size()-1; iPeak++) {
            Peak peak = (Peak)peaks.get(iPeak);
            Peak prevPeak = (Peak)peaks.get(iPeak-1);
            Peak nextPeak = (Peak)peaks.get(iPeak+1);
            
            if (peak.equals(selectedPeak)) {
                // draw guassian curves for previous, current, and next peaks
                drawGaussian(g, prevPeak);
                drawGaussian(g, peak);
                drawGaussian(g, nextPeak);
                
                drawGaussianSum(g, prevPeak, peak, nextPeak);
            }
        }
    }
    
    private void drawGaussian(Graphics g, Peak peak) {
        double u = peak.x;
        double s = selectedPlot.getSigmas(peak)[1];
        
        int xLocWidth = xMaxLoc - xMinLoc;
        int n = xLocWidth / 2 + 1;
        int[] xLoc = new int[n+1];
        int[] yLoc = new int[n+1]; 
        for (int i = 0; i <= n; i++) {
            xLoc[i] = (int)((double)xLocWidth / n * i + xMinLoc);
            double x = getXValue(xLoc[i]);
            double y = (1.0 / (2.0 * s * Math.PI)) 
                    * Math.exp(-(x - u)*(x - u) / (2.0 * s * s))
                    + selectedPlot.yBaseline;
            y = (selectedPlot.getY(u) - selectedPlot.yBaseline)
                    * Math.exp(-(x - u)*(x - u) / (2.0 * s * s))
                    + selectedPlot.yBaseline;
            yLoc[i] =  getYLoc(y);
        }
        g.setColor(plotColor);
        g.drawPolyline(xLoc, yLoc, n);
    }
    
    private void drawGaussianSum(Graphics g, Peak p1, Peak p2, Peak p3) {
        double u1 = p1.x;
        double u2 = p2.x;
        double u3 = p3.x;
        
        double[] sigmas = selectedPlot.getSigmas(p2);
        double s1 = sigmas[0];
        double s2 = sigmas[1];
        double s3 = sigmas[2];
        
        int xLocWidth = xMaxLoc - xMinLoc;
        int n = xLocWidth / 2 + 1;
        int[] xLoc = new int[n+1];
        int[] yLoc = new int[n+1]; 
        for (int i = 0; i <= n; i++) {
            xLoc[i] = (int)((double)xLocWidth / n * i + xMinLoc);
            double x = getXValue(xLoc[i]);
            
            double y = selectedPlot.yBaseline;
            y += (selectedPlot.getY(u1) - selectedPlot.yBaseline)
                    * Math.exp(-(x - u1)*(x - u1) / (2.0 * s1 * s1));
            y += (selectedPlot.getY(u2) - selectedPlot.yBaseline)
                    * Math.exp(-(x - u2)*(x - u2) / (2.0 * s2 * s2));
            y += (selectedPlot.getY(u3) - selectedPlot.yBaseline)
                    * Math.exp(-(x - u3)*(x - u3) / (2.0 * s3 * s3));

            yLoc[i] =  getYLoc(y);
        }
        g.setColor(Color.RED);
        g.drawPolyline(xLoc, yLoc, n);
    }
    
    private void drawMouseDrag(Graphics g) {
        // draw dragging rectangle
        int xLeft = min(xMouseDragStart, xMouseLoc);
        int xRight = max(xMouseDragStart, xMouseLoc);
        int yTop = min(yMouseDragStart, yMouseLoc);
        int yBottom = max(yMouseDragStart, yMouseLoc);
        int w = xRight - xLeft;
        int h = yBottom - yTop;
        g.setColor(Color.BLACK);
        drawDashedLine(g, xLeft, yTop, xLeft, yTop+h, 5, 5);
        drawDashedLine(g, xLeft+w, yTop, xLeft+w, yTop+h, 5, 5);
        drawDashedLine(g, xLeft, yTop, xLeft+w, yTop, 5, 5);
        drawDashedLine(g, xLeft, yTop+h, xLeft+w, yTop+h, 5, 5);
    }
    
    private void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2,
            double dashlength, double spacelength) {
        if ((x1 == x2) && (y1 == y2)) {
            g.drawLine(x1, y1, x2, y2);
            return;
        }
        double linelength=Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
        double xincdashspace=(x2-x1)/(linelength/(dashlength+spacelength));
        double yincdashspace=(y2-y1)/(linelength/(dashlength+spacelength));
        double xincdash=(x2-x1)/(linelength/(dashlength));
        double yincdash=(y2-y1)/(linelength/(dashlength));
        int counter=0;
        for (double i=0;i<linelength-dashlength;i+=dashlength+spacelength){
            g.drawLine((int) (x1+xincdashspace*counter),
                    (int) (y1+yincdashspace*counter),
                    (int) (x1+xincdashspace*counter+xincdash),
                    (int) (y1+yincdashspace*counter+yincdash));
            counter++;
        }
        if ((dashlength+spacelength)*counter<=linelength)
            g.drawLine((int) (x1+xincdashspace*counter),
                    (int) (y1+yincdashspace*counter),
                    x2,y2);
    }
    

    public void mousePressedHandler(MouseEvent e) {   
        int x = e.getX();
        int y = e.getY();
        xMouseLoc = e.getX();
        yMouseLoc = e.getY();
        dragAction = DRAG_ACTION_NONE; // reset
        
        switch (e.getButton()) {
            case MouseEvent.BUTTON1: // left button  
                // left drag may do various things depending on what on
                if (isControlDown == true) {
                    // right click on plot adds peak
                    Plot hitPlot = getPlotHit(e.getX(), e.getY());
                    if (hitPlot == selectedPlot && selectedPlot != null) {
                        selectedPeak = selectedPlot.addPeak(getXValue(e.getX()));
                    } else {
                        // right click elsewhere resets zoom
                        resetWindow();
                    }
                }
                else if (getPeakHit(x, y) != null && getPeakHit(x, y) == selectedPeak) 
                    dragAction = DRAG_ACTION_PEAK;
                else if (selectedPeakLeftHit(x, y))
                    dragAction = DRAG_ACTION_PEAK_LEFT;            
                else if (selectedPeakRightHit(x, y))
                    dragAction = DRAG_ACTION_PEAK_RIGHT; 
                else if (selectedPlotBaselineHit(x, y))
                    dragAction = DRAG_ACTION_PLOT_BASELINE;
                else if (selectedPlotStartHit(x, y))
                    dragAction = DRAG_ACTION_PLOT_START;
                else if (selectedPlotEndHit(x, y))
                    dragAction = DRAG_ACTION_PLOT_END;
                else {
                    // left click selects peak
                    Peak hitPeak = getPeakHit(x, y);
                    if (hitPeak != null) {
                        selectedPeak = hitPeak;
                        break;
                    }

                    // left click selects a plot
                    Plot hitPlot = getPlotHit(e.getX(), e.getY());
                    if (selectedPlot == null && hitPlot != null 
                            || selectedPlot == hitPlot) {
                        selectedPlot = hitPlot;
                        selectedPeak = null;
                        break;
                    } 
                    
                    // letf click elsewhere initiates zoom
                    dragAction = DRAG_ACTION_ZOOM;
                }
                break;

            case MouseEvent.BUTTON3: // right button
                // right click on plot adds peak
                Plot hitPlot = getPlotHit(e.getX(), e.getY());
                if (hitPlot == selectedPlot && selectedPlot != null) {
                    selectedPeak = selectedPlot.addPeak(getXValue(e.getX()));
                    break;
                }
                
                // right click elsewhere resets zoom
                resetWindow();
                break;
        }
        repaint();
                
        // save drag start coordinates
        xMouseDragStart = e.getX();
        yMouseDragStart = e.getY();
    }
    
    public void mouseReleasedHandler(MouseEvent e) {
        // perform relase actions
        switch (dragAction) {
            case DRAG_ACTION_PEAK:
                // recalculate left and right
                if (selectedPlot != null && selectedPeak != null)
                    selectedPlot.initializePeakBoundaries(selectedPeak);
                break;
            
            case DRAG_ACTION_ZOOM:
                if (Math.abs(xMouseDragStart - xMouseLoc) < 10 ||
                        Math.abs(yMouseDragStart - yMouseLoc) < 10) {
                    // window too small, deselect plot rather than zoom
                    selectedPeak = null;
                    selectedPlot = null;
                    break;
                }
                
                double x2 = getXValue(xMouseDragStart);
                double x1 = getXValue(xMouseLoc);
                double y2 = getYValue(yMouseDragStart);
                double y1 = getYValue(yMouseLoc);
                double xLeft = (x2 < x1) ? x2 : x1;
                double xRight = (x2 > x1) ? x2 : x1;  
                double yBottom = (y2 < y1) ? y2 : y1;
                double yTop = (y2 > y1) ? y2 : y1;  
                setWindow(xLeft, xRight, yBottom, yTop);
                break;
        }
        
        // done dragging
        dragAction = DRAG_ACTION_NONE;
        repaint();
    }
    
    public void mouseDraggedHandler(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        switch (dragAction) {
            case DRAG_ACTION_PLOT_START:
                if (selectedPlot != null) {
                    if (getXValue(x) < selectedPlot.xEnd)
                        selectedPlot.xStart = getXValue(x);
                    else
                        selectedPlot.xStart = selectedPlot.xEnd;
                }
                break;
                
            case DRAG_ACTION_PLOT_END:
                if (selectedPlot != null) {
                    if (getXValue(x) > selectedPlot.xStart)
                        selectedPlot.xEnd = getXValue(x);
                    else
                        selectedPlot.xEnd = selectedPlot.xStart;
                }
                break;
                
            case DRAG_ACTION_PLOT_BASELINE:
                if (selectedPlot != null) {
                    if (getYValue(y) >= 0)
                        selectedPlot.yBaseline = getYValue(y);
                    else
                        selectedPlot.yBaseline = 0;
                }
                break;
                
            case DRAG_ACTION_PEAK_LEFT:
                if (selectedPeak != null) {
                    if (getXValue(x) < selectedPeak.xRight)
                        selectedPeak.xLeft = getXValue(x);
                    else
                        selectedPeak.xLeft = selectedPeak.xRight;
                }
                break;
                    
            case DRAG_ACTION_PEAK_RIGHT:
                if (selectedPeak != null) {
                    if (getXValue(x) > selectedPeak.xLeft)
                        selectedPeak.xRight = getXValue(x);
                    else
                        selectedPeak.xRight = selectedPeak.xLeft;
                }
                break;
                
            case DRAG_ACTION_PEAK:
                if (selectedPeak != null) {
                    selectedPeak.x = getXValue(x);
                    selectedPeak.y = selectedPlot.getY(selectedPeak.x);
                }
                break;
                
            case DRAG_ACTION_ZOOM:
                // perform action on release, not drag
                break;                
        }
        
        xMouseLoc = e.getX();
        yMouseLoc = e.getY();
        repaint();
    }      
    
    public Peak getPeakHit(int xHitLoc, int yHitLoc) {
        if (selectedPlot == null) 
            return null;
        
        ArrayList peaks = selectedPlot.getPeaks();
        Iterator i = peaks.iterator();
        while (i.hasNext()) {
            Peak peak = (Peak)i.next();
            
            // d = sqrt((x2 - x1)^2 + (y2 - y1)^2)
            int xLoc = getXLoc(peak.x);
            int yLoc = getYLoc(peak.y);
            int dx = Math.max(Math.abs(xLoc), Math.abs(xHitLoc)) - 
                    Math.min(Math.abs(xLoc), Math.abs(xHitLoc));
            int dy = Math.max(Math.abs(yLoc), Math.abs(yHitLoc)) - 
                    Math.min(Math.abs(yLoc), Math.abs(yHitLoc));
            double d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
            int r = 5; // pixel threshold
            if (d < r)                     
                return peak;
        }
        return null;
    }
    
    public Plot getPlotHit(int xHitLoc, int yHitLoc) {
        int r = 5; // pixel threshold
        Iterator i = plots.iterator();
        while (i.hasNext()) {
            Plot plot = (Plot)i.next();
            for (int xLoc = xHitLoc - r; xLoc < xHitLoc + r; xLoc++) {
                // d = sqrt((x2 - x1)^2 + (y2 - y1)^2)
                int yLoc = getYLoc(plot.getY(getXValue(xLoc)));
                int dx = Math.max(Math.abs(xLoc), Math.abs(xHitLoc)) - 
                        Math.min(Math.abs(xLoc), Math.abs(xHitLoc));
                int dy = Math.max(Math.abs(yLoc), Math.abs(yHitLoc)) - 
                        Math.min(Math.abs(yLoc), Math.abs(yHitLoc));
                double d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                if (d < r)                     
                    return plot;
            }
        }
        return null;
    }
    
    public boolean selectedPlotStartHit(int xHitLoc, int yHitLoc) {
        if (selectedPlot == null)
            return false;
        
        int xLine = getXLoc(selectedPlot.xStart);
        if (xLine > xHitLoc - 3 && xLine < xHitLoc + 3)
            return true;
        else        
            return false;
    }
    
    public boolean selectedPlotEndHit(int xHitLoc, int yHitLoc) {
        if (selectedPlot == null)
            return false;
        
        int xLine = getXLoc(selectedPlot.xEnd);
        if (xLine > xHitLoc - 3 && xLine < xHitLoc + 3)
            return true;
        else        
            return false;
    }
    
    public boolean selectedPlotBaselineHit(int xHitLoc, int yHitLoc) {
        if (selectedPlot == null)
            return false;
        
        int yLine = getYLoc(selectedPlot.yBaseline);
        if (yLine > yHitLoc - 3 && yLine < yHitLoc + 3)
            return true;
        else        
            return false;
    }
    
    public boolean selectedPeakLeftHit(int xHitLoc, int yHitLoc) {
        if (selectedPlot == null || selectedPeak == null)
            return false;
        
        int xLine = getXLoc(selectedPeak.xLeft);
        if (xLine > xHitLoc - 3 && xLine < xHitLoc + 3)
            return true;
        else        
            return false;
    }
    
    public boolean selectedPeakRightHit(int xHitLoc, int yHitLoc) {
        if (selectedPeak == null)
            return false;
        
        int xLine = getXLoc(selectedPeak.xRight);
        if (xLine > xHitLoc - 3 && xLine < xHitLoc + 3)
            return true;
        else        
            return false;
    }
    
    public void keyPressedHandler(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_UNDERSCORE:
                if (selectedPlot != null && selectedPeak != null) {
                    selectedPlot.getPeaks().remove(selectedPeak);
                    selectedPeak = null;
                    repaint();
                }
                break;
            case KeyEvent.VK_CONTROL:
                isControlDown = true;
                break;
        }
    }
    
    public void keyReleasedHandler(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
                isControlDown = false;
                break;
        }
    }
}
