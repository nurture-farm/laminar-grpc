mysql -u root -pwelcome -e "create database finaOne; GRANT ALL PRIVILEGES ON finaOne.* TO root@localhost IDENTIFIED BY 'welcome'"\n
mysql -u root -pwelcome finaOne < dump.sql