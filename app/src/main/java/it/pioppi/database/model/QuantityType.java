package it.pioppi.database.model;

public enum QuantityType {

    STRATO("Strato"),
    CARTONE("Cartone"),
    SACCHETTO("Sacchetto"),
    PEZZI("Pezzi");

    private final String description;
    private Integer quantity;

    QuantityType(String description) {
        this.description = description;
        this.quantity = 0;
    }

    public static Integer getTotPortions(QuantityType quantityType, Integer quantity) {

        switch (quantityType) {
            case STRATO:
                return quantity * 9 * 5 * 10;
            case CARTONE:
                return quantity * 5 * 10;
            case SACCHETTO:
                return quantity * 10;
            default:
                return quantity;
        }
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}