# FastQRGenerator

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fineiasantonio/fastqrgenerator)](https://central.sonatype.com/artifact/io.github.fineiasantonio/fastqrgenerator)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](#requirements)

A fast, dependency-free Java library for generating QR Codes. It implements the
ISO/IEC 18004 pipeline from scratch — Reed-Solomon error correction, matrix
construction, mask selection and rendering — with **zero runtime dependencies**.

<p align="center">
  <img src="samples/sample_V5.png" alt="Sample QR code" width="220">
</p>

## Features

- **No runtime dependencies** — pure Java on top of the JDK only.
- **Full version range** — QR versions 1 through 40.
- **All error-correction levels** — L, M, Q, H.
- **Automatic mask selection** following the ISO 18004 penalty rules.
- **Styled rendering** — custom colors, border thickness and rounded modules.
- **Multiple output formats** — PNG, JPG, JPEG, BMP, plus ANSI terminal printing.

## Requirements

- Java 8 or newer.

## Installation

### Maven

```xml
<dependency>
  <groupId>io.github.fineiasantonio</groupId>
  <artifactId>fastqrgenerator</artifactId>
  <version>0.0.4-alpha</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.fineiasantonio:fastqrgenerator:0.0.4-alpha'
```

## Quick start

```java
import com.qrlib.QRCode;
import com.qrlib.QRCodeGenerator;
import com.qrlib.QRCodeGeneratorBuilder;
import com.qrlib.config.ImageExtensions;

import java.io.FileOutputStream;

// No version or ECC level needed: the smallest fitting version is chosen
// automatically, with error-correction level M by default.
QRCodeGenerator generator = new QRCodeGeneratorBuilder().build();

QRCode qr = generator.generate("https://github.com/FineiasAntonio/FastQRGenerator");

try (FileOutputStream out = new FileOutputStream("qr.png")) {
    qr.getAsImage(ImageExtensions.PNG).writeTo(out);
}
```

## Usage

### Choosing the symbol size

By default the **smallest version that fits the payload** is selected
automatically, so you usually don't need to set one. To pin an explicit version
(`V1`–`V40`)…

```java
new QRCodeGeneratorBuilder().version(QRCodeVersion.V10).build();
```

…or a named `QRCodeSize` preset:

| Preset   | Version |
|----------|---------|
| `TINY`   | V1      |
| `SMALL`  | V2      |
| `MEDIUM` | V5      |
| `LARGE`  | V10     |
| `HUGE`   | V20     |
| `MAX`    | V40     |

```java
new QRCodeGeneratorBuilder().size(QRCodeSize.MEDIUM).build();
```

> When you pin a version, it must hold the payload — otherwise `generate(...)`
> throws `IllegalArgumentException`. Higher versions and higher error-correction
> levels reduce the data capacity.

### Error-correction level

```java
new QRCodeGeneratorBuilder()
        .version(QRCodeVersion.V5)
        .ECCLevel(ECCLevel.H) // L, M (default), Q, H
        .build();
```

### Output formats and saving to a file

`getAsImage(...)` returns a `ByteArrayOutputStream`, so you can write it anywhere:

```java
// default: PNG, module size 10px, default style
qr.getAsImage();

// pick a format
qr.getAsImage(ImageExtensions.JPG);

// pick a format and module size (pixels per module)
qr.getAsImage(ImageExtensions.PNG, 8);

byte[] bytes = qr.getAsImage(ImageExtensions.PNG).toByteArray();
```

### Styling

```java
QRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()
        .moduleColor("#1A1A2E")  // #RRGGBB or shorthand #RGB
        .backgroundColor("#FFF")
        .borderThickness(4)      // quiet-zone width, in modules
        .cornerRadius(0.4)       // 0 (square) to 0.5 (fully round), as a fraction of the module
        .build();

qr.getAsImage(ImageExtensions.PNG, 10, style);
```

Notes:

- Colors accept `#RRGGBB` or shorthand `#RGB` hex and are validated when set.
- `borderColor(...)` tints the quiet zone; when not set it follows the
  background color.
- `cornerRadius(...)` implies rounded modules; `roundedCorners(true)` uses the
  maximum radius. Rounded modules are drawn antialiased, and connected runs of
  modules merge into a single smooth shape rounded only at its ends.
- Keep enough contrast between module and background colors — low-contrast
  symbols may not scan reliably.

### Printing to the terminal

```java
qr.print(); // renders the symbol with ANSI background blocks
```

### Accessing the raw matrix

If you only need the module data (no AWT), read it directly:

```java
int[][] matrix = qr.getMatrixData().getMatrix(); // 1 = dark, 0 = light
```

## Sample output

The [`samples/`](samples) directory contains generated symbols from version 2 up
to version 40.

## Building from source

```bash
./mvnw clean test     # compile and run the test suite
./mvnw clean package  # build the jar
```

## Limitations

This library is in **alpha**. Notable constraints:

- Data is encoded in **byte mode (UTF-8)**, which accepts any text or binary
  payload; there is no numeric/alphanumeric/kanji mode optimization, so purely
  numeric data is not encoded at maximum density.
- Image rendering depends on `java.awt`, which is unavailable on some runtimes
  (e.g. Android).

## License

Licensed under the [Apache License 2.0](LICENSE).
