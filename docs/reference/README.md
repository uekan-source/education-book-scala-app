# 参考資料の索引

設計の判断で迷ったとき、**まずこの索引を見て、該当するファイルだけを読む。** 全部読む必要はない。

## ファイル一覧

| ファイル | 何が書いてあるか | 出どころ |
|---|---|---|
| [`my_discovery_process.md`](./my_discovery_process.md) | **洗い出しの手順メモ（①②③⑤）。** 自分でつまずいた点を自分の言葉で整理したもの | **自分で書いた** |
| [`curriculum_01_guide_and_exercises.md`](./curriculum_01_guide_and_exercises.md) | 洗い出しの 5 ステップ、演習の要求と提出物リスト、**分量の目安**、AI の使い方 | 教材の写し |
| [`curriculum_02_burger_domain.md`](./curriculum_02_burger_domain.md) | **命名規則（接頭辞・テーブル名）、`common` の 3 条件、コンテキストの判断軸。** 最も頻繁に参照する | 教材の写し |
| [`curriculum_03_worked_example.md`](./curriculum_03_worked_example.md) | 記入例（営業時間と臨時休業）。テンプレート、提出版の実物、`1:1` と `1:*` の決め方 | 教材の写し |
| [`curriculum_04_naming.md`](./curriculum_04_naming.md) | 命名を評価で決める 7 軸、AI へのプロンプト、テーブル名を単数形にする理由 | 教材の写し |
| [`model_templates.md`](./model_templates.md) | **雛形モデル 24 個の全文**（`edu.common` / `edu.customer` / `edu.shop`）。フィールド・区分値・`extension` まで。**書き方の作法はここを真似る** | ボスが精査して作ったもの |

## どの場面でどれを読むか

| 迷ったこと | 読むファイル |
|---|---|
| 業務フローの例外をどこまで書くか | `my_discovery_process.md`（①） |
| 名詞をエンティティにするか属性にするか | `my_discovery_process.md`（③） |
| `enum` にするかどうか | `my_discovery_process.md`（③の「enum はどこから来るか」） |
| エンティティをどのコンテキストに置くか | `curriculum_02_burger_domain.md`（`common` の 3 条件）＋ `my_discovery_process.md`（⑤） |
| 英語名をどう決めるか | `curriculum_04_naming.md`（7 軸） |
| テーブル名の付け方 | `curriculum_02_burger_domain.md`（接頭辞）＋ `curriculum_04_naming.md`（単数形） |
| 属性の持ち方（相対値か絶対値か、束ねるか分けるか） | `curriculum_03_worked_example.md`（論点4） |
| `1:1` にするか `1:*` にするか | `curriculum_03_worked_example.md`（付録C） |
| 書く分量が分からない | `curriculum_01_guide_and_exercises.md`（分量の目安） |
| 提出物に何を書くか | `curriculum_01_guide_and_exercises.md`（提出物の形式） |
| **`case class` の書き方（セクション区切り・コメントの粒度・`WithNoId`）** | `model_templates.md` |
| **区分値の `code` の振り方、`name` を持たせるかどうか** | `model_templates.md` |
| **既存のエンティティが何を持っているか** | `model_templates.md` |

## 教材の写しについて

**原本は研修側で管理されており、更新される。** ここにあるのは貼り付けた時点の写し。

- 取得日は各ファイルの冒頭にある。古そうなら貼り直す
- **加工しない。** 要約や整形をせず、そのまま貼る。教材の記述と設計文書の記述がズレたときに、どちらが正か判断できなくなるため
- 貼り直したらこの索引の該当行も確認する

## まだ取得していない資料

文書内から参照されているが、まだ手元にないもの。

| 呼び名 | 何が書いてあるか（参照から推測） |
|---|---|
| 問題2 の元資料 | `Order.Status` の値の定義（`IS_DELIVERED` など） |
| 第2部 01. Domain/Context | エンティティと値オブジェクトの違い（同一性） |
| 第2部 04. Checkpoint | 「状態と履歴を分ける」総合問題 |
