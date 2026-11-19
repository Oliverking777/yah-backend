package tsgp.yah_enterprise.unit.domain.port;

/**
 * Gives the code of a new unit: U0001, U0002... from a database sequence, so two units created at the same time never
 * get the same one. A number is never reused, even when the creation fails afterwards.
 */
public interface UnitCodeGenerator {

    /** The next code, never given before. */
    String nextCode();
}