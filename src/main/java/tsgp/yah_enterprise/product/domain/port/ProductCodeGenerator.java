package tsgp.yah_enterprise.product.domain.port;

/**
 * Gives the code of a new product: PRD-000001, PRD-000002... from a database sequence, so two products created at
 * the same time never get the same one. A number is never reused, even when the creation fails afterwards.
 */
public interface ProductCodeGenerator {

    /** The next code, never given before. */
    String nextCode();
}