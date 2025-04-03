package it.pioppi.database.model;

public enum QuantityTypeEnum {

    STRATO("Strato"),
    CARTONE("Cartone"),
    VASCHETTA("Vaschetta"),
    SACCHETTO("Sacchetto"),
    PACCO("Pacco"),
    FUSTO("Fusto"),
    PEZZO("Pezzo"),
    PORZIONE("Porzione");

    private final String description;

    QuantityTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static int calculateTotalPortions(int quantity, int unitsPerPack) {
        return quantity * unitsPerPack;
    }

}
