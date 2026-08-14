/*
 * Copyright IxiaS, Inc. All Rights Reserved.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

package edu.shop.model

// ====================================================================
// 決済明細（下書き）
//
// TODO: Payment / Product / CustomProduct が未実装のためコメントアウトしている。
//       3 つが揃ったら以下を有効にする（エディタで一括トグルすれば戻せる）。
// ====================================================================
//
// 決済明細:
// 会計で実際に請求した商品 1 行。
//
// 注文（Order / OrderItem）は受け渡しが済めば役目を終えるが、
// 会計の内訳は残す必要があるため、決済側に注文時点の単価を写して保持する。
//
// 本部商品と店舗独自商品のどちらも載るため参照を 2 つ持ち、
// どちらか一方だけが Some になる。
//  - productId       … 本部商品（Product）
//  - productCustomId … 店舗独自商品（CustomProduct）
//
//   unitPrice * productNum = subTotal
//   billSubTotal = SUM(subTotal)
//
// import ixias.core.model.*
// import edu.common.model.Product
//
// import PaymentItem.*
// case class PaymentItem(
//   id:              Option[Id],               // 管理Id
//   paymentId:       Payment.Id,               // 決済Id
//   productId:       Option[Product.Id],       // 商品: 本部商品Id
//   productCustomId: Option[CustomProduct.Id], // 商品: 店舗独自商品Id
//   productNum:      Int,                      // 個数
//   unitPrice:       Int,                      // 金額: 単価 (円)
//   subTotal:        Int,                      // 金額: 小計 (円)
//   updatedAt:       LocalDateTime = Now,      // データ更新日
//   createdAt:       LocalDateTime = Now       // データ作成日
// ) extends EntityModel[Id]
//
// 決済明細: 付随する型と処理の定義
//
// object PaymentItem:
//
//   // --[ Type Aliases ]------------------------------------------------
//   type Id         = Id.Repr
//   type WithNoId   = Entity.WithNoId[Id, PaymentItem]
//   type EmbeddedId = Entity.EmbeddedId[Id, PaymentItem]
//
//   // --[ Opaque Values ]-----------------------------------------------
//   object Id extends Entity.Id[Long]
