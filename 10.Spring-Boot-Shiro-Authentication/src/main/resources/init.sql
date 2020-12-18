DROP TABLE `t_user`;
CREATE TABLE `t_user` (
    `ID` int(11) NOT NULL,
    `username` varchar(20) NOT NULL,
    `passwd` varchar(128) NOT NULL,
    `create_time` datetime DEFAULT NULL,
    `status` char(1) NOT NULL,
    PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO `t_user`(`ID`, `username`, `passwd`, `create_time`, `status`) VALUES (1, 'mrbird', '42ee25d1e43e9f57119a00d0a39e5250', '2017-11-19 10:52:48', '1');
INSERT INTO `t_user`(`ID`, `username`, `passwd`, `create_time`, `status`) VALUES (2, 'test', '7a38c13ec5e9310aed731de58bbc4214', '2017-11-19 17:20:21', '0');
