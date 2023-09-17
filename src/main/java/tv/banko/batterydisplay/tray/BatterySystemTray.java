package tv.banko.batterydisplay.tray;

import tv.banko.batterydisplay.device.DeviceState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Objects;

public class BatterySystemTray {

    public BatterySystemTray() throws IOException {
        TrayIcon trayIcon;

        if (!SystemTray.isSupported()) {
            throw new IllegalCallerException("No SystemTray available");
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png")));

        ActionListener exitListener = e -> System.exit(0);
        PopupMenu popup = new PopupMenu();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setFont(Font.getFont("Arial"));
        exitItem.addActionListener(exitListener);
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "BatteryDisplay", popup);
        trayIcon.addActionListener(exitListener);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
