-- MySQL Script generated by MySQL Workbench
-- Tue Jul  5 14:45:36 2016
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema dbintcloud
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema dbIntCloud
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema dbIntCloud
-- -----------------------------------------------------


DROP DATABASE IF EXISTS dbIntCloud;


CREATE SCHEMA IF NOT EXISTS `dbIntCloud` ;
USE `dbIntCloud` ;

-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_APP_TYPE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_APP_TYPE` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(1000) NULL DEFAULT NULL,
  `buildable` INT(1) NULL DEFAULT '1',
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_APPLICATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_APPLICATION` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `hash_id` VARCHAR(24) NULL DEFAULT NULL,
  `description` VARCHAR(1000) NULL DEFAULT NULL,
  `tenant_id` INT(11) NOT NULL,
  `default_version` VARCHAR(24) NULL DEFAULT NULL,
  `app_type_id` INT(11) NULL DEFAULT NULL,
  `capp_name` VARCHAR(45) NULL DEFAULT NULL,
  `param_configuration` VARCHAR(10000) NULL DEFAULT NULL,
  `task_configuration` VARCHAR(10000) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_Application_NAME_TID_REV` (`name` ASC, `tenant_id` ASC),
  INDEX `fk_Application_ApplicationType1` (`app_type_id` ASC),
  CONSTRAINT `fk_Application_ApplicationType1`
    FOREIGN KEY (`app_type_id`)
    REFERENCES `dbIntCloud`.`AC_APP_TYPE` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_APP_ICON`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_APP_ICON` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `icon` MEDIUMBLOB NULL DEFAULT NULL,
  `application_id` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `application_id_UNIQUE` (`application_id` ASC),
  CONSTRAINT `fk_AC_APPLICATION_ICON_AC_APPLICATION1`
    FOREIGN KEY (`application_id`)
    REFERENCES `dbIntCloud`.`AC_APPLICATION` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_RUNTIME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_RUNTIME` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `repo_url` VARCHAR(250) NULL DEFAULT NULL,
  `image_name` VARCHAR(100) NULL DEFAULT NULL,
  `tag` VARCHAR(45) NOT NULL,
  `description` VARCHAR(1000) NULL DEFAULT NULL,
  PRIMARY KEY (`id`, `name`))
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_APP_TYPE_RUNTIME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_APP_TYPE_RUNTIME` (
  `app_type_id` INT(11) NOT NULL,
  `runtime_id` INT(11) NOT NULL,
  INDEX `fk_ApplicationType_has_ApplicationRuntime_ApplicationType1` (`app_type_id` ASC),
  INDEX `fk_ApplicationType_has_ApplicationRuntime_ApplicationRuntime1` (`runtime_id` ASC),
  CONSTRAINT `fk_ApplicationType_has_ApplicationRuntime_ApplicationType1`
    FOREIGN KEY (`app_type_id`)
    REFERENCES `dbIntCloud`.`AC_APP_TYPE` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ApplicationType_has_ApplicationRuntime_ApplicationRuntime1`
    FOREIGN KEY (`runtime_id`)
    REFERENCES `dbIntCloud`.`AC_RUNTIME` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_CONTAINER_SERVICE_PROXY`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_CONTAINER_SERVICE_PROXY` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NULL DEFAULT NULL,
  `protocol` VARCHAR(20) NULL DEFAULT NULL,
  `port` INT(11) NULL DEFAULT NULL,
  `backend_port` VARCHAR(45) NULL DEFAULT NULL,
  `container_id` INT(11) NOT NULL,
  `tenant_id` INT(11) NULL DEFAULT NULL,
  `host_url` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_EVENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_EVENT` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `status` VARCHAR(45) NULL DEFAULT NULL,
  `version_id` INT(11) NOT NULL,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `description` VARCHAR(1000) NULL DEFAULT NULL,
  `tenant_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_SUBSCRIPTION_PLANS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_SUBSCRIPTION_PLANS` (
  `PLAN_ID` INT(11) NOT NULL AUTO_INCREMENT,
  `PLAN_NAME` VARCHAR(200) NOT NULL,
  `MAX_APPLICATIONS` INT(11) NOT NULL,
  PRIMARY KEY (`PLAN_ID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_TRANSPORT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_TRANSPORT` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(20) NOT NULL,
  `port` INT(11) NOT NULL,
  `protocol` VARCHAR(4) NOT NULL,
  `service_prefix` VARCHAR(3) NOT NULL,
  `description` VARCHAR(1000) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_VERSION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_VERSION` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(13) NULL DEFAULT NULL,
  `hash_id` VARCHAR(24) NULL DEFAULT NULL,
  `application_id` INT(11) NOT NULL,
  `runtime_id` INT(11) NULL DEFAULT NULL,
  `status` VARCHAR(45) NULL DEFAULT NULL,
  `tenant_id` INT(11) NULL DEFAULT NULL,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_white_listed` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  INDEX `fk_AC_VERSION_AC_APPLICATION1` (`application_id` ASC),
  INDEX `fk_AC_VERSION_ApplicationRuntime1` (`runtime_id` ASC),
  CONSTRAINT `fk_AC_VERSION_AC_APPLICATION1`
    FOREIGN KEY (`application_id`)
    REFERENCES `dbIntCloud`.`AC_APPLICATION` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `dbIntCloud`.`AC_WHITE_LISTED_TENANTS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `dbIntCloud`.`AC_WHITE_LISTED_TENANTS` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` INT(11) NOT NULL,
  `max_app_count` INT(11) NOT NULL,
  PRIMARY KEY (`id`, `tenant_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

