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
      <groupId>com.google.code.gson</groupId>
      <artifactId>gson</artifactId>
      <version>2.8.5</version>
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
    Link link = new Link(null, null, "test", null, null);
```

This will create a link object that you can operate with. 

You can populate a link and track artifacts using the Artifact class and the
ArtifactHash subclass. You can, for example track materials as follows:

```java
    Artifact a = new Artifact("alice");
    HashMap<String, ArtifactHash> materials = new HashMap<String, ArtifactHash>();
    materials.put(a.getURI(), a.getArtifactHashes());
    Link link = new Link(materials, null, "test", null, null, null);
```

Once the artfifact is populated, it hashes the target artifact with any of the
supported hashes. You can then populate an artifact dicitonary with it and
instantiate the Link object as before.

Finally, you can sign and dump a link by adding it to a metablock and then
calling sign and dump on it:

```java
import io.in_toto.keys.Key;
import io.in_toto.keys.RSAKey;
...
    Key thiskey = RSAKey.read("src/test/resources/somekey.pem");
    System.out.println("Loaded key: " + thiskey.computeKeyId());

    ...
    Link link = new Link(materials, null, "test", null, null, null);
    Metablock mb = new Metablock(link, null);

    System.out.println("dumping file...");
    mb.sign(thiskey);
    mb.dump("somelink.link");
```

You can see a complete example on `src/java/io/in\_toto/lib/App.java`.

## Limitations

Right now the library is in very early stages. Although the functionality
provided is proven to work, some aspects are still yet to be polished. Namely,
I intend to add the following in the forseeable future:

- A more user-friendly API to create and interact with metadata.
- Layout metadata support
- DSA (and possibly GPG) key support.
- A more thorough test suite that includes integration tests.

So far the only guarantee of this library is that dumped link metadata passes
in-toto verification on the [python](https://github.com/in-toto/in-toto)
reference implementation when providing the right key. This was a crucial
aspect given that, without interoperability, having a separate implementation
would be rather useless.

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
