ALTER TABLE `zstack`.`PriceVO` modify price DOUBLE(9,5) DEFAULT NULL;

ALTER TABLE `VipVO` DROP FOREIGN KEY `fkVipVOL3NetworkEO1`;
ALTER TABLE VipVO ADD CONSTRAINT fkVipVOL3NetworkEO1 FOREIGN KEY (peerL3NetworkUuid) REFERENCES L3NetworkEO (uuid) ON DELETE SET NULL;

CREATE TABLE  `zstack`.`AsyncRestVO` (
    `uuid` varchar(32) NOT NULL UNIQUE,
    `apiMessage` text DEFAULT NULL,
    `state` varchar(64) NOT NULL,
    `result` text DEFAULT NULL,
    `lastOpDate` timestamp ON UPDATE CURRENT_TIMESTAMP,
    `createDate` timestamp,
    PRIMARY KEY  (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
