# SecretsManager - Bancolombia


This library will help you to decouple your application of your secrets provider. It supports the following conectors to get secrets:

  - AWS Secrets Manager
  - File Secrets (E.g Kubernetes Secrets )
  - Environment System Secrets (E.g Kubernetes Secrets )


# How to use

SecretsManager require [Java] v8+

Add the following dependency to your build.gradle file.

File: build.gradle 
```java
dependencies {
	compile group: 'co.com.bancolombia', name: 'secretsmanager', version: '2.0.3'
  }
```

Fie: Secrets.java
```java
import co.com.bancolombia.commons.secretsmanager.connector.AbstractConnector;
import co.com.bancolombia.commons.secretsmanager.connector.clients.AWSSecretManagerConnector;
import co.com.bancolombia.commons.secretsmanager.manager.GenericManager;

String REGION_SECRET = "us-east-1";
String NAME_SECRET = "secret-db-dev";
AbstractConnector connector = new AWSSecretManagerConnector(REGION_SECRET);
GenericManager manager = new GenericManager(connector);

try {
	DefineYourModel obj = manager.getSecretModel(NAME_SECRET, DefineYourModel.class);	     
	this.URLDB = obj.getHost();
	this.USERNAMEDB = obj.getUsername();
	this.PASSWORDDB = obj.getPassword();
	this.SCHEMADB = obj.getDbname();
} catch(Exception e) {
	// Catch error...
}
```

If you need to use a local instance for ```AWSSecretManagerConnector``` like localstack or a docker container, replace before connector instance with a new instance and the local endpoint like:

```
AbstractConnector connector = new AWSSecretManagerConnector("localhost:4566", REGION_SECRET);
```

Remind you have to define your model with the fields you will need. Ypu can find a default AWSSecretDBModel model, it includes default fields to connect a RDS database. 

To convert `JSON`  to a `POJO`, it uses `Gson`.  If you need use field with custom names, you have to create your model like:

```java
package co.com.bancolombia...;

import com.google.gson.annotations.SerializedName;

public class DefineYourModel {

	@SerializedName("aes_key") // Esta anotación es la de Gson no se puede usar por ejemplo la de Jackson.
	private String aesKey;

	@SerializedName("rsa_key")
	private String rsaKey;

	...

}
```

## How I can contribute ?

Great !!, Please:

* Clone this repo
* Create a new feature branch
* Add new features or improvements 
* Send us a Pull Request 

### To Do

- New conectores for other services.
  - Vault
  - Key Valu Azure
- Improve our tests

License
----
MIT
