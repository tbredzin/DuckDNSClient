package com.github.tbredzin.duckdns.systemtray;

import com.github.tbredzin.duckdns.DuckDnsClient;
import com.github.tbredzin.duckdns.DuckDnsTimerTask;
import com.github.tbredzin.duckdns.systemtray.popup.DuckDnsAbout;
import com.github.tbredzin.duckdns.systemtray.popup.DuckDnsMyIp;
import com.github.tbredzin.duckdns.systemtray.popup.DuckDnsSettingsDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.prefs.Preferences;

import static javax.swing.JOptionPane.showMessageDialog;

public class DuckdnsTray extends TrayIcon {

    private final String title;
    private final ResourceBundle resourceBundle;
    private final Preferences prefs;

    private final DuckDnsClient client;
    private String lastUpdateStatus = "";

    public DuckdnsTray(String title, Preferences prefs) throws IOException, AWTException {
        super(
                ImageIO.read(ClassLoader.getSystemResource("logo.png")),
                title
        );
        this.title = title;
        this.resourceBundle = loadResourceBundle();
        this.prefs = prefs;
        this.client = new DuckDnsClient();

        // Check the SystemTray support
        if (!SystemTray.isSupported()) {
            throw new IOException("Unable to run application in system tray");
        }

        //Setup tray
        this.setImageAutoSize(true);

        //Setup menu
        final PopupMenu popup = new PopupMenu();
        popup.add(new MenuItem(this.resourceBundle.getString("menu.settings")))
                .addActionListener(e -> {
                    final DuckDnsSettingsDialog settings = new DuckDnsSettingsDialog(this);
                    settings.setLocationRelativeTo(null); //center
                    settings.setVisible(true); //set visible

                    if (Objects.equals(JOptionPane.OK_OPTION, settings.getValue())) {
                        prefs.put("domain", settings.getDomain());
                        prefs.put("token", settings.getToken());
                        prefs.put("updatemessages", settings.getShowNotification());
                        if (!settings.getRefresh().equals(prefs.get("refresh", "5"))) {
                            prefs.put("refresh", settings.getRefresh());
                            fireEvent("refresh");
                        }
                        displayMessage(title, this.resourceBundle.getString("panel.setting.ok"), MessageType.INFO);
                    } else {
                        displayMessage(title, this.resourceBundle.getString("panel.setting.cancel"), MessageType.INFO);
                    }
                });

        popup.add(new MenuItem(this.resourceBundle.getString("menu.force-update")))
                .addActionListener(e -> new DuckDnsTimerTask(this).run());
        popup.add(new MenuItem(this.resourceBundle.getString("menu.get-my-ip")))
                .addActionListener(e -> client.getMyIp().ifPresentOrElse(
                        ip -> showMessageDialog(null, new DuckDnsMyIp(this, ip)),
                        () -> showMessageDialog(null, this.resourceBundle.getString("my-ip.error"))
                ));
        popup.addSeparator();
        popup.add(new MenuItem(this.resourceBundle.getString("menu.about")))
                .addActionListener(e -> showMessageDialog(null, new DuckDnsAbout(this)));
        popup.add(new MenuItem(this.resourceBundle.getString("menu.exit")))
                .addActionListener(e -> {
                    SystemTray.getSystemTray().remove(this);
                    System.exit(0);
                });
        this.setPopupMenu(popup);

        SystemTray.getSystemTray().add(this);

    }

    private ResourceBundle loadResourceBundle() {
        try {
            return ResourceBundle.getBundle("MessageBundle");
        } catch (MissingResourceException mre) {
            return ResourceBundle.getBundle("MessageBundle", Locale.US);
        }
    }

    private void fireEvent(String event) {
        for (ActionListener al : this.getActionListeners()) {
            al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, event));
        }
    }

    public String getTitle() {
        return title;
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public Preferences getPrefs() {
        return prefs;
    }

    public DuckDnsClient getClient() {
        return client;
    }

    public void setLastUpdateStatus(String status) {
        lastUpdateStatus = status;
    }

    public void updateToolTip() {
        final DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");
        final int refreshInterval = Integer.parseInt(prefs.get("refresh", "5"));
        final Date nextRefresh = Date.from(Instant.now()
                .plus(refreshInterval, ChronoUnit.MINUTES));

        this.setToolTip(title + "\n" +
                lastUpdateStatus + "\n" +
                String.format(resourceBundle.getString("tooltip.next-update-format"), dateFormat.format(nextRefresh)) + "\n" +
                String.format(resourceBundle.getString("tooltip.refresh-minute-format"), refreshInterval)
        );
    }

    public void launchUrl(String urlToLaunch) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                JOptionPane.showMessageDialog(null, resourceBundle.getString("error.browser.unsupported"));
                return;
            }
            Desktop.getDesktop().browse(new URI(urlToLaunch));
        } catch (URISyntaxException ex) {
            JOptionPane.showMessageDialog(null, String.format(resourceBundle.getString("error.browser.invalid-url"), urlToLaunch));
        } catch (IOException ex) {
            final String errorMsg = String.format(resourceBundle.getString("error.browser.unknown-error"), ex.getMessage());
            JOptionPane.showMessageDialog(null, errorMsg);
        }
    }
}
