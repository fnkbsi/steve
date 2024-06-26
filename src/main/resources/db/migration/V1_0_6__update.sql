ALTER TABLE `user`
    ADD COLUMN `mail_enabled` TINYINT(1) NULL DEFAULT '1' AFTER `e_mail`;
