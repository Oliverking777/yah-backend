-- =============================================================
-- V22 – Payment module
-- =============================================================
-- Payments, receivables (what a sale owes), allocations (which payment paid which sale) and credit approvals.
-- Debt is never stored as an editable number: outstanding = total_amount - allocated_amount of the receivable,
-- and allocations are never deleted (a reversed payment flips them to REVERSED).
-- Money is whole FCFA in BIGINT. No foreign keys to other modules: customer, location, sale and user ids are
-- plain UUIDs. Permissions (PAYMENT_CREATE, PAYMENT_READ, BALANCE_READ, CREDIT_APPROVE) already exist in V10.
-- Merge order: Inventory (V20), Transfer (V21), then this one.
-- =============================================================

CREATE SEQUENCE public.payment_number_seq START WITH 1 INCREMENT BY 1;

-- ---------- payments ----------
CREATE TABLE public.payments (
    payment_id       UUID         NOT NULL,
    payment_number   VARCHAR(20)  NOT NULL,
    customer_id      UUID         NOT NULL,
    location_id      UUID         NOT NULL,
    amount           BIGINT       NOT NULL,
    payment_method   VARCHAR(20)  NOT NULL,
    reference        VARCHAR(50),
    received_by      UUID         NOT NULL,
    received_at      TIMESTAMPTZ  NOT NULL,
    payment_status   VARCHAR(20)  NOT NULL,
    allocated_amount BIGINT       NOT NULL DEFAULT 0,
    reversed_by      UUID,
    reversal_reason  VARCHAR(500),
    reversed_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_payments PRIMARY KEY (payment_id),
    CONSTRAINT uq_payments_payment_number UNIQUE (payment_number),
    CONSTRAINT ck_payments_amount CHECK (amount > 0),
    CONSTRAINT ck_payments_allocated CHECK (allocated_amount >= 0 AND allocated_amount <= amount),
    CONSTRAINT ck_payments_method CHECK (payment_method IN ('CASH', 'MOBILE_MONEY')),
    CONSTRAINT ck_payments_status CHECK (payment_status IN ('RECORDED', 'REVERSED')),
    CONSTRAINT ck_payments_reference CHECK (
        (payment_method = 'MOBILE_MONEY' AND reference IS NOT NULL AND char_length(reference) >= 3)
        OR (payment_method = 'CASH' AND reference IS NULL)),
    CONSTRAINT ck_payments_reversal CHECK (
        (payment_status = 'RECORDED' AND reversed_by IS NULL AND reversed_at IS NULL AND reversal_reason IS NULL)
        OR (payment_status = 'REVERSED' AND reversed_by IS NOT NULL AND reversed_at IS NOT NULL
            AND reversal_reason IS NOT NULL AND allocated_amount = 0))
);

-- The same provider reference cannot be recorded twice for one method (stops a double entry of one mobile payment).
CREATE UNIQUE INDEX uq_payments_method_reference
    ON public.payments (payment_method, reference) WHERE reference IS NOT NULL;
CREATE INDEX idx_payments_customer ON public.payments (customer_id, received_at DESC);
CREATE INDEX idx_payments_location ON public.payments (location_id, received_at DESC);

COMMENT ON TABLE  public.payments IS 'Money received from a customer. Never deleted: a wrong payment is reversed. Never store card, PIN or wallet credentials.';
COMMENT ON COLUMN public.payments.reference IS 'Mobile money provider reference. Always null for CASH.';

-- ---------- receivables ----------
CREATE TABLE public.receivables (
    sale_id          UUID         NOT NULL,
    customer_id      UUID         NOT NULL,
    location_id      UUID         NOT NULL,
    total_amount     BIGINT       NOT NULL,
    allocated_amount BIGINT       NOT NULL DEFAULT 0,
    version          BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_receivables PRIMARY KEY (sale_id),
    CONSTRAINT ck_receivables_total CHECK (total_amount > 0),
    CONSTRAINT ck_receivables_allocated CHECK (allocated_amount >= 0 AND allocated_amount <= total_amount)
);

CREATE INDEX idx_receivables_customer ON public.receivables (customer_id, created_at);
CREATE INDEX idx_receivables_open ON public.receivables (created_at) WHERE allocated_amount < total_amount;

