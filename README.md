# BungeeCommandAliases
Bukkitにおけるコマンドの別名機能（commands.ymlのやつ）をBungeecordに追加するplugin

pluginsにjarをドロップして、起動するとpluginsフォルダ内にBungeeCommandAliases/commands.ymlが生成されるので、[このページ](https://www.spigotmc.org/go/commands-yml)などを参考にしてaliasを作ればok。

Bukkitにはない機能としては、タブ補完機能があります。
これは、子に指定したコマンドの引数をタブ補完して、aliasに適応するやつです。例えば

```yml
aliases:
  sendtohub:
  - send $$1 hub
```

と設定すれば `sendtohub <player>` の `<player>` が引数 `$$1` に合わせて補完されます。
複数のコマンドで同じ引数を指定した場合は、それぞれの子コマンドのタブ補完の結果がすべてaliasの補完として表示されます

設定をリロードする場合は
`/reloadaliases` (権限: `bungeecommandaliases.reload`)
を使用してください。


以下英語版 (↓ English version)

--- 

This is plugin that adds bukkit's command alias function (that is configured in commands.yml).

To use this plugin, drop jar into plugins folder and restart the proxy. After that, BungeeCommandAliases/commands.yml will be generated in plugins folder. Configure the commands.yml. You can refer to [this page](https://www.spigotmc.org/go/commands-yml).

This plugin also adds tab completion for aliases. For example:

```yml
aliases:
  sendtohub:
  - send $$1 hub
```

`sendtohub <player>` will be completed via `$$1` variable from child command `send $$1 hub`.
If you set multiple child commands, every child command's tab completion result will be shown for alias.
