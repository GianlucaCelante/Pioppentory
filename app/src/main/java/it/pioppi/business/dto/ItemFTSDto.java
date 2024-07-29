package it.pioppi.business.dto;

public class ItemFTSDto {

        private Integer rowid;
        private String name;
        private String barcode;

        public ItemFTSDto(Integer rowid, String name, String barcode) {
            this.rowid = rowid;
            this.name = name;
            this.barcode = barcode;
        }

        public Integer getId() {
            return rowid;
        }

        public String getName() {
            return name;
        }

        public String getBarcode() {
            return barcode;
        }
}
