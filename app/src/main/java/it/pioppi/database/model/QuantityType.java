package it.pioppi.database.model;

public enum QuantityType {

    STRATO,
    CARTONE,
    SACCHETTO,
    KG;


    public static Integer convertToPorzione(QuantityType quantityType, Integer quantity) {

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

}
