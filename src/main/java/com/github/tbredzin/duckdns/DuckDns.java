package com.github.tbredzin.duckdns;

import com.github.tbredzin.duckdns.systemtray.DuckdnsTray;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

public class DuckDns implements ActionListener, Runnable {

    public static final String DUCK_DNS_TITLE = "DuckDns Updater (vs1.0.5)";
    private final Preferences preferences;
    private final DuckdnsTray duckdnsTray;
    private Timer timer = null;

    public static void main(String[] args) {
        try {
            DuckDns app = new DuckDns();
            new Thread(app).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DuckDns() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        preferences = Preferences.userNodeForPackage(DuckDns.class);
        duckdnsTray = new DuckdnsTray(DUCK_DNS_TITLE, preferences);
        duckdnsTray.addActionListener(this);
    }

    @Override
    public void run() {
        schedule();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        schedule();
    }

    private void schedule() {
        final long refresh = TimeUnit.MINUTES.toMillis(
                Long.parseLong(preferences.get("refresh", "5"))
        );
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new DuckDnsTimerTask(duckdnsTray), refresh, refresh);
    }
}