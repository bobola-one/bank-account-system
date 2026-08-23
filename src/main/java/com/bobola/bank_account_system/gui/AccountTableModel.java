package com.bobola.bank_account_system.gui;

import com.bobola.bank_account_system.dto.AccountResponse;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model backing the account list shown in {@link MainFrame}.
 * <p>
 * Swing's {@link javax.swing.JTable} does not know how to display an
 * {@link AccountResponse} directly; this class adapts a list of accounts
 * into the rows and columns the table understands.
 */
public class AccountTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"ID", "Account Holder", "Balance", "Created At"};
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private List<AccountResponse> accounts = new ArrayList<>();

    /**
     * Replaces the data currently shown in the table with a new list of accounts.
     *
     * @param accounts the accounts to display
     */
    public void setAccounts(List<AccountResponse> accounts) {
        this.accounts = accounts;
        fireTableDataChanged();
    }

    /**
     * Returns the account backing a given table row, used when a row is
     * selected and an action (deposit, withdraw, etc.) needs to know which
     * account was picked.
     *
     * @param rowIndex the selected row
     * @return the account at that row
     */
    public AccountResponse getAccountAt(int rowIndex) {
        return accounts.get(rowIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return accounts.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AccountResponse account = accounts.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> account.id();
            case 1 -> account.accountHolderName();
            case 2 -> account.balance();
            case 3 -> account.createdAt().format(DATE_FORMAT);
            default -> null;
        };
    }
}