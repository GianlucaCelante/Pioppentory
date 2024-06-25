CREATE TABLE IF NOT EXISTS `ITEM` (
    `id` TEXT NOT NULL,
    `name` TEXT,
    `tot_portions` INTEGER,
    `status` TEXT,
    `barcode` TEXT,
    `check_date` TEXT,
    `has_note` INTEGER,
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
    `note` TEXT,
    `creation_date` TEXT,
    `last_update` TEXT,
    `item_id` TEXT,
    PRIMARY KEY(`id`),
    FOREIGN KEY(`item_id`) REFERENCES `ITEM`(`id`)
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
    FOREIGN KEY(`item_id`) REFERENCES `ITEM`(`id`)
);

CREATE TABLE IF NOT EXISTS `QUANTITY_TYPE` (
    `id` TEXT NOT NULL,
    `quantity_type` TEXT,
    `quantity_type_description` TEXT,
    `quantity_type_available` INTEGER,
    `creation_date` TEXT,
    `last_update` TEXT,
    `item_id` TEXT,
    PRIMARY KEY(`id`),
    FOREIGN KEY(`item_id`) REFERENCES `ITEM`(`id`)
);

INSERT INTO ITEM (id, name, tot_portions, status, barcode, has_note, check_date, creation_date, last_update) VALUES
('00000000-0000-0000-0000-000000000001', 'Item 1', 10, 'NOT_CHECKED', 'barcode', 1, '2023-01-01T00:00:00', '2023-01-02T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000002', 'Item 2', 20, 'GREEN', 'barcode', 0, '2023-01-03T00:00:00', '2023-01-04T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000003', 'Item 3', 30, 'RED', 'barcode', 0, '2023-01-05T00:00:00', '2023-01-06T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000004', 'Item 4', 40, 'NOT_CHECKED', 'barcode', 0, '2023-01-07T00:00:00', '2023-01-08T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000005', 'Item 5', 50, 'GREEN', 'barcode', 0, '2023-01-09T00:00:00', '2023-01-10T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000006', 'Item 6', 60, 'RED', 'barcode', 0, '2023-01-11T00:00:00', '2023-01-12T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000007', 'Item 7', 70, 'NOT_CHECKED', 'barcode', 1, '2023-01-13T00:00:00', '2023-01-14T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000008', 'Item 8', 80, 'GREEN', 'barcode', 0, '2023-01-15T00:00:00', '2023-01-16T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000009', 'Item 9', 90, 'RED', 'barcode', 0, '2023-01-17T00:00:00', '2023-01-18T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000010', 'Item 10', 100, 'NOT_CHECKED', 'barcode', 0, '2023-01-19T00:00:00', '2023-01-20T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000011', 'Item 11', 110, 'GREEN', 'barcode', 0, '2023-01-21T00:00:00', '2023-01-22T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000012', 'Item 12', 120, 'RED', 'barcode', 0, '2023-01-23T00:00:00', '2023-01-24T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000013', 'Item 13', 130, 'NOT_CHECKED', 'barcode', 0, '2023-01-25T00:00:00', '2023-01-26T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000014', 'Item 14', 140, 'GREEN', 'barcode', 0, '2023-01-27T00:00:00', '2023-01-28T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000015', 'Item 15', 150, 'RED', 'barcode', 0, '2023-01-29T00:00:00', '2023-01-30T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000016', 'Item 16', 160, 'NOT_CHECKED', 'barcode', 0, '2023-01-31T00:00:00', '2024-01-01T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000017', 'Item 17', 170, 'GREEN', 'barcode', 0, '2024-01-02T00:00:00', '2024-01-03T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000018', 'Item 18', 180, 'RED', 'barcode', 1, '2024-01-04T00:00:00', '2024-01-05T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000019', 'Item 19', 190, 'NOT_CHECKED', 'barcode', 0, '2024-01-06T00:00:00', '2024-01-07T00:00:00', '2023-01-02T00:00:00'),
('00000000-0000-0000-0000-000000000020', 'Item 20', 200, 'GREEN', 'barcode', 0, '2024-01-08T00:00:00', '2024-01-09T00:00:00', '2023-01-02T00:00:00');

INSERT INTO QUANTITY_TYPE (id, quantity_type, quantity_type_description, quantity_type_available, creation_date, last_update, item_id) VALUES
('00000000-0000-0000-0000-000000000001', 'STRATO', 'Strato', 5, '2024-01-09T00:00:00', '2024-01-09T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000002', 'CARTONE', 'Cartone', 10,'2024-01-09T00:00:00', '2024-01-09T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000003', 'SACCHETTO', 'Sacchetto', 15, '2024-01-09T00:00:00', '2024-01-09T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000004', 'KG', 'Kg', 20, '2024-01-09T00:00:00', '2024-01-09T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000005', 'STRATO', 'Strato', 5, '2024-01-09T00:00:00', '2024-01-09T00:00:00', '00000000-0000-0000-0000-000000000002');

INSERT INTO PROVIDER (id, name, address, phone_number, email, creation_date, last_update, item_id) VALUES
('00000000-0000-0000-0000-000000000001', 'Provider 1', 'Address 1', '1111111111', 'provider1@example.com', '2023-01-01T00:00:00', '2023-01-02T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000002', 'Provider 2', 'Address 2', '2222222222', 'provider2@example.com', '2023-01-03T00:00:00', '2023-01-04T00:00:00', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000003', 'Provider 3', 'Address 3', '3333333333', 'provider3@example.com', '2023-01-05T00:00:00', '2023-01-06T00:00:00', '00000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000004', 'Provider 4', 'Address 4', '4444444444', 'provider4@example.com', '2023-01-07T00:00:00', '2023-01-08T00:00:00', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000005', 'Provider 5', 'Address 5', '5555555555', 'provider5@example.com', '2023-01-09T00:00:00', '2023-01-10T00:00:00', '00000000-0000-0000-0000-000000000005');

