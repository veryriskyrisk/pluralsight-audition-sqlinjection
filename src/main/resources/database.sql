CREATE TABLE IF NOT EXISTS `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL
) DEFAULT CHARSET=utf8;

INSERT INTO `users` (`username`, `password`) VALUES
	('admin', 'who stores plaintext passwords in database these days?'),
	('mat', 'very compelex password that\'s extremely hard to remember');

CREATE TABLE IF NOT EXISTS `visitors` (
  `timestamp` datetime DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL
) DEFAULT CHARSET=utf8;

INSERT INTO `visitors` (`timestamp`, `name`) VALUES
	('2019-04-04 23:36:56', 'Summer Smith'),
	('2019-04-04 23:37:00', 'Morty Smith');