COMMENT ON TABLE  public.receivables IS 'What one sale owes, keyed by the sale id (a Sales id: plain UUID). Outstanding = total_amount - allocated_amount.';

-- ---------- payment_allocations ----------
CREATE TABLE public.payment_allocations (
    allocation_id     UUID         NOT NULL,
    payment_id        UUID         NOT NULL,
    sale_id           UUID         NOT NULL,
    allocated_amount  BIGINT       NOT NULL,
    allocation_status VARCHAR(20)  NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    reversed_at       TIMESTAMPTZ,

    CONSTRAINT pk_payment_allocations PRIMARY KEY (allocation_id),
    CONSTRAINT fk_payment_allocations_payment FOREIGN KEY (payment_id) REFERENCES public.payments (payment_id),
    CONSTRAINT fk_payment_allocations_receivable FOREIGN KEY (sale_id) REFERENCES public.receivables (sale_id),
    CONSTRAINT ck_payment_allocations_amount CHECK (allocated_amount > 0),
    CONSTRAINT ck_payment_allocations_status CHECK (allocation_status IN ('ACTIVE', 'REVERSED')),
    CONSTRAINT ck_payment_allocations_reversed CHECK (
        (allocation_status = 'ACTIVE' AND reversed_at IS NULL)
        OR (allocation_status = 'REVERSED' AND reversed_at IS NOT NULL))
);

CREATE INDEX idx_payment_allocations_payment ON public.payment_allocations (payment_id);
CREATE INDEX idx_payment_allocations_sale ON public.payment_allocations (sale_id);

COMMENT ON TABLE public.payment_allocations IS 'Part of one payment applied to one sale. Never deleted: reversing the payment flips ACTIVE to REVERSED.';

-- ---------- credit_approvals ----------
CREATE TABLE public.credit_approvals (
    credit_approval_id UUID         NOT NULL,
    sale_id            UUID         NOT NULL,
    customer_id        UUID         NOT NULL,
    requested_amount   BIGINT       NOT NULL,
    approved_amount    BIGINT,
    reason             VARCHAR(500) NOT NULL,
    decision_note      VARCHAR(500),
    approval_status    VARCHAR(20)  NOT NULL,
    requested_by       UUID         NOT NULL,
    approved_by        UUID,
    requested_at       TIMESTAMPTZ  NOT NULL,
    decided_at         TIMESTAMPTZ,
    consumed_at        TIMESTAMPTZ,

    CONSTRAINT pk_credit_approvals PRIMARY KEY (credit_approval_id),
    CONSTRAINT ck_credit_approvals_requested CHECK (requested_amount > 0),
    CONSTRAINT ck_credit_approvals_approved CHECK (
        approved_amount IS NULL OR (approved_amount > 0 AND approved_amount <= requested_amount)),
    CONSTRAINT ck_credit_approvals_status CHECK (approval_status IN ('REQUESTED', 'APPROVED', 'REJECTED', 'CONSUMED')),
    CONSTRAINT ck_credit_approvals_decision CHECK (
        approval_status = 'REQUESTED' OR (approved_by IS NOT NULL AND decided_at IS NOT NULL)),
    CONSTRAINT ck_credit_approvals_approver CHECK (approved_by IS NULL OR approved_by <> requested_by),
    CONSTRAINT ck_credit_approvals_consumed CHECK (
        (approval_status = 'CONSUMED' AND consumed_at IS NOT NULL AND approved_amount IS NOT NULL)
        OR (approval_status <> 'CONSUMED' AND consumed_at IS NULL))
);

-- At most one open (REQUESTED or APPROVED) approval per sale.
CREATE UNIQUE INDEX uq_credit_approvals_open_sale
    ON public.credit_approvals (sale_id) WHERE approval_status IN ('REQUESTED', 'APPROVED');
CREATE INDEX idx_credit_approvals_status ON public.credit_approvals (approval_status, requested_at DESC);
CREATE INDEX idx_credit_approvals_customer ON public.credit_approvals (customer_id);

COMMENT ON TABLE public.credit_approvals IS 'Permission to sell on credit. The approver is never the requester. An APPROVED approval is consumed once by a completed sale.';
