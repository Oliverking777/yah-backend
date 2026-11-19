package tsgp.yah_enterprise.purchasing.domain.repository;

import tsgp.yah_enterprise.purchasing.domain.model.GoodsReceipt;
import tsgp.yah_enterprise.purchasing.domain.model.ReceiptStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepository {

    GoodsReceipt save(GoodsReceipt receipt);

    Optional<GoodsReceipt> findById(UUID goodsReceiptId);

    /** Reads only the purchase id, so no receipt is cached before the purchase is locked. */
    Optional<UUID> findPurchaseIdOf(UUID goodsReceiptId);

    List<GoodsReceipt> findByPurchase(UUID purchaseId);

    PagedReceipts findAll(int page, int size, ReceiptStatus status, UUID purchaseId, UUID locationId);

    /** Number of CONFIRMED receipts of a purchase. */
    default long countConfirmed(UUID purchaseId) {
        return findByPurchase(purchaseId).stream()
                .filter(r -> r.getReceiptStatus() == ReceiptStatus.CONFIRMED).count();
    }

    /** Draft receipts of a purchase (cancelled when the purchase is closed). */
    default List<GoodsReceipt> findDraftsByPurchase(UUID purchaseId) {
        return findByPurchase(purchaseId).stream()
                .filter(r -> r.getReceiptStatus() == ReceiptStatus.DRAFT).toList();
    }
}