INSERT INTO ITEM_DETAIL (id, quantity_to_be_ordered, ordered_quantity, portions_required_on_saturday, portions_required_on_sunday, portions_per_weekend, portions_on_holiday, max_portions_sold, delivery_date, note, creation_date, last_update, item_id) VALUES
('00000000-0000-0000-0000-000000000001', 5, 3, 1, 2, 3, 1, 5, '2023-01-02T00:00:00', 'Note 1', '2023-01-01T00:00:00', '2023-01-02T00:00:00', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000002', 10, 6, 2, 4, 6, 2, 10, '2023-01-04T00:00:00', 'Note 2', '2023-01-03T00:00:00', '2023-01-04T00:00:00', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000003', 15, 9, 3, 6, 9, 3, 15, '2023-01-06T00:00:00', 'Note 3', '2023-01-05T00:00:00', '2023-01-06T00:00:00', '00000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000004', 20, 12, 4, 8, 12, 4, 20, '2023-01-08T00:00:00', 'Note 4', '2023-01-07T00:00:00', '2023-01-08T00:00:00', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000005', 25, 15, 5, 10, 15, 5, 25, '2023-01-10T00:00:00', 'Note 5', '2023-01-09T00:00:00', '2023-01-10T00:00:00', '00000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0000-000000000006', 30, 18, 6, 12, 18, 6, 30, '2023-01-12T00:00:00', 'Note 6', '2023-01-11T00:00:00', '2023-01-12T00:00:00', '00000000-0000-0000-0000-000000000006'),
('00000000-0000-0000-0000-000000000007', 35, 21, 7, 14, 21, 7, 35, '2023-01-14T00:00:00', 'Note 7', '2023-01-13T00:00:00', '2023-01-14T00:00:00', '00000000-0000-0000-0000-000000000007'),
('00000000-0000-0000-0000-000000000008', 40, 24, 8, 16, 24, 8, 40, '2023-01-16T00:00:00', 'Note 8', '2023-01-15T00:00:00', '2023-01-16T00:00:00', '00000000-0000-0000-0000-000000000008'),
('00000000-0000-0000-0000-000000000009', 45, 27, 9, 18, 27, 9, 45, '2023-01-18T00:00:00', 'Note 9', '2023-01-17T00:00:00', '2023-01-18T00:00:00', '00000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0000-000000000010', 50, 30, 10, 20, 30, 10, 50, '2023-01-20T00:00:00', 'Note 10', '2023-01-19T00:00:00', '2023-01-20T00:00:00', '00000000-0000-0000-0000-000000000010'),
('00000000-0000-0000-0000-000000000011', 55, 33, 11, 22, 33, 11, 55, '2023-01-22T00:00:00', 'Note 11', '2023-01-21T00:00:00', '2023-01-22T00:00:00', '00000000-0000-0000-0000-000000000011'),
('00000000-0000-0000-0000-000000000012', 60, 36, 12, 24, 36, 12, 60, '2023-01-24T00:00:00', 'Note 12', '2023-01-23T00:00:00', '2023-01-24T00:00:00', '00000000-0000-0000-0000-000000000012'),
('00000000-0000-0000-0000-000000000013', 65, 39, 13, 26, 39, 13, 65, '2023-01-26T00:00:00', 'Note 13', '2023-01-25T00:00:00', '2023-01-26T00:00:00', '00000000-0000-0000-0000-000000000013'),
('00000000-0000-0000-0000-000000000014', 70, 42, 14, 28, 42, 14, 70, '2023-01-28T00:00:00', 'Note 14', '2023-01-27T00:00:00', '2023-01-28T00:00:00', '00000000-0000-0000-0000-000000000014'),
('00000000-0000-0000-0000-000000000015', 75, 45, 15, 30, 45, 15, 75, '2023-01-30T00:00:00', 'Note 15', '2023-01-29T00:00:00', '2023-01-30T00:00:00', '00000000-0000-0000-0000-000000000015'),
('00000000-0000-0000-0000-000000000016', 80, 48, 16, 32, 48, 16, 80, '2024-01-01T00:00:00', 'Note 16', '2023-01-31T00:00:00', '2024-01-01T00:00:00', '00000000-0000-0000-0000-000000000016'),
('00000000-0000-0000-0000-000000000017', 85, 51, 17, 34, 51, 17, 85, '2024-01-03T00:00:00', 'Note 17', '2024-01-02T00:00:00', '2024-01-03T00:00:00', '00000000-0000-0000-0000-000000000017'),
('00000000-0000-0000-0000-000000000018', 90, 54, 18, 36, 54, 18, 90, '2024-01-05T00:00:00', 'Note 18', '2024-01-04T00:00:00', '2024-01-05T00:00:00', '00000000-0000-0000-0000-000000000018'),
('00000000-0000-0000-0000-000000000019', 95, 57, 19, 38, 57, 19, 95, '2024-01-07T00:00:00', 'Note 19', '2024-01-06T00:00:00', '2024-01-07T00:00:00', '00000000-0000-0000-0000-000000000019');

CREATE INDEX IF NOT EXISTS `index_ITEM_id` ON `ITEM` (`id`);
CREATE INDEX IF NOT EXISTS `index_ITEM_DETAIL_item_id` ON `ITEM_DETAIL` (`item_id`);
CREATE INDEX IF NOT EXISTS `index_PROVIDER_item_id` ON `PROVIDER` (`item_id`);
CREATE INDEX IF NOT EXISTS `index_QUANTITY_TYPE_item_id` ON `QUANTITY_TYPE` (`item_id`);
