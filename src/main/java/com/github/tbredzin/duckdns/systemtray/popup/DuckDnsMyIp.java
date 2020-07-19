package com.github.tbredzin.duckdns.systemtray.popup;

import com.github.tbredzin.duckdns.systemtray.DuckdnsTray;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public class DuckDnsMyIp extends JEditorPane {

    public DuckDnsMyIp(DuckdnsTray parent, String ip) {
        super("text/html", "Your External IP Address: <a href=\"http://checkip.amazonaws.com\">" + ip + "</a>");
        // handle link events
        this.addHyperlinkListener(e13 -> {
            if (e13.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                parent.launchUrl(e13.getURL().toString()); // roll your own link launcher or use Desktop if J6+
            }
        });
        final JLabel jLabel = new JLabel();
        this.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        this.setFont(jLabel.getFont());
        this.setBackground(jLabel.getBackground());
        this.setEditable(false);
    }
}
