# Users and Groups API

## ユーザー検索 API

### エンドポイント

```
GET https://connpass.com/api/v2/users/
```

**認証**: 不要

**説明**: ユーザー情報を検索・取得する

### リクエストパラメータ

| パラメータ | 型 | 必須 | 説明 | 例 |
|-----------|---|------|------|---|
| `nickname` | string/array | ❌ | ユーザーのニックネーム（複数指定可、カンマ区切り） | `taro_yamada` or `taro,hanako` |
| `start` | integer | ❌ | 取得開始位置（1-indexed） | `1` |
| `count` | integer | ❌ | 取得件数（デフォルト:10, 最大:100） | `20` |

### レスポンス（成功時: 200 OK）

```json
{
  "results_start": 1,
  "results_returned": 1,
  "results_available": 1,
  "users": [
    {
      "user_id": 123,
      "nickname": "taro_yamada",
      "display_name": "山田太郎",
      "profile": "Kotlinエンジニアです",
      "icon_url": "https://connpass-tokyo.s3.amazonaws.com/user/123/icon.png",
      "twitter_screen_name": "taro_dev",
      "github_username": "taro-yamada",
      "connpass_url": "https://connpass.com/user/taro_yamada/"
    }
  ]
}
```

### ユーザーオブジェクトフィールド

| フィールド | 型 | null可 | 説明 |
|-----------|---|-------|------|
| `user_id` | integer | ❌ | ユーザーID（一意） |
| `nickname` | string | ❌ | ニックネーム（URL用） |
| `display_name` | string | ❌ | 表示名 |
| `profile` | string | ✅ | プロフィール |
| `icon_url` | string | ✅ | アイコン画像URL |
| `twitter_screen_name` | string | ✅ | Twitterアカウント（@なし） |
| `github_username` | string | ✅ | GitHubユーザー名 |
| `connpass_url` | string | ❌ | connpassプロフィールURL |

### 使用例

#### 例1: ニックネームでユーザー検索

```
GET https://connpass.com/api/v2/users/?nickname=taro_yamada
```

#### 例2: 複数ユーザーの一括取得

```
GET https://connpass.com/api/v2/users/?nickname=taro_yamada,hanako_tanaka,jiro_suzuki
```

### EventMeetアプリでの利用パターン

#### US-2: イベントで会った人を検索

```kotlin
// ニックネームやIDでユーザーを検索
GET /api/v2/users/?nickname={search_query}

パラメータ:
- nickname: ユーザーが入力した検索キーワード
```

#### US-6: ユーザー詳細情報の取得

```kotlin
// 特定ユーザーの詳細を取得
GET /api/v2/users/?nickname={user_nickname}
```

---

## グループ検索 API

### エンドポイント

```
GET https://connpass.com/api/v2/groups/
```

**認証**: 不要

**説明**: グループ情報を検索・取得する

### リクエストパラメータ

| パラメータ | 型 | 必須 | 説明 | 例 |
|-----------|---|------|------|---|
| `subdomain` | string/array | ❌ | グループのサブドメイン（複数指定可） | `kotlin-study` or `kotlin-study,android-dev` |
| `start` | integer | ❌ | 取得開始位置（1-indexed） | `1` |
| `count` | integer | ❌ | 取得件数（デフォルト:10, 最大:100） | `20` |

### レスポンス（成功時: 200 OK）

```json
{
  "results_start": 1,
  "results_returned": 1,
  "results_available": 1,
  "groups": [
    {
      "group_id": 456,
      "title": "Kotlin勉強会グループ",
      "subdomain": "kotlin-study",
      "description": "Kotlinの勉強会を定期開催しています",
      "group_url": "https://kotlin-study.connpass.com/",
      "logo": "https://connpass.com/static/img/456/logo.png",
      "member_count": 350
    }
  ]
}
```

### グループオブジェクトフィールド

| フィールド | 型 | null可 | 説明 |
|-----------|---|-------|------|
| `group_id` | integer | ❌ | グループID（一意） |
| `title` | string | ❌ | グループ名 |
| `subdomain` | string | ❌ | サブドメイン |
| `description` | string | ✅ | グループ説明 |
| `group_url` | string | ❌ | グループURL |
| `logo` | string | ✅ | グループロゴ画像URL |
| `member_count` | integer | ❌ | メンバー数 |

### 使用例

```
GET https://connpass.com/api/v2/groups/?subdomain=kotlin-study
```

---

## ユーザーの所属グループ API（認証必要）

### エンドポイント

```
GET https://connpass.com/api/v2/users/{nickname}/groups/
```

**認証**: 必要（APIキー）

**説明**: 特定ユーザーが所属しているグループ一覧を取得

### パスパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|---|------|------|
| `nickname` | string | ✅ | ユーザーのニックネーム |

### クエリパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|---|------|------|
| `start` | integer | ❌ | 取得開始位置（1-indexed） |
| `count` | integer | ❌ | 取得件数（デフォルト:10, 最大:100） |

### リクエスト例

```http
GET https://connpass.com/api/v2/users/taro_yamada/groups/?count=50
X-API-Key: your_api_key_here
```

### レスポンス（成功時: 200 OK）

