# in-toto java

This repository contains an in-toto compliant library in Java. This document
describes the repository layout, the usage purpose of this library as well as
its current limitations.

# Usage

## Installation

This library is intended to be used with maven build system, although you can
probably easily move it to any other if you're familiar with those. To add it to
your mvn project edit the pom.xml file to add:

```xml
    ...
    <dependency>
    <groupId>io.github.in-toto</groupId>
    <artifactId>in-toto</artifactId>
    <version>0.4.0</version>
    </dependency>
    ...
```

With it, you should be able to use the library inside your project.

## Using the new library

The library exposes a new set of models used for in-toto 0.1.0, DSSE 1.0.0 and .
If you wish to use the deprecated legacy Link library, please skip to the next
section.

The new library allows you to instantiate a Statement and populate it as
follows:

```java
Subject subject=new Subject();
subject.setName("curl-7.72.0.tar.bz2");
subject.setDigest(
Map.of(
DigestSetAlgorithmType.SHA256.toString(),
"d4d5899a3868fbb6ae1856c3e55a32ce35913de3956d1973caccd37bd0174fa2"))
Predicate predicate=createPredicate();
Statement statement=new Statement();
statement.setSubject(List.of(subject));
statement.setPredicate(predicate);
```

Finally, you can use the built-in `IntotoHelper` class to validate and transform
it into its JSON representation as follows:

```java
String jsonStatement=IntotoHelper.validateAndTransformToJson(statement);
```

If the statement passed to the method is malformed the library will throw
an `InvalidModelException` that will contain a message with the errors.

If you, however wish to create a DSSE based In-toto envelope, the library
features a convenience method:

```java
IntotoEnvelope intotoEnvelope=IntotoHelper.produceIntotoEnvelope(statement,signer);
```

This method accepts a `io.github.intoto.models.Statement` and an implementation
of the ` io.github.intoto.dsse.models.Signer` interface.

### Implementing a Signer and a Verifier

The Signer and Verifier are used to abstract away the sign and verify mechanism
from this library. This allows the user to implement their own Signer/Verifier.
An example of such an implementation is available in
the [io.github.intoto.dsse.helpers](https://github.com/in-toto/in-toto-java/tree/master/src/main/java/io/github/intoto/dsse/helpers)
package.

### Creating a new Predicate

Users that wish to extend the Predicate in the library will see that the
Predicate contains an abstract method:

```java
String getPredicateType();
```

When extending the base Predicate type to create your own, make sure that this
method returns a String that contains a URI identifying the type of the
Predicate.

The library will use the Predicate type and automatically fill in the
Statement's predicateType field with its value.

### Generating keys

The keys in the project where generated with:

```
openssl ecparam -genkey -name secp521r1 -noout -out private.pem #generate private key
openssl ec -in private.pem -pubout -out public.pem #generate public key
openssl pkcs8 -topk8 -nocrypt -in private.pem -out p8private.pem #convert to pkcs8 format
```

## Using the legacy Link library

The library exposes a series of objects and convenience methods to create, sign,
and serialize in-toto metadata. As of now, only Link metadata is supported (see
the Limitations section to see what exactly is supported as of now).

Metadata classes are located in the `io.github.intoto.legacy.models.*` package.
You can, for example create a link as follows:

```java
    Link link = new Link(null,null,"test",null,null);
```

This will create a link object that you can operate with.

You can populate a link and track artifacts using the Artifact class and the
ArtifactHash subclass. You can also use the link's convenience method:

```java
    link.addArtifact("alice");
```

Once the artifact is populated, it hashes the target artifact with any of the
supported hashes.

Finally, you can sign and dump a link by calling sign and dump respectively.

```java
    ...
    Key thiskey = RSAKey.read("src/test/resources/somekey.pem");
    System.out.println("Loaded key: "+thiskey.computeKeyId());

    ...
    Link link = new Link(null,null,"test",null,null,null);
    link.addMaterialt("alice");

    link.sign(thiskey);
    link.dump(somelink);
```

You can see a complete example on `src/java/io/github/legacy/lib/App.java`.

## Acknowledgements

This work was mostly driven forward by the awesome guys at
[control-plane](https://control-plane.io). If you're interested in cloud native
security, do check out their website.

If you'd like to help with the development of this library, patches are welcome!
