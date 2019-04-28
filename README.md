#Weather API
Weather API is a RESTful micro-service for providing weather details. 
It currently supports the following set of queries: 
1. Get current, minimum and maximum temperature data for a city (latitude, longitude) on a requested date
2. Get hourly (24 hours) temperature data of a city (latitude, longitude) for a specific date
3. Get global minimum and maximum temperature trends over a period of time

## Sample API queries:
1. GET /temperature: provides hourly temperature data for a city (latitude, longitude) on a required date
```bash
curl -X GET \
  'http://localhost:8080/temperature/hourly?lat=42.3602534&date=2019-04-17%2018:30:09&long=-71.0582912' \
  -H 'Postman-Token: c03b26f7-aa5b-4813-a62b-33dce84888ea' \
  -H 'cache-control: no-cache'
```

2. POST /weather: provides the endpoint for consuming raw weather data
```bash
curl -X POST \
  http://localhost:9080/weather \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 3021075a-6af5-48f2-a378-175e51376b08' \
  -H 'cache-control: no-cache' \
  -d '{
    "date": "2019-04-18 16:30:09",
    "hour":16,
    "lat": 34.053688,
    "longitude": -118.2427668,
    "wind": 0.25,
    "temperature": 2.0
}'
```

3. GET /temperature/global: provides global minimum or maximum temperature trends on historic data between two dates
```bash
curl -X GET \
  'http://localhost:8080/temperature/global?fromDate=2019-04-16%2014:30:09&toDate=2019-04-19%2014:30:09&type=min&limit=14' \
  -H 'Postman-Token: 6980752a-d364-4ab1-a62d-fe2b699ba78b' \
  -H 'cache-control: no-cache'
```

## Database setup
1. Install PostgreSQL v9.5
3. Run `service postgresql start`

## Start RabbitMQ container
1. docker pull rabbitmq:management-alpine
2. docker run -d --hostname mon_the1 --name dw-rabbitmq-mgt -p 4040:15672 -p 4041:5672 rabbitmq:management-alpine
3. Management console link : http://localhost:4040

## Build images
1. Update database config in application.yml file of query-service module - url, port, user, password
2. cd query-service && docker build -t query-service .
3. cd ingester && docker build -t ingester .
4. cd external-connector && docker build -t external-connector .

## Run images
1. docker run -p 8080:8080 -p 8081:8081 --network="host" query-service:latest
2. docker run -p 9080:9080 -p 9081:9081 --network="host" external-connector:latest
3. docker run --network="host" ingester:latest

## Seed database with dummy data (Optional)
Run insert queries written in 'insertDbQueries.txt' in postgres container.