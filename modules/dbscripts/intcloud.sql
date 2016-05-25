SELECT * FROM AppCloudDB.AC_APP_TYPE;
INSERT INTO AC_APP_TYPE (`id`, `name`, `description`) VALUES
(5, 'car', 'Allows you to create composite application projects');

SELECT * FROM AppCloudDB.AC_RUNTIME;

INSERT INTO `AC_RUNTIME` (`id`, `name`, `repo_url`, `image_name`, `tag`, `description`) VALUES(6, 'ESB 5.0', 'https://localhost:9453/carbon', 'esb', '5.0.0', 'OS:Debian, Java Version:7u101');

SELECT `AC_APP_TYPE_RUNTIME`.`app_type_id`,
    `AC_APP_TYPE_RUNTIME`.`runtime_id`
FROM `AppCloudDB`.`AC_APP_TYPE_RUNTIME`;

INSERT INTO `AppCloudDB`.`AC_APP_TYPE_RUNTIME`(`app_type_id`,`runtime_id`) VALUES (5,6);

ALTER TABLE `AppCloudDB`.`AC_VERSION` 
CHANGE COLUMN `con_spec_cpu` `con_spec_cpu` VARCHAR(10) NULL ,
CHANGE COLUMN `con_spec_memory` `con_spec_memory` VARCHAR(10) NULL ;

