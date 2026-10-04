# VCF

[![Maven Central](https://img.shields.io/maven-central/v/de.soderer/vcf)](https://central.sonatype.com/artifact/de.soderer/vcf)

JAVA VCF Format Reader (de.soderer.utilities.vcf.VcfReader) and Writer (de.soderer.utilities.vcf.VcfWriter)

Datacontainer class is de.soderer.utilities.vcf.VcfCard

The VCF format stores personal information like those printed on business cards.
- Names
- Addresses
- Phonenumbers
- EmailAddresses
- ...

It is widely spread and the defacto standard of contact storage on smartphones.

This Reader and Writer support streamed data, so it can be stored and loaded in any container technology like plain file, zipped file, internet http data stream.

## Usage

The library is available on Maven Central. Replace `VERSION` with the version shown in the badge above.

Maven:

```xml
<dependency>
	<groupId>de.soderer</groupId>
	<artifactId>vcf</artifactId>
	<version>VERSION</version>
</dependency>
```

Gradle:

```groovy
implementation "de.soderer:vcf:VERSION"
```

Without a build tool, the jar can be downloaded from the [GitHub releases](https://github.com/hudeany/vcf/releases).
