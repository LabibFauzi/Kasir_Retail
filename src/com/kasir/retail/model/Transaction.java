package com.kasir.retail.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private int id;
    private String invoice;
    private LocalDateTime date;
    private double total;
    private double payment;
    private double change;
    private List<TransactionItem> items;

    public Transaction() {
        this.items = new ArrayList<>();
    }

    public Transaction(int id, String invoice, LocalDateTime date, double total, double payment, double change) {
        this.id = id;
        this.invoice = invoice;
        this.date = date;
        this.total = total;
        this.payment = payment;
        this.change = change;
        this.items = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getInvoice() { return invoice; }
    public void setInvoice(String invoice) { this.invoice = invoice; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public double getPayment() { return payment; }
    public void setPayment(double payment) { this.payment = payment; }
    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }
    public List<TransactionItem> getItems() { return items; }
    public void setItems(List<TransactionItem> items) { this.items = items; }

    public String getDateFormatted() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    @Override
    public String toString() {
        return String.format("Invoice: %s | %s | Total: Rp%,.0f", invoice, getDateFormatted(), total);
    }
}
