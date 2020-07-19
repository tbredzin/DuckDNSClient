package com.github.tbredzin.duckdns.systemtray.popup;

import com.github.tbredzin.duckdns.systemtray.DuckdnsTray;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public class DuckDnsAbout extends JEditorPane {

    public static final String ABOUT_HTML = "<br>Developed by: ETX Software Inc.<br><a href=\"http://www.ETX.ca\">www.ETX.ca</a><br>" +
            "Forked by <a href=\"https://github.com/tbredzin\">tbredzin</a>.";

    public DuckDnsAbout(DuckdnsTray parent) {
        super("text/html", parent.getTitle() + ABOUT_HTML);

        // handle link events
        this.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                parent.launchUrl(e.getURL().toString()); // roll your own link launcher or use Desktop if J6+
            }
        });
        this.setEditable(false);
        final JLabel jLabel = new JLabel();
        this.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        this.setFont(jLabel.getFont());
        this.setBackground(jLabel.getBackground());
    }
}
