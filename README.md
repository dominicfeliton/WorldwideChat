# WorldwideChat

![](https://github.com/dominicfeliton/WorldwideChat/blob/main/resources/wwc-clean.png)

## ![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/dominicfeliton/WorldwideChat/build-latest-worldwidechat-commit.yml?style=for-the-badge) ![Repo Size](https://img.shields.io/github/repo-size/dominicfeliton/WorldwideChat?style=for-the-badge) ![Pizza rolls](https://img.shields.io/badge/Mom%20brought%20pizza%20rolls-That's%20awesome-brightgreen?style=for-the-badge)

#### PSA: If you are on 1.13-1.15, please use our Spigot jar regardless if you are using Paper or not. The adventure API + additional Paper components are not available before 1.16.

### WorldwideChat is a translation plugin for Spigot, PaperMC, and Folia 1.13+ on JDK 11+.

It allows you to translate various parts of Minecraft using several Translation APIs on-the-fly.
Some of its notable features include:

- Amazon Translate, Azure Translate, ChatGPT, DeepL Translate, Google Translate, Libre Translate, Ollama, and Systran Translate support.
- Translate incoming/outgoing chat, signs, entities, and written books with an interactive GUI.
- Translation globally, for other players, or just you.
- The plugin itself has been localized into more than 30 languages thanks to Amazon Translate.
- A cache that stores the most used phrases by players on your server, improving speed/reliability/lower costs.
- Auto-adjust to match with the user's localization.
- A blacklist to avoid translating certain phrases/words.
- Configurable rate limits so your players don't rack up substantial translation costs.
- Persistent translation sessions saved per player across server reboots and reloads.
- Stat tracker to keep track of how your players use this plugin.
- A configuration GUI to easily change your config.yml + add/override custom localizations.
- SQL (MariaDB/MySQL) + PostgreSQL + MongoDB Support.
- [Citizens](https://github.com/CitizensDev/Citizens2) (NPC Dialogue) + [DecentHolograms](https://github.com/DecentSoftware-eu/DecentHolograms) Support.

And much more to come!

## How to Setup

This [youtube video](https://www.youtube.com/watch?v=vXl9KDFdPLQ&t=814s) should provide a good crash-course into the basics on how to use this plugin.

If the video doesn't cover what you're looking for, please read the [Wiki](https://github.com/dominicfeliton/WorldwideChat/wiki). File an issue if you don't think something is
working right!

Additionally, if you want to test this plugin out before installing, you can!

- Check out our **Test Server** here:
  ```152.67.252.19```

## Installation

Download WorldwideChat from the **Releases** tab. View more info about it on
its [SpigotMC](https://www.spigotmc.org/resources/worldwidechat.89910/) page.

## Contributing

Pull requests are welcome. File issues in the **Issues** tab; include things like version number, server software,
console logs, and a detailed description.

_(If you would like to develop for this plugin, please check out ```dev/COMPILE.md```.)_

## Credits

Thank you to:

- **jedk1** for OldVersionBookTranslate.java _(no longer included as of 1.15.0)_
- **DarkBlade12** for ReflectionUtils.java _(no longer included as of 1.15.0)_

## License

[GPLv3](https://choosealicense.com/licenses/gpl-3.0/)
