# maven-scm2cisync-plugin

## what's this ?
Source Control Management System (ex.Subversion) to CI Server (ex.Jenkins) syncronize Maven Plugin.
(sorry ... not jenkins-plugin.)

## author
Kazuhito Miura ( @kazuhito_m )

## Usages

このプラグインのインストールと使い方について、以下に示します。

### インストール

ローカルリポジトリにインストールする(インハウスリポジトリなどでなく、そのマシンのみで動かす)前提にて、手順を書きます。

1. 直下にて、以下のコマンドを実行します。
    - `mvn clean install`

### プラグインのインストール

1. プラグイン実行用の `pom.xml` を作成する
    - [./src/test/resources/com/github/kazuhito_m/scm2cisync/core/pom.xml](./src/test/resources/com/github/kazuhito_m/scm2cisync/core/pom.xml) に`すべてのパラメータを網羅したテンプレート` があるため、これをコピーし作成する
0. `mvn` コマンドにてプラグインを実行
    - `mvn scm2cisync:sync` を実行する
