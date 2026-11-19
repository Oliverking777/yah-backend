package tsgp.yah_enterprise.payment.domain.repository;

/** An amount in whole FCFA for one group. key is "ALL", a location id or a local day (yyyy-MM-dd). */
public record AmountRow(String key, long amount) { }
