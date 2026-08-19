# 雛形モデル（ボスが要件定義の後に精査して作ったもの）

取得日：2026-08-14 ／ **原本は研修側で管理されており更新される。古そうなら貼り直す**

**加工しない。** 要約・整形をせず、共有されたまま貼る。

全 24 モデル。`edu.common` 7 / `edu.customer` 7 / `edu.shop` 10。
**この写しは古い。** その後 `PaymentItem`（決済明細）が本実装され、`upstream/chapter-03` ブランチは
**25 モデル**になっている。差分は `README.md` の「モデルの実物はブランチにある」を見る。

| コンテキスト | モデル |
|---|---|
| `edu.common` | `Coupon` / `CouponOffer` / `Product` / `SalesTemplate` / `SalesTemplateMenu` / `SalesTemplateMenuItem` / `StampCard` |
| `edu.customer` | `Cart` / `Customer` / `CustomerCoupon` / `CustomerPassword` / `CustomerSession` / `CustomerStampCard` / `CustomerStampCardItem` |
| `edu.shop` | `CustomMenu` / `CustomMenuItem` / `CustomProduct` / `ExcludedMenu` / `ExcludedProduct` / `Order` / `OrderItem` / `Payment` / `PaymentDiscount` / `Shop` |

> ⚠️ **教材の写し（`curriculum_02_burger_domain.md`）とはコンテキストの切り方が違う。**
> 教材は `udb` / `common/menu` / `shop` / `sales` の 4 つだが、雛形は `common` / `customer` / `shop` の 3 つ。
> 会員は `udb.User` ではなく `customer.Customer`、注文は `sales` ではなく `shop` に入っている。
> **雛形が新しいので、設計はこちらに合わせる。**

---

# `edu.common`

## `Coupon`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * クーポン: 割引の内容を定義する本部マスタ。
 *
 * 配布方法は持たない。誰にどう配るかは [[CouponOffer]] が受け持つ。
 */
