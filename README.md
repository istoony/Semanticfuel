# Semantic Fuel

The goal of the project is to show a list of gas stations, given the point of start, the end and the type of fuel required. The prices and the location of the gas stations are published every day on the MiSE website. 

## Technologies
We used the following technologies:

### Semantic Technologies

We implement the application by using an ontology defined by us, we convert the csv in RDF database and we use rmlmapper to perform geospatial queries on the dataset.

### Java Framework
We used Maven, Spring Boot, html and jQuery.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Install Maven and Java 1.8


### Installing and Run
You can run the code by importing the semanticfuelserver folder on eclipse or by package the jar with maven:

```
mvn clean package
```
once the package step is finished you can run the application by execute the jar

```
target/semanticfuelserver-0.0.1-SNAPSHOT.jar 
```

## Authors

* **Antonio Gianola** - [PurpleBooth](https://github.com/Istoony)
* **Michele Todero**
* **Enrico Calafiore**

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is provided ASIS.

