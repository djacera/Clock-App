import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ClockApp {
    private static Timer messageTimer;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(250, 120);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setLocation(1000, 50);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setLayout(null); // absolute layout

        // Clock label
        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        timeLabel.setForeground(Color.BLACK);
        timeLabel.setBounds(0, 10, 250, 50);
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(timeLabel);

        // Close button (visible on transparent frame)
        JButton closeBtn = new JButton("X");
        closeBtn.setBounds(230, 0, 20, 20);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(true); // make visible
        closeBtn.setBackground(new Color(200, 0, 0, 180)); // semi-transparent red
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> System.exit(0));
        frame.add(closeBtn);

        // Floating message label
        JLabel messageLabel = new JLabel();
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setBounds(0, 120, 250, 30);
        messageLabel.setForeground(new Color(255, 255, 255, 0));
        frame.add(messageLabel);

        // Draggable frame
        final Point[] mouseDownCompCoords = {null};
        frame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { mouseDownCompCoords[0] = e.getPoint(); }
            public void mouseReleased(MouseEvent e) { mouseDownCompCoords[0] = null; }
        });
        frame.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                frame.setLocation(currCoords.x - mouseDownCompCoords[0].x,
                                  currCoords.y - mouseDownCompCoords[0].y);
            }
        });

        final Color[] currentColor = {Color.BLACK};
        final int[] lastShownHour = {-1}; // prevent multiple triggers per hour

        Timer clockTimer = new Timer(1000, e -> {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            timeLabel.setText(time);

            // Clock color logic
            Color targetColor;
            boolean glow = false;
            if (hour >= 22 || hour < 4) { targetColor = Color.RED; glow = true; }
            else if (hour >= 5 && hour < 10) targetColor = Color.BLUE;
            else targetColor = Color.BLACK;

            // Smooth interpolation
            int r = currentColor[0].getRed() + (targetColor.getRed() - currentColor[0].getRed()) / 5;
            int g = currentColor[0].getGreen() + (targetColor.getGreen() - currentColor[0].getGreen()) / 5;
            int b = currentColor[0].getBlue() + (targetColor.getBlue() - currentColor[0].getBlue()) / 5;
            currentColor[0] = new Color(r, g, b);
            timeLabel.setForeground(currentColor[0]);
            timeLabel.setFont(new Font("Arial", Font.BOLD, glow ? 38 : 36));

            // Trigger messages only once per hour
            if (hour != lastShownHour[0]) {
                switch (hour) {
                    case 4 -> triggerMessage(messageLabel, "Good Morning", targetColor);
                    case 6 -> triggerMessage(messageLabel, "Bro Time for SCHOOL", targetColor);
                    case 22 -> triggerMessage(messageLabel, "It is already 10", targetColor);
                    case 0 -> triggerMessage(messageLabel, "BROO? Why are you still awake??", targetColor);
                }
                lastShownHour[0] = hour;
            }
        });
        clockTimer.start();
        frame.setVisible(true);
    }

    private static void triggerMessage(JLabel label, String text, Color glowColor) {
        // If same message is already displayed, do nothing
        if (label.getText().equals(text)) return;

        // Stop any previous message
        if (messageTimer != null && messageTimer.isRunning()) messageTimer.stop();

        label.setText(text);
        final long startTime = System.currentTimeMillis();
        final int holdTime = 15 * 60 * 1000; // 15 minutes
        messageTimer = new Timer(50, null);

        messageTimer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            int alpha;
            int y;

            if (elapsed < 2000) { // fade in and slide up
                alpha = (int)(255 * elapsed / 2000.0);
                y = 120 - (int)(50 * elapsed / 2000.0);
            } else if (elapsed < holdTime + 2000) { // hold stationary
                alpha = 255;
                y = 70;
            } else if (elapsed < holdTime + 4000) { // fade out
                alpha = (int)(255 * (1 - (elapsed - holdTime - 2000)/2000.0));
                y = 70;
            } else { // done
                alpha = 0;
                label.setText("");
                messageTimer.stop();
                return;
            }

            int glowR = Math.min(255, glowColor.getRed() + 50);
            int glowG = Math.min(255, glowColor.getGreen() + 50);
            int glowB = Math.min(255, glowColor.getBlue() + 50);
            label.setForeground(new Color(glowR, glowG, glowB, alpha));
            label.setLocation(0, y);
        });

        messageTimer.start();
    }
}