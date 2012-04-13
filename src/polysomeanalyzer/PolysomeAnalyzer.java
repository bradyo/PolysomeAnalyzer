/*
 * PolysomeAnalyzer.java
 *
 * Created on November 6, 2008, 2:37 PM
 */

package polysomeanalyzer;

import java.awt.GridBagConstraints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JFileChooser;

/**
 *
 * @author  brady
 */
public class PolysomeAnalyzer extends javax.swing.JFrame {

    private PlotManager plotManager = new PlotManager();
    private Plotter plotter = new Plotter();
    
    // flags to temporarily block event firing on change
    private boolean silenceGroupCombo = false;
    private boolean silencePlotCombo = false; 
    private boolean silencePeakCombo = false;
    private boolean silenceXAlignmentCombo = false;
    private boolean silenceYAlignmentCombo = false;
    private boolean ctrlDown = false;
    
    /** Creates new form PolysomeAnalyzer */
    public PolysomeAnalyzer() {
        initComponents();

        // add plotter panel to plot tab
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jRawOutputTab.add(jOutputTextPanel, gridBagConstraints);
        jPlotTab.add(plotter, gridBagConstraints);
        
        plotter.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                plotterMousePressedHandler(evt);
            }
            public void mouseReleased(MouseEvent evt) {
                plotterMouseReleasedHandler(evt);
            }
        });
        
        plotter.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                plotterKeyPressedHandler(evt);
            }
        });

    }
    
    private void updateOutputText() {
        String output = "";
        if (jGroupCombo.getSelectedIndex() <= 0) {
            output += plotManager.getOutput();
            output += "\n\n=================================================\n\n";
            output += plotManager.getAnalysisOutput();
        } else {
            String group = (String)jGroupCombo.getSelectedItem();
            output += plotManager.getOutput(group);
            output += "\n\n=================================================\n\n";
            output += plotManager.getAnalysisOutput(group);
        }
        jRawOutputText.setText(output);
    }
    
    private void plotterMousePressedHandler(MouseEvent e) {
        plotter.requestFocus();
        // update controls       
        Plot selectedPlot = plotter.getSelectedPlot();
        if (selectedPlot == null) {
            silencePlotCombo = true;
            jPlotCombo.setSelectedIndex(0);
            jPlotGroupText.setText("");
            jPlotNameText.setText("");
            silencePlotCombo = false;
            
            silencePeakCombo = true;
            jPeakCombo.removeAllItems();
            jPeakCombo.addItem("All");
            jPeakNameText.setText("");
            silencePeakCombo = false;
        } else {
            silencePlotCombo = true;
            jPlotCombo.setSelectedItem(selectedPlot);
            jPlotGroupText.setText(selectedPlot.group);
            jPlotNameText.setText(selectedPlot.name);
            silencePlotCombo = false;
            
            // clear peaks list ( no plot selected)
            silencePeakCombo = true;
            jPeakCombo.removeAllItems();
            jPeakCombo.addItem("All");
            ArrayList peaks = selectedPlot.getPeaks();
            for (int i = 0; i < peaks.size(); i++) {
                jPeakCombo.addItem(peaks.get(i));
            }
            if (plotter.getSelectedPeak() != null) {
                jPeakCombo.setSelectedItem(plotter.getSelectedPeak());
                jPeakNameText.setText(plotter.getSelectedPeak().name);
            } else {
                jPeakCombo.setSelectedIndex(0);
                jPeakNameText.setText("");
            }
            silencePeakCombo = false;
        }
    }
    
    private void plotterMouseReleasedHandler(MouseEvent e) {
        // update controls       
        Plot selectedPlot = plotter.getSelectedPlot();
        if (selectedPlot != jPlotCombo.getSelectedItem() && selectedPlot == null) {
            silencePlotCombo = true;
            jPlotCombo.setSelectedIndex(0);
            jPlotGroupText.setText("");
            jPlotNameText.setText("");
            silencePlotCombo = false;
            
            silencePeakCombo = true;
            jPeakCombo.removeAllItems();
            jPeakCombo.addItem("All");
            jPeakNameText.setText("");
            silencePeakCombo = false;
        }
    }
    
    private void plotterKeyPressedHandler(KeyEvent e) {
        // update controls       
        updatePeakControls();
    }

    private void updateGroupControls() {
        silenceGroupCombo = true;
        jGroupCombo.removeAllItems();
        jGroupCombo.addItem("All");
        jGroupCombo.setSelectedIndex(0);
        ArrayList groupNames = plotManager.getGroupNames();
        for (int iGroup = 0; iGroup < groupNames.size(); iGroup++) {
            jGroupCombo.addItem((String)groupNames.get(iGroup));
        }
        silenceGroupCombo = false;
        jXAlignCombo.setSelectedIndex(0);
        jYAlignCombo.setSelectedIndex(0);
        updatePlotControls();
    }
    
    private void updatePlotControls() {
        // update plot controls with plotter plots
        silencePlotCombo = true;
        jPlotCombo.removeAllItems();
        jPlotCombo.addItem("All");
        jPlotCombo.setSelectedIndex(0);
        ArrayList plots = plotter.getPlots();
        for (int i = 0; i < plots.size(); i++) {
            jPlotCombo.addItem(plots.get(i));
        }
        Plot selectedPlot = plotter.getSelectedPlot();
        if (selectedPlot != null) {
            jPlotCombo.setSelectedItem(selectedPlot);
            jPlotNameText.setText(selectedPlot.name);
            jPlotGroupText.setText(selectedPlot.group);
        } else {
            jPlotCombo.setSelectedIndex(0);
            jPlotNameText.setText("");
            jPlotGroupText.setText("");
        }
        silencePlotCombo = false;
        
        // plot may have changed, update peak controls
        updatePeakControls();
    }
    
    private void updatePeakControls() {
        // update plot controls
        silencePeakCombo = true;
        jPeakCombo.removeAllItems();
        jPeakCombo.addItem("All");
        ArrayList peaks = plotter.getSelectedPlotPeaks();
        for (int i = 0; i < peaks.size(); i++) {
            jPeakCombo.addItem(peaks.get(i));
        }
        Peak selectedPeak = plotter.getSelectedPeak();
        if (selectedPeak != null) {
            jPeakCombo.setSelectedItem(selectedPeak);
            jPeakNameText.setText(selectedPeak.name);
        } else {
            jPeakCombo.setSelectedIndex(0);
            jPeakNameText.setText("");
        }
        silencePeakCombo = false;    
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jImportChooser = new javax.swing.JFileChooser();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabPane = new javax.swing.JTabbedPane();
        jPlotTab = new javax.swing.JPanel();
        jRawOutputTab = new javax.swing.JPanel();
        jOutputTextPanel = new javax.swing.JScrollPane();
        jRawOutputText = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jControlPanel = new javax.swing.JPanel();
        jMainPanel = new javax.swing.JPanel();
        jImportButton = new javax.swing.JButton();
        jGroupPanel = new javax.swing.JPanel();
        jGroupLabel = new javax.swing.JLabel();
        jGroupCombo = new javax.swing.JComboBox();
        jXAlignLabel = new javax.swing.JLabel();
        jXAlignCombo = new javax.swing.JComboBox();
        jYAlignCombo = new javax.swing.JComboBox();
        jYAlignLabel = new javax.swing.JLabel();
        jPlotPanel = new javax.swing.JPanel();
        jPlotNameLabel = new javax.swing.JLabel();
        jPlotNameText = new javax.swing.JTextField();
        jPlotGroupText = new javax.swing.JTextField();
        jPlotGroupLabel = new javax.swing.JLabel();
        jPlotLabel = new javax.swing.JLabel();
        jPlotCombo = new javax.swing.JComboBox();
        jPeakPanel = new javax.swing.JPanel();
        jPeakLabel = new javax.swing.JLabel();
        jPeakCombo = new javax.swing.JComboBox();
        jPeakNameText = new javax.swing.JTextField();
        jPeakNameLabel1 = new javax.swing.JLabel();
        jCommandsPanel = new javax.swing.JPanel();
        jImportButton1 = new javax.swing.JButton();
        jPaddingLabel = new javax.swing.JLabel();
        jOptionPanel = new javax.swing.JPanel();
        jWindowOptionsPanel = new javax.swing.JPanel();
        jXMaxLabel = new javax.swing.JLabel();
        jXMaxText = new javax.swing.JTextField();
        jXMinText = new javax.swing.JTextField();
        jXMinLabel = new javax.swing.JLabel();
        jPlotWindowLabel = new javax.swing.JLabel();
        jYMinLabel = new javax.swing.JLabel();
        jYMinText = new javax.swing.JTextField();
        jYMaxLabel = new javax.swing.JLabel();
        jYMaxText = new javax.swing.JTextField();
        jExtraOptionsPanel = new javax.swing.JPanel();
        jShowSlopeLabel = new javax.swing.JLabel();
        jShowSlopeCheck = new javax.swing.JCheckBox();
        jTrendlineHelpLabel = new javax.swing.JLabel();
        jPaddingLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Polysome Analyzer");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setResizeWeight(1.0);

        jTabPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabPaneStateChanged(evt);
            }
        });

        jPlotTab.setLayout(new java.awt.GridBagLayout());
        jTabPane.addTab("Plot", jPlotTab);

        jRawOutputTab.setLayout(new java.awt.GridBagLayout());

        jRawOutputText.setColumns(20);
        jRawOutputText.setRows(5);
        jRawOutputText.setMargin(new java.awt.Insets(10, 10, 10, 10));
        jOutputTextPanel.setViewportView(jRawOutputText);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jRawOutputTab.add(jOutputTextPanel, gridBagConstraints);

        jTabPane.addTab("Raw Output", jRawOutputTab);

        jSplitPane1.setLeftComponent(jTabPane);

        jControlPanel.setAlignmentY(0.0F);
        jControlPanel.setPreferredSize(new java.awt.Dimension(150, 0));
        jControlPanel.setLayout(new java.awt.GridBagLayout());

        jMainPanel.setBackground(new java.awt.Color(255, 255, 255));
        jMainPanel.setLayout(new java.awt.GridBagLayout());

        jImportButton.setText("Import");
        jImportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jImportButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jMainPanel.add(jImportButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        jControlPanel.add(jMainPanel, gridBagConstraints);

        jGroupPanel.setBackground(new java.awt.Color(255, 255, 255));
        jGroupPanel.setLayout(new java.awt.GridBagLayout());

        jGroupLabel.setText("Group:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jGroupPanel.add(jGroupLabel, gridBagConstraints);

        jGroupCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All" }));
        jGroupCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGroupComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jGroupPanel.add(jGroupCombo, gridBagConstraints);

        jXAlignLabel.setText("x-Align:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jGroupPanel.add(jXAlignLabel, gridBagConstraints);

        jXAlignCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Left", "Right", "Both" }));
        jXAlignCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jXAlignComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jGroupPanel.add(jXAlignCombo, gridBagConstraints);

        jYAlignCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Top", "Baseline", "Both", "Normal" }));
        jYAlignCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jYAlignComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jGroupPanel.add(jYAlignCombo, gridBagConstraints);

        jYAlignLabel.setText("y-Align:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jGroupPanel.add(jYAlignLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jControlPanel.add(jGroupPanel, gridBagConstraints);

        jPlotPanel.setBackground(new java.awt.Color(255, 255, 255));
        jPlotPanel.setLayout(new java.awt.GridBagLayout());

        jPlotNameLabel.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jPlotPanel.add(jPlotNameLabel, gridBagConstraints);

        jPlotNameText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPlotNameTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jPlotPanel.add(jPlotNameText, gridBagConstraints);

        jPlotGroupText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPlotGroupTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jPlotPanel.add(jPlotGroupText, gridBagConstraints);

        jPlotGroupLabel.setText("Group:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jPlotPanel.add(jPlotGroupLabel, gridBagConstraints);

        jPlotLabel.setText("Plot:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jPlotPanel.add(jPlotLabel, gridBagConstraints);

        jPlotCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All" }));
        jPlotCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPlotComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jPlotPanel.add(jPlotCombo, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jControlPanel.add(jPlotPanel, gridBagConstraints);

        jPeakPanel.setBackground(new java.awt.Color(255, 255, 255));
        jPeakPanel.setLayout(new java.awt.GridBagLayout());

        jPeakLabel.setText("Peak:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jPeakPanel.add(jPeakLabel, gridBagConstraints);

        jPeakCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All" }));
        jPeakCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPeakComboActionPerformed(evt);
            }
        });
        jPeakCombo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPeakComboKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jPeakPanel.add(jPeakCombo, gridBagConstraints);

        jPeakNameText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPeakNameTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jPeakPanel.add(jPeakNameText, gridBagConstraints);

        jPeakNameLabel1.setText("Name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jPeakPanel.add(jPeakNameLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jControlPanel.add(jPeakPanel, gridBagConstraints);

        jCommandsPanel.setBackground(new java.awt.Color(255, 255, 255));
        jCommandsPanel.setLayout(new java.awt.GridBagLayout());

        jImportButton1.setText("Guess Peak Names");
        jImportButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jImportButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jCommandsPanel.add(jImportButton1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jControlPanel.add(jCommandsPanel, gridBagConstraints);

        jPaddingLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jControlPanel.add(jPaddingLabel, gridBagConstraints);

        jTabbedPane1.addTab("Data", jControlPanel);

        jOptionPanel.setLayout(new java.awt.GridBagLayout());

        jWindowOptionsPanel.setBackground(new java.awt.Color(255, 255, 255));
        jWindowOptionsPanel.setLayout(new java.awt.GridBagLayout());

        jXMaxLabel.setText("xMax:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jWindowOptionsPanel.add(jXMaxLabel, gridBagConstraints);

        jXMaxText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jXMaxTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jWindowOptionsPanel.add(jXMaxText, gridBagConstraints);

        jXMinText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jXMinTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jWindowOptionsPanel.add(jXMinText, gridBagConstraints);

        jXMinLabel.setText("xMin:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jWindowOptionsPanel.add(jXMinLabel, gridBagConstraints);

        jPlotWindowLabel.setText("Plot Window:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 5, 5);
        jWindowOptionsPanel.add(jPlotWindowLabel, gridBagConstraints);

        jYMinLabel.setText("yMin:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jWindowOptionsPanel.add(jYMinLabel, gridBagConstraints);

        jYMinText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jYMinTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jWindowOptionsPanel.add(jYMinText, gridBagConstraints);

        jYMaxLabel.setText("yMax:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        jWindowOptionsPanel.add(jYMaxLabel, gridBagConstraints);

        jYMaxText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jYMaxTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jWindowOptionsPanel.add(jYMaxText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        jOptionPanel.add(jWindowOptionsPanel, gridBagConstraints);

        jExtraOptionsPanel.setBackground(new java.awt.Color(255, 255, 255));
        jExtraOptionsPanel.setLayout(new java.awt.GridBagLayout());

        jShowSlopeLabel.setText("Show trendline");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jExtraOptionsPanel.add(jShowSlopeLabel, gridBagConstraints);

        jShowSlopeCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowSlopeCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jExtraOptionsPanel.add(jShowSlopeCheck, gridBagConstraints);

        jTrendlineHelpLabel.setText("<html>Line of best fit for 80S<br>and bound peaks to the right</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(1, 5, 1, 5);
        jExtraOptionsPanel.add(jTrendlineHelpLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jOptionPanel.add(jExtraOptionsPanel, gridBagConstraints);

        jPaddingLabel1.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jOptionPanel.add(jPaddingLabel1, gridBagConstraints);

        jTabbedPane1.addTab("Options", jOptionPanel);

        jSplitPane1.setRightComponent(jTabbedPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jSplitPane1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jImportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jImportButtonActionPerformed
    int returnVal = jImportChooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = jImportChooser.getSelectedFile().getAbsoluteFile();
        plotManager.loadFromFile(file.getPath());
        plotter.setPlots(plotManager.getPlots());
        plotter.setWindow(0, plotManager.getXMax(), 0, plotManager.getYMax());
        plotter.repaint();
        
        updateGroupControls();
    }

}//GEN-LAST:event_jImportButtonActionPerformed

private void jGroupComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGroupComboActionPerformed
    if (silenceGroupCombo == true)
        return;
    
    String groupName = (String)jGroupCombo.getSelectedItem();
    plotter.setPlots(plotManager.getGroupPlots(groupName));
    plotter.repaint();
    
    // add plots to Plot combo
    silencePlotCombo = true;
    jPlotCombo.removeAllItems();
    jPlotCombo.addItem("All");
    jPlotCombo.setSelectedIndex(0);
    plotter.setSelectedPlot(null);
    ArrayList plots = plotter.getPlots();
    for (int i = 0; i < plots.size(); i++) {
        jPlotCombo.addItem(plots.get(i));
    }
    silencePeakCombo = true;
    jPlotNameText.setText("");
    jPlotGroupText.setText("");
    
    // clear peaks list ( no plot selected)
    jPeakCombo.removeAllItems();
    jPeakCombo.addItem("All");
    silencePeakCombo = false;
    silencePlotCombo = false;
    jPeakNameText.setText("");
    
    updateOutputText();
}//GEN-LAST:event_jGroupComboActionPerformed

private void jXAlignComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXAlignComboActionPerformed
    if (silenceXAlignmentCombo == true)
        return;
    
    int xAlign = jXAlignCombo.getSelectedIndex();
    plotManager.setXAlign(xAlign);
    plotter.setWindow(0, plotManager.getXMax(), 0, plotManager.getYMax());
    plotter.repaint();   
    
    // fix plot selectors
    updatePlotControls();
}//GEN-LAST:event_jXAlignComboActionPerformed

private void jPlotComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPlotComboActionPerformed
    if (silencePlotCombo == true)
        return;
    
    if (jPlotCombo.getSelectedIndex() == 0) { // ALL
        if (jGroupCombo.getSelectedIndex() == 0){
            plotter.setPlots(plotManager.getPlots());
        } else {
            String group = (String)jGroupCombo.getSelectedItem();
            plotter.setPlots(plotManager.getGroupPlots(group));
        }
        plotter.setSelectedPlot(null);
        plotter.repaint();
                
        jPlotNameText.setText("");
        jPlotGroupText.setText("");
        
        silencePeakCombo = true;
        jPeakCombo.removeAllItems();
        jPeakCombo.addItem("All");
        silencePeakCombo = false;
        jPeakNameText.setText("");
        plotter.setSelectedPeak(null);
    } else {
        Plot plot = (Plot)jPlotCombo.getSelectedItem();
        plotter.setSelectedPlot(plot);
        plotter.repaint();
        
        // add peaks to peaks combo
        silencePeakCombo = true;
        jPeakCombo.removeAllItems();
        jPeakCombo.addItem("All");
        jPeakCombo.setSelectedIndex(0);
        plotter.setSelectedPeak(null);
        ArrayList peaks = plotter.getSelectedPlotPeaks();
        for (int i = 0; i < peaks.size(); i++) {
            jPeakCombo.addItem(peaks.get(i));
        }
        silencePeakCombo = false;
        jPeakNameText.setText("");
        
        jPlotNameText.setText(plot.name);
        jPlotGroupText.setText(plot.group);
    }
}//GEN-LAST:event_jPlotComboActionPerformed

private void jPeakComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPeakComboActionPerformed
    if (silencePeakCombo == true) 
        return;
    if (jPeakCombo.getSelectedIndex() > 0) {   
        plotter.setSelectedPeak((Peak)jPeakCombo.getSelectedItem());
        plotter.repaint();
        jPeakNameText.setText(plotter.getSelectedPeak().name);
    } else {
        plotter.setSelectedPeak(null);
        plotter.repaint();
        jPeakNameText.setText("");
    }
    
}//GEN-LAST:event_jPeakComboActionPerformed

private void jPlotGroupTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPlotGroupTextActionPerformed
    Plot selectedPlot = (Plot)jPlotCombo.getSelectedItem();
    selectedPlot.group = jPlotGroupText.getText();
    
    // refresh group list
    silenceGroupCombo = true;
    jGroupCombo.removeAllItems();
    jGroupCombo.addItem("All");
    ArrayList groupNames = plotManager.getGroupNames();
    for (int i = 0; i < groupNames.size(); i++) {
        jGroupCombo.addItem(groupNames.get(i));
    }
    jGroupCombo.setSelectedItem(selectedPlot.group);
    silenceGroupCombo = false;
    
    // refresh plot list
    silencePlotCombo = true;
    jPlotCombo.removeAllItems();
    jPlotCombo.addItem("All");
    ArrayList plots = plotManager.getGroupPlots(selectedPlot.group);
    for (int i = 0; i < plots.size(); i++) {
        jPlotCombo.addItem(plots.get(i));
    }   
    jPlotCombo.setSelectedItem(selectedPlot);
}//GEN-LAST:event_jPlotGroupTextActionPerformed

private void jPlotNameTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPlotNameTextActionPerformed
    if (jPlotCombo.getSelectedIndex() < 1)
        return;
    Plot selectedPlot = (Plot)jPlotCombo.getSelectedItem();
    if (selectedPlot != null) {
        selectedPlot.name = jPlotNameText.getText();
        jPlotCombo.repaint();
    }
}//GEN-LAST:event_jPlotNameTextActionPerformed

private void jPeakNameTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPeakNameTextActionPerformed
    if (jPlotCombo.getSelectedIndex() < 1)
        return;
    Peak selectedPeak = (Peak)jPeakCombo.getSelectedItem();
    if (selectedPeak != null) {
        selectedPeak.name = jPeakNameText.getText();
        jPeakCombo.repaint();
        plotter.repaint();
    }
}//GEN-LAST:event_jPeakNameTextActionPerformed

private void jTabPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabPaneStateChanged

    switch (jTabPane.getSelectedIndex()) {
        case 0: // plot tab
            break;
            
        case 1: // output tab
            updateOutputText();
            jRawOutputText.requestFocus();
            break;
    }
    jOutputTextPanel.requestFocus();
}//GEN-LAST:event_jTabPaneStateChanged

private void jYAlignComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jYAlignComboActionPerformed
    if (silenceYAlignmentCombo == true)
        return;
    
    int yAlignType = jYAlignCombo.getSelectedIndex();
    plotManager.setYAlign(yAlignType);
    plotter.setWindow(0, plotManager.getXMax(), 0, plotManager.getYMax());
    plotter.repaint(); 
    
    // fix plot selectors
    updatePlotControls();
}//GEN-LAST:event_jYAlignComboActionPerformed

private void jXMaxTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXMaxTextActionPerformed
    double xMax = Double.parseDouble(jXMaxText.getText());
    plotter.setWindowXMax(xMax);
    plotter.repaint();
}//GEN-LAST:event_jXMaxTextActionPerformed

private void jXMinTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXMinTextActionPerformed
    double xMin = Double.parseDouble(jXMinText.getText());
    plotter.setWindowXMin(xMin);
    plotter.repaint();
}//GEN-LAST:event_jXMinTextActionPerformed

private void jYMinTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jYMinTextActionPerformed
    double yMin = Double.parseDouble(jYMinText.getText());
    plotter.setWindowYMin(yMin);
    plotter.repaint();
}//GEN-LAST:event_jYMinTextActionPerformed

private void jYMaxTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jYMaxTextActionPerformed
    double yMax = Double.parseDouble(jYMaxText.getText());
    plotter.setWindowYMax(yMax);
    plotter.repaint();
}//GEN-LAST:event_jYMaxTextActionPerformed

private void jShowSlopeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShowSlopeCheckActionPerformed
    plotter.showTrendLine(jShowSlopeCheck.isSelected());
    plotter.repaint();
}//GEN-LAST:event_jShowSlopeCheckActionPerformed

