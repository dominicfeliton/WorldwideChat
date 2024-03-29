# Compiling WWC

Compiling this project is designed to be straightforward. 

First, make sure you put a YAMLTranslator configuration file in the root of the project, titled "yt-settings.yml."
[You can view the default configuration values here.](https://github.com/BadSkater0729/YAMLTranslator/blob/main/src/main/resources/yt-settings.yml)

Next, install Docker Desktop for your distribution.

Run <code>setup-dev-env.sh</code> on macOS/linux, and <code>setup-dev-env.bat</code> on Windows. 
This will setup a mongo/sql environment for our unit tests. You can interrupt the script to kill this container.

Now import the WorldwideChat dir you cloned as a Maven Project in Eclipse/IntelliJ, then use the following Maven configuration for the most optimal compilation environment:

<code>clean package</code>

This cleans our POM, runs YAMLTranslator, and then cleans/exports WWC.

If you cannot run YAMLTranslator because you do not have access to AWS, use this command instead:

<code>clean package -Dyamltranslator.skip=true</code>

Let me know if you have any questions!
_- BadSkater0729_