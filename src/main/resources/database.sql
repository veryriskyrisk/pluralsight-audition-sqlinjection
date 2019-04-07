CREATE TABLE IF NOT EXISTS `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

INSERT INTO `users` (`username`, `password`) VALUES
	('admin', 'who stores plaintext passwords in database these days?'),
	('mat', 'very compelex password that\'s extremely hard to remember');

CREATE TABLE IF NOT EXISTS `visits` (
	`visit_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`timestamp` DATETIME NULL DEFAULT NULL,
	`name` VARCHAR(100) NULL DEFAULT NULL,
	`ip` VARCHAR(15) NOT NULL DEFAULT '0',
	PRIMARY KEY(`visit_id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;


INSERT INTO `visits` (`timestamp`, `name`, `ip`) VALUES
	('2019-04-04 23:36:56', 'Summer Smith', '0.0.0.0'),
	('2019-04-04 23:37:00', 'Morty Smith', '255.255.255.255');
