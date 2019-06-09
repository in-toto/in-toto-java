in-toto java
============

This repository contains an in-toto compliant library in Java. This document
describes the repository layout, the usage purpose of this library as well as
its current limitations.

# Usage

## installation

This library is intended to be used with maven buildsystem, although you can
probably easily move it to any other if you're familiar with those. To add it
to your mvn project edit the pom.xml file to add:

```xml
    ...
    <dependency>
      <groupId>io.github.in-toto</groupId>
      <artifactId>in-toto</artifactId>
      <version>0.1</version>
      <scope>compile</scope>
    </dependency>
    ...
```

With it you should be able to use the library inside of your project.

## Using the library

The library exposes a series of objects and convenience methods to create,
sign, and serialize in-toto metadata. As of now, only Link metadata is
supported (see the Limitations section to see what exactly is supported as of
now).

Metadata classes are located in the `io.in_toto.models.*` namespace. You can,
for example create a link as follows:

```java
    Link link = new LinkBuilder("test").build();
```

This will create a link object that you can operate with. 

You can populate a link and track artifacts using the Artifact class and the
ArtifactHash subclass. Because a link is immutable you should use the LinkBuilder helper class:

```java
    LinkBuilder linkBuilder = new LinkBuilder("test");
    linkBuilder.addMaterial("alice");
    Link link = linkBuilder.build();
```

Once the artfifact is populated, it hashes the target artifact with any of the
supported hashes.

Finally, you can create a Metablock and sign and dump this as a JSON string.

```java
import io.github.in_toto.keys.Key;
import io.github.in_toto.keys.RSAKey;
...
    Key thiskey = RSAKey.read("src/test/resources/somekey.pem");
    System.out.println("Loaded key: " + thiskey.computeKeyId());

    ...
    LinkBuilder linkBuilder = new LinkBuilder("test");
    linkBuilder.addMaterial("alice");
    Link link = linkBuilder.build();
    
    Metablock metablock = new Metablock<Link>(link, null);

    metablock.sign(thiskey);
    metablock.dump();
```

You can see a complete example on `src/test/java/io/github/in_toto/lib/HelloWorldTest.java`.

## Note on reduced feature-set

in-toto java is not yet a fully compliant in-toto implementation. This
implementation is focused on providing a stable, usable core feature set for
its main goal. The features that we will add in the near future include:

- A more user-friendly API to create and interact with metadata.
- Layout metadata support, including full supply chain verification.
- DSA (and possibly GPG) key support.
- A more thorough test suite that includes integration tests.

We can guarantee that the dumped link metadata passes in-toto verification on
the [python](https://github.com/in-toto/in-toto) reference implementation when
providing the right key.

As of now, the near-future goals of this library are to be used in a Jenkins
plugin and to support Android buildsystems. However, for any other step in the
supply chain I *highly* recommend you use the python implementation, for it has
more features, it's better tested and will be updated to comply wih the spec
before this one.

If you'd like to help with the development of this library, patches are
welcome!

## Acknowledgements

This work was mostly driven forward by the awesome guys at
[control-plane](https://control-plane.io). If you're interested in cloud native
security, do check out their website.
