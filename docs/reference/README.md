# 参考資料の索引

設計の判断で迷ったとき、**まずこの索引を見て、該当するファイルだけを読む。** 全部読む必要はない。

> ⚠️ **手順の正は `docs/notes/design_procedure.md`（9 段階）。** ここにある教材の「5 ステップ」は原典であって、回すのはあちら。

## ファイル一覧

| ファイル | 何が書いてあるか | 出どころ |
|---|---|---|
| [`my_discovery_process.md`](./my_discovery_process.md) | **洗い出しの手順メモ（①②③⑤）。** 自分でつまずいた点を自分の言葉で整理したもの | **自分で書いた** |
| [`curriculum_01_guide_and_exercises.md`](./curriculum_01_guide_and_exercises.md) | 洗い出しの 5 ステップ、演習の要求と提出物リスト、**分量の目安**、AI の使い方 | 教材の写し |
| [`curriculum_02_burger_domain.md`](./curriculum_02_burger_domain.md) | **命名規則（接頭辞・テーブル名）、`common` の 3 条件、コンテキストの判断軸。** 最も頻繁に参照する | 教材の写し |
| [`curriculum_03_worked_example.md`](./curriculum_03_worked_example.md) | 記入例（営業時間と臨時休業）。テンプレート、提出版の実物、`1:1` と `1:*` の決め方 | 教材の写し |
| [`curriculum_04_naming.md`](./curriculum_04_naming.md) | 命名を評価で決める 7 軸、AI へのプロンプト、テーブル名を単数形にする理由 | 教材の写し |
| [`curriculum_05_model_design.md`](./curriculum_05_model_design.md) | **モデル設計の進め方。** 変更コストのフェーズ、**アクター → 境界 → 所持物**、ワークフロー 4 本での検証。**02 章の境界をなぜ捨てたか** | 教材の写し |
| [`curriculum_06_design_patterns.md`](./curriculum_06_design_patterns.md) | **案件を問わず使える 3 つの型。** アクター洗い出しチェックリスト／スナップショットの定石／冗長な列（`shopId`）の判断順序 | 教材の写し |
| [`model_templates.md`](./model_templates.md) | 雛形モデルの全文。フィールド・区分値・`extension` まで。**書き方の作法はここを真似る**（※下の注意を読むこと） | ボスが精査して作ったもの |

## どの場面でどれを読むか

### 進め方で迷ったとき

| 迷ったこと | 読むファイル |
|---|---|
| **モデル設計をどの順で進めるか** | `curriculum_05`（5 ステップ） |
| **いま設計を壊してよいか、もう遅いか** | `curriculum_05`（変更コストのフェーズ 1/2/3） |
| **テーブルも一緒に作ってよいか** | `curriculum_05`（作らない。レビュー範囲が 3 倍になる） |
| 書く分量が分からない | `curriculum_01_guide_and_exercises.md`（分量の目安） |
| 提出物に何を書くか | `curriculum_01_guide_and_exercises.md`（提出物の形式） |

### 置き場所で迷ったとき

| 迷ったこと | 読むファイル |
|---|---|
| **登場人物を洗い出す** | `curriculum_06`（7 つの問い・見落とす 6 類型・テンプレート） |
| **エンティティをどのコンテキストに置くか** | `curriculum_05`（**アクターの利用範囲＝境界**）＋ `curriculum_02_burger_domain.md`（`common` の 3 条件） |
| **「作った人」と「持ち主」が違う気がする** | `curriculum_05`（注文を作るのは顧客だが、注文は店舗のもの） |
| 名詞をエンティティにするか属性にするか | `my_discovery_process.md`（③） |
| 業務フローの例外をどこまで書くか | `my_discovery_process.md`（①） |

### フィールドで迷ったとき

| 迷ったこと | 読むファイル |
|---|---|
| **値を保存するか、毎回計算するか** | `curriculum_06`（スナップショットの 3 つの問い） |
| **スナップショットをどのモデルに置くか** | `curriculum_06`（**揮発データには置かない**） |
| **冗長な列を持ってよいか** | `curriculum_06`（判断の順序 4 段。`shopId` は例外なし） |
| **境界をまたぐときコピーか参照か** | `curriculum_05`（ワークフロー②。コピーする） |
| `enum` にするかどうか | `my_discovery_process.md`（③の「enum はどこから来るか」） |
| 属性の持ち方（相対値か絶対値か、束ねるか分けるか） | `curriculum_03_worked_example.md`（論点4） |
| `1:1` にするか `1:*` にするか | `curriculum_03_worked_example.md`（付録C） |

### 書き方で迷ったとき

| 迷ったこと | 読むファイル |
|---|---|
| `case class` の書き方（セクション区切り・コメントの粒度・`WithNoId`） | `model_templates.md` |
| 区分値の `code` の振り方、`name` を持たせるかどうか | `model_templates.md` |
| **既存のエンティティが何を持っているか** | **`upstream/chapter-03` ブランチの実物**（下の注意を読むこと） |
| 英語名をどう決めるか | `curriculum_04_naming.md`（7 軸） |
| テーブル名の付け方 | `curriculum_02_burger_domain.md`（接頭辞）＋ `curriculum_04_naming.md`（単数形） |

## 教材の写しについて

**原本は研修側で管理されており、更新される。** ここにあるのは貼り付けた時点の写し。

- 取得日は各ファイルの冒頭にある。古そうなら貼り直す
- **加工しない。** 要約や整形をせず、そのまま貼る。教材の記述と設計文書の記述がズレたときに、どちらが正か判断できなくなるため
- 貼り直したらこの索引の該当行も確認する
- `curriculum_05` / `curriculum_06` の図（`./img/*.svg`）は写していない。**原本の alt テキストが詳細なので、それを残してある**

## モデルの実物はブランチにある

**`model_templates.md` は 2026-08-14 時点の写しで、その後モデルが変わっている。**

| | `model_templates.md`（08-14） | `upstream/chapter-03`（現在） |
|---|---|---|
| モデル数 | 24 | **25** |
| `PaymentItem` | **無い**（写しに含まれない） | **本実装。`productName` / `productCategory` まで写す** |
| `PaymentDiscount` | `discountProductId: Product.Id` | **`discountItemId: PaymentItem.Id`** |
| `shopId` | `PaymentDiscount` に無し | **`Shop` 以外の全 10 モデルに有り** |
| 店舗独自商品 | `BuyItem` / `OrderItem` が持てない | **`productCustomId` を追加済み** |

**最新の実物はブランチで見る。**

```bash
git switch upstream/chapter-03
# app-lib/framework/app-core/src/main/scala/edu/{common,shop,customer}/model/
git switch develop
```

変更の理由は `curriculum_05`（ワークフロー検証で 3 回作り替えた話）と `curriculum_06`（`shopId` を全モデルに置く判断）に書かれている。

## まだ取得していない資料

文書内から参照されているが、まだ手元にないもの。

| 呼び名 | 何が書いてあるか（参照から推測） |
|---|---|
| 問題2 の元資料 | `Order.Status` の値の定義（`IS_DELIVERED` など） |
| 第2部 01. Domain/Context | エンティティと値オブジェクトの違い（同一性） |
| 第2部 02. EntityModel | `case class` の書き方そのもの（`curriculum_05` が前提として参照している） |
| 第2部 04. Checkpoint | 「状態と履歴を分ける」総合問題 |
| 第3部 03 の 04 章以降 | 25 モデルをアクター別に読む章、永続化（テーブル・リポジトリ）の配線 |
