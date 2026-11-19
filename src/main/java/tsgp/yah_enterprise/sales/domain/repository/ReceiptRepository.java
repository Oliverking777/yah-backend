package tsgp.yah_enterprise.sales.domain.repository;

import tsgp.yah_enterprise.sales.domain.model.Receipt;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository {

    Receipt save(Receipt receipt);

    Optional<Receipt> findByOrderId(UUID orderId);
}
