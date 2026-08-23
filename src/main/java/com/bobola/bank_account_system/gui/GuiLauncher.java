package com.bobola.bank_account_system.gui;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Swing desktop client.
 * <p>
 * Run this class separately from {@code BankAccountSystemApplication} — the
 * backend server must already be running on port 8080 for this GUI to work,
 * since every action here goes through {@link ApiClient} over HTTP.
 */
public class GuiLauncher {

    /**
     * Starts the Swing GUI on Swing's event dispatch thread.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}