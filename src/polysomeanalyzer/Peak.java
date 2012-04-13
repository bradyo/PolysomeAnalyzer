/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package polysomeanalyzer;

/**
 *
 * @author brady
 */
public class Peak {
    public String name = "peak";
    public double x = 0;
    public double y = 0;
    public double xLeft = 0;
    public double xRight = 0;
    public double sigma = 0;

    // initial values for revert
    private double xInitial = 0;
    private double xLeftInitial = 0;
    private double xRightInitial = 0;
    private double yInitial = 0;
    
    
    public Peak(String newName, double x, double y, double xLeft, 
            double xRight) {
        name = newName;
        xInitial = x;
        yInitial = y;
        this.x = x;
        this.y = y;
        
        xLeftInitial = xLeft;
        xRightInitial = xRight;
        this.xLeft = xLeft;
        this.xRight = xRight;
    } 
    
    public Peak(String newName, double x, double y) {
        name = newName;
        xInitial = x;
        yInitial = y;
        this.x = x;
        this.y = y;
        
        xLeftInitial = x;
        xRightInitial = x;
        this.xLeft = x;
        this.xRight = x;
    }
    
    public String toString() {
        return this.name;
    }
    
    public void setX(double x) {
        xInitial = x;
        this.x = x;
    }
    
    public void setXLeft(double xLeft) {
        xLeftInitial = xLeft;
        this.xLeft = xLeft;
    }
    
    public void setXRight(double xRight) {
        xRightInitial = xRight;
        this.xRight = xRight;
    }
    
    public void xTransform(double xOffset, double xScale) {
        x = xInitial * xScale + xOffset;
        xLeft = xLeftInitial * xScale + xOffset;
        xRight = xRightInitial * xScale + xOffset;
    }
    
    public void yTransform(double yOffset, double yScale) {
        y = yInitial * yScale + yOffset;
    }
    
    public void xRevert() {
        x = xInitial;
        xLeft = xLeftInitial;
        xRight = xRightInitial;
    }
    
    public void yRevert() {
        y  = yInitial;
    }
}
