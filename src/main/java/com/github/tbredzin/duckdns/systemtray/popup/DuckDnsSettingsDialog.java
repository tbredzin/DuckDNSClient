package com.github.tbredzin.duckdns.systemtray.popup;

import com.github.tbredzin.duckdns.systemtray.DuckdnsTray;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.Objects;

public class DuckDnsSettingsDialog extends JDialog {

    private final JTextField domainField;
    private final JTextField tokenField;
    private final JComboBox<String> refreshBox;
    private final JComboBox<String> showNotificationsBox;
    private final JOptionPane optionPane;

    public DuckDnsSettingsDialog(DuckdnsTray parent) {

        //Create a window using JFrame with title ( Two text component in JOptionPane )
        super((JFrame) null, "DuckDNS Settings", true);

        final JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel(parent.getResourceBundle().getString("panel.settings.domain")));
        domainField = new HintTextField(parent.getPrefs().get("domain", ""), 30,
                parent.getResourceBundle().getString("hint.domain"));
        panel.add(domainField);
        panel.add(new JLabel(parent.getResourceBundle().getString("panel.settings.token")));
        tokenField = new HintTextField(parent.getPrefs().get("token", ""), 30,
                parent.getResourceBundle().getString("hint.token"));
        panel.add(tokenField);

        panel.add(new JLabel(parent.getResourceBundle().getString("panel.settings.refresh-interval")));
        refreshBox = new JComboBox<>(new String[]{"5", "10", "15", "30", "60"});
        refreshBox.setSelectedItem(parent.getPrefs().get("refresh", "5"));
        panel.add(refreshBox);

        panel.add(new JLabel(parent.getResourceBundle().getString("panel.settings.show-notifications")));
        showNotificationsBox = new JComboBox<>(new String[]{"true", "false"});
        showNotificationsBox.setSelectedItem(parent.getPrefs().get("updatemessages", "true"));
        panel.add(showNotificationsBox);

        panel.add(new JLabel(parent.getResourceBundle().getString("panel.settings.settings")));

        // html content
        final JEditorPane externalLink = new JEditorPane("text/html", "<html><a href=\"https://www.duckdns.org\">https://duckdns.org</a></html>");
        externalLink.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        final JLabel label = new JLabel();
        externalLink.setEditable(false);
        externalLink.setFont(label.getFont());
        externalLink.setBackground(label.getBackground());

        // handle link events
        externalLink.addHyperlinkListener(e1 -> {
            if (e1.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                parent.launchUrl(e1.getURL().toString());
            }
        });
        panel.add(externalLink);

        optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        optionPane.addPropertyChangeListener(e1 -> {
            String prop = e1.getPropertyName();
            if (this.isVisible()
                    && (e1.getSource() == optionPane)
                    && (prop.equals(JOptionPane.VALUE_PROPERTY))
                    && !Objects.equals(JOptionPane.UNINITIALIZED_VALUE, e1.getNewValue())) {
                if (Objects.equals(JOptionPane.OK_OPTION, e1.getNewValue()) && !this.validateInput()) {
                    optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE); //Reset choice on validation fail
                } else {
                    this.setVisible(false);
                }
            }
        });

        this.setContentPane(optionPane);
        this.pack();
    }

    public Object getValue() {
        return optionPane.getValue();
    }


    public String getDomain() {
        return domainField.getText().replace(".duckdns.org", "");
    }

    public String getToken() {
        return tokenField.getText();
    }

    public String getRefresh() {
        return (String) refreshBox.getSelectedItem();
    }

    public String getShowNotification() {
        return (String) showNotificationsBox.getSelectedItem();
    }

    public boolean validateInput() {
        if (getDomain().length() < 1 && (getToken().length() < 1)) {
            domainField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            tokenField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            return false;
        } else if (getDomain().length() < 1) {
            domainField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            return false;
        } else if (getToken().length() < 1) {
            tokenField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            return false;
        }
        final Border defaultBorder = UIManager.getBorder("Textfield.border");
        domainField.setBorder(defaultBorder);
        tokenField.setBorder(defaultBorder);
        return true;

    }
}

