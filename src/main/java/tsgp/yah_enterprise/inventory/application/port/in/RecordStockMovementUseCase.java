package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.RecordStockMovementCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockMovementResult;

/** The only way other modules change stock. There is no HTTP endpoint for it. */
public interface RecordStockMovementUseCase {

    /**
     * Appends one movement to the ledger and updates the stock level in the same transaction.
     * Recording the same document line twice applies it once and returns the first movement.
     *
     * @throws tsgp.yah_enterprise.inventory.domain.exception.InvalidStockMovementException    if the type, quantity or references are invalid
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockProductNotUsableException   if the product does not exist
     * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationNotUsableException  if the location does not exist
     * @throws tsgp.yah_enterprise.inventory.domain.exception.InsufficientStockException       if an OUT movement would take stock below zero
     */
    StockMovementResult recordMovement(RecordStockMovementCommand command);
}
