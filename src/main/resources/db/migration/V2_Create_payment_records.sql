USE `Kee_V2C_Platform`;



CREATE TABLE `payment_records` (
	`id` int NOT NULL AUTO_INCREMENT,
	`order_id` int NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `payment_method` varchar(50) NOT NULL,
    `transaction_reference` varchar(255),
	`created_at` datetime default current_timestamp,
    `processed_at` datetime default null,
    `idempotency_key` varchar(36) ,
    `status` varchar(20) default 'PENDING',  -- FAILED,SUCCESS,PENDING
	PRIMARY KEY (`id`),
    Constraint `payment_record_order_fk` Foreign key (`order_id`) REFERENCES orders(`id`),
	UNIQUE INDEX idx_unique_idempotency_key (idempotency_key)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;


