/*
 *  Copyright 2022 Nurture.Farm
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

DROP table if exists app_config;


DROP TABLE IF EXISTS User;


CREATE TABLE `app_config` (

        `id` bigint NOT NULL AUTO_INCREMENT,

        `title` varchar(64) CHARACTER SET utf8mb4 NOT NULL,

        `req_name` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL,

        `res_name` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL,

        `decl_req` tinyint(1) NOT NULL DEFAULT '1',

        `decl_res` tinyint(1) NOT NULL DEFAULT '1',

        `decl_grpc` tinyint(1) NOT NULL DEFAULT '1',

        `decl_grapql` tinyint(1) NOT NULL DEFAULT '1',

        `sql_stmt` mediumtext CHARACTER SET utf8mb4,

        `sql_params` mediumtext CHARACTER SET utf8mb4,

        `sql_uniquekey` tinyint(1) DEFAULT '0',

        `sql_replace` mediumtext CHARACTER SET utf8mb4,

        `sql_pool` varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL,

        `impl_dao` tinyint(1) NOT NULL DEFAULT '1',

        `impl_grpc` tinyint(1) NOT NULL DEFAULT '1',

        `impl_reacrjs` tinyint(1) NOT NULL DEFAULT '0',

        `req_override` mediumtext CHARACTER SET utf8mb4,

        `res_override` mediumtext CHARACTER SET utf8mb4,

        `mutation` enum('I','U','D','S','-') CHARACTER SET utf8mb4 NOT NULL DEFAULT '-',

        `oauth_public` tinyint(1) DEFAULT '1',

        `oauth_claims` mediumtext CHARACTER SET utf8mb4 ,

        `status` tinyint(1) DEFAULT '1',

        PRIMARY KEY (`id`),

        UNIQUE KEY `title` (`title`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Application configuration';

CREATE TABLE `User` (
  `id` int NOT NULL,
  `age` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