private void jImportButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jImportButton1ActionPerformed
    ArrayList plots = plotManager.getPlots();
    for (int i = 0; i < plots.size(); i++) {
        Plot plot = (Plot)plots.get(i);
        plot.guessNames();
    }
    plotter.repaint();
}//GEN-LAST:event_jImportButton1ActionPerformed

private void jPeakComboKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPeakComboKeyPressed

    switch (evt.getKeyCode()) {//GEN-LAST:event_jPeakComboKeyPressed
        case KeyEvent.VK_DELETE:
        case KeyEvent.VK_MINUS:
        case KeyEvent.VK_UNDERSCORE:
            Plot selectedPlot = (Plot)jPlotCombo.getSelectedItem();
            Peak selectedPeak = (Peak)jPeakCombo.getSelectedItem();
            if (selectedPlot != null && selectedPeak != null) {
                selectedPlot.getPeaks().remove(selectedPeak);
                plotter.setSelectedPeak(null);
                plotter.repaint();
            }
            updatePeakControls();
            break;
    }
    
}

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PolysomeAnalyzer analyzer = new PolysomeAnalyzer();
                analyzer.setSize(600, 500);
                analyzer.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jCommandsPanel;
    private javax.swing.JPanel jControlPanel;
    private javax.swing.JPanel jExtraOptionsPanel;
    private javax.swing.JComboBox jGroupCombo;
    private javax.swing.JLabel jGroupLabel;
    private javax.swing.JPanel jGroupPanel;
    private javax.swing.JButton jImportButton;
    private javax.swing.JButton jImportButton1;
    private javax.swing.JFileChooser jImportChooser;
    private javax.swing.JPanel jMainPanel;
    private javax.swing.JPanel jOptionPanel;
    private javax.swing.JScrollPane jOutputTextPanel;
    private javax.swing.JLabel jPaddingLabel;
    private javax.swing.JLabel jPaddingLabel1;
    private javax.swing.JComboBox jPeakCombo;
    private javax.swing.JLabel jPeakLabel;
    private javax.swing.JLabel jPeakNameLabel1;
    private javax.swing.JTextField jPeakNameText;
    private javax.swing.JPanel jPeakPanel;
    private javax.swing.JComboBox jPlotCombo;
    private javax.swing.JLabel jPlotGroupLabel;
    private javax.swing.JTextField jPlotGroupText;
    private javax.swing.JLabel jPlotLabel;
    private javax.swing.JLabel jPlotNameLabel;
    private javax.swing.JTextField jPlotNameText;
    private javax.swing.JPanel jPlotPanel;
    private javax.swing.JPanel jPlotTab;
    private javax.swing.JLabel jPlotWindowLabel;
    private javax.swing.JPanel jRawOutputTab;
    private javax.swing.JTextArea jRawOutputText;
    private javax.swing.JCheckBox jShowSlopeCheck;
    private javax.swing.JLabel jShowSlopeLabel;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabPane;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel jTrendlineHelpLabel;
    private javax.swing.JPanel jWindowOptionsPanel;
    private javax.swing.JComboBox jXAlignCombo;
    private javax.swing.JLabel jXAlignLabel;
    private javax.swing.JLabel jXMaxLabel;
    private javax.swing.JTextField jXMaxText;
    private javax.swing.JLabel jXMinLabel;
    private javax.swing.JTextField jXMinText;
    private javax.swing.JComboBox jYAlignCombo;
    private javax.swing.JLabel jYAlignLabel;
    private javax.swing.JLabel jYMaxLabel;
    private javax.swing.JTextField jYMaxText;
    private javax.swing.JLabel jYMinLabel;
    private javax.swing.JTextField jYMinText;
    // End of variables declaration//GEN-END:variables

}