import Coupon.*
case class Coupon(
  id:                 Option[Id],           // クーポンId
  name:               String,               // クーポン名
  discountType:       DiscountType,         // 割引種別
  discountProductId:  Option[Product.Id],   // 対象商品, None: 全商品
  discountValue:      Option[Int],          // 割引値
  validDays:          Short,                // 取得後の有効日数
  state:              Status,               // 提供状態
  updatedAt:          LocalDateTime = Now,  // データ更新日
  createdAt:          LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * クーポン: 付随する型と処理の定義
 */
object Coupon:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Coupon]
  type EmbeddedId = Entity.EmbeddedId[Id, Coupon]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 割引種別: discountValue の読み方が変わる
   */
  enum DiscountType(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_FREE   extends DiscountType(code = 1, name = "商品無料")
    case IS_AMOUNT extends DiscountType(code = 2, name = "定額割引")

  /**
   * 提供状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE   extends Status(code = -1) // 廃止:   新しい配布を止めた
    case IS_PREPARING extends Status(code =  0) // 準備中: 配布はまだ始まっていない
    case IS_ACTIVE    extends Status(code =  1) // 有効
```

## `CouponOffer`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * クーポン配布: [[Coupon]] を「いつ・誰に・何枚まで」配るかの設定。
 * 1 つのクーポンに対して複数の配布を持てる。
 *
 *  - `promoCode`  … None はアプリの一覧配布、Some はコード入力による配布
 *  - `promoLimit` … 配布上限。None は無制限
 */
import CouponOffer.*
case class CouponOffer(
  id:         Option[Id],          // 配布Id
  couponId:   Coupon.Id,           // クーポンId
  issueType:  IssueType,           // 発行形態: 直接消費 / 付与消費
  promoCode:  Option[Code],        // プロモコード。None は一覧配布
  promoLimit: Option[Int],         // 配布上限。None は無制限
  dateStart:  Option[LocalDate],   // 配布: 開始日
  dateEnd:    Option[LocalDate],   // 配布: 終了日
  state:      Status,              // 配布状態
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * クーポン配布: 付随する型と処理の定義
 */
object CouponOffer:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, CouponOffer]
  type EmbeddedId = Entity.EmbeddedId[Id, CouponOffer]

  // --[ Opaque Values ]-----------------------------------------------
  object Id   extends Entity.Id[Long]
  object Code extends Entity.Id[String]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 発行形態
   */
  enum IssueType(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_DIRECT  extends IssueType(code = 1, name = "直接消費")  // 発行せずカートで使う
    case IS_GRANTED extends IssueType(code = 2, name = "付与消費")  // 発行して保有してから使う

  /**
   * 配布状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED extends Status(code = -1) // 停止: 配布を打ち切った
    case IS_OPEN   extends Status(code =  1) // 配布中
```

## `Product`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * 商品: 販売する 1 品（バーガー / ドリンク / セットなど）。
 * セットメニューも商品として扱い、含まれる商品を `subItem` に持つ。
 */
import Product.*
case class Product(
  id:          Option[Id],           // 商品Id
  name:        String,               // 商品名
  category:    Category,             // 商品: カテゴリ
  subItem:     Seq[Product.Id],      // セットメニュー: 含まれる商品Id
  price:       Int,                  // 価格
  state:       Status,               // 販売状態
  description: String,               // 説明文
  updatedAt:   LocalDateTime = Now,  // データ更新日
  createdAt:   LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * 商品: 付随する型と処理の定義
 */
object Product:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Product]
  type EmbeddedId = Entity.EmbeddedId[Id, Product]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 商品カテゴリ: 100 番台が単品、200 番台がセット
   */
  enum Category(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_BURGER  extends Category(code = 100, name = "バーガー")
    case IS_SIDE    extends Category(code = 101, name = "サイド")
    case IS_DRINK   extends Category(code = 102, name = "ドリンク")
    case IS_DESSERT extends Category(code = 103, name = "デザート")
    case IS_SET     extends Category(code = 200, name = "セットメニュー")

  /**
   * 販売状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_DISCONTINUED extends Status(code = -1) // 販売終了
    case IS_PLAN         extends Status(code =  0) // 販売予定
    case IS_ON_SALE      extends Status(code =  1) // 販売中
```

## `SalesTemplate`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * 販売テンプレート: 店舗に割り当てるメニュー構成のひな型。
 *
 * 本部がいくつか用意し、各店舗はこのうち 1 つを参照する。
 */
import SalesTemplate.*
case class SalesTemplate(
  id:        Option[Id],          // 管理Id
  name:      String,              // テンプレート名 (例: デフォルト / 大型店舗 / 小型店舗)
  state:     Status,              // 提供状態
  note:      String,              // 説明文
  updatedAt: LocalDateTime = Now, // データ更新日
  createdAt: LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 販売テンプレート: 付随する型と処理の定義
 */
object SalesTemplate:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, SalesTemplate]
  type EmbeddedId = Entity.EmbeddedId[Id, SalesTemplate]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 提供状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 廃止: 新しい割り当てを止めた
    case IS_ACTIVE  extends Status(code =  1) // 有効
```

## `SalesTemplateMenu`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * 販売テンプレートのメニュー: 注文画面のタブにあたる区分（グランド / 朝 / 夜 / 期間限定）。
 *
 * 販売できる期間を 2 つの枠で持つ。
 *
 *  - `dateStart` 〜 `dateEnd`  … 販売する日の範囲。両端を含む
 *  - `timeStart` ＋ `timeOpen` … その日のうち注文できる時間帯。終了時刻ではなく
 *    長さで持つので、深夜をまたぐ時間帯も 1 組で表せる
 *
 * どちらの枠も、片側が None ならその側に制限がない。4 つとも None なら常時販売。
 */
import SalesTemplateMenu.*
case class SalesTemplateMenu(
  id:         Option[Id],           // メニューId
  templateId: SalesTemplate.Id,     // 販売テンプレートId
  name:       String,               // メニュー名 (例: グランド / 朝 / 夜 / 春の期間限定)
  dateStart:  Option[LocalDate],    // 販売日: 開始
  dateEnd:    Option[LocalDate],    // 販売日: 終了
  timeStart:  Option[LocalTime],    // 販売時間: 開始
  timeOpen:   Option[Duration],     // 販売時間: 長さ。深夜またぎもこれで表す
  state:      Status,               // 公開状態
  sortOrder:  Short,                // タブの表示順
  updatedAt:  LocalDateTime = Now,  // データ更新日
  createdAt:  LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * 販売テンプレートのメニュー: 付随する型と処理の定義
 */
object SalesTemplateMenu:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, SalesTemplateMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, SalesTemplateMenu]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 公開状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 取り下げ: 公開したものを下げた
    case IS_PLAN    extends Status(code = 0)  // 準備中: まだ公開していない
    case IS_PUBLIC  extends Status(code = 1)  // 公開
```

## `SalesTemplateMenuItem`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * 販売テンプレートのメニューの表示アイテム
 * メニューに並ぶ商品と、その表示順。
 *
 * [[SalesTemplateMenu]] と [[Product]] の対応を表す。
 */
import SalesTemplateMenuItem.*
case class SalesTemplateMenuItem(
  id:             Option[Id],           // 管理Id
  templateId:     SalesTemplate.Id,     // 販売テンプレートId
  templateMenuId: SalesTemplateMenu.Id, // メニューId
  productId:      Product.Id,           // 商品Id
  sortOrder:      Short,                // 表示順
  updatedAt:      LocalDateTime = Now,  // データ更新日
  createdAt:      LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * 販売テンプレートのメニューの表示アイテム: 付随する型と処理の定義
 */
object SalesTemplateMenuItem:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, SalesTemplateMenuItem]
  type EmbeddedId = Entity.EmbeddedId[Id, SalesTemplateMenuItem]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

## `StampCard`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.common.model

import ixias.core.model.*

/**
 * スタンプ台帳: スタンプカードを定義する本部マスタ。
 * `issueStampNum` 個たまると [[Coupon]] 1 枚に引き換えられる。
 */
import StampCard.*
case class StampCard(
  id:            Option[Id],          // 台帳Id
  name:          String,              // 台帳名 (例: 2026 春のスタンプカード)
  rule:          Rule,                // 付与ルールの種類
  issueCouponId: Coupon.Id,           // 引換: 発行するクーポンId
  issueStampNum: Int,                 // 引換: 必要スタンプ数
  dateStart:     Option[LocalDate],   // 配布: 開始日
  dateEnd:       Option[LocalDate],   // 配布: 終了日。当日を含む
  state:         Status,              // 提供状態
  updatedAt:     LocalDateTime = Now, // データ更新日
  createdAt:     LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * スタンプ台帳: 付随する型と処理の定義
 */
object StampCard:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, StampCard]
  type EmbeddedId = Entity.EmbeddedId[Id, StampCard]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 付与ルール
   */
  enum Rule(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_PAYMENT_COUNT  extends Rule(code = 1, name = "会計回数")
    case IS_PAYMENT_AMOUNT extends Rule(code = 2, name = "会計金額")

  /**
   * 提供状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_ARCHIVE extends Status(code = -1) // 廃止
    case IS_ACTIVE  extends Status(code =  1) // 有効
```

---

# `edu.customer`

## `Cart`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*
import ixias.core.model.value.Token

import edu.shop.model.Shop
import edu.common.model.{ Product, Coupon, CouponOffer }

/**
 * カート: ある店舗に対して注文しようとしている内容。
 *
 * ログインしていなくても利用できる。
 * 誰のものかではなくカート自身の `token`で識別し、
 * `customerId` はログインしたときに後から紐づけることができる
 */
import Cart.*
case class Cart(
  id:         Option[Id],                        // 管理Id
  token:      Token,                             // カートのトークン（未署名）
  customerId: Option[Customer.Id],               // 顧客Id。ログイン後に紐づく
  shopId:     Shop.Id,                           // 店舗Id
  items:      Seq[BuyItem]  = Nil,               // 購入商品
  coupons:    Seq[UseCoupon] = Nil,              // 利用クーポン
  state:      Status        = Status.IS_EDITING, // カートの状態
  updatedAt:  LocalDateTime = Now,               // データ更新日
  createdAt:  LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

/**
 * カート: 付随する型と処理の定義
 */
object Cart:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Cart]
  type EmbeddedId = Entity.EmbeddedId[Id, Cart]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * カートに入れた商品
   */
  case class BuyItem(
    productId:  Product.Id,  // 商品Id
    productNum: Int,         // 注文数
  )

  /**
   * カートに適用したクーポン
   *
   * `couponId` は割引の内容。
   *  - `couponOfferId`    … 直接消費型（[[CouponOffer.IssueType.IS_DIRECT]]）
   *  - `customerCouponId` … 付与型。会員が保有する 1 枚を消費する
   */
  case class UseCoupon(
    couponId:         Coupon.Id,                 // クーポンId
    couponOfferId:    Option[CouponOffer.Id],    // クーポン: 配布Id（直接消費のとき）
    customerCouponId: Option[CustomerCoupon.Id]  // 顧客: 保有クーポンId（付与型のとき）
  )

  /**
   * カートの状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_DISCARDED extends Status(code = -1) // 破棄
    case IS_EDITING   extends Status(code =  1) // 編集中
    case IS_ORDERED   extends Status(code =  2) // 注文済み

  // --[ Extensions ]--------------------------------------------------
  /**
   * カート: 変数値だけで完結する処理
   */
  extension (self: Cart)

    /**
     * ログイン前のカートか
     */
    def isAnonymous: Boolean =
      self.customerId.isEmpty
```

## `Customer`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

/**
 * 顧客: 会員登録されたアカウント。プロフィールのみを持つ。
 */
import Customer.*
case class Customer(
  id:        Option[Id],                        // 管理Id
  uuid:      UUID,                              // UUID
  email:     String,                            // ログインId (メールアドレス)
  name:      String,                            // 表示名
  state:     Status        = Status.IS_ACTIVE,  // アカウント状態
  updatedAt: LocalDateTime = Now,               // データ更新日
  createdAt: LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

/**
 * 顧客: 付随する型と処理の定義
 */
object Customer:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type UUID       = UUID.Repr
  type WithNoId   = Entity.WithNoId[Id, Customer]
  type EmbeddedId = Entity.EmbeddedId[Id, Customer]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  /**
   * 公開用の識別子
   */
  object UUID extends Entity.Id[String]:
    def generate: UUID = UUID(java.util.UUID.randomUUID.toString)

  // --[ Value Objects ]-----------------------------------------------
  /**
   * アカウント状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_INACTIVE extends Status(code = -1) // 停止
    case IS_ACTIVE   extends Status(code =  1) // 有効
```

## `CustomerCoupon`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

import edu.common.model.{ Coupon, CouponOffer, StampCard }

/**
 * 会員が保有しているクーポン 1 枚。
 *
 * `couponId` は割引の内容
 *  - `couponOfferId` … 配布から取得したとき（[[CouponOffer]] の付与型）
 *  - `stampCardId`   … [[CustomerStampCard]] の引き換えで発行されたとき
 */
import CustomerCoupon.*
case class CustomerCoupon(
  id:            Option[Id],             // 保有Id
  customerId:    Customer.Id,            // 顧客Id
  couponId:      Coupon.Id,              // クーポンId
  couponOfferId: Option[CouponOffer.Id], // 配布Id（配布から取得したとき）
  stampCardId:   Option[StampCard.Id],   // 台帳Id（スタンプ引換のとき）
  expiredAt:     LocalDateTime,          // 有効期限。取得時に確定
  state:         Status,                 // 保有状態
  usedAt:        Option[LocalDateTime],  // 使用日時。IS_USED で埋まる
  updatedAt:     LocalDateTime = Now,    // データ更新日
  createdAt:     LocalDateTime = Now     // データ作成日
) extends EntityModel[Id]

/**
 * 保有クーポン: 付随する型と処理の定義
 */
object CustomerCoupon:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerCoupon]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerCoupon]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 保有状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_REVOKED extends Status(code = -1) // 取消: 不正取得などで運営が回収した
    case IS_UNUSED  extends Status(code =  1) // 未使用
    case IS_USED    extends Status(code =  2) // 使用済

  // --[ Extensions ]--------------------------------------------------
  /**
   * 保有クーポン: 変数値だけで完結する処理
   */
  extension (self: CustomerCoupon)

    /**
     * その時点で使えるか。失効は状態ではなく日付で判定する
     */
    def isUsableAt(at: LocalDateTime): Boolean =
      self.state == Status.IS_UNUSED && at.isBefore(self.expiredAt)
```

## `CustomerPassword`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*
import ixias.core.security.PBKDF2

/**
 * 顧客パスワード
 */
import CustomerPassword.*
case class CustomerPassword(
  id:         Option[Id],          // 管理Id
  customerId: Customer.Id,         // 顧客Id
  hash:       String,              // PBKDF2ハッシュ文字列
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 顧客パスワード: 付随する型と処理の定義
 */
object CustomerPassword:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerPassword]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerPassword]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Factory Methods ]---------------------------------------------
  /**
   * 平文のパスワードを PBKDF2 でハッシュ化して新しい行を作る
   */
  def hashed(customerId: Customer.Id, raw: String): WithNoId =
    CustomerPassword(
      id         = None,
      customerId = customerId,
      hash       = PBKDF2.hash(raw)
    ).toWithNoId

  // --[ Extensions ]--------------------------------------------------
  /**
   * 顧客パスワード: 変数値だけで完結する処理
   */
  extension (self: CustomerPassword)

    /**
     * 平文のパスワードが保存済みハッシュと一致するか
     */
    def verify(raw: String): Boolean =
      PBKDF2.compare(raw, self.hash)
```

## `CustomerSession`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*
import ixias.core.model.value.Token

/**
 * 顧客セッション: サーバ側で保持するログインセッション。
 */
import CustomerSession.*
case class CustomerSession(
  id:         Option[Id],                        // 管理 ID
  customerId: Customer.Id,                       // ユーザー ID
  token:      Token,                             // セッショントークン（未署名）
  state:      Status        = Status.IS_ACTIVE,  // セッション状態
  expiresAt:  LocalDateTime = Now.plusDays(30),  // 有効期限
  updatedAt:  LocalDateTime = Now,               // データ更新日
  createdAt:  LocalDateTime = Now                // データ作成日
) extends EntityModel[Id]

/**
 * 顧客セッション: 付随する型と処理の定義
 */
object CustomerSession:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerSession]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerSession]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * セッション状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED extends Status(code = -1) // 無効化: ログアウト済み
    case IS_ACTIVE extends Status(code =  1) // 有効
```

## `CustomerStampCard`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*
import edu.common.model.StampCard

/**
 * スタンプカード
 *
 * 押されたスタンプは [[CustomerStampCardItem]] の行で、たまった数はその件数。
 * `issuedCoupon*` は引き換えの記録。
 */
import CustomerStampCard.*
case class CustomerStampCard(
  id:             Option[Id],                // 保有Id
  cardId:         StampCard.Id,              // 台帳Id
  customerId:     Customer.Id,               // 顧客Id
  issuedCouponId: Option[CustomerCoupon.Id], // 発行: クーポンId
  issuedCouponAt: Option[LocalDateTime],     // 発行: 日時
  state:          Status,                    // 保有状態
  updatedAt:      LocalDateTime = Now,       // データ更新日
  createdAt:      LocalDateTime = Now        // データ作成日
) extends EntityModel[Id]

/**
 * 保有スタンプカード: 付随する型と処理の定義
 */
object CustomerStampCard:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerStampCard]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerStampCard]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 保有状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_COLLECTING extends Status(code = 1) // 集め中
    case IS_EXCHANGED  extends Status(code = 2) // 引換済: クーポンを発行した

  // --[ Extensions ]--------------------------------------------------
  /**
   * 保有スタンプカード: 変数値だけで完結する処理
   */
  extension (self: CustomerStampCard)

    /**
     * その日にまだスタンプを押せるカードか。失効は状態ではなく台帳の日付で判定する
     */
    def isCollectingOn(date: LocalDate, ledger: StampCard): Boolean =
      self.state == Status.IS_COLLECTING && ledger.dateEnd.forall(!date.isAfter(_))
```

## `CustomerStampCardItem`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.customer.model

import ixias.core.model.*

import edu.common.model.StampCard
import edu.shop.model.Payment

/**
 * スタンプ明細: カードに押されたスタンプ 1 個。
 * `cardId` は本部の台帳、`customerCardId` は会員が保有するカード。
 */
import CustomerStampCardItem.*
case class CustomerStampCardItem(
  id:             Option[Id],           // 管理Id
  cardId:         StampCard.Id,         // 台帳Id
  customerId:     Customer.Id,          // 顧客Id
  customerCardId: CustomerStampCard.Id, // 顧客: 保有カードId
  paymentId:      Payment.Id,           // どの会計で押されたか
  updatedAt:      LocalDateTime = Now,  // データ更新日
  createdAt:      LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * スタンプ明細: 付随する型と処理の定義
 */
object CustomerStampCardItem:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomerStampCardItem]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomerStampCardItem]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

---

# `edu.shop`

## `CustomMenu`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.SalesTemplateMenu

/**
 * 店舗独自メニュー: 店舗が自分で立てるタブ。
 *
 * [[SalesTemplateMenu]] とほぼ同じ形だが、テンプレートに属さないので
 * `templateId` を持たない。販売期間の 2 つの枠の扱いは本部メニューと同じ。
 */
import CustomMenu.*
case class CustomMenu(
  id:        Option[Id],                // メニューId
  shopId:    Shop.Id,                   // 店舗Id
  name:      String,                    // メニュー名 (例: 当店限定)
  dateStart: Option[LocalDate],         // 販売日: 開始
  dateEnd:   Option[LocalDate],         // 販売日: 終了
  timeStart: Option[LocalTime],         // 販売時間: 開始
  timeOpen:  Option[Duration],          // 販売時間: 長さ。深夜またぎもこれで表す
  state:     SalesTemplateMenu.Status,  // 公開状態
  sortOrder: Short,                     // タブの表示順
  updatedAt: LocalDateTime = Now,       // データ更新日
  createdAt: LocalDateTime = Now        // データ作成日
) extends EntityModel[Id]

/**
 * 店舗独自メニュー: 付随する型と処理の定義
 */
object CustomMenu:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomMenu]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

## `CustomMenuItem`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.Product

/**
 * 店舗独自メニューの表示アイテム
 * 店舗独自タブに並ぶ商品。
 *
 * 本部商品と店舗独自商品のどちらも載るため参照を 2 つ持ち、
 * どちらか一方だけがSome になる
 *  - `productId`       … 本部商品（[[Product]]）
 *  - `productCustomId` … 店舗独自商品（[[CustomProduct]]）
 */
import CustomMenuItem.*
case class CustomMenuItem(
  id:              Option[Id],               // 管理Id
  shopId:          Shop.Id,                  // 店舗Id
  menuId:          CustomMenu.Id,            // メニューId
  productId:       Option[Product.Id],       // 商品: 本部商品Id
  productCustomId: Option[CustomProduct.Id], // 商品: 店舗独自商品Id
  sortOrder:       Short,                    // 表示順
  updatedAt:       LocalDateTime = Now,      // データ更新日
  createdAt:       LocalDateTime = Now       // データ作成日
) extends EntityModel[Id]

/**
 * 店舗独自メニューの表示アイテム: 付随する型と処理の定義
 */
object CustomMenuItem:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomMenuItem]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomMenuItem]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

## `CustomProduct`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.Product

/**
 * 店舗独自商品: その店舗だけで販売する商品。
 */
import CustomProduct.*
case class CustomProduct(
  id:          Option[Id],           // 商品Id
  shopId:      Shop.Id,              // 店舗Id
  name:        String,               // 商品名
  category:    Product.Category,     // 商品: カテゴリ
  price:       Int,                  // 価格
  state:       Product.Status,       // 販売状態
  description: String,               // 説明文
  updatedAt:   LocalDateTime = Now,  // データ更新日
  createdAt:   LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * 店舗独自商品: 付随する型と処理の定義
 */
object CustomProduct:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, CustomProduct]
  type EmbeddedId = Entity.EmbeddedId[Id, CustomProduct]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

## `ExcludedMenu`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.{ SalesTemplate, SalesTemplateMenu }

/**
 * 除外メニュー: この店舗が運用しないメニューの登録。
 * `templateId` は登録した時点の販売テンプレート。
 */
import ExcludedMenu.*
case class ExcludedMenu(
  id:             Option[Id],           // 管理Id
  shopId:         Shop.Id,              // 店舗Id
  templateId:     SalesTemplate.Id,     // 販売テンプレートId
  templateMenuId: SalesTemplateMenu.Id, // メニューId
  note:           Option[String],       // 理由の覚書。判定には使わない
  updatedAt:      LocalDateTime = Now,  // データ更新日
  createdAt:      LocalDateTime = Now   // データ作成日
) extends EntityModel[Id]

/**
 * 除外メニュー: 付随する型と処理の定義
 */
object ExcludedMenu:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ExcludedMenu]
  type EmbeddedId = Entity.EmbeddedId[Id, ExcludedMenu]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

## `ExcludedProduct`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.Product

/**
 * 除外商品: この店舗が提供できない商品の登録。
 * メニューとは独立して除外商品を登録することで、
 * メニューに載っている商品でも除外できる。
 */
import ExcludedProduct.*
case class ExcludedProduct(
  id:        Option[Id],          // 管理Id
  shopId:    Shop.Id,             // 店舗Id
  productId: Product.Id,          // 除外する商品Id
  note:      Option[String],      // 理由の覚書
  updatedAt: LocalDateTime = Now, // データ更新日
  createdAt: LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 除外商品: 付随する型と処理の定義
 */
object ExcludedProduct:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, ExcludedProduct]
  type EmbeddedId = Entity.EmbeddedId[Id, ExcludedProduct]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

## `Order`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.customer.model.Customer

/**
 * 注文: 確定した注文。
 * 金額は持たず、請求額の内訳は [[Payment]] が保持する。
 * `code` は外部に見せる受付番号。
 */
import Order.*
case class Order(
  id:         Option[Id],             // 管理Id
  shopId:     Shop.Id,                // 店舗Id
  customerId: Customer.Id,            // 顧客Id
  code:       Code,                   // 受付番号（公開用の識別子）
  state:      Status,                 // 注文の状態
  pickupAt:   LocalDateTime,          // 受取: 予定時刻
  pickupedAt: Option[LocalDateTime],  // 受取: 完了時刻。IS_HANDED で埋まる
  updatedAt:  LocalDateTime = Now,    // データ更新日
  createdAt:  LocalDateTime = Now     // データ作成日
) extends EntityModel[Id]

/**
 * 注文: 付随する型と処理の定義
 */
object Order:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type Code       = Code.Repr
  type WithNoId   = Entity.WithNoId[Id, Order]
  type EmbeddedId = Entity.EmbeddedId[Id, Order]

  // --[ Opaque Values ]-----------------------------------------------
  object Id   extends Entity.Id[Long]
  object Code extends Entity.Id[String]:
    def generate: Code = Code(java.util.UUID.randomUUID.toString.take(8).toUpperCase)

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 注文の状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CANCELED extends Status(code = -1) // キャンセル
    case IS_ACCEPTED extends Status(code =  1) // 受付
    case IS_COOKING  extends Status(code =  2) // 調理中
    case IS_READY    extends Status(code =  3) // 受取準備完了
    case IS_HANDED   extends Status(code =  4) // 受渡し完了
```

## `OrderItem`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.Product
import edu.customer.model.Customer

/**
 * 注文明細: 確定した注文 1 行。
 * 商品への参照と個数だけを持つ。
 */
import OrderItem.*
case class OrderItem(
  id:         Option[Id],          // 管理Id
  shopId:     Shop.Id,             // 店舗Id
  orderId:    Order.Id,            // オーダーId
  customerId: Customer.Id,         // 顧客Id
  productId:  Product.Id,          // 商品Id
  productNum: Int,                 // 注文数
  updatedAt:  LocalDateTime = Now, // データ更新日
  createdAt:  LocalDateTime = Now  // データ作成日
) extends EntityModel[Id]

/**
 * 注文明細: 付随する型と処理の定義
 */
object OrderItem:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, OrderItem]
  type EmbeddedId = Entity.EmbeddedId[Id, OrderItem]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

## `Payment`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.customer.model.Customer

/**
 * 決済: 注文に対して実際に請求した記録。
 *
 * 金額は 2 つのグループに分かれる。
 *   `bill*` は計算した内訳
 *   `pay*`  は実際に動いた額。
 *
 * {{{
 *   billTaxTotal   = round((billSubTotal - billDiscountTotal) * billTaxRate)
 *   payBilledTotal = billSubTotal - billDiscountTotal + billTaxTotal
 *   0 <= payRefundedTotal <= payBilledTotal
 * }}}
 */
import Payment.*
case class Payment(
  id:                Option[Id],              // 決済Id
  shopId:            Shop.Id,                 // 店舗Id
  orderId:           Order.Id,                // オーダーId
  customerId:        Customer.Id,             // 顧客Id
  method:            Method,                  // 決済手段
  transactionId:     Option[TransactionId],   // 決済トランザクションId
  billSubTotal:      Int,                     // 金額: 税抜合計 (円)
  billDiscountTotal: Int,                     // 金額: 割引合計 (円)
  billTaxRate:       BigDecimal,              // 金額: 消費税率 (0.1000 = 10%)
  billTaxTotal:      Int,                     // 金額: 消費税額 (円)
  payBilledTotal:    Int,                     // 実績: 請求した額 (円)
  payRefundedTotal:  Int,                     // 実績: 返金した額 (円)
  state:             Status,                  // 決済の状態
  note:              Option[String],          // 備考
  completedAt:       Option[LocalDateTime],   // 決済日時
  refundedAt:        Option[LocalDateTime],   // 返金日時
  updatedAt:         LocalDateTime = Now,     // データ更新日
  createdAt:         LocalDateTime = Now      // データ作成日
) extends EntityModel[Id]

/**
 * 決済: 付随する型と処理の定義
 */
object Payment:

  // --[ Type Aliases ]------------------------------------------------
  type Id            = Id.Repr
  type TransactionId = TransactionId.Repr
  type WithNoId      = Entity.WithNoId[Id, Payment]
  type EmbeddedId    = Entity.EmbeddedId[Id, Payment]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  /**
   * 決済サービス側の取引Id
   */
  object TransactionId extends Entity.Id[String]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 決済手段
   */
  enum Method(val code: Short, val name: String) extends EnumStatus[Short]:
    case IS_CASH   extends Method(code = 1, name = "現金")
    case IS_CREDIT extends Method(code = 2, name = "クレジットカード")
    case IS_QR     extends Method(code = 3, name = "QRコード決済")

  /**
   * 決済の状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_REFUNDED  extends Status(code = -3) // 返金済: 完了後に返した
    case IS_CANCELED  extends Status(code = -2) // 取消:   決済前に注文がキャンセルされた
    case IS_FAILED    extends Status(code = -1) // 失敗:   決済サービスが拒否。再試行は新しい行
    case IS_DRAFT     extends Status(code =  0) // 下書き: 決済サービスをまだ呼んでいない
    case IS_PENDING   extends Status(code =  1) // 処理中: 決済サービスの応答待ち
    case IS_COMPLETED extends Status(code =  2) // 完了:   入金確定
```

## `PaymentDiscount`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import edu.common.model.{ Coupon, CouponOffer, Product }
import edu.customer.model.CustomerCoupon

/**
 * 決済割引:
 * 会計で実際に適用した割引 1 行。
 * `coupon*` は割引の出どころ、`discount*` は実際に引いた内容。
 *
 * {{{
 *   discountUnitValue * discountProductNum = discountSubTotal
 *   billDiscountTotal = SUM(discountSubTotal)
 * }}}
 */
import PaymentDiscount.*
case class PaymentDiscount(
  id:                 Option[Id],                 // 管理Id
  paymentId:          Payment.Id,                 // 決済Id
  couponId:           Coupon.Id,                  // クーポンId
  couponOfferId:      Option[CouponOffer.Id],     // クーポン: 配布Id。スタンプ引換なら None
  customerCouponId:   Option[CustomerCoupon.Id],  // 顧客: 所持クーポンId
  discountType:       Coupon.DiscountType,        // 割引: 種別
  discountProductId:  Product.Id,                 // 割引: 対象商品Id
  discountProductNum: Int,                        // 割引: 対象商品個数
  discountUnitValue:  Int,                        // 割引: 単品あたり (円)
  discountSubTotal:   Int,                        // 割引: 小計 (円)
  updatedAt:          LocalDateTime = Now,        // データ更新日
  createdAt:          LocalDateTime = Now         // データ作成日
) extends EntityModel[Id]

/**
 * 決済割引: 付随する型と処理の定義
 */
object PaymentDiscount:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, PaymentDiscount]
  type EmbeddedId = Entity.EmbeddedId[Id, PaymentDiscount]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]
```

## `Shop`

```scala
/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

import ixias.core.model.*
import ixias.core.model.value.PhoneNumber
import edu.common.model.SalesTemplate

/**
 * 店舗: 注文を受け取る実店舗。
 * 曜日ごとの営業時間を「開店時刻 ＋ 営業の長さ」の組で持ち、None は定休日。
 */
import Shop.*
case class Shop(
  id:          Option[Id],                     // 管理Id
  name:        String,                         // 店舗名
  templateId:  SalesTemplate.Id,               // 適用する販売テンプレートId
  openTimeMon: Option[(LocalTime, Duration)],  // 月曜: 開店時刻, 営業時間(h)
  openTimeTue: Option[(LocalTime, Duration)],  // 火曜: 開店時刻, 営業時間(h)
  openTimeWed: Option[(LocalTime, Duration)],  // 水曜: 開店時刻, 営業時間(h)
  openTimeThu: Option[(LocalTime, Duration)],  // 木曜: 開店時刻, 営業時間(h)
  openTimeFri: Option[(LocalTime, Duration)],  // 金曜: 開店時刻, 営業時間(h)
  openTimeSat: Option[(LocalTime, Duration)],  // 土曜: 開店時刻, 営業時間(h)
  openTimeSun: Option[(LocalTime, Duration)],  // 日曜: 開店時刻, 営業時間(h)
  phone:       PhoneNumber,                    // 電話番号
  address:     String,                         // 住所
  state:       Status        = Status.IS_OPEN, // 店舗の状態
  updatedAt:   LocalDateTime = Now,            // データ更新日
  createdAt:   LocalDateTime = Now             // データ作成日
) extends EntityModel[Id]

/**
 * 店舗: 付随する型と処理の定義
 */
object Shop:

  // --[ Type Aliases ]------------------------------------------------
  type Id         = Id.Repr
  type WithNoId   = Entity.WithNoId[Id, Shop]
  type EmbeddedId = Entity.EmbeddedId[Id, Shop]

  // --[ Opaque Values ]-----------------------------------------------
  object Id extends Entity.Id[Long]

  // --[ Value Objects ]-----------------------------------------------
  /**
   * 店舗の状態
   */
  enum Status(val code: Short) extends EnumStatus[Short]:
    case IS_CLOSED    extends Status(code = -1) // 休業中: 長期休業・閉店
    case IS_PREPARING extends Status(code =  0) // 開店準備中: まだ注文を受けない
    case IS_OPEN      extends Status(code =  1) // 営業中
```
