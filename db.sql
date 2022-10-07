-- PureHTML
CREATE TABLE `PureHTML`.`User` (
    `userID`        INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `username`      VARCHAR(32) UNIQUE NOT NULL,
    `pwd_hash`      VARCHAR(64) NOT NULL,
    `name`          VARCHAR(32) NOT NULL,
    `surname`       VARCHAR(32) NOT NULL
) ENGINE = InnoDB;

CREATE TABLE `PureHTML`.`Folder` (
    `id`            INT UNIQUE NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(64) NOT NULL,
    `owner`         VARCHAR(65) NOT NULL,
    `creation_date` DATE NOT NULL,
    `parent_folder` INT DEFAULT NULL,
    `user`          INT NOT NULL,
    PRIMARY KEY (`id`, `user`),
    CONSTRAINT `FolderFK` FOREIGN KEY (`user`) REFERENCES `PureHTML`.`User`(`userID`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE `PureHTML`.`Document` (
    `id`            INT UNIQUE NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(128) NOT NULL,
    `type`          VARCHAR(32) NOT NULL,
    `owner`         VARCHAR(65) NOT NULL,
    `creation_date` DATE NOT NULL,
    `summary`       VARCHAR(512),
    `subfolderID`   INT NOT NULL,
    PRIMARY KEY (`id`, `subfolderID`),
    CONSTRAINT `DocumentFK` FOREIGN KEY (`subfolderID`) REFERENCES `PureHTML`.`Folder`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB;

-- RIA
CREATE TABLE `RIA`.`User` (
    `userID`        INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    `email`         VARCHAR(128) UNIQUE NOT NULL,
    `pwd_hash`      VARCHAR(64) NOT NULL,
    `name`          VARCHAR(32) NOT NULL,
    `surname`       VARCHAR(32) NOT NULL
) ENGINE = InnoDB;

CREATE TABLE `RIA`.`Folder` (
    `id`            INT UNIQUE NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(64) NOT NULL,
    `owner`         VARCHAR(65) NOT NULL,
    `creation_date` DATE NOT NULL,
    `parent_folder` INT DEFAULT NULL,
    `user`          INT NOT NULL,
    PRIMARY KEY (`id`, `user`),
    CONSTRAINT `FolderFK` FOREIGN KEY (`user`) REFERENCES `RIA`.`User`(`userID`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE `RIA`.`Document` (
    `id`            INT UNIQUE NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(128) NOT NULL,
    `type`          VARCHAR(32) NOT NULL,
    `owner`         VARCHAR(65) NOT NULL,
    `creation_date` DATE NOT NULL,
    `summary`       VARCHAR(512),
    `subfolderID`   INT NOT NULL,
    PRIMARY KEY (`id`, `subfolderID`),
    CONSTRAINT `DocumentFK` FOREIGN KEY (`subfolderID`) REFERENCES `RIA`.`Folder`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB;