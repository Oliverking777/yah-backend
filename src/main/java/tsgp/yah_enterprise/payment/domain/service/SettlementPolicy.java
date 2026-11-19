package tsgp.yah_enterprise.payment.domain.service;

import tsgp.yah_enterprise.payment.domain.exception.*;
import tsgp.yah_enterprise.payment.domain.model.CreditApproval;
import tsgp.yah_enterprise.payment.domain.valueobject.Money;

import java.util.UUID;

/** The rules for closing a sale: paid in full, or the rest on an approved credit. */
public final class SettlementPolicy {

    private SettlementPolicy() {
    }

    /** Returns the amount left on credit (zero when paid in full), or throws. */
    public static Money amountDue(UUID saleId, UUID customerId, boolean walkIn, Money total, Money paid,
                                  CreditApproval approval) {
        if (!total.isPositive()) {
            throw new InvalidPaymentAmountException("The total of a sale must be greater than zero");
        }
        if (paid.isGreaterThan(total)) {
            throw new SettlementOverpaymentException("Paid " + paid + " FCFA is more than the total " + total);
        }
        Money due = total.minus(paid);
        if (due.isZero()) {
            return due;
        }
        if (walkIn) {
            throw new CreditNotAllowedForCustomerException(
                    "The walk-in customer must pay in full; credit needs an identified customer");
        }
        if (approval == null) {
            throw new CreditApprovalRequiredException("A credit approval is required for the unpaid " + due + " FCFA");
        }
        if (!saleId.equals(approval.getSaleId()) || !customerId.equals(approval.getCustomerId())
                || !approval.isApproved()) {
            throw new CreditApprovalMismatchException(
                    "The credit approval does not match this sale and customer or is not approved");
        }
        if (approval.getApprovedAmount().compareTo(due) < 0) {
            throw new CreditApprovalMismatchException("The approval covers only " + approval.getApprovedAmount()
                    + " FCFA but " + due + " FCFA are unpaid");
        }
        return due;
    }
}
