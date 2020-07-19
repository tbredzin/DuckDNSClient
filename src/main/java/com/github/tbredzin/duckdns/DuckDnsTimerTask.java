package com.github.tbredzin.duckdns;

import com.github.tbredzin.duckdns.systemtray.DuckdnsTray;

import java.awt.*;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import static com.github.tbredzin.duckdns.DuckDns.DUCK_DNS_TITLE;

public class DuckDnsTimerTask extends TimerTask {

    private final DuckdnsTray tray;

    public DuckDnsTimerTask(DuckdnsTray tray) {
        this.tray = tray;
    }

    @Override
    public void run() {
        final Preferences prefs = tray.getPrefs();
        final ResourceBundle bundle = tray.getResourceBundle();
        final DuckDnsClient client = tray.getClient();

        final String domain = prefs.get("domain", "");
        final String token = prefs.get("token", "");

        final Optional<String> myIp = client.getMyIp();
        final String registeredDomainIp;
        try {
            registeredDomainIp = client.getDomainIP(domain);
            boolean notify = Boolean.parseBoolean(prefs.get("updatemessages", "false"));
            myIp.ifPresentOrElse(
                    ip -> {
                        if (ip.equals(registeredDomainIp)) { //already in sync
                            final String message = String.format(bundle.getString("background.status.in-sync"), ip, domain);
                            tray.setLastUpdateStatus(message);
                            if (notify) {
                                tray.displayMessage(DUCK_DNS_TITLE, message, TrayIcon.MessageType.INFO);
                            }
                        } else { //Need update
                            switch (client.updateDuckDns(domain, token, ip)) {
                                case "KO":
                                    tray.displayMessage(DUCK_DNS_TITLE, bundle.getString("background.update.error"),
                                            TrayIcon.MessageType.ERROR);
                                    break;

                                case "OK":
                                    if (notify) {
                                        String msg = String.format(bundle.getString("background.update.success"), ip);
                                        tray.displayMessage(DUCK_DNS_TITLE, msg, TrayIcon.MessageType.INFO);
                                    }
                                    tray.setLastUpdateStatus(String.format(
                                            bundle.getString("background.status.updated"), registeredDomainIp, myIp
                                    ));
                                    break;

                                default:
                                    tray.displayMessage(DUCK_DNS_TITLE, bundle.getString("background.update.unknown-error"),
                                            TrayIcon.MessageType.ERROR);
                                    tray.setLastUpdateStatus(String.format(bundle.getString("background.status.unknown-error"), registeredDomainIp));
                            }
                        }
                    },
                    () -> tray.displayMessage(DUCK_DNS_TITLE, bundle.getString("my-ip.error"), TrayIcon.MessageType.ERROR)
            );
        } catch (UnknownHostException e) {
            final String error = String.format(bundle.getString("background.update.domain-error"), domain, e.getMessage());
            tray.displayMessage(DUCK_DNS_TITLE, error, TrayIcon.MessageType.ERROR);
        }
        tray.updateToolTip();
    }
}
