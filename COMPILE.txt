# Compiling WWC

Compiling this project is very straightforward. Import as a Maven Project in Eclipse/IntelliJ, then use the following Maven configuration for the most optimal compilation environment:

<code>com.github.ekryd.sortpom:sortpom-maven-plugin:sort exec:java clean package</code>

This cleans our POM, runs YAMLTranslator, and then cleans/exports WWC.

If you cannot run YAMLTranslator because you do not have access to AWS, use this command instead:

<code>com.github.ekryd.sortpom:sortpom-maven-plugin:sort clean package</code>

If you do not want the POM to be sorted automatically, you really only need these two phrases:

<code>clean package</code>

Let me know if you have any questions.

