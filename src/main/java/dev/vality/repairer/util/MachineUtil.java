package dev.vality.repairer.util;

public class MachineUtil {

    public static final String INVOICE_NAMESPACE = "invoice";
    public static final String WITHDRAWAL_NAMESPACE = "withdrawal";

    public static boolean isInvoicing(String namespace) {
        return INVOICE_NAMESPACE.equals(namespace);
    }

    public static boolean isWithdrawal(String namespace) {
        return WITHDRAWAL_NAMESPACE.equals(namespace);
    }
}
