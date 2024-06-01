package com.github.dubasdey;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.awt.event.ActionEvent;

public class MainWindow {

    private static final String CONFFILE = "config.properties";

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    
    private JFrame frmShutdownProgrammer;

    private long endTime=0;
    
    private long startTime = 0;
    
    private long pollTime = 1;
     
    private ScheduledFuture<?> task = null;
    
    private JProgressBar timeBar = new JProgressBar();
    
    private JComboBox<TimeModel> cmbTime = new JComboBox<>();
    private JComboBox<ActionModel> cmbAction = new JComboBox<>();

    private JButton btStart = new JButton("Start");
    private JButton btCancel = new JButton("Cancel");
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName());
                    MainWindow window = new MainWindow();
                    window.frmShutdownProgrammer.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainWindow() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmShutdownProgrammer = new JFrame();
        frmShutdownProgrammer.setTitle("Shutdown programmer");
        frmShutdownProgrammer.setResizable(false);
        frmShutdownProgrammer.setBounds(100, 100, 376, 195);
        frmShutdownProgrammer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmShutdownProgrammer.getContentPane().setLayout(null);
        JLabel lbTime = new JLabel("Time");
        JLabel lbAction = new JLabel("Action");
        
        cmbTime.setModel(new DefaultComboBoxModel<TimeModel>(
            new TimeModel[]{
                new TimeModel("1   hour",60),
                new TimeModel("1.5 hours",90),
                new TimeModel("2   hours",120),
                new TimeModel("3   hours",180),
                new TimeModel("4   hours",240)
            }
        ));
       // new String[] {"1 hour", "2 hours", "3 hours", "4 hours"}));
        cmbTime.setBounds(130, 27, 98, 22);
        frmShutdownProgrammer.getContentPane().add(cmbTime);
        
        cmbAction.setModel(new DefaultComboBoxModel<ActionModel>(OS.allowedActions()));
        
        cmbAction.setBounds(130, 60, 98, 22);
        frmShutdownProgrammer.getContentPane().add(cmbAction);
        
        lbTime.setBounds(35, 31, 46, 14);
        frmShutdownProgrammer.getContentPane().add(lbTime);
        
        lbAction.setBounds(35, 64, 46, 14);
        frmShutdownProgrammer.getContentPane().add(lbAction);
        
        btStart.setBounds(248, 27, 89, 23);
        frmShutdownProgrammer.getContentPane().add(btStart);

        btCancel.setEnabled(false);
        btCancel.setBounds(248, 60, 89, 23);
        frmShutdownProgrammer.getContentPane().add(btCancel);
        
        timeBar.setEnabled(false);
        timeBar.setBounds(39, 111, 298, 14);
        frmShutdownProgrammer.getContentPane().add(timeBar);
        
        btCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enable();
                stop();
                OS.cancel();
            }
        });
        
        btStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disable();
                save();
                start();
            }
        });
        
        // Reload las settings (if any)
        restore();
    }
    
    /**
     * Enable controls
     * Allows to start
     */
    private void enable() {
        cmbAction.setEnabled(true);
        cmbTime.setEnabled(true);
        btStart.setEnabled(true);
        btCancel.setEnabled(false);
        timeBar.setEnabled(false);
        timeBar.setValue(0);
    }
    
    /**
     * Disable controls (running) 
     * Allows to cancel
     */
    private void disable() {
        cmbAction.setEnabled(false);
        cmbTime.setEnabled(false);
        btStart.setEnabled(false);
        btCancel.setEnabled(true);
        timeBar.setEnabled(true);
    }
    
    /**
     * Save last settings
     */
    private void save() {
        Properties p = new Properties();
        try {
            p.setProperty("action", String.valueOf( cmbAction.getSelectedIndex()));
            p.setProperty("time", String.valueOf(cmbTime.getSelectedIndex()));
            p.store(new FileOutputStream(CONFFILE), "saved");
        } catch (IOException e) {
            // Ignore
            e.printStackTrace();
        }
    }
    
    /**
     * Restore last settings
     */
    private void restore() {
        Properties p = new Properties();
        cmbAction.setSelectedIndex(0);
        cmbTime.setSelectedIndex(0);
        if(Files.exists(Paths.get(CONFFILE))){
            try {
                p.load(new FileInputStream(CONFFILE));
                int a = Integer.parseInt(p.getProperty("action", "0"));
                int t = Integer.parseInt(p.getProperty("time", "0"));
                cmbAction.setSelectedIndex(a);
                cmbTime.setSelectedIndex(t);
            } catch (IOException e) {
                // Ignore
                e.printStackTrace();
            }
        }
    }
    
    // Start timming
    private void start() {
        int timeInMinutes = ((TimeModel) cmbTime.getSelectedItem()).getMinutes();
        startTime = System.currentTimeMillis();
        endTime = System.currentTimeMillis() + ( (timeInMinutes) * 60 * 1000);
        task = executor.scheduleAtFixedRate(() -> runAction() ,pollTime, pollTime, TimeUnit.SECONDS);
        
    }    
    
    /**
     * Stop timming
     */
    private void stop() {
        if(task!=null) {
            task.cancel(true);
            task = null;
        }
    }

    /**
     * Execute the programmed action
     */
    private void runAction() {
        if (endTime  < System.currentTimeMillis()) {
            // First stop and reenable to set all GUI ready in the hibernate case
            stop();
            restore();
            enable();
            OS.executeAction(((ActionModel) cmbAction.getSelectedItem()));
        }
        double totalTime = endTime - startTime;
        double currentTime =  System.currentTimeMillis() - startTime ;
        int p =(int) Math.round( (currentTime / totalTime) * 100);
        timeBar.setValue(p); 
    }
    
}
