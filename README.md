# JThaiPDF

Thai-language PDF generation utilities for Java.

JThaiPDF fixes Thai text rendering when generating PDFs via iText (OpenPDF)
or JasperReports. It rewrites Thai strings into glyph-positioned form using
Private Use Area codepoints (U+F700–U+F71A) that bundled Thai PDF fonts
(Angsana, Cordia, etc.) map to pre-shifted glyphs, so combining marks no
longer collide with ascenders/descenders of base consonants.

## Requirements

- JDK 25
- JasperReports 7.0.6 (only when using the JasperReports adapter)
- OpenPDF (`com.lowagie.text.*`) — pulled in transitively by JasperReports

## Maven coordinates

Published to GitHub Packages:

```xml
<dependency>
    <groupId>io.github.okemaster</groupId>
    <artifactId>jthaipdf</artifactId>
    <version>2.0.0</version>
</dependency>
```

Add the GitHub Packages repository to your `pom.xml` or `settings.xml`:

```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/okemaster/jthaipdf</url>
</repository>
```

## Usage with iText (OpenPDF)

Replace `com.lowagie.text.Chunk` with `com.googlecode.jthaipdf.itext.ThaiChunk`,
or wrap an existing `Chunk` via the copy constructor:

```java
Chunk chunk = new ThaiChunk("ข้อความภาษาไทย", font);
```

## Usage with JasperReports

Either use `ThaiJRPdfExporter` directly in place of `JRPdfExporter`, or call
`ThaiExporterManager` for the common file/stream cases:

```java
ThaiExporterManager.exportReportToPdfFile(jasperPrint, "out.pdf");
ThaiExporterManager.exportReportToPdfStream(jasperPrint, outputStream);
```

## Build

| Task            | Command                                  |
| --------------- | ---------------------------------------- |
| Package         | `mvn package` (→ `target/jthaipdf.jar`)  |
| Install locally | `mvn install`                            |

Releases are published automatically to GitHub Packages by
`.github/workflows/maven-publish.yml` when a GitHub release is created.

## License

See [license.txt](license.txt).

## Authors

Original author: Virask Dungsriakaew (virask@gmail.com)
