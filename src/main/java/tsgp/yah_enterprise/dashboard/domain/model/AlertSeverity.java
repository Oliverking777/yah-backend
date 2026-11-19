package tsgp.yah_enterprise.dashboard.domain.model;

public enum AlertSeverity {
    INFO(0), WARNING(1), CRITICAL(2);

    private final int rank;

    AlertSeverity(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
