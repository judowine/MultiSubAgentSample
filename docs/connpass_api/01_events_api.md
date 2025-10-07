# Events API

## エンドポイント

### イベント検索

```
GET https://connpass.com/api/v2/events/
```

**認証**: 不要

**説明**: イベント情報を検索・取得する

## リクエストパラメータ

| パラメータ | 型 | 必須 | 説明 | 例 |
|-----------|---|------|------|---|
| `event_id` | integer/array | ❌ | イベントID（複数指定可、カンマ区切り） | `12345` or `12345,67890` |
| `keyword` | string | ❌ | イベントタイトル・概要のAND検索キーワード | `Kotlin Android` |
| `keyword_or` | string | ❌ | イベントタイトル・概要のOR検索キーワード | `Kotlin,Android` |
| `ym` | integer | ❌ | 年月指定（YYYYMM形式） | `202501` |
| `ymd` | integer | ❌ | 年月日指定（YYYYMMDD形式） | `20250115` |
| `nickname` | string/array | ❌ | 参加者のニックネーム（複数指定可） | `taro_yamada` |
| `owner_nickname` | string/array | ❌ | 管理者のニックネーム（複数指定可） | `event_manager` |
| `group_id` | integer/array | ❌ | グループID（複数指定可） | `123` or `123,456` |
| `prefecture` | string | ❌ | 開催地の都道府県 | `東京都` |
| `order` | integer | ❌ | ソート順 (1:更新日, 2:開催日, 3:新着順) | `2` |
| `start` | integer | ❌ | 取得開始位置（1-indexed） | `1` |
| `count` | integer | ❌ | 取得件数（デフォルト:10, 最大:100） | `20` |

### パラメータ詳細

#### keyword (AND検索)
複数キーワードをスペース区切りで指定すると、すべてのキーワードを含むイベントを検索

```
例: keyword=Kotlin Android
→ "Kotlin" AND "Android" を含むイベント
```

#### keyword_or (OR検索)
複数キーワードをカンマ区切りで指定すると、いずれかのキーワードを含むイベントを検索

```
例: keyword_or=Kotlin,Swift,Flutter
→ "Kotlin" OR "Swift" OR "Flutter" を含むイベント
```

#### ym / ymd (日付検索)
- `ym`: 指定月に開催されるイベント
- `ymd`: 指定日に開催されるイベント

```
例: ym=202501 → 2025年1月開催のイベント
例: ymd=20250115 → 2025年1月15日開催のイベント
```

#### order (ソート順)
| 値 | 説明 |
|---|------|
| 1 | 更新日時順（最近更新されたイベントが先） |
| 2 | 開催日時順（開催日が近いイベントが先） |
| 3 | 新着順（新しく作成されたイベントが先） |

## レスポンス

### 成功時（200 OK）

```json
{
  "results_start": 1,
  "results_returned": 2,
  "results_available": 150,
  "events": [
    {
      "event_id": 12345,
      "title": "Kotlin勉強会 #1",
      "catch": "Kotlinの基礎を学ぼう！",
      "description": "Kotlin初心者向けの勉強会です...",
      "event_url": "https://connpass.com/event/12345/",
      "hash_tag": "kotlin",
      "started_at": "2025-01-15T19:00:00+09:00",
      "ended_at": "2025-01-15T21:00:00+09:00",
      "limit": 30,
      "accepted": 25,
      "waiting": 5,
      "updated_at": "2025-01-10T12:00:00+09:00",
      "owner_id": 123,
      "owner_nickname": "taro_yamada",
      "owner_display_name": "山田太郎",
      "place": "東京都渋谷区...",
      "address": "東京都渋谷区渋谷1-2-3",
      "lat": "35.6594945",
      "lon": "139.7005684",
      "group_id": 456,
      "group_title": "Kotlin勉強会グループ",
      "group_url": "https://kotlin-study.connpass.com/",
      "group_logo": "https://connpass.com/static/...",
      "series": {
        "id": 789,
        "title": "Kotlin勉強会シリーズ",
        "url": "https://kotlin-study.connpass.com/series/789/"
      }
    },
    {
      "event_id": 67890,
      "title": "Android開発ハンズオン",
      ...
    }
  ]
}
```

### イベントオブジェクトフィールド

| フィールド | 型 | null可 | 説明 |
|-----------|---|-------|------|
| `event_id` | integer | ❌ | イベントID（一意） |
| `title` | string | ❌ | イベントタイトル |
| `catch` | string | ✅ | キャッチコピー |
| `description` | string | ❌ | イベント説明（HTML含む） |
| `event_url` | string | ❌ | イベントページURL |
| `hash_tag` | string | ✅ | ハッシュタグ |
| `started_at` | string (ISO 8601) | ❌ | 開始日時 |
| `ended_at` | string (ISO 8601) | ❌ | 終了日時 |
| `limit` | integer | ✅ | 定員（nullの場合は無制限） |
| `accepted` | integer | ❌ | 参加者数 |
| `waiting` | integer | ❌ | 補欠者数 |
| `updated_at` | string (ISO 8601) | ❌ | 更新日時 |
| `owner_id` | integer | ❌ | 管理者ユーザーID |
| `owner_nickname` | string | ❌ | 管理者ニックネーム |
| `owner_display_name` | string | ❌ | 管理者表示名 |
| `place` | string | ✅ | 開催場所名 |
| `address` | string | ✅ | 住所 |
| `lat` | string | ✅ | 緯度 |
| `lon` | string | ✅ | 経度 |
| `group_id` | integer | ✅ | グループID |
| `group_title` | string | ✅ | グループ名 |
| `group_url` | string | ✅ | グループURL |
| `group_logo` | string | ✅ | グループロゴ画像URL |
| `series` | object | ✅ | シリーズ情報 |

#### seriesオブジェクト

| フィールド | 型 | 説明 |
|-----------|---|------|
| `id` | integer | シリーズID |
| `title` | string | シリーズタイトル |
| `url` | string | シリーズURL |

## 使用例

### 例1: 特定ユーザーの参加イベント取得

```
GET https://connpass.com/api/v2/events/?nickname=taro_yamada&order=2&count=20
```

**説明**: ユーザー `taro_yamada` が参加するイベントを開催日順に20件取得

### 例2: キーワード検索

```
GET https://connpass.com/api/v2/events/?keyword=Kotlin%20Android&order=2
```

**説明**: "Kotlin" AND "Android" を含むイベントを開催日順に取得

### 例3: 特定月のイベント取得

```
GET https://connpass.com/api/v2/events/?ym=202501&prefecture=東京都&count=50
```

**説明**: 2025年1月に東京都で開催されるイベントを50件取得

### 例4: 複数ユーザーのイベント取得

```
GET https://connpass.com/api/v2/events/?nickname=taro_yamada,hanako_tanaka&order=2
```

**説明**: `taro_yamada` または `hanako_tanaka` が参加するイベントを取得

## EventMeetアプリでの利用パターン

### US-1: 自分の参加イベント一覧取得

```kotlin
// ユーザーのニックネームで参加イベントを取得
GET /api/v2/events/?nickname={user_nickname}&order=2&count=50

パラメータ:
- nickname: ユーザーが登録したニックネーム
- order: 2 (開催日順)
- count: 50 (一度に多めに取得してキャッシュ)
```

### US-6: ユーザーの活動確認（検索したユーザーの参加イベント）

```kotlin
// 検索したユーザーの参加イベントを取得
GET /api/v2/events/?nickname={searched_user_nickname}&order=2&count=50
```

### US-6: 共通参加イベントの抽出

```kotlin
// 2人のユーザーの共通イベントを見つける
GET /api/v2/events/?nickname={user1},{user2}&order=2&count=100

レスポンスを処理:
- 両方のユーザーが参加しているイベントをフィルタリング
- accepted に両ユーザーが含まれているか確認
```

## エラーケース

### 不正なパラメータ

```json
{
  "message": "Invalid parameter: count must be between 1 and 100"
}
```

### イベントが見つからない

```json
{
  "results_start": 1,
  "results_returned": 0,
  "results_available": 0,
  "events": []
}
```

## Kotlin実装例（Ktor Client）

```kotlin
data class EventsResponse(
    @SerialName("results_start") val resultsStart: Int,
    @SerialName("results_returned") val resultsReturned: Int,
    @SerialName("results_available") val resultsAvailable: Int,
    val events: List<Event>
)

data class Event(
    @SerialName("event_id") val eventId: Int,
    val title: String,
    val catch: String?,
    val description: String,
    @SerialName("event_url") val eventUrl: String,
    @SerialName("hash_tag") val hashTag: String?,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String,
    val limit: Int?,
    val accepted: Int,
    val waiting: Int,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("owner_id") val ownerId: Int,
    @SerialName("owner_nickname") val ownerNickname: String,
    @SerialName("owner_display_name") val ownerDisplayName: String,
    val place: String?,
    val address: String?,
    val lat: String?,
    val lon: String?,
    @SerialName("group_id") val groupId: Int?,
    @SerialName("group_title") val groupTitle: String?,
    @SerialName("group_url") val groupUrl: String?,
    @SerialName("group_logo") val groupLogo: String?,
    val series: Series?
)

data class Series(
    val id: Int,
    val title: String,
    val url: String
)

// API呼び出し例
suspend fun getEventsByNickname(nickname: String, count: Int = 20): EventsResponse {
    return client.get("https://connpass.com/api/v2/events/") {
        parameter("nickname", nickname)
        parameter("order", 2) // 開催日順
        parameter("count", count)
    }.body()
}
```

## 注意事項

1. **日時フォーマット**: すべての日時はISO 8601形式（JST: +09:00）
2. **HTML含有**: `description` フィールドにはHTMLタグが含まれる可能性がある
3. **null値**: 多くのフィールドがnullの可能性があるため、null安全な実装が必要
4. **ページネーション**: `results_available` > `count` の場合は追加取得が必要
5. **レート制限**: 連続リクエストは1秒間隔を空ける
