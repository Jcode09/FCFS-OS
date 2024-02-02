import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;

public class test extends JFrame {

    private JTextField numberOfProcessesField;
    private JTextField[] arrivalTimeField;
    private JTextField[] burstTimeField;
    private JButton simulateButton;
    private JButton clearButton; // New button for clearing fields
    private JTable resultTable;
    private JScrollPane resultScrollPane;
    private JPanel ganttChartPanel;
    private JLabel[] ganttChartLabels;
    private JPanel ganttChartBoxPanel;
    private JLabel avgTurnaroundLabel;
    private JLabel avgWaitingLabel;

    public test() {
        setTitle("CPU Scheduling - FCFS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 3));

        numberOfProcessesField = new JTextField();
        inputPanel.add(new JLabel("Number of Processes:"));
        inputPanel.add(numberOfProcessesField);
        JButton setProcessesButton = new JButton("Set Processes");
        setProcessesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = Integer.parseInt(numberOfProcessesField.getText());
                setNumberOfProcesses(n);
            }
        });
        inputPanel.add(setProcessesButton);

        simulateButton = new JButton("Simulate");
        clearButton = new JButton("Clear"); // New button for clearing fields

        resultTable = new JTable();
        resultScrollPane = new JScrollPane(resultTable);

        ganttChartPanel = new JPanel();
        ganttChartPanel.setLayout(new BorderLayout());

        ganttChartBoxPanel = new JPanel();
        ganttChartPanel.add(ganttChartBoxPanel, BorderLayout.CENTER);
        ganttChartBoxPanel.setLayout(new GridLayout(2, 1));

        JPanel averagePanel = new JPanel();
        avgTurnaroundLabel = new JLabel("Average Turnaround Time: ");
        avgWaitingLabel = new JLabel("Average Waiting Time: ");
        avgTurnaroundLabel.setFont(new Font("Arial", Font.BOLD, 16));
        avgWaitingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        averagePanel.setLayout(new GridLayout(2, 1));
        averagePanel.add(avgTurnaroundLabel);
        averagePanel.add(avgWaitingLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(simulateButton);
        buttonPanel.add(clearButton); // Add the clear button

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(resultScrollPane, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
        add(ganttChartPanel, BorderLayout.CENTER);
        add(averagePanel, BorderLayout.WEST);

        simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGanttChart();
                simulateFCFS();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
    }
    private void setNumberOfProcesses(int n) {
        arrivalTimeField = new JTextField[n];
        burstTimeField = new JTextField[n];

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 3));

        JLabel processLabel = new JLabel("Process");
        JLabel arrivalLabel = new JLabel("Arrival Time");
        JLabel burstLabel = new JLabel("Burst Time");

        inputPanel.add(processLabel);
        inputPanel.add(arrivalLabel);
        inputPanel.add(burstLabel);

        for (int i = 0; i < n; i++) {
            JTextField processField = new JTextField("P" + (i + 1));
            processField.setEditable(false);
            arrivalTimeField[i] = new JTextField();
            burstTimeField[i] = new JTextField();

            inputPanel.add(processField);
            inputPanel.add(arrivalTimeField[i]);
            inputPanel.add(burstTimeField[i]);
        }

        Container contentPane = getContentPane();
        contentPane.remove(0);
        contentPane.add(inputPanel, BorderLayout.NORTH);
        contentPane.revalidate();
    }

    private void resetGanttChart() {
        ganttChartBoxPanel.removeAll();
        ganttChartBoxPanel.revalidate();
        ganttChartLabels = null;
    }

    private void animateGanttChart(int[] processOrder) {
        ganttChartLabels = new JLabel[processOrder.length];

        for (int i = 0; i < processOrder.length; i++) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            label.setOpaque(true);
            label.setBackground(getRandomColor());
            ganttChartBoxPanel.add(label);
            ganttChartLabels[i] = label;

            final int index = i;

            Timer timer = new Timer(500 * i, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    textAndColorTransition(ganttChartLabels[index], "P" + processOrder[index], getRandomColor());
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void textAndColorTransition(JLabel label, String targetText, Color targetColor) {
        Timer timer = new Timer(50, new ActionListener() {
            Color currentColor = label.getBackground();
            String currentText = label.getText();
            float[] currentHSB = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null);
            float[] targetHSB = Color.RGBtoHSB(targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue(), null);
            float[] deltaHSB = {
                    (targetHSB[0] - currentHSB[0]) / 20,
                    (targetHSB[1] - currentHSB[1]) / 20,
                    (targetHSB[2] - currentHSB[2]) / 20
            };
            int steps = 20;
            int currentStep = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStep < steps) {
                    currentHSB[0] += deltaHSB[0];
                    currentHSB[1] += deltaHSB[1];
                    currentHSB[2] += deltaHSB[2];
                    label.setBackground(Color.getHSBColor(currentHSB[0], currentHSB[1], currentHSB[2]));
                    label.setText(targetText);
                    currentStep++;
                } else {
                    label.setBackground(targetColor);
                    label.setText(targetText);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }

    private void simulateFCFS() {
        int n = arrivalTimeField.length;
        int[] at = new int[n];
        int[] bt = new int[n];
        int[] ct = new int[n];
        int[] ta = new int[n];
        int[] wt = new int[n];
        int[] f = new int[n];
    
        for (int i = 0; i < n; i++) {
            at[i] = Integer.parseInt(arrivalTimeField[i].getText());
            bt[i] = Integer.parseInt(burstTimeField[i].getText());
            f[i] = 0;
        }
    
        int st = 0, tot = 0;
        float avgwt = 0, avgta = 0;
        int[] processOrder = new int[n];
    
         // Sort processes based on arrival time
    Integer[] sortedIndices = new Integer[n];
    for (int i = 0; i < n; i++) {
        sortedIndices[i] = i;
    }

    Arrays.sort(sortedIndices, Comparator.comparingInt(index -> at[index]));

    while (true) {
        int c = n, min = Integer.MAX_VALUE;

        if (tot == n) {
            break;
        }

        for (int i = 0; i < n; i++) {
            int index = sortedIndices[i];
            if ((at[index] <= st) && (f[index] == 0) && (bt[index] < min)) {
                min = bt[index];
                c = index;
            }
        }

        if (c == n) {
            st++;
        } else {
            ct[c] = st + bt[c];
            st += bt[c];
            ta[c] = ct[c] - at[c];
            wt[c] = ta[c] - bt[c];
            f[c] = 1;
            tot++;
            processOrder[tot - 1] = c + 1;
        }
    }

    // After the loop, call animateGanttChart with the actual number of processes
    animateGanttChart(Arrays.copyOf(processOrder, tot));

        // Populate the table in the order of arrival time
        String[] columnNames = {"Process", "Arrival Time", "Burst Time", "Completion Time", "Turnaround Time", "Waiting Time"};
        String[][] data = new String[n][6];
    
        // Sort the indices based on arrival time
        Arrays.sort(sortedIndices, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Integer.compare(at[a], at[b]);
            }
        });
    
        for (int i = 0; i < n; i++) {
            int index = sortedIndices[i];
    
            avgwt += wt[index];
            avgta += ta[index];
    
            data[i][0] = "P" + (index + 1);
            data[i][1] = String.valueOf(at[index]);
            data[i][2] = String.valueOf(bt[index]);
            data[i][3] = String.valueOf(ct[index]);
            data[i][4] = String.valueOf(ta[index]);
            data[i][5] = String.valueOf(wt[index]);
        }
    
        avgwt /= n;
        avgta /= n;
    
        String[] averageRow = {"Average", "", "", "", String.format("%.2f ms", avgta), String.format("%.2f ms", avgwt)};
    
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        model.addRow(averageRow);
        resultTable.setModel(model);
    
        avgTurnaroundLabel.setText("Average Turnaround Time: " + String.format("%.2f Units", avgta));
        avgWaitingLabel.setText("Average Waiting Time: " + String.format("%.2f Units", avgwt));
    }
    private void clearFields() {
        numberOfProcessesField.setText("");
        arrivalTimeField = null;
        burstTimeField = null;
    
        // Remove the existing inputPanel
        Container contentPane = getContentPane();
        contentPane.remove(0);
    
        // Recreate the inputPanel with the "Number of Processes" field and "Set Processes" button
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 3));
        
        numberOfProcessesField = new JTextField();
        inputPanel.add(new JLabel("Number of Processes:"));
        inputPanel.add(numberOfProcessesField);
        JButton setProcessesButton = new JButton("Set Processes");
        setProcessesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = Integer.parseInt(numberOfProcessesField.getText());
                setNumberOfProcesses(n);
            }
        });
        inputPanel.add(setProcessesButton);
    
        contentPane.add(inputPanel, BorderLayout.NORTH);
        contentPane.revalidate();
    
        // Clear the results table and reset the Gantt chart
        resultTable.setModel(new DefaultTableModel());
        resetGanttChart();
    
        // Reset the average labels
        avgTurnaroundLabel.setText("Average Turnaround Time: ");
        avgWaitingLabel.setText("Average Waiting Time: ");
    }
    
    
    private Color getRandomColor() {
        return new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new test().setVisible(true);
            }
        });
    }
}