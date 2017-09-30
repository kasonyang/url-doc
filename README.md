![Maven Central](https://img.shields.io/maven-central/v/site.kason/url-doc.svg)

# What is UrlDoc?

UrlDoc is url document generator for annotation-driven spring web applications.

# How to use?

gradle

    //specify output file
    System.setProperty("urldoc.out.file","build/url-doc.txt")
    //add UrlDoc dependency
    dependencies {
      compileOnly 'site.kason:url-doc:XXX'
    }