```json
{
  "results_start": 1,
  "results_returned": 3,
  "results_available": 3,
  "groups": [
    {
      "group_id": 456,
      "title": "Kotlin勉強会グループ",
      "subdomain": "kotlin-study",
      "description": "Kotlinの勉強会を定期開催しています",
      "group_url": "https://kotlin-study.connpass.com/",
      "logo": "https://connpass.com/static/img/456/logo.png",
      "member_count": 350
    },
    {
      "group_id": 789,
      "title": "Android開発者コミュニティ",
      "subdomain": "android-dev",
      ...
    }
  ]
}
```

### EventMeetアプリでの利用

**将来拡張機能（Phase 2）**:
- ユーザーの興味・活動領域を把握
- 共通のグループを見つける

---

## ユーザーの参加イベント API（認証必要）

### エンドポイント

```
GET https://connpass.com/api/v2/users/{nickname}/events/
```

**認証**: 必要（APIキー）

**説明**: 特定ユーザーが参加したイベント一覧を取得

### パスパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|---|------|------|
| `nickname` | string | ✅ | ユーザーのニックネーム |

### クエリパラメータ

| パラメータ | 型 | 必須 | 説明 |
|-----------|---|------|------|
| `order` | integer | ❌ | ソート順 (1:更新日, 2:開催日, 3:新着順) |
| `start` | integer | ❌ | 取得開始位置（1-indexed） |
| `count` | integer | ❌ | 取得件数（デフォルト:10, 最大:100） |

### リクエスト例

```http
GET https://connpass.com/api/v2/users/taro_yamada/events/?order=2&count=50
X-API-Key: your_api_key_here
```

### レスポンス

Events APIと同じ形式のEventオブジェクト配列を返す（詳細は `01_events_api.md` 参照）

```json
{
  "results_start": 1,
  "results_returned": 50,
  "results_available": 150,
  "events": [ ... ]
}
```

### EventMeetアプリでの利用

**Phase 1（認証なし）**:
```kotlin
// 代替手段: Events APIのnicknameパラメータを使用
GET /api/v2/events/?nickname={user_nickname}&order=2
```

**Phase 2（認証あり）**:
```kotlin
// より正確な参加イベント取得
GET /api/v2/users/{nickname}/events/?order=2&count=100
```

---

## Kotlin実装例

### ユーザー検索

```kotlin
data class UsersResponse(
    @SerialName("results_start") val resultsStart: Int,
    @SerialName("results_returned") val resultsReturned: Int,
    @SerialName("results_available") val resultsAvailable: Int,
    val users: List<User>
)

data class User(
    @SerialName("user_id") val userId: Int,
    val nickname: String,
    @SerialName("display_name") val displayName: String,
    val profile: String?,
    @SerialName("icon_url") val iconUrl: String?,
    @SerialName("twitter_screen_name") val twitterScreenName: String?,
    @SerialName("github_username") val githubUsername: String?,
    @SerialName("connpass_url") val connpassUrl: String
)

suspend fun searchUsers(nickname: String): UsersResponse {
    return client.get("https://connpass.com/api/v2/users/") {
        parameter("nickname", nickname)
    }.body()
}
```

### グループ検索

```kotlin
data class GroupsResponse(
    @SerialName("results_start") val resultsStart: Int,
    @SerialName("results_returned") val resultsReturned: Int,
    @SerialName("results_available") val resultsAvailable: Int,
    val groups: List<Group>
)

data class Group(
    @SerialName("group_id") val groupId: Int,
    val title: String,
    val subdomain: String,
    val description: String?,
    @SerialName("group_url") val groupUrl: String,
    val logo: String?,
    @SerialName("member_count") val memberCount: Int
)

suspend fun searchGroups(subdomain: String): GroupsResponse {
    return client.get("https://connpass.com/api/v2/groups/") {
        parameter("subdomain", subdomain)
    }.body()
}
```

---

## エラーハンドリング

### ユーザーが見つからない

```json
{
  "results_start": 1,
  "results_returned": 0,
  "results_available": 0,
  "users": []
}
```

### 認証エラー（401 Unauthorized）

```json
{
  "message": "Invalid API key"
}
```

### 存在しないニックネーム（認証必要なエンドポイント）

```http
HTTP/1.1 404 Not Found

{
  "message": "User not found"
}
```

---

## EventMeetアプリでの実装優先度

### Phase 1（必須）
- ✅ **ユーザー検索API** - 会った人を検索するために必須
- ✅ **Events API（nicknameパラメータ）** - 参加イベント取得の代替手段

### Phase 2（将来拡張）
- ⏳ **ユーザーの参加イベントAPI** - より正確な情報取得（認証必要）
- ⏳ **ユーザーの所属グループAPI** - 興味関心の把握（認証必要）
- ⏳ **グループ検索API** - コミュニティ情報の取得

### 実装方針

1. **Phase 1では認証なしで実装**
   - ユーザー検索: `/api/v2/users/`
   - 参加イベント: `/api/v2/events/?nickname={nickname}`

2. **Phase 2でAPIキー設定機能を追加**
   - 設定画面でAPIキーを登録
   - 認証必要なエンドポイントを有効化
   - より詳細な情報取得が可能に

3. **キャッシュ戦略**
   - ユーザー情報はRoomに保存
   - 検索履歴を保持して再検索を高速化
   - オフライン時も基本情報を表示可能
