package tsgp.yah_enterprise.location.domain.port;

/**
 * Gives the code of a new location: LOC-0001, LOC-0002... from a database sequence, so two locations created at the
 * same time never get the same one. A number is never reused, even when the creation fails afterwards.
 */
public interface LocationCodeGenerator {

    /** The next code, never given before. */
    String nextCode();
}