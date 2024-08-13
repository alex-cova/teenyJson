
# TeenyJSON

TeenyJSON is a lightweight JSON processing library for Java. It provides simple and efficient ways to serialize and deserialize Java objects to and from JSON.

## Features

- Serialize Java objects to JSON
- Deserialize JSON to Java objects
- Support for complex objects, collections, and arrays
- Easy-to-use API

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven

### Installation

Add the following dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>com.alexcova</groupId>
    <artifactId>teenyjson</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Usage

#### Serialize an Object to JSON

```java
ComplexObject complexObject = new ComplexObject();
complexObject.setString("example")
             .setAnInt(42)
             .setDate(new Date());

String json = TeenyJSON.serialize(complexObject);
System.out.println(json);
```

#### Deserialize JSON to an Object

```java
String json = "{\"string\":\"example\",\"anInt\":42,\"date\":\"2023-10-01T12:00:00Z\"}";
ComplexObject complexObject = TeenyJSON.deserialize(json, ComplexObject.class);
System.out.println(complexObject);
```

## Running Tests

To run the tests, use the following Maven command:

```sh
mvn test
```

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.

## Authors

- Alex Cova - [alex-cova](https://github.com/alex-cova)
