package tsgp.yah_enterprise.pricing.application.dto;

/**
 * Input for changing a price.
 * <ul>
 *   <li>price: null means the amount stays as it is.</li>
 *   <li>minimumQuantity: null means the range stays as it is. When it is given, the whole range is
 *       replaced by (minimumQuantity, maximumQuantity), and a null maximumQuantity then means
 *       "no upper bound".</li>
 * </ul>
 */
public record ChangeProductPriceCommand(
        Long price,
        Integer minimumQuantity,
        Integer maximumQuantity
) {
}
