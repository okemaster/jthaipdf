# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

JThaiPDF is a small Java library that fixes Thai text rendering when generating PDFs via iText or JasperReports. Distributed as a Maven artifact `io.github.okemaster:jthaipdf` on GitHub Packages, published automatically by `.github/workflows/maven-publish.yml` when a GitHub release is created.

## Build & test

- Build: `mvn package` (output: `target/jthaipdf.jar`)
- Install to local repo: `mvn install`
- Tests: JUnit 5 (Jupiter) under `src/test/java`. Run all tests with `mvn test`, or a single test with `mvn -Dtest=ClassName#method test`.
- Toolchain: JDK 25 (`maven.compiler.release=25`). Source encoding is UTF-8 — required because the codebase contains Thai characters and PUA codepoints (0xF7xx).

## Architecture

The library is intentionally tiny (4 source files). The whole design hangs off one algorithm and two thin adapters that plug it into iText and JasperReports.

### The core: `util/ThaiDisplayUtils.toDisplayString`

Thai script stacks combining marks (tone marks, vowels) above/below base consonants. PDF font glyph tables expect those marks at fixed positions, but plain Unicode Thai (U+0E00 block) leaves them on the canonical baseline, producing visual collisions when the base consonant has an ascender (ป ฝ ฟ ฬ) or a descender (ฎ ฏ ฐ ญ ฤ ฦ).

`ThaiDisplayUtils` rewrites a Thai string into a glyph-positioned form by replacing offending combining marks with codepoints from the Private Use Area (U+F700–U+F71A) that fonts like Angsana/Cordia map to pre-shifted glyphs. Categories driving the rewrite:

- **Up-tail consonants** (ป ฝ ฟ ฬ): force any upper-level mark to its left-shifted PUA variant.
- **Down-tail consonants** (ฎ ฏ ฐ ญ ฤ ฦ): if a lower-level vowel (สระอุ/อู/พินทุ) follows, prefer the "cut-tail" PUA form of the consonant; otherwise pull the vowel down.
- **Upper level 1 marks** (ไม้หันอากาศ, สระอิ/อี/อึ/อื, ไม้ไต่คู้, นิคหิต): only need a left-shift over up-tail consonants.
- **Upper level 2 marks** (ไม้เอก/โท/ตรี/จัตวา, ทัณฑฆาต): if an upper-level-1 mark already sits over the base, the level-2 mark must be pulled-down-and-shifted; over an up-tail base, just shifted; otherwise just pulled down.
- **SARA AM (ำ)** is not a single-cell glyph — `explodeSaraAm` splits it into NIKHAHIT + SARA AA before the rewrite pass so the nikhahit can participate in stacking rules.

This is a pure transformation: `String → String` (also `char[]` and `StringBuffer` overloads). Callers feed it text on the way out, after layout has chosen positions but before glyphs are emitted.

### Adapters

Two integration points hook this into PDF libraries:

- `itext/ThaiChunk` — extends OpenPDF's `com.lowagie.text.Chunk` and rewrites `this.content` in its constructor. Drop-in replacement: swap `Chunk` for `ThaiChunk` (or wrap an existing chunk via the copy constructor).
- `jasperreports/engine/export/ThaiJRPdfExporter` — extends `net.sf.jasperreports.pdf.JRPdfExporter` and overrides the protected `getChunk(...)` so every text chunk passes through `toDisplayString` before reaching the PDF writer.
- `jasperreports/engine/ThaiExporterManager` — convenience wrappers `exportReportToPdfFile` / `exportReportToPdfStream` that wire `ThaiJRPdfExporter` to a `JasperPrint` and an output target.

### Conventions to preserve when editing

- Do not "modernize" the Unicode tables at the bottom of `ThaiDisplayUtils.java` — those exact PUA codepoints match what the bundled Thai PDF fonts expect. Renaming or reformatting is fine; changing values will break output.
- The rewrite walks one character at a time and looks back at most two positions (`pch`, occasionally `content[i-2]` when the previous char was a lower-level vowel). New rules should follow the same single-pass shape so output stays deterministic.
- iText integration uses **OpenPDF** (`com.lowagie.text.*`), the LGPL fork — not the modern itextpdf 7+ API. JasperReports 7.0.6 still bundles OpenPDF transitively, which is why no explicit OpenPDF dependency is declared.
