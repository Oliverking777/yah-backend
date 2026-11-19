package tsgp.yah_enterprise.transfer.application.dto;

import java.util.UUID;

/** One requested line: a product, the unit the employee chose and the quantity in that unit. */
public record TransferLineCommand(UUID productId, UUID unitId, int quantity) { }
