# Compiling WWC

Compiling this project is designed to be straightforward.

First, make sure you put a YAMLTranslator configuration file in the root of the project, titled ```yt-settings.yml.```
[You can view the default configuration values here.](https://github.com/dominicfeliton/YAMLTranslator/blob/main/src/main/resources/yt-settings.yml)

Next, install Docker Desktop for your distribution.

Run ```setup-dev-env.sh``` on macOS/linux, and ```setup-dev-env.bat``` on Windows.
This will setup a MongoDB/MySQL/PostgreSQL environment for our unit tests.

Now import the WorldwideChat dir you cloned as a Maven Project in Eclipse/IntelliJ and use this ```mvn``` command:

```clean package```

This cleans our POM, runs our MockBukkit unit tests, runs YAMLTranslator, and then cleans+exports WWC for Spigot/Paper.

If you cannot run YAMLTranslator because you do not have access to Amazon Translate, use this command instead:

```clean package -Dyamltranslator.skip=true```

Once you are done, you can kill the MongoDB/MySQL/PostgreSQL containers via ```takedown-dev-env.sh``` on macOS/linux,
and ```takedown-dev-env.bat``` on Windows.

Let me know if you have any questions!
_- Dominic Feliton_
