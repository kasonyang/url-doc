title=UrlDoc
date=2017-10-11
type=post
tags=
status=published
~~~~~~

# Overview

UrlDoc is url document generator for annotation-driven spring web applications.

# Usage

gradle

    //specify output file
    System.setProperty("urldoc.out.file","build/url-doc.txt")
    //add UrlDoc dependency
    dependencies {
      compileOnly 'site.kason:url-doc:XXX'
    }


