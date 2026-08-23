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
        this.accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        JButton depositButton = new JButton("Deposit");
        depositButton.addActionListener(e -> showAmountDialog("Deposit", apiClient::deposit));
        panel.add(depositButton);

        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(e -> showAmountDialog("Withdraw", apiClient::withdraw));
        panel.add(withdrawButton);

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
     * Prompts for an amount and applies it to the currently selected account
     * using the given operation (deposit or withdraw).
     * <p>
     * Shows an error if no row is selected, since the action needs to know
     * which account to apply the amount to.
     *
     * @param title     dialog title and label, e.g. "Deposit" or "Withdraw"
     * @param operation the API call to perform, taking an account id and amount
     */
    private void showAmountDialog(String title, AccountOperation operation) {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an account first",
                    "No Account Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AccountResponse account = tableModel.getAccountAt(selectedRow);

        String input = JOptionPane.showInputDialog(
                this, title + " amount for " + account.accountHolderName() + ":", title, JOptionPane.PLAIN_MESSAGE);

        if (input == null) {
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(input.trim());
            operation.apply(account.id(), amount);
            refreshAccounts();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (IOException | InterruptedException | ApiException e) {
            showError(title + " failed", e);
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

    /**
     * Represents an {@link ApiClient} operation that takes an account id and
     * an amount, such as {@code deposit} or {@code withdraw}.
     * <p>
     * Exists so {@link #showAmountDialog} can be shared between both buttons
     * instead of duplicating the same dialog/validation logic twice.
     */
    @FunctionalInterface
    private interface AccountOperation {

        /**
         * Applies this operation to the given account.
         *
         * @param accountId the account to apply the operation to
         * @param amount    the amount involved
         * @return the updated account
         * @throws IOException          if the request fails to send or the response fails to read
         * @throws InterruptedException if the request is interrupted
         * @throws ApiException         if the server returns an error response
         */
        AccountResponse apply(Long accountId, BigDecimal amount)
                throws IOException, InterruptedException, ApiException;
    }
}  