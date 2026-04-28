



CREATE TABLE `payment_records` (
	`id` int NOT NULL AUTO_INCREMENT,
	`order_id` int NOT NULL,
    `idempotency_key` varchar(36) NOT NULL,-- could be the same as the users address or different
    `status` varchar(20) default 'PENDING',  -- FAILED,SUCCESS,PENDING
    `amount` DECIMAL(10,2) NOT NULL,
    `transaction_reference` varchar(255), 
    `processed_at` datetime default current_timestamp,
	PRIMARY KEY (`id`),
    Constraint `payment_record_order_fk` Foreign key (`order_id`) REFERENCES orders(`id`),
	UNIQUE INDEX idx_unique_idempotency_key (idempotency_key)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

