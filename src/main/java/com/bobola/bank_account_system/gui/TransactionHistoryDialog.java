package com.bobola.bank_account_system.gui;

import com.bobola.bank_account_system.dto.AccountResponse;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog showing the full transaction history for a single account.
 */
public class TransactionHistoryDialog extends JDialog {

    /**
     * Constructs and lays out the dialog for the given account, but does
     * not display it. Call {@link #setVisible(boolean)} to show it.
     *
     * @param owner   the parent window this dialog is centered relative to
     * @param account the account whose transaction history is shown
     */
    public TransactionHistoryDialog(Frame owner, AccountResponse account) {
        super(owner, "Transaction History: " + account.accountHolderName(), true);

        TransactionTableModel tableModel = new TransactionTableModel(account.transactions());
        JTable table = new JTable(tableModel);

        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildSummaryPanel(account), BorderLayout.NORTH);

        setSize(500, 350);
        setLocationRelativeTo(owner);
    }

    /**
     * Builds a small header showing the account holder and current balance
     * above the transaction table.
     *
     * @param account the account being displayed
     * @return a panel with summary labels
     */
    private JPanel buildSummaryPanel(AccountResponse account) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Account Holder: " + account.accountHolderName()));
        panel.add(new JLabel("   |   Current Balance: " + account.balance()));
        return panel;
    }
}