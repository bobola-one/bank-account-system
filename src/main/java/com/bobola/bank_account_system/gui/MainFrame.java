package com.bobola.bank_account_system.gui;

import com.bobola.bank_account_system.dto.AccountResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Main window for the Bank Account Management System desktop client.
 * <p>
 * Displays all accounts in a table and provides actions to create accounts,
 * deposit, withdraw, and view transaction history, communicating with the
 * backend exclusively through {@link ApiClient}.
 */
public class MainFrame extends JFrame {

    private final ApiClient apiClient;
    private final AccountTableModel tableModel;
    private final JTable accountTable;

    /**
     * Constructs and lays out the main window, but does not show it.
     * Call {@link #setVisible(boolean)} to display it.
     */
    public MainFrame() {
        super("Bank Account Management System");

        this.apiClient = new ApiClient();
        this.tableModel = new AccountTableModel();
        this.accountTable = new JTable(tableModel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new JScrollPane(accountTable), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        refreshAccounts();
    }

    /**
     * Builds the row of action buttons shown at the bottom of the window.
     *
     * @return a panel containing all action buttons
     */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAccounts());
        panel.add(refreshButton);

        JButton createButton = new JButton("Create Account");
        createButton.addActionListener(e -> showCreateAccountDialog());
        panel.add(createButton);

        return panel;
    }

    /**
     * Reloads the account list from the server and updates the table.
     * <p>
     * Any communication failure is shown to the user in a dialog rather
     * than crashing the application.
     */
    private void refreshAccounts() {
        try {
            List<AccountResponse> accounts = apiClient.getAllAccounts();
            tableModel.setAccounts(accounts);
        } catch (IOException | InterruptedException | ApiException e) {
            showError("Failed to load accounts", e);
        }
    }

    /**
     * Shows a small dialog prompting for a new account's holder name and
     * initial balance, then creates the account via the API.
     */
    private void showCreateAccountDialog() {
        JTextField nameField = new JTextField();
        JTextField balanceField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Account Holder Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Initial Balance:"));
        panel.add(balanceField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Create Account", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            BigDecimal balance = new BigDecimal(balanceField.getText().trim());
            apiClient.createAccount(nameField.getText().trim(), balance);
            refreshAccounts();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Balance must be a valid number",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | InterruptedException | ApiException e) {
            showError("Failed to create account", e);
        }
    }

    /**
     * Displays an error dialog for a failed API call, preferring the
     * server's own message when the failure was an {@link ApiException}.
     *
     * @param context short description of what was being attempted
     * @param e       the exception that occurred
     */
    private void showError(String context, Exception e) {
        String message = (e instanceof ApiException apiException)
                ? apiException.getMessage()
                : e.getMessage();

        JOptionPane.showMessageDialog(this, context + ": " + message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}