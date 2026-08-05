# SQL 最低限メモ

対象：MySQL 8.0（InnoDB）＋ Slick。このリポジトリで必要な範囲に絞る。

例はスタンプカードのテーブル（`common_coupon` / `sales_user_coupon` / `sales_user_coupon_stamp`）で書く。

## 優先順位

| いつ | 何を |
|---|---|
| 今すぐ | 1（実行順）、3（NULL）、5 の警告（`WHERE` 忘れ） |
| 今週中 | 2（JOIN）、4（集約）、9（Slick 対応表） |
| 実装に入ったら | 6（トランザクション）、7（DDL）、8（インデックス） |

1 と 3 を押さえるだけで、SQL で詰まる場面の半分は消える。

---

## 1. 書く順と実行順は違う

これが一番効く。SQL は書く順序と処理される順序が逆転している。

```sql
SELECT   coupon_id, COUNT(*)      -- ⑤ 最後に、何を出すか決まる
FROM     sales_user_coupon        -- ① どのテーブルから
WHERE    state = 200              -- ② 行を絞る（グループ化の前）
GROUP BY coupon_id                -- ③ まとめる
HAVING   COUNT(*) >= 10           -- ④ まとめた結果を絞る
ORDER BY COUNT(*) DESC            -- ⑥ 並べる
LIMIT    10                       -- ⑦ 件数を切る
```

`WHERE` は集計前、`HAVING` は集計後。これを知らないと「なぜ `WHERE COUNT(*) > 10` が動かないのか」で必ず詰まる。

## 2. JOIN は 2 種類だけ覚える

```sql
-- INNER JOIN: 両方に存在する行だけ
SELECT uc.id, c.name
FROM sales_user_coupon uc
INNER JOIN common_coupon c ON uc.coupon_id = c.id;

-- LEFT JOIN: 左は全部残す。右が無ければ NULL
SELECT uc.id, COUNT(s.id)
FROM sales_user_coupon uc
LEFT JOIN sales_user_coupon_stamp s ON s.user_coupon_id = uc.id
GROUP BY uc.id;
```

下の例が重要で、`INNER` にするとスタンプ 0 個のカードが消える。**0 件を数えたいなら LEFT**。

## 3. NULL の扱い

`used_order_id` のように NULL を取る列があるので必須。

```sql
WHERE used_order_id = NULL     -- NG。常に何もヒットしない
WHERE used_order_id IS NULL    -- OK。未使用のクーポン
WHERE used_order_id IS NOT NULL
```

NULL は「値が無い」であって「等しいか比較できるもの」ではない。

`COUNT(列名)` も NULL を数えない。全行数が欲しいときは `COUNT(*)`。

## 4. 集約関数

```sql
COUNT(*)        -- 行数
COUNT(列)       -- NULL を除いた個数
SUM(列)  AVG(列)  MAX(列)  MIN(列)
```

`GROUP BY` と一緒に使うと「グループごとの」計算になる。

## 5. 書き込み 3 つ

```sql
INSERT INTO sales_user_coupon_stamp (user_coupon_id, order_id) VALUES (53, 5208);

UPDATE sales_user_coupon SET state = 200 WHERE id = 53;

DELETE FROM sales_user_coupon_stamp WHERE user_coupon_id = 53;
```

`UPDATE` と `DELETE` は `WHERE` を忘れると全行に効く。実務で最も怖い事故。

## 6. トランザクションとロック

```sql
START TRANSACTION;
  SELECT * FROM sales_user_coupon WHERE id = 53 FOR UPDATE;  -- 行ロック
  UPDATE sales_user_coupon SET state = -1 WHERE id = 53;
COMMIT;   -- または ROLLBACK
```

`FOR UPDATE` を付けると、`COMMIT` するまで他のトランザクションがその行を触れない。

スタンプカードの設計で「使用確定は対象行をロックし、`state` が `IS_AVAILABLE` であることを確認してから更新する」と決めているのがこれ。

## 7. DDL（テーブル定義）

Slick では書けないので生 SQL になる。既存の `udb_user`（`etc/database/migration/`）と同じ形にそろえる。

```sql
CREATE TABLE `sales_user_coupon` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT UNSIGNED NOT NULL,
  `coupon_id`     BIGINT UNSIGNED NOT NULL,
  `used_order_id` BIGINT UNSIGNED NULL,
  `state`         SMALLINT        NOT NULL,
  `expires_at`    DATE            NOT NULL,
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY  `ukey01` (`used_order_id`),
  KEY         `key01`  (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

| 要素 | 意味 |
|---|---|
| `NOT NULL` | 空を許さない |
| `PRIMARY KEY` | 主キー。自動でインデックスが付く |
| `UNIQUE KEY` | 重複禁止。ただし **NULL は複数入れられる** |
| `KEY` | ただのインデックス（重複可） |

### NULL が複数入れられることの利用

MySQL には「特定の状態のときだけ一意」という部分ユニークインデックスが無い。生成列と組み合わせて代用する。

```sql
`collecting_key` BIGINT UNSIGNED AS (IF(`state` = 100, `user_id`, NULL)) STORED,
UNIQUE KEY `ukey02` (`collecting_key`)
```

`state` が 100（貯め中）以外の行は `NULL` になり、一意制約の対象から外れる。結果として「貯め中の行は会員あたり 1 件」だけを強制できる。

## 8. インデックスが効く条件

```sql
-- 効く
WHERE user_coupon_id = 53
WHERE user_id = 100 AND state = 100      -- 複合インデックス (user_id, state) があれば

-- 効かない
WHERE DATE(created_at) = '2026-08-05'    -- 列を関数で包むと使われない
WHERE name LIKE '%クーポン'               -- 前方一致以外は使われない
```

原則は「列をそのまま比較する」。関数で包んだ瞬間にインデックスが使われなくなる。

## 9. Slick との対応

このプロジェクトでは生 SQL をほとんど書かない。読み替えができれば十分。

| SQL | Slick |
|---|---|
| `WHERE x = ?` | `.filter(_.x === x)` |
| `AND` / `OR` | `&&` / `\|\|` |
| `INNER JOIN` | `.join(other).on(...)` |
| `LEFT JOIN` | `.joinLeft(other).on(...)` |
| `GROUP BY` | `.groupBy(...)` |
| `ORDER BY x DESC` | `.sortBy(_.x.desc)` |
| `LIMIT n` | `.take(n)` |
| `COUNT(*)` | `.length` |
| `INSERT` | `+=` / `++=` |
| `DELETE` | `.delete` |

実例は `app-lib/framework/app-core/src/main/scala/edu/udb/persistence/UserSession.scala` を参照。
`WHERE token = ?` が `.filter(_.token === token)` になっている。

## 補足：設計書で SQL が出てくる理由

設計書（`docs/domain/`）に `WHERE user_coupon_id = ?` のような断片が出てくるのは、
言語に依存しない書き方だから。読者が Scala 実装者とは限らず、
「どんな条件で絞るか」だけを議論したい場面で Slick の記法を持ち込むと本題がぼやける。
