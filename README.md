1. Install postgres, update database url, user and password in application.yml file, Also update hibernate.hbm2ddl.auto field to 'create'.
This is to create schema for the first time.
2. Do mvn clean install
3. Start service by running WeatherServiceAppication class
4. Use insert queries to seed data, they are written in files insertQuery*.txt in root folder of project
Now ibernate.hbm2ddl.auto field can be reverted to 'validate', so that data is not wiped on each application start.
5. Check endpoint : 

http://localhost:8080/temperature?lat=34.0536834&date=2019-04-17 11:00:00&long=-118.2427669
http://localhost:8080/temperature/hourly?lat=34.0536834&date=2019-04-17 09:30:09&long=-118.2427669
http://localhost:8080/temperature/global?limit=1&fromDate=2019-04-13 11:30:09&toDate=2019-04-18 11:30:09