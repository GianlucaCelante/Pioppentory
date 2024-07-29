-- Creazione delle tabelle principali
CREATE TABLE IF NOT EXISTS `ITEM` (
    `id` TEXT NOT NULL,
    `fts_id` INTEGER NOT NULL,
    `name` TEXT,
    `tot_portions` INTEGER,
    `status` TEXT,
    `barcode` TEXT,
    `check_date` TEXT,
    `note` TEXT,
    `creation_date` TEXT,
    `last_update` TEXT,
    PRIMARY KEY(`id`)
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
    `item_id` TEXT,
    PRIMARY KEY(`id`),
    FOREIGN KEY(`item_id`) REFERENCES `ITEM`(`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `QUANTITY_TYPE` (
    `id` TEXT NOT NULL,
    `quantity_type` TEXT,
    `quantity_type_description` TEXT,
    `quantity_type_available` INTEGER,
    `quantity_type_purpose` TEXT,
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

-- Creazione degli indici
CREATE INDEX IF NOT EXISTS `index_ITEM_id` ON `ITEM` (`id`);
CREATE INDEX IF NOT EXISTS `index_ITEM_DETAIL_item_id` ON `ITEM_DETAIL` (`item_id`);
CREATE INDEX IF NOT EXISTS `index_PROVIDER_item_id` ON `PROVIDER` (`item_id`);
CREATE INDEX IF NOT EXISTS `index_QUANTITY_TYPE_item_id` ON `QUANTITY_TYPE` (`item_id`);

-- Inserimento dei dati di esempio nella tabella ITEM
INSERT INTO ITEM (id, fts_id, name, tot_portions, status, barcode, note, check_date, creation_date, last_update) VALUES
('00000000-0000-0000-0000-000000000001', '1', 'Item 1', 10, 'WHITE', 'barcode1', 'note1', '2023-01-01T00:00:00', '2023-01-02T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000002', '2', 'Item 2', 20, 'GREEN', 'barcode2', null, '2023-01-03T00:00:00', '2023-01-04T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000003', '3', 'Item 3', 30, 'RED', 'barcode3', null, '2023-01-05T00:00:00', '2023-01-06T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000004', '4', 'Item 4', 40, 'BLUE', 'barcode4', null, '2023-01-07T00:00:00', '2023-01-08T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000005', '5', 'Item 5', 50, 'GREEN', 'barcode5', null, '2023-01-09T00:00:00', '2023-01-10T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000006', '6', 'Item 6', 60, 'RED', 'barcode6', null, '2023-01-11T00:00:00', '2023-01-12T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000007', '7', 'Item 7', 70, 'WHITE', 'barcode7', null, '2023-01-13T00:00:00', '2023-01-14T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000008', '8', 'Item 8', 80, 'GREEN', 'barcode8', null, '2023-01-15T00:00:00', '2023-01-16T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000009', '9', 'Item 9', 90, 'RED', 'barcode9', null, '2023-01-17T00:00:00', '2023-01-18T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000010', '10', 'Item 10', 100, 'WHITE', 'barcode10', null, '2023-01-19T00:00:00', '2023-01-20T00:00:00', '2023-01-02T00:00:00');

-- Inserimento dei dati di esempio nella tabella ITEM_DETAIL
INSERT INTO ITEM_DETAIL (id, quantity_to_be_ordered, ordered_quantity, portions_required_on_saturday, portions_required_on_sunday, portions_per_weekend, portions_on_holiday, max_portions_sold, delivery_date, creation_date, last_update, item_id) VALUES
('00000000-0000-0000-0000-000000000001', 5, 3, 1, 2, 3, 1, 5, '2023-01-02T00:00:00', '2023-01-01T00:00:00', '2023-01-02T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000002', 10, 6, 2, 4, 6, 2, 10, '2023-01-04T00:00:00', '2023-01-03T00:00:00', '2023-01-04T00:00:00', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000003', 15, 9, 3, 6, 9, 3, 15, '2023-01-06T00:00:00', '2023-01-05T00:00:00', '2023-01-06T00:00:00', '00000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000004', 20, 12, 4, 8, 12, 4, 20, '2023-01-08T00:00:00', '2023-01-07T00:00:00', '2023-01-08T00:00:00', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000005', 25, 15, 5, 10, 15, 5, 25, '2023-01-10T00:00:00', '2023-01-09T00:00:00', '2023-01-10T00:00:00', '00000000-0000-0000-0000-000000000005');

-- Inserimento dei dati di esempio nella tabella PROVIDER
INSERT INTO PROVIDER (id, name, address, phone_number, email, creation_date, last_update, item_id) VALUES
('00000000-0000-0000-0000-000000000001', 'Provider 1', 'Address 1', '1111111111', 'provider1@example.com', '2023-01-01T00:00:00', '2023-01-02T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000002', 'Provider 2', 'Address 2', '2222222222', 'provider2@example.com', '2023-01-03T00:00:00', '2023-01-04T00:00:00', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000003', 'Provider 3', 'Address 3', '3333333333', 'provider3@example.com', '2023-01-05T00:00:00', '2023-01-06T00:00:00', '00000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000004', 'Provider 4', 'Address 4', '4444444444', 'provider4@example.com', '2023-01-07T00:00:00', '2023-01-08T00:00:00', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000005', 'Provider 5', 'Address 5', '5555555555', 'provider5@example.com', '2023-01-09T00:00:00', '2023-01-10T00:00:00', '00000000-0000-0000-0000-000000000005');

-- Inserimento dei dati di esempio nella tabella QUANTITY_TYPE
INSERT INTO QUANTITY_TYPE (id, quantity_type, quantity_type_description, quantity_type_available, quantity_type_purpose, creation_date, last_update, item_id) VALUES
('00000000-0000-0000-0000-000000000001', 'STRATO', 'Strato', 5, 'AVAILABLE', '2023-01-01T00:00:00', '2023-01-02T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000002', 'CARTONE', 'Cartone', 10, 'AVAILABLE', '2023-01-03T00:00:00', '2023-01-04T00:00:00', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000003', 'SACCHETTO', 'Sacchetto', 15, 'AVAILABLE', '2023-01-05T00:00:00', '2023-01-06T00:00:00', '00000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000004', 'PEZZI', 'Pezzi', 20, 'AVAILABLE', '2023-01-07T00:00:00', '2023-01-08T00:00:00', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000005', 'STRATO', 'Strato', 25, 'TO_BE_ORDERED', '2023-01-09T00:00:00', '2023-01-10T00:00:00', '00000000-0000-0000-0000-000000000005');

-- Aggiornamento della tabella item_fts con i dati inseriti nella tabella ITEM
INSERT INTO item_fts (rowid, name, barcode)
SELECT name, barcode FROM ITEM;
