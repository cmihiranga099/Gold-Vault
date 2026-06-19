package lk.goldvault.backend.enums;

public enum GoldPurity {
    K24("24K"), K22("22K"), K21("21K"), K18("18K"), P916("916"), P750("750"), OTHER("OTHER");

    private final String label;

    GoldPurity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}