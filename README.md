![](https://github.com/bancolombia/secrets-manager/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bancolombia_secrets-manager&metric=alert_status)](https://sonarcloud.io/dashboard?id=bancolombia_secrets-manager)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=bancolombia_secrets-manager&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=bancolombia_secrets-manager)
[![codecov](https://codecov.io/gh/bancolombia/secrets-manager/branch/master/graph/badge.svg)](https://codecov.io/gh/bancolombia/secrets-manager)
[![GitHub license](https://img.shields.io/github/license/Naereen/StrapDown.js.svg)](https://github.com/bancolombia/secrets-manager/blob/master/LICENSE)

# SecretsManager - Bancolombia

This library will help you to decouple your application of your secrets provider. It supports the following conectors to get secrets:

- AWS Secrets Manager Sync 
- AWS Secrets Manager Async (Non blocking flows)
- File Secrets (E.g Kubernetes Secrets )
- Environment System Secrets (E.g Kubernetes Secrets )

# How to use

SecretsManager require [Java] v8+


## Secrets Manager Sync
```java
dependencies {
    implementation 'com.github.bancolombia:aws-secrets-manager-sync:2.0.0'
}
```

```java
import co.com.bancolombia.commons.secretsmanager.connector.AbstractConnector;
import co.com.bancolombia.commons.secretsmanager.connector.clients.connector.connector.AWSSecretManagerConnector;
import co.com.bancolombia.commons.secretsmanager.manager.api.GenericManager;

String REGION_SECRET = "us-east-1";
String NAME_SECRET = "secretName";
GenericManager connector = new AWSSecretManagerConnector(REGION_SECRET);

try {
    DefineYourModel secret = connector.getSecret(NAME_SECRET, DefineYourModel.class);
    ...
} catch(Exception e) {
    ...
}
```

Remind you have to define your model with the fields you will need. You can find a default AWSSecretDBModel model, it includes default fields to connect a RDS database.

To convert `JSON` to a `POJO`, it uses `Gson`. If you need use field with custom names, you have to create your model like:

```java
package co.com.bancolombia...;

import com.google.gson.annotations.SerializedName;

public class DefineYourModel {

    @SerializedName("aes_key")
    private String aesKey;

    @SerializedName("rsa_key")
    private String rsaKey;

    ...

}
```

## Secrets Manager Async (Compatible with Reactor)
```java
dependencies {
    // Reactor Core is required! 
    implementation group: 'io.projectreactor', name: 'reactor-core', version: '3.3.10.RELEASE'
    // secrets-manager-async     
    implementation 'com.github.bancolombia:aws-secrets-manager-async:2.0.0'
}
```

Define your configuration:
```java
// Default Config
AWSSecretsManagerConfig config = AWSSecretsManagerConfig.builder().build();

// Customized config
AWSSecretsManagerConfig config = AWSSecretsManagerConfig.builder()
				.region(Region.US_EAST_1) //define your region
				.cacheSeconds(600)  //define your cache time
				.cacheSize(300) //define your cache size
				.endpoint("http://localhost:4566") // Override the enpoint 
				.build();

```

##### Configurations 

You can pass the following variables to AWSSecretsManagerConfig:

- **region**: AWS Region that you are using, **"us-east-1"** (North virginia) is the default value.
- **cacheSeconds**: During this time the secret requested to AWS Secrets Manager will be saved in memory. 
The next requests to the same secret will be resolved from the cache. The default value is 0 (no cache).  
- **cacheSize**: The maximum amount of secrets you want to save in cache. The default value is 0. 
- **endpoint**: The AWS endpoint is the default value but you can override it if you want to test locally with localStack
or others tools. 

Create the connector:
```java
AWSSecretManagerConnector connector = new AWSSecretManagerConnector(config);
```

Get the secret in String:
```java
connector.getSecret("secretName")
    .doOnNext(System.out::println);
    // ... develop your async flow
```
Get the secret deserialized:
```java
connector.getSecret("pruebaLibreria", DefineYourModel.class)
    .doOnNext(secret -> {
       //... develop your async flow
    })
```

## Environment System Secrets
```java
dependencies {
    implementation 'com.github.bancolombia:env-secrets-manager:2.0.0'
}
```

## File Secrets
```java
dependencies {
    implementation 'com.github.bancolombia:file-secrets-manager:2.0.0'
}
```

## How can I contribute ?
                                  
Great !!:

- Clone this repo
- Create a new feature branch
- Add new features or improvements
- Send us a Pull Request

### To Do

- New connectors for other services.
  - Vault
  - Key Vault Azure
- Improve our tests
