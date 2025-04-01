-- Creazione delle tabelle principali
CREATE TABLE IF NOT EXISTS `ITEM` (
    `id` TEXT NOT NULL,
    `fts_id` INTEGER,
    `name` TEXT,
    `tot_portions` INTEGER,
    `status` TEXT,
    `checked` INTEGER NOT NULL DEFAULT 0,
    `barcode` TEXT,
    `check_date` TEXT,
    `note` TEXT,
    `image_url` TEXT,
    `creation_date` TEXT,
    `last_update` TEXT,
    `provider_id` TEXT,
    PRIMARY KEY(`id`),
    FOREIGN KEY(`provider_id`) REFERENCES `PROVIDER`(`id`) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS `ITEM_DETAIL` (
    `id` TEXT NOT NULL,
    `quantity_to_be_ordered` INTEGER,
    `ordered_quantity` INTEGER,
    `portions_required_on_saturday` INTEGER,
    `portions_required_on_sunday` INTEGER,
    `portions_per_weekend` INTEGER,
    `portions_on_holiday` INTEGER,
    `max_portions_sold` INTEGER,
    `delivery_date` TEXT,
    `creation_date` TEXT,
    `last_update` TEXT,
    `item_id` TEXT,
    PRIMARY KEY(`id`),
    FOREIGN KEY(`item_id`) REFERENCES `ITEM`(`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `PROVIDER` (
    `id` TEXT NOT NULL,
    `name` TEXT,
    `address` TEXT,
    `phone_number` TEXT,
    `email` TEXT,
    `creation_date` TEXT,
    `last_update` TEXT,
    PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `QUANTITY_TYPE` (
    `id` TEXT NOT NULL,
    `quantity_type` TEXT,
    `quantity_type_description` TEXT,
    `quantity_type_available` INTEGER,
    `quantity_type_purpose` TEXT,
    `units_per_quantity_type` INTEGER,
    `creation_date` TEXT,
    `last_update` TEXT,
    `item_id` TEXT,
    PRIMARY KEY(`id`),
    FOREIGN KEY(`item_id`) REFERENCES `ITEM`(`id`) ON DELETE CASCADE
);

-- Creazione della tabella virtuale per la ricerca full-text
CREATE VIRTUAL TABLE item_fts USING fts4(
    content=`ITEM`,
    name,
    barcode

);

CREATE TABLE IF NOT EXISTS `ITEM_HISTORY` (
    `id` TEXT NOT NULL,
    `provider_name` TEXT,
    `item_name` TEXT,
    `quantity_present` INTEGER,
    `quantity_ordered` INTEGER,
    `portions_per_weekend` INTEGER,
    `inventory_closure_date` TEXT,
    `delivery_date` TEXT,
    `barcode` TEXT,
    `note` TEXT,
    `creation_date` TEXT,
    `last_update` TEXT,
    PRIMARY KEY(`id`)
);

CREATE TABLE IF NOT EXISTS `ITEM_TAG` (
    `id` TEXT NOT NULL,
    `name` TEXT,
    `creation_date` TEXT,
    `last_update` TEXT,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `ITEM_TAG_JOIN` (
    `item_id` TEXT NOT NULL,
    `tag_id` TEXT NOT NULL,
    PRIMARY KEY (`item_id`, `tag_id`),
    FOREIGN KEY (`item_id`) REFERENCES `ITEM`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `ITEM_TAG`(`id`) ON DELETE NO ACTION
);

-- Creazione degli indici
CREATE INDEX IF NOT EXISTS `index_ITEM_id` ON `ITEM` (`id`);
CREATE INDEX IF NOT EXISTS `index_ITEM_provider_id` ON `ITEM` (`provider_id`);
CREATE INDEX IF NOT EXISTS `index_ITEM_DETAIL_item_id` ON `ITEM_DETAIL` (`item_id`);
CREATE INDEX IF NOT EXISTS `index_QUANTITY_TYPE_item_id` ON `QUANTITY_TYPE` (`item_id`);
CREATE INDEX IF NOT EXISTS `index_ITEM_HISTORY_id` ON `ITEM_HISTORY` (`id`);
CREATE INDEX IF NOT EXISTS `index_ITEM_TAG_id` ON `ITEM_TAG`(`id`);
CREATE INDEX `index_ITEM_TAG_JOIN_itemId` ON `ITEM_TAG_JOIN`(`item_id`);
CREATE INDEX `index_ITEM_TAG_JOIN_tagId` ON `ITEM_TAG_JOIN`(`tag_id`);

-- Aggiornamento della tabella item_fts con i dati inseriti nella tabella ITEM
INSERT INTO item_fts (rowid, name, barcode)
SELECT fts_id, name, barcode FROM ITEM;