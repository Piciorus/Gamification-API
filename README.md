```
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

public class MouseMover {

    private ScheduledExecutorService scheduler;
    private Robot robot;
    private boolean running = false;
    private int direction = 1;

    // GUI components
    private JFrame frame;
    private JLabel statusLabel;
    private JButton startButton;
    private JButton stopButton;
    private JSpinner intervalSpinner;
    private JSpinner durationSpinner;
    private JProgressBar progressBar;
    private JLabel timeLeftLabel;
    private Timer countdownTimer;
    private int secondsLeft;

    public MouseMover() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(null,
                "Eroare: Nu s-a putut initializa Robot-ul pentru control mouse.\n" + e.getMessage(),
                "Eroare", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        buildUI();
    }

    private void buildUI() {
        // Look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        frame = new JFrame("Teams Stay Active 🟢");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 380);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(245, 247, 250));

        // Title
        JLabel titleLabel = new JLabel("Teams Stay Active");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(50, 50, 150));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Muta mouse-ul automat pentru a ramane online");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Settings panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Interval setting
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel intervalLabel = new JLabel("Interval miscare (secunde):");
        intervalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        settingsPanel.add(intervalLabel, gbc);

        gbc.gridx = 1;
        intervalSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        intervalSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) intervalSpinner.getEditor()).getTextField().setColumns(5);
        settingsPanel.add(intervalSpinner, gbc);

        // Duration setting
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel durationLabel = new JLabel("Oprire automata dupa (minute):");
        durationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        settingsPanel.add(durationLabel, gbc);

        gbc.gridx = 1;
        // 0 = fara limita
        durationSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 480, 5));
        durationSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) durationSpinner.getEditor()).getTextField().setColumns(5);
        settingsPanel.add(durationSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel hint = new JLabel("  ℹ️  0 minute = fara oprire automata");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        settingsPanel.add(hint, gbc);

        mainPanel.add(settingsPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Status label
        statusLabel = new JLabel("⏸  Oprit");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(150, 50, 50));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        // Time left label
        timeLeftLabel = new JLabel(" ");
        timeLeftLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLeftLabel.setForeground(new Color(80, 80, 80));
        timeLeftLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(timeLeftLabel);
        mainPanel.add(Box.createVerticalStrut(8));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setForeground(new Color(70, 130, 180));
        progressBar.setBackground(new Color(220, 225, 235));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        progressBar.setVisible(false);
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(15));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(new Color(245, 247, 250));

        startButton = new JButton("▶  Porneste");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        startButton.setBackground(new Color(70, 160, 80));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setPreferredSize(new Dimension(140, 38));
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        stopButton = new JButton("⏹  Opreste");
        stopButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        stopButton.setBackground(new Color(200, 70, 70));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setBorderPainted(false);
        stopButton.setPreferredSize(new Dimension(140, 38));
        stopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stopButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        mainPanel.add(buttonPanel);

        // Hover effects
        startButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (startButton.isEnabled()) startButton.setBackground(new Color(50, 140, 60)); }
            public void mouseExited(MouseEvent e) { if (startButton.isEnabled()) startButton.setBackground(new Color(70, 160, 80)); }
        });
        stopButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (stopButton.isEnabled()) stopButton.setBackground(new Color(180, 50, 50)); }
            public void mouseExited(MouseEvent e) { if (stopButton.isEnabled()) stopButton.setBackground(new Color(200, 70, 70)); }
        });

        // Actions
        startButton.addActionListener(e -> startMoving());
        stopButton.addActionListener(e -> stopMoving());

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void startMoving() {
        int intervalSec = (int) intervalSpinner.getValue();
        int durationMin = (int) durationSpinner.getValue();

        running = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        intervalSpinner.setEnabled(false);
        durationSpinner.setEnabled(false);
        statusLabel.setText("🟢  Activ - mutand mouse-ul...");
        statusLabel.setForeground(new Color(30, 130, 60));

        // Schedule mouse movements
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::moveMouseSlightly, 0, intervalSec, TimeUnit.SECONDS);

        // Handle duration countdown
        if (durationMin > 0) {
            secondsLeft = durationMin * 60;
            progressBar.setMaximum(secondsLeft);
            progressBar.setValue(secondsLeft);
            progressBar.setVisible(true);

            countdownTimer = new Timer(1000, null);
            countdownTimer.addActionListener(e -> {
                secondsLeft--;
                progressBar.setValue(secondsLeft);

                int mLeft = secondsLeft / 60;
                int sLeft = secondsLeft % 60;
                timeLeftLabel.setText(String.format("Timp ramas: %d min %02d sec", mLeft, sLeft));

                if (secondsLeft <= 0) {
                    countdownTimer.stop();
                    stopMoving();
                    JOptionPane.showMessageDialog(frame,
                        "Programul s-a oprit dupa " + durationMin + " minute.\nAcum vei intra in Away pe Teams.",
                        "Timp expirat", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            countdownTimer.start();
        } else {
            timeLeftLabel.setText("Fara oprire automata");
            progressBar.setVisible(false);
        }
    }

    private void stopMoving() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        SwingUtilities.invokeLater(() -> {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            intervalSpinner.setEnabled(true);
            durationSpinner.setEnabled(true);
            statusLabel.setText("⏸  Oprit");
            statusLabel.setForeground(new Color(150, 50, 50));
            timeLeftLabel.setText(" ");
            progressBar.setVisible(false);
        });
    }

    private void moveMouseSlightly() {
        if (!running) return;
        Point currentPos = MouseInfo.getPointerInfo().getLocation();
        // Misca 1 pixel alternativ stanga-dreapta (aproape invizibil)
        int newX = currentPos.x + direction;
        robot.mouseMove(newX, currentPos.y);
        direction = -direction;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MouseMover::new);
    }
}

```


```
@echo off
echo Compilare MouseMover...
javac MouseMover.java
if %errorlevel% neq 0 (
    echo EROARE: Compilarea a esuat. Asigura-te ca ai Java JDK instalat.
    pause
    exit /b 1
)
echo Pornire aplicatie...
java MouseMover
pause

```
