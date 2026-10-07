![](https://github.com/bancolombia/secrets-manager/workflows/Java%20CI%20with%20Gradle/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bancolombia_secrets-manager&metric=alert_status)](https://sonarcloud.io/dashboard?id=bancolombia_secrets-manager)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=bancolombia_secrets-manager&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=bancolombia_secrets-manager)
[![codecov](https://codecov.io/gh/bancolombia/secrets-manager/branch/master/graph/badge.svg)](https://codecov.io/gh/bancolombia/secrets-manager)
[![GitHub license](https://img.shields.io/github/license/Naereen/StrapDown.js.svg)](https://github.com/bancolombia/secrets-manager/blob/master/LICENSE)
[![Scorecards supply-chain security](https://github.com/bancolombia/secrets-manager/actions/workflows/scorecards-analysis.yml/badge.svg)](https://github.com/bancolombia/secrets-manager/actions/workflows/scorecards-analysis.yml)

# Secrets Manager - Bancolombia

This library helps you to decouple your application of your secrets provider. It supports the following connectors to get secrets:

- AWS Secrets Manager Sync
- AWS Secrets Manager Async (Non blocking flows)
- AWS Parameter Store Sync
- AWS Parameter Store Async (Non blocking flows)
- Vault Sync
- Vault Async (Non blocking flows)
- File Secrets (E.g Kubernetes Secrets)
- Environment System Secrets (E.g Kubernetes Secrets)

## Table of contents

- [Compatibility](#compatibility)
- [Mapping JSON to your model](#mapping-json-to-your-model)
- [Sync connectors](#sync-connectors)
  - [AWS Secrets Manager](#aws-secrets-manager-sync)
  - [AWS Parameter Store](#aws-parameter-store-sync)
  - [Vault](#vault-sync)
  - [Environment variables](#environment-system-secrets)
  - [Files](#file-secrets)
- [Async connectors (Reactor)](#async-connectors-reactor)
  - [AWS Secrets Manager](#aws-secrets-manager-async)
  - [AWS Parameter Store](#aws-parameter-store-async)
  - [Vault](#vault-async)
- [How can I contribute?](#how-can-i-contribute)

## Compatibility

| Version | Spring Boot | AWS Bom   | Java |
|---------|-------------|-----------|------|
| 4.5.0   | 4.0.0       | 2.40.1    | 17+  |
| 4.3.1   | 3.2.1       | 2.23.4    | 11+  |
| 4.2.0   | 3.1.1       | 2.22.5    | 11+  |
| 4.1.0   | 3.1.1       | 2.20.94   | 11+  |
| 4.0.0   | 3.0.2       | 2.19.31   | 11+  |
| 3.2.0   | 2.7.6       | 2.18.39   | 8+   |
| 3.1.1   | 2.6.7       | 2.17.184  | 8+   |

## Mapping JSON to your model

Applies to all the connectors, sync and async (AWS Secrets Manager, Vault, Parameter Store).

The secret is converted from `JSON` to your model (`POJO` or `record`) using `Gson` (not Jackson). Gson matches each
JSON key with the field name, so:

- If the key equals the field name, no annotation is needed. This also applies to nested JSON objects.
- If the key is different or has characters that are not valid in Java (`.`, `-`, `_`, etc.), you **must** use
  `@SerializedName`. Otherwise the field stays `null`.

```java
package co.com.bancolombia...;

import com.google.gson.annotations.SerializedName;

// Custom names: {"aes_key":"a","rsa_key":"b"}
public record DefineYourModel(
        @SerializedName("aes_key") String aesKey,
        @SerializedName("rsa_key") String rsaKey) {
}

// Nested JSON object: {"service":{"token":"a"}} (field names match the keys, no annotation required)
public record Service(String token) {}
public record Nested(Service service) {}

// Keys with dot, underscore or hyphen: {"service.token":"a"}, {"service_token":"a"}, {"service-token":"a"}
public record Dotted(@SerializedName("service.token") String token) {}
public record Underscore(@SerializedName("service_token") String token) {}
public record Hyphen(@SerializedName("service-token") String token) {}

// Mixed fields and several accepted keys for the same field
public record Mixed(String name, @SerializedName(value = "service.token", alternate = {"service_token"}) String token) {}
```

`@SerializedName` works on `record` components and regular classes, and it is also used to serialize back
(`modelToString`) with the same key.

> **Important:** Jackson annotations such as `@JsonProperty` are **ignored**. A model annotated only with
> `@JsonProperty("service.token")` will return `null`; use `@SerializedName("service.token")` instead.

> **Spring Boot:** The `gson` dependency is not exposed transitively, so to use `@SerializedName` in your models
> add it to your project, for example `implementation 'org.springframework.boot:spring-boot-starter-gson'` or `implementation 'com.google.code.gson:gson:<version>'`.

> You can find a default [AWSSecretDBModel](sync/aws-secrets-manager-sync/src/main/java/co/com/bancolombia/secretsmanager/model/AWSSecretDBModel.java)
> model that includes the default fields to connect to an RDS database.

## Sync connectors

### AWS Secrets Manager (Sync)

```groovy
dependencies {
    implementation 'com.github.bancolombia:aws-secrets-manager-sync:<version>'
}
```

```java
import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.connector.AWSSecretManagerConnector;

String REGION_SECRET = "us-east-1";
String NAME_SECRET = "secretName";
GenericManager connector = new AWSSecretManagerConnector(REGION_SECRET);

try {
    DefineYourModel secret = connector.getSecret(NAME_SECRET, DefineYourModel.class);
    ...
} catch(SecretException e) {
    ...
}
```

### AWS Parameter Store (Sync)

```groovy
dependencies {
    implementation 'com.github.bancolombia:aws-parameter-store-manager-sync:<version>'
}
```

```java
import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.connector.AWSParameterStoreConnector;

String REGION_PARAMETER = "us-east-1";
String NAME_PARAMETER = "parameterName";
GenericManager connector = new AWSParameterStoreConnector(REGION_PARAMETER);

try {
    String parameter = connector.getSecret(NAME_PARAMETER);
    ...
} catch(SecretException e) {
    ...
}
```

### Vault (Sync)

```groovy
dependencies {
    implementation 'com.github.bancolombia:vault-sync:<version>'
}
```

Define your configuration (the properties are described in [Vault configurations](#vault-configurations)):

```java
VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
        .withProperties(VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .ssl(false)
                .roleId("65903d42-6dd4-2aa3-6a61-xxxxxxxxxx")  // for authentication with vault
                .secretId("0cce6d0b-e756-c12e-9729-xxxxxxxxx") // for authentication with vault
                .build())
        .build();
```

Create the connector:

```java
GenericManager connector = configurator.getVaultClient();
```

Get the secret as String:

```java
String secret = connector.getSecret("my/secret/path");
// ... continue your sync flow
```

Get the secret deserialized:

```java
DBCredentials creds = connector.getSecret("my/database/credentials", DBCredentials.class);
// ... continue your sync flow
```

### Environment System Secrets

```groovy
dependencies {
    implementation 'com.github.bancolombia:env-secrets-manager:<version>'
}
```

No configuration is needed. `EnvConnector` reads the value of an environment variable of the process, using the
variable name as the secret name:

```java
import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.connector.EnvConnector;

GenericManager connector = new EnvConnector();

try {
    String dbPassword = connector.getSecret("DB_PASSWORD"); // value of the DB_PASSWORD environment variable
    ...
} catch (SecretException e) {
    // the environment variable is not defined
}
```

- If the variable is not defined, a `SecretException` is thrown.
- `getSecret(name, Class)` is **not supported** and throws `UnsupportedOperationException`: the value is returned
  as a `String` and [Mapping JSON to your model](#mapping-json-to-your-model) does not apply.

### File Secrets

```groovy
dependencies {
    implementation 'com.github.bancolombia:file-secrets-manager:<version>'
}
```

`FileConnector` reads the content of a file, using the file name as the secret name. You only need to give the
directory where the secrets are stored (for example the directory where Docker or Kubernetes mount them):

```java
import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.connector.FileConnector;

// Directory with the secrets. A trailing separator is added if it is missing.
GenericManager connector = new FileConnector("/run/secrets/");

try {
    String dbPassword = connector.getSecret("db_password"); // content of /run/secrets/db_password
    ...
} catch (SecretException e) {
    // the directory or the file does not exist, or it cannot be read
}
```

- The directory can be any path, or one of the constants `FileConnector.PATH_DOCKER_LINUX` (`/run/secrets/`) and
  `FileConnector.PATH_DOCKER_WINDOWS` (`C:\ProgramData\Docker\secrets`).
- The whole file is returned as a `String` (UTF-8), with no trimming. If your file ends with a line break, it will be
  part of the value.
- If the file cannot be read, a `SecretException` is thrown.
- `getSecret(name, Class)` is **not supported** and throws `UnsupportedOperationException`: the value is returned
  as a `String` and [Mapping JSON to your model](#mapping-json-to-your-model) does not apply.

## Async connectors (Reactor)

Async connectors return `Mono` and require [Reactor Core](https://projectreactor.io/) in your project.

### AWS Secrets Manager (Async)

```groovy
dependencies {
    // Reactor Core is required!
    implementation group: 'io.projectreactor', name: 'reactor-core', version: '3.4.17'
    implementation 'com.github.bancolombia:aws-secrets-manager-async:<version>'
}
```

Define your configuration:

```java
// Default Config
AWSSecretsManagerConfig config = AWSSecretsManagerConfig.builder().build();

// Customized config
AWSSecretsManagerConfig config = AWSSecretsManagerConfig.builder()
        .region(Region.US_EAST_1) // define your region
        .cacheSeconds(600)        // define your cache time
        .cacheSize(300)           // define your cache size
        .endpoint("http://localhost:4566") // override the endpoint
        .build();
```

You can pass the following variables to `AWSSecretsManagerConfig`:

- **region**: AWS Region that you are using, **"us-east-1"** (North Virginia) is the default value.
- **cacheSeconds**: During this time the secret requested to AWS Secrets Manager will be saved in memory.
  The next requests to the same secret will be resolved from the cache. The default value is 0 (no cache).
- **cacheSize**: The maximum amount of secrets you want to save in cache. The default value is 0.
- **endpoint**: The AWS endpoint is the default value but you can override it if you want to test locally with
  LocalStack or other tools.

Create the connector:

```java
AWSSecretManagerConnectorAsync connector = new AWSSecretManagerConnectorAsync(config);
```

Get the secret as String:

```java
connector.getSecret("secretName")
    .doOnNext(System.out::println);
    // ... develop your async flow
```

Get the secret deserialized:

```java
connector.getSecret("secretName", DefineYourModel.class)
    .doOnNext(secret -> {
        // ... develop your async flow
    });
```

### AWS Parameter Store (Async)

```groovy
dependencies {
    // Reactor Core is required!
    implementation 'io.projectreactor:reactor-core:3.4.17'
    implementation 'com.github.bancolombia:aws-parameter-store-manager-async:<version>'
}
```

Define your configuration:

```java
// Default Config
AWSParameterStoreConfig config = AWSParameterStoreConfig.builder().build();

// Customized config
AWSParameterStoreConfig config = AWSParameterStoreConfig.builder()
        .region(Region.US_EAST_1) // define your region
        .cacheSeconds(600)        // define your cache time
        .cacheSize(300)           // define your cache size
        .endpoint("http://localhost:4566") // override the endpoint
        .build();
```

You can pass the following variables to `AWSParameterStoreConfig`:

- **region**: AWS Region that you are using, **"us-east-1"** (North Virginia) is the default value.
- **cacheSeconds**: During this time the parameter requested to AWS will be saved in memory.
  The next requests to the same parameter will be resolved from the cache. The default value is 0 (no cache).
- **cacheSize**: The maximum amount of parameters you want to save in cache. The default value is 0.
- **endpoint**: The AWS endpoint is the default value but you can override it if you want to test locally with
  LocalStack or other tools.

Create the connector:

```java
AWSParameterStoreConnectorAsync connector = new AWSParameterStoreConnectorAsync(config);
```

Get the secret as String:

```java
connector.getSecret("parameterName")
    .doOnNext(System.out::println);
    // ... develop your async flow
```

### Vault (Async)

```groovy
dependencies {
    // Reactor Core is required!
    implementation group: 'io.projectreactor', name: 'reactor-core', version: '3.4.17'
    implementation 'com.github.bancolombia:vault-async:<version>'
}
```

Define your configuration:

```java
VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
        .withProperties(VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .ssl(false)
                .roleId("65903d42-6dd4-2aa3-6a61-xxxxxxxxxx")  // for authentication with vault
                .secretId("0cce6d0b-e756-c12e-9729-xxxxxxxxx") // for authentication with vault
                .build())
        .build();
```

#### Vault configurations

You can pass the following variables to `VaultSecretsManagerProperties` (they apply to both Vault Sync and Vault Async):

- **host**: host name or IP address of the vault server. The default value is localhost.
- **port**: port number of the vault server. The default value is 8200.
- **ssl**: Defines if the connection to the vault server is secure or not. The default value is false.
- **token**: If you already have a token to interact with Vault API, you can pass it to the configurator.
  No auth is performed.

Authentication with vault can be done in two ways with this library:

- **roleId** and **secretId**: If AppRole auth is enabled, you can pass the roleId and secretId to the configurator.
  The library will authenticate with vault and obtain a token.
- **vaultRoleForK8sAuth**: If Kubernetes auth is enabled, you can pass here the vault role for which you would like to
  receive a token in the namespace for your app. For more information please refer to the
  [Kubernetes Auth Method](https://developer.hashicorp.com/vault/docs/auth/kubernetes) documentation.

For other configurations, you can use the `VaultSecretsManagerProperties` class.

Create the connector:

```java
GenericManagerAsync connector = configurator.getVaultClient();
```

Get the secret as String:

```java
connector.getSecret("my/secret/path")
    .doOnNext(System.out::println);
    // ... develop your async flow
```

Get the secret deserialized:

```java
connector.getSecret("my/database/credentials", DBCredentials.class)
    .doOnNext(secret -> {
        // ... develop your async flow
    });
```

## How can I contribute?

Great!!:

- Clone this repo
- Create a new feature branch
- Add new features or improvements
- Send us a Pull Request

### To Do

- New connectors for other services.
  - Key Vault Azure
- Improve our tests