CREATE TABLE IF NOT EXISTS AC_CONTAINER_SPECIFICATIONS (
    CON_SPEC_ID     INTEGER NOT NULL AUTO_INCREMENT,
    CON_SPEC_NAME   VARCHAR(200) NOT NULL,
    CPU              INT NOT NULL,	
    MEMORY	     INT NOT NULL,
    COST_PER_HOUR    INT NOT NULL,
    PRIMARY KEY (CON_SPEC_ID))
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS AC_RUNTIME_CONTAINER_SPECIFICATIONS (
  id int(11) NOT NULL,
  CON_SPEC_ID int(11) NOT NULL,
  PRIMARY KEY (id,CON_SPEC_ID),
  KEY CON_SPEC_ID (CON_SPEC_ID))
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS AC_SUBSCRIPTION_PLANS (
    PLAN_ID	INTEGER NOT NULL AUTO_INCREMENT,
    PLAN_NAME   VARCHAR(200) NOT NULL,	
    MAX_APPLICATIONS	INT NOT NULL,
    PRIMARY KEY (PLAN_ID))
ENGINE = InnoDB;


INSERT INTO `AC_APP_TYPE` (`id`, `name`, `description`) VALUES
(1, 'car', 'Allows you to create ESB configuration projects');

INSERT INTO `AC_RUNTIME` (`id`, `name`, `repo_url`, `image_name`, `tag`, `description`) VALUES(1, 'ESB 5.0', 'https://localhost:9453/carbon', 'esb', '5.0.0', 'OS:Debian, Java Version:7u101');

INSERT INTO `AC_APP_TYPE_RUNTIME` (`app_type_id`, `runtime_id`) VALUES (1, 1);

INSERT INTO `AC_CONTAINER_SPECIFICATIONS` (`CON_SPEC_NAME`, `CPU`, `MEMORY`, `COST_PER_HOUR`) VALUES
('SMALL(128MB RAM and 0.1x vCPU)', 100, 128, 1),
('MEDIUM(256MB RAM and 0.2x vCPU)', 200, 256, 2),
('LARGE(512MB RAM and 0.3x vCPU)', 300, 512, 3);

INSERT INTO `AC_SUBSCRIPTION_PLANS` (`PLAN_NAME`, `MAX_APPLICATIONS`) VALUES
('FREE', 3),
('PAID', 10);

INSERT INTO `AC_RUNTIME_CONTAINER_SPECIFICATIONS` (`id`, `CON_SPEC_ID`) VALUES
(1, 3);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
