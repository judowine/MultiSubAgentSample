# connpass API v2 概要

## 基本情報

### ベースURL
```
https://connpass.com/api/v2/
```

### 認証方式
**APIキー認証**
- HTTPヘッダーに `X-API-Key` を指定
- APIキーの取得方法: connpassにログイン後、APIキー発行ページから取得

### レート制限
```
1リクエスト/秒/APIキー
```

**重要**: EventMeetアプリでは、APIキーなしでの実装を想定（一部エンドポイントは認証不要）

## 利用可能なエンドポイント

| エンドポイント | メソッド | 認証 | 説明 |
|--------------|---------|------|------|
| `/api/v2/events/` | GET | 不要* | イベント検索 |
| `/api/v2/events/{id}/presentations/` | GET | 不要* | イベントの発表資料取得 |
| `/api/v2/groups/` | GET | 不要* | グループ検索 |
| `/api/v2/users/` | GET | 不要* | ユーザー検索 |
| `/api/v2/users/{nickname}/groups/` | GET | 必要 | ユーザーの所属グループ |
| `/api/v2/users/{nickname}/events/` | GET | 必要 | ユーザーの参加イベント |

*認証不要エンドポイントは公開情報のみ取得可能。認証ありの場合はより詳細な情報が取得できる場合がある。

## 共通パラメータ

### ページネーション

すべてのリストエンドポイントで利用可能:

| パラメータ | 型 | デフォルト | 最大 | 説明 |
|-----------|---|----------|------|------|
| `start` | integer | 1 | - | 取得開始位置（1-indexed） |
| `count` | integer | 10 | 100 | 1ページあたりの最大件数 |

### レスポンス形式

すべてのエンドポイントは以下の形式でレスポンスを返す:

```json
{
  "results_start": 1,
  "results_returned": 10,
  "results_available": 150,
  "events": [ ... ],      // または users, groups など
  "message": null          // エラー時にメッセージが入る
}
```

| フィールド | 型 | 説明 |
|-----------|---|------|
| `results_start` | integer | 今回の取得開始位置 |
| `results_returned` | integer | 今回取得した件数 |
| `results_available` | integer | 検索条件に合致する総件数 |
| `message` | string/null | エラーメッセージ（正常時はnull） |

## エラーハンドリング

### HTTPステータスコード

| ステータス | 説明 |
|----------|------|
| 200 | 成功 |
| 400 | リクエストパラメータ不正 |
| 401 | 認証エラー（APIキー不正） |
| 404 | リソースが見つからない |
| 429 | レート制限超過 |
| 500 | サーバーエラー |

### エラーレスポンス例

```json
{
  "message": "Invalid parameter: count must be <= 100"
}
```

## EventMeetアプリでの利用方針

### Phase 1: 基盤構築
- 認証なしでの実装（公開APIのみ使用）
- `/api/v2/events/` - イベント検索
- `/api/v2/users/` - ユーザー検索

### Phase 2: 将来拡張（認証機能追加時）
- APIキー設定機能の実装
- `/api/v2/users/{nickname}/events/` - 参加イベント取得
- `/api/v2/users/{nickname}/groups/` - 所属グループ取得

### 実装時の注意点

1. **レート制限の遵守**
   - 連続リクエストは1秒間隔を空ける
   - リトライ処理にExponential Backoffを実装

2. **キャッシング戦略**
   - イベント情報はRoomデータベースにキャッシュ
   - ユーザー情報もローカルに保存
   - 再取得は手動更新または一定期間経過後

3. **ネットワークエラー処理**
   - タイムアウト設定: 30秒
   - リトライ回数: 最大3回
   - オフライン時はキャッシュデータを表示

4. **パラメータ検証**
   - `count`は1〜100の範囲内
   - `start`は1以上
   - 日付フォーマット: `YYYYMM` or `YYYYMMDD`

## 参考リンク

- [connpass API v2 公式ドキュメント](https://connpass.com/about/api/v2/)
- [OpenAPI仕様](https://connpass.com/about/api/v2/openapi.json)
