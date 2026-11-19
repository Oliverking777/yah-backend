package tsgp.yah_enterprise.inventory.application.port.in;

import tsgp.yah_enterprise.inventory.application.dto.InitializeStockCommand;
import tsgp.yah_enterprise.inventory.application.dto.StockInitializationResult;

/**
 * Create and execute a stock initialization containing multiple products at one location.
 * The entire operation is atomic: if one item fails, none are committed.
 *
 * @throws tsgp.yah_enterprise.inventory.domain.exception.InvalidStockInitializationException        if data is invalid
 * @throws tsgp.yah_enterprise.inventory.domain.exception.StockProductNotUsableException              if a product is missing or inactive
 * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationNotUsableException            if the location is missing or inactive
 * @throws tsgp.yah_enterprise.inventory.domain.exception.StockAlreadyInitializedException           if a product was already initialized at this location
 * @throws tsgp.yah_enterprise.inventory.domain.exception.DuplicateProductInInitializationException  if the same product appears more than once
 * @throws tsgp.yah_enterprise.inventory.domain.exception.StockLocationAccessException              if a limited user targets another location
 */
public interface InitializeStockUseCase {

    StockInitializationResult initializeStock(InitializeStockCommand command);
}
