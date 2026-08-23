package com.bobola.bank_account_system.gui;

import com.bobola.bank_account_system.dto.TransactionResponse;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Table model backing the transaction history shown in a
 * {@link TransactionHistoryDialog}.
 * <p>
 * Adapts a fixed list of {@link TransactionResponse} objects into rows and
 * columns for a {@link javax.swing.JTable}. Unlike {@link AccountTableModel},
 * this data never changes after construction, since a transaction history
 * is a read-only snapshot for a specific account at the moment it was fetched.
 */
public class TransactionTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {"Type", "Amount", "Date"};
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final List<TransactionResponse> transactions;

    /**
     * Constructs the model from a fixed list of transactions.
     *
     * @param transactions the transactions to display, typically from an
     *                     {@code AccountResponse}
     */
    public TransactionTableModel(List<TransactionResponse> transactions) {
        this.transactions = transactions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return transactions.size();
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
        TransactionResponse transaction = transactions.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> transaction.type();
            case 1 -> transaction.amount();
            case 2 -> transaction.timestamp().format(DATE_FORMAT);
            default -> null;
        };
    }
}