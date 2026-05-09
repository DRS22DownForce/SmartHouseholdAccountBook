# 04. CI と PR レビュー：壊れ込みを防ぎ、人の見落としを減らす

> この章で学ぶこと: **GitHub Actions が何をするか**、**このリポジトリで CI が `mvn verify` になる理由**、**PR をきっかけにした自動化の流れ**、**Cursor Bugbot の最小構成（概要・`.cursor/BUGBOT.md`・設定方針）**、**セキュリティとパフォーマンスの注意点**。

## 目次

### 前半: GitHub Actions と CI

1. [なぜ CI が必要か](#なぜ-ci-が必要か)
2. [GitHub Actions のしくみ](#github-actions-のしくみ)
3. [このプロジェクトの CI で行うこと](#このプロジェクトの-ci-で行うこと)
4. [ワークフローファイル（ジョブ部分）を読む](#ワークフローファイルジョブ部分を読む)
5. [CI 側のセキュリティとパフォーマンス](#ci-側のセキュリティとパフォーマンス)

### 後半: Cursor Bugbot（簡潔版）

6. [Bugbot の概要（これだけ把握すれば OK）](#bugbot-の概要これだけ把握すれば-ok)
7. [`.cursor/BUGBOT.md` の役割](#cursorbugbotmd-の役割)
8. [Cursor 側の設定方針（本リポジトリ）](#cursor-側の設定方針本リポジトリ)
9. [CI と Bugbot の使い分け](#ci-と-bugbot-の使い分け)

---

## なぜ CI が必要か

チーム開発では、次のような事故が起きやすくなります。

```text
ローカルではテストが通った
が、他の人の環境や main ブランチと合わせたらビルドが壊れていた
```

**CI（Continuous Integration：継続的インテグレーション）**は、コードがリポジトリに入るタイミングで**決められたコマンドを自動実行**し、壊れた状態を早く見つける仕組みです。

代表的なトリガーは次の2つです。

| トリガー | いつ動くか | よくある目的 |
|----------|------------|--------------|
| `pull_request` | PR の作成や更新 | main にマージする前にビルドとテストを通す |
| `push` | 指定ブランチへの直接 push | main を常にビルド可能な状態に保つ |

CI が緑（成功）なら「この時点のコードは、定義したチェックを満たしている」という合図になります。逆に赤なら、マージ前に直すチャンスが得られます。

---

## GitHub Actions のしくみ

**GitHub Actions**は、GitHub が提供する CI/CD の仕組みです。リポジトリ内の YAML ファイルに「いつ・何をするか」を書きます。

ざっくり次の階層で覚えるとよいです。

| 用語 | 意味 |
|------|------|
| **イベント** | きっかけ。例: `pull_request`、`push` |
| **ワークフロー（Workflow）** | 1 つの YAML ファイルに対応する、一連の自動化の定義 |
| **ジョブ（Job）** | 同じランバー上でまとめて実行される処理のかたまり |
| **ステップ（Step）** | ジョブの中の 1 手順。シェルコマンドや既製アクションの実行 |
| **ランバー（Runner）** | ジョブが動く仮想マシン。`ubuntu-latest` など |

> **包含関係と実行のされ方**: 構造は **ワークフロー ⊃ ジョブ ⊃ ステップ** の入れ子です。1 つのワークフロー（= 1 つの YAML ファイル）の中に **複数のジョブを並べられ**、ジョブ同士はデフォルトで **別々のランバー（仮想マシン）で並列に**実行されます（互いにファイルや状態は共有しない）。一方、**同じジョブの中のステップは同じランバー上で上から順に**動くので、`checkout` で取ってきたコードや `setup-java` で入れた JDK を、後続の `mvn verify` がそのまま使えます。`ci.yml` には今 `backend-verify` ジョブが 1 つだけありますが、将来フロントエンドの lint ジョブを足せば **両者は並列に**走るイメージです。

### `concurrency`（同時実行の制御）

PR に何度も push すると、CI が何本も並列で走ります。**古い実行は意味が薄い**ことが多いので、`concurrency` で「同じ PR の古い実行はキャンセルする」といった制御がよく使われます。無駄な待ち時間と計算資源の節約になります。

このリポジトリの `ci.yml` では `group: ci-${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}` を使っています。`A || B` は「A があれば A、なければ B」を使う式です。つまり **PR のときは PR 番号**、**push のときは `github.ref`（例: `refs/heads/main`）**をグループキーにして、`cancel-in-progress: true` で同じキーの古い実行を止め、最新の実行だけ残す設計です。

---

## このプロジェクトの CI で行うこと

このリポジトリのバックエンドは **Spring Boot** で、[Maven](./01-maven.md) がビルドとテストを担当します。CI の中心コマンドは **`mvn verify`** です。

`verify` は、コンパイルに加えてテストを実行し、品質チェックがあればそれも含めたうえで、成果物の検証まで進むフェーズです（プロジェクトの `pom.xml` の定義に従います）。

### なぜ `backend` ディレクトリで実行するのか

Maven の設定ファイル `pom.xml` は `backend` フォルダにあります。また、このプロジェクトでは **OpenAPI 仕様から Java コードを生成**してからビルドします。生成プラグインは `../openapi/openapi.yaml` を参照する前提になっているため、**リポジトリのルートを checkout したうえで `backend` に移動して Maven を実行**します。

```text
リポジトリルート/
  openapi/openapi.yaml   ← 仕様
  backend/pom.xml        ← Maven がここを見る
  backend/src/...
```

---

## ワークフローファイル（ジョブ部分）を読む

実体は [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) です。前半の `on:` `concurrency:` `permissions:` は前のセクションで触れたので、ここでは **ジョブの中身（19〜39 行目）** を 1 ブロックずつ読み解きます。

### ジョブと実行環境（19〜26 行目）

```yaml
jobs:
  backend-verify:
    name: Backend (Maven verify)
    runs-on: ubuntu-latest
    timeout-minutes: 30
    defaults:
      run:
        working-directory: backend
```

| 要素 | 意味 |
|------|------|
| `jobs:` | ジョブを定義する宣言。複数ジョブを並列に書ける。 |
| `backend-verify:` | ジョブの **ID**。他ジョブから参照する時の機械向けの名前。 |
| `name:` | GitHub の画面に表示される **人間向けの名前**。 |
| `runs-on: ubuntu-latest` | GitHub が用意する **使い捨ての Ubuntu 仮想マシン**で実行する。前回の状態は残らないので、JDK などは毎回入れ直す必要がある。 |
| `timeout-minutes: 30` | **最大 30 分で打ち切る**安全弁。無限ループや固まったときに無料枠を浪費しない。 |
| `defaults: run: working-directory: backend` | このジョブの `run:` ステップを **すべて `backend/` で実行**する共通設定。`uses:` のアクションには影響しないので、checkout はリポジトリルートで動く。 |

### ステップ① ソースコードを取得（27〜29 行目）

```yaml
- name: Checkout repository
  uses: actions/checkout@v4
```

| 要素 | 意味 |
|------|------|
| `uses:` | **既製のアクション**（再利用可能な処理パッケージ）を呼び出す YAML キー。シェルコマンドを直接書く `run:` と対になる存在。 |
| `actions/checkout@v4` | `OWNER/REPO@VERSION` の形式。**GitHub 公式の `actions` 組織が提供する `checkout` アクションの v4** を使う、という指定。役割は **このリポジトリをランバー上にクローン**すること。 |

借りたばかりの仮想マシンには **まだ自分のコードが入っていない**ため、最初にこのステップでコードを持ってくる必要があります。`@v4` のようにバージョンを固定しておくと、アクション側の更新で挙動が突然変わるのを防げます。

### ステップ② JDK 25 を準備（31〜36 行目）

```yaml
- name: Set up JDK 25
  uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: "25"
    cache: maven
```

`with:` はアクションへの引数指定です。

| パラメータ | 意味 |
|------------|------|
| `distribution: temurin` | Java の配布元として **Eclipse Temurin** を選ぶ。広く使われていてライセンス面でも安心。 |
| `java-version: "25"` | JDK バージョンを **25** に固定。`backend/pom.xml` の `java.version` と揃えることで「ローカルでは通るのに CI だけ落ちる」事故を防ぐ。クオートは YAML が `25.0` のような数値に丸めて解釈するのを避ける保険。 |
| `cache: maven` | `~/.m2/repository`（依存ライブラリの保管場所）を **`pom.xml` の内容をキーにキャッシュ**。2 回目以降のビルドが大幅に速くなる。 |

### ステップ③ Maven verify を実行（38〜39 行目）

```yaml
- name: Run Maven verify
  run: mvn -B -ntp verify
```

`run:` はシェルコマンドの実行で、`defaults` の効果で **`backend/` 内**で動きます。

| フラグ/引数 | 意味 |
|-------------|------|
| `-B` | `--batch-mode` の短縮形。**対話モードを無効化**して、CI 用に「人間に質問しない」モードにする。 |
| `-ntp` | `--no-transfer-progress` の短縮形。ライブラリのダウンロード進捗を出さず、**ログを読みやすく**する。 |
| `verify` | Maven の **デフォルトライフサイクル** `validate → compile → test → package → verify → install → deploy` のうち、**`verify` までを順番に実行**する指定。コンパイル・ユニットテスト・パッケージング・統合テストまでまとめて走る、CI の定番。 |

> `mvn test` だとユニットテストまで、`mvn package` だと jar/war 作成まで、`mvn verify` だと統合テストまで。さらに進めた **`mvn install`** は、ビルド成果物を **ローカルの `~/.m2/repository` に登録**し、同じ PC の他プロジェクトから依存として参照できる状態にするフェーズです（`deploy` はリモートリポジトリへの公開）。CI のランバーは使い捨てで `~/.m2` も次回は消えるため、CI では **`install` まで進めず `verify` で止める**のが一般的です。

---

## CI 側のセキュリティとパフォーマンス

### セキュリティ

- **シークレット**（API キー、パスワードなど）は GitHub の **Repository secrets** や **Environments** に登録し、ワークフローから `${{ secrets.NAME }}` のように参照します。**リポジトリに平文でコミットしない**のが原則です。
- **`pull_request` イベントはフォークからの PR で権限が弱い**挙動になります。悪意のある PR がワークフローを改ざんしてシークレットを盗むのを防ぐための設計です。フォーク PR で追加の権限が必要な処理をするときは、GitHub のドキュメントで **「安全なパターン」**を確認してください。
- ログに **トークンやパスワードが出ない**よう、デバッグ出力に注意します。

### パフォーマンス

- **Maven キャッシュ**を有効にすると、依存の再ダウンロードが減り、CI 時間が短くなります。
- ジョブを無闇に増やすと待ち行列やメンテコストが増えます。**最初はバックエンドの `verify` だけ**に絞り、必要になったらフロントの lint などを足すのが現実的です。

---

## Bugbot の概要（これだけ把握すれば OK）

**Bugbot**は、Cursor が提供する **PR 向けの自動レビュー**です。PR の差分を読んで、バグやセキュリティ上の懸念をコメントで補助します。

ポイントは 2 つです。

- **CI（GitHub Actions）とは役割が違う**: CI はコマンド実行、Bugbot は差分レビュー。
- **YAML だけでは動かない**: Cursor 側の GitHub 連携とリポジトリ設定が必要。

公式ドキュメント: [Bugbot | Cursor Docs](https://cursor.com/docs/bugbot)

---

## `.cursor/BUGBOT.md` の役割

`.cursor/BUGBOT.md` には、**このプロジェクトで特に見てほしい観点**を書きます。  
例: Spring Security の変更、シークレット混入、テスト不足、性能劣化。

このリポジトリではすでに `.cursor/BUGBOT.md` を配置済みです。運用ルールを変えたいときは、このファイルを更新します。

---

## Cursor 側の設定方針（本リポジトリ）

本リポジトリは、Cursor 側で次の方針で運用しています。

- **Trigger Mode**: `Once Per PR`（1つのPRで自動レビューは基本1回。追加 push では自動再レビューしない）
- **Review Draft PRs**: `OFF`（Draft PR の間はレビュー対象にせず、Ready for review 後にレビュー対象にする）
- **PR Summaries**: `ON`（PR の変更点の要約を自動生成し、レビュアーが全体像を掴みやすくする）
- **Autofix Mode**: `Create New Branch`（自動修正は元ブランチに直接入れず、修正用の別ブランチで提案する）
- **Autofix Severity Threshold**: `Low / Medium / High`（軽微なものから重大なものまで自動修正候補の対象にする）

この設定により、**レビューの質を維持しつつ、通知の過多を防ぐ**運用にしています。

---

## CI と Bugbot の使い分け

- **CI**: `mvn verify` でビルド・テストが通るかを機械的に確認
- **Bugbot**: diff を読み、実装上の見落としを補助

最終判断は人間のレビューで行います。まず CI で壊れ込みを防ぎ、そのうえで Bugbot の指摘を取り込む流れが基本です。

---

## まず覚えるポイント

- **CI**は、PR や main への変更のたびに **ビルドとテストを自動実行**し、壊れ込みを早く見つける仕組み。
- **GitHub Actions**は、`.github/workflows/*.yml` に **イベント・ジョブ・ステップ**を書く。
- このプロジェクトでは **`backend` で `mvn verify`** し、OpenAPI 生成を含む一連の検証を行う。
- **Bugbot**は Cursor の **GitHub 連携とダッシュボード設定**が必要で、**ワークフロー YAML だけでは有効にならない**。
- **`.cursor/BUGBOT.md`**にプロジェクト固有のレビュー観点を残せる。
