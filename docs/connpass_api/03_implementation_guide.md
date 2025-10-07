# EventMeet実装ガイド

## API実装の全体方針

### アーキテクチャマッピング

connpass APIとEventMeetアプリのアーキテクチャマッピング:

```
┌─────────────────────────────────────────────┐
│           /composeApp (Presentation)        │
│  - EventListScreen                          │
│  - UserSearchScreen                         │
│  - EventDetailScreen                        │
└──────────────────┬──────────────────────────┘
                   │ depends on
┌──────────────────▼──────────────────────────┐
│            /shared (Domain)                  │
│  Domain Models:                              │
│  - User (domain model)                       │
│  - Event (domain model)                      │
│  - Encounter (出会い記録)                     │
│  Use Cases:                                  │
│  - GetUserEventsUseCase                      │
│  - SearchUserUseCase                         │
│  - CreateEncounterUseCase                    │
└──────────────────┬──────────────────────────┘
                   │ depends on
┌──────────────────▼──────────────────────────┐
│             /data (Data Layer)               │
│  Repository Interfaces & Implementations:    │
│  - UserRepository                            │
│  - EventRepository                           │
│  - EncounterRepository                       │
│  Data Sources:                               │
│  - ConnpassApiClient (Ktor)                  │
│  - AppDatabase (Room)                        │
│  DTOs:                                       │
│  - EventDto → Event (domain)                 │
│  - UserDto → User (domain)                   │
└─────────────────────────────────────────────┘
```

---

## Phase 1: 基盤構築（Unit-1, Unit-2）

### Unit-1: ローカルDB基盤構築

#### タスク対応表

| タスク | データ層 | 対応するconnpass API |
|-------|---------|-------------------|
| Roomのセットアップ | AppDatabase | - |
| ユーザープロフィールエンティティ | UserProfileEntity | Users API |
| 出会い記録エンティティ | EncounterEntity | - |
| タグエンティティ | TagEntity | - |
| DAOの定義 | UserDao, EventDao, EncounterDao | - |
| データベースマイグレーション設定 | Migration strategies | - |

#### Roomエンティティ設計

```kotlin
// /data/database/entity/UserProfileEntity.kt
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: Int,           // connpass user_id
    val nickname: String,                   // connpass nickname
    val displayName: String,                // connpass display_name
    val profile: String?,                   // connpass profile
    val iconUrl: String?,                   // connpass icon_url
    val twitterScreenName: String?,         // connpass twitter_screen_name
    val githubUsername: String?,            // connpass github_username
    val connpassUrl: String,                // connpass connpass_url
    val cachedAt: Long                      // キャッシュ日時
)

// /data/database/entity/EventEntity.kt
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val eventId: Int,          // connpass event_id
    val title: String,                      // connpass title
    val catch: String?,                     // connpass catch
    val description: String,                // connpass description
    val eventUrl: String,                   // connpass event_url
    val hashTag: String?,                   // connpass hash_tag
    val startedAt: String,                  // connpass started_at (ISO 8601)
    val endedAt: String,                    // connpass ended_at
    val limit: Int?,                        // connpass limit
    val accepted: Int,                      // connpass accepted
    val waiting: Int,                       // connpass waiting
    val updatedAt: String,                  // connpass updated_at
    val ownerId: Int,                       // connpass owner_id
    val ownerNickname: String,              // connpass owner_nickname
    val ownerDisplayName: String,           // connpass owner_display_name
    val place: String?,                     // connpass place
    val address: String?,                   // connpass address
    val lat: String?,                       // connpass lat
    val lon: String?,                       // connpass lon
    val groupId: Int?,                      // connpass group_id
    val groupTitle: String?,                // connpass group_title
    val groupUrl: String?,                  // connpass group_url
    val groupLogo: String?,                 // connpass group_logo
    val cachedAt: Long                      // キャッシュ日時
)

// /data/database/entity/EncounterEntity.kt
@Entity(
    tableName = "encounters",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["eventId"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["metUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId"), Index("metUserId")]
)
data class EncounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Int,                       // どのイベントで
    val metUserId: Int,                     // 誰に会ったか
    val memo: String?,                      // メモ
    val createdAt: Long,                    // 記録作成日時
    val updatedAt: Long                     // 最終更新日時
)

// /data/database/entity/TagEntity.kt
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String                        // タグ名（例: "技術の話", "採用"）
)

// /data/database/entity/EncounterTagCrossRef.kt（多対多関係）
@Entity(
    tableName = "encounter_tag_cross_ref",
    primaryKeys = ["encounterId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = EncounterEntity::class,
            parentColumns = ["id"],
            childColumns = ["encounterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("encounterId"), Index("tagId")]
)
data class EncounterTagCrossRef(
    val encounterId: Long,
    val tagId: Long
)
```

---

### Unit-2: connpass API連携基盤

#### タスク対応表

| タスク | 実装クラス | 対応するAPI |
|-------|----------|-----------|
| Ktor Clientのセットアップ | HttpClientFactory | - |
| connpass APIエンドポイント定義 | ConnpassApiRoutes | 全エンドポイント |
| DTOクラスの作成 | EventDto, UserDto | Events API, Users API |
| APIクライアント実装 | ConnpassApiClient | 全API |
| エラーハンドリング | ApiException, ApiErrorHandler | - |

#### Ktor Client セットアップ

```kotlin
// /data/network/HttpClientFactory.kt
object HttpClientFactory {
    fun create(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }
    }
}
```

#### API Routes定義

```kotlin
// /data/network/ConnpassApiRoutes.kt
object ConnpassApiRoutes {
    private const val BASE_URL = "https://connpass.com/api/v2"

    const val EVENTS = "$BASE_URL/events/"
    const val USERS = "$BASE_URL/users/"
    const val GROUPS = "$BASE_URL/groups/"

    fun userEvents(nickname: String) = "$BASE_URL/users/$nickname/events/"
    fun userGroups(nickname: String) = "$BASE_URL/users/$nickname/groups/"
    fun eventPresentations(eventId: Int) = "$BASE_URL/events/$eventId/presentations/"
}
```

#### DTOクラス

```kotlin
// /data/network/dto/EventsResponseDto.kt
@Serializable
data class EventsResponseDto(
    @SerialName("results_start") val resultsStart: Int,
    @SerialName("results_returned") val resultsReturned: Int,
    @SerialName("results_available") val resultsAvailable: Int,
    val events: List<EventDto>
)

@Serializable
data class EventDto(
    @SerialName("event_id") val eventId: Int,
    val title: String,
    val catch: String? = null,
    val description: String,
    @SerialName("event_url") val eventUrl: String,
    @SerialName("hash_tag") val hashTag: String? = null,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String,
    val limit: Int? = null,
    val accepted: Int,
    val waiting: Int,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("owner_id") val ownerId: Int,
    @SerialName("owner_nickname") val ownerNickname: String,
    @SerialName("owner_display_name") val ownerDisplayName: String,
    val place: String? = null,
    val address: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    @SerialName("group_id") val groupId: Int? = null,
    @SerialName("group_title") val groupTitle: String? = null,
    @SerialName("group_url") val groupUrl: String? = null,
    @SerialName("group_logo") val groupLogo: String? = null,
    val series: SeriesDto? = null
)

@Serializable
data class SeriesDto(
    val id: Int,
    val title: String,
    val url: String
)

// /data/network/dto/UsersResponseDto.kt
@Serializable
data class UsersResponseDto(
    @SerialName("results_start") val resultsStart: Int,
    @SerialName("results_returned") val resultsReturned: Int,
    @SerialName("results_available") val resultsAvailable: Int,
    val users: List<UserDto>
)

@Serializable
data class UserDto(
    @SerialName("user_id") val userId: Int,
    val nickname: String,
    @SerialName("display_name") val displayName: String,
    val profile: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("twitter_screen_name") val twitterScreenName: String? = null,
    @SerialName("github_username") val githubUsername: String? = null,
    @SerialName("connpass_url") val connpassUrl: String
)
```

#### APIクライアント実装

```kotlin
// /data/network/ConnpassApiClient.kt
class ConnpassApiClient(
    private val client: HttpClient = HttpClientFactory.create()
) {
    /**
     * イベント検索
     * @param nickname 参加者のニックネーム
     * @param order ソート順 (1:更新日, 2:開催日, 3:新着順)
     * @param count 取得件数（最大100）
     */
    suspend fun searchEvents(
        nickname: String? = null,
        keyword: String? = null,
        order: Int = 2,
        start: Int = 1,
        count: Int = 20
    ): Result<EventsResponseDto> = runCatching {
        client.get(ConnpassApiRoutes.EVENTS) {
            nickname?.let { parameter("nickname", it) }
            keyword?.let { parameter("keyword", it) }
            parameter("order", order)
            parameter("start", start)
            parameter("count", count)
        }.body()
    }.onFailure { error ->
        // エラーログ記録
        println("API Error: ${error.message}")
    }

    /**
     * ユーザー検索
     * @param nickname ニックネーム（複数指定可: "user1,user2"）
     */
    suspend fun searchUsers(
        nickname: String,
        start: Int = 1,
        count: Int = 20
    ): Result<UsersResponseDto> = runCatching {
        client.get(ConnpassApiRoutes.USERS) {
            parameter("nickname", nickname)
            parameter("start", start)
            parameter("count", count)
        }.body()
    }

    /**
     * レート制限対策: リクエスト間隔を1秒空ける
     */
    suspend fun <T> withRateLimit(block: suspend () -> T): T {
        delay(1000) // 1秒待機
        return block()
    }
}
```

---

## マッパー実装

DTOからドメインモデルへの変換

```kotlin
// /data/mapper/EventMapper.kt
fun EventDto.toDomainModel(): Event {
    return Event(
        eventId = eventId,
        title = title,
        description = description,
        eventUrl = eventUrl,
        startedAt = kotlinx.datetime.Instant.parse(startedAt),
        endedAt = kotlinx.datetime.Instant.parse(endedAt),
        place = place,
        accepted = accepted,
        limit = limit,
        ownerNickname = ownerNickname,
        ownerDisplayName = ownerDisplayName
        // ドメインに必要なフィールドのみマッピング
    )
}

fun EventDto.toEntity(): EventEntity {
    return EventEntity(
        eventId = eventId,
        title = title,
        catch = catch,
        description = description,
        eventUrl = eventUrl,
        hashTag = hashTag,
        startedAt = startedAt,
        endedAt = endedAt,
        limit = limit,
        accepted = accepted,
        waiting = waiting,
        updatedAt = updatedAt,
        ownerId = ownerId,
        ownerNickname = ownerNickname,
        ownerDisplayName = ownerDisplayName,
        place = place,
        address = address,
        lat = lat,
        lon = lon,
        groupId = groupId,
        groupTitle = groupTitle,
        groupUrl = groupUrl,
        groupLogo = groupLogo,
        cachedAt = System.currentTimeMillis()
    )
}

// /data/mapper/UserMapper.kt
fun UserDto.toDomainModel(): User {
    return User(
        userId = userId,
        nickname = nickname,
        displayName = displayName,
        iconUrl = iconUrl
    )
}

fun UserDto.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        userId = userId,
        nickname = nickname,
        displayName = displayName,
        profile = profile,
        iconUrl = iconUrl,
        twitterScreenName = twitterScreenName,
        githubUsername = githubUsername,
        connpassUrl = connpassUrl,
        cachedAt = System.currentTimeMillis()
    )
}
```

---

## リポジトリ実装例

```kotlin
// /data/repository/EventRepositoryImpl.kt
class EventRepositoryImpl(
    private val apiClient: ConnpassApiClient,
    private val eventDao: EventDao
) : EventRepository {

    override suspend fun getEventsByNickname(
        nickname: String,
        forceRefresh: Boolean
    ): Result<List<Event>> {
        // キャッシュ戦略: 強制更新 or キャッシュが古い場合はAPI取得
        if (forceRefresh || isCacheStale(nickname)) {
            return fetchAndCacheEvents(nickname)
        }

        // ローカルキャッシュから取得
        return runCatching {
            eventDao.getEventsByParticipant(nickname)
                .map { it.toDomainModel() }
        }
    }

    private suspend fun fetchAndCacheEvents(nickname: String): Result<List<Event>> {
        return apiClient.searchEvents(nickname = nickname, count = 50)
            .mapCatching { response ->
                // DTOをEntityに変換してキャッシュ
                val entities = response.events.map { it.toEntity() }
                eventDao.insertAll(entities)

                // ドメインモデルに変換して返す
                response.events.map { it.toDomainModel() }
            }
    }

    private fun isCacheStale(nickname: String): Boolean {
        val lastCached = eventDao.getLastCacheTime(nickname)
        val oneHourAgo = System.currentTimeMillis() - 3600_000
        return lastCached < oneHourAgo
    }
}
```

---

## エラーハンドリング戦略

```kotlin
sealed class ApiError : Exception() {
    data class NetworkError(override val message: String) : ApiError()
    data class ServerError(val code: Int, override val message: String) : ApiError()
    data class RateLimitExceeded(override val message: String) : ApiError()
    data class NotFound(override val message: String) : ApiError()
    data class Unknown(override val cause: Throwable?) : ApiError()
}

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Result<T>
): Result<T> {
    return try {
        apiCall()
    } catch (e: Exception) {
        when (e) {
            is kotlinx.io.IOException -> Result.failure(ApiError.NetworkError(e.message ?: "Network error"))
            else -> Result.failure(ApiError.Unknown(e))
        }
    }
}
```

---

## テスト戦略

### Unit-1, Unit-2のテスト

```kotlin
// /data/src/commonTest/kotlin/repository/EventRepositoryImplTest.kt
class EventRepositoryImplTest {
    private lateinit var repository: EventRepositoryImpl
    private lateinit var mockApiClient: ConnpassApiClient
    private lateinit var mockEventDao: EventDao

    @Before
    fun setup() {
        mockApiClient = mockk()
        mockEventDao = mockk()
        repository = EventRepositoryImpl(mockApiClient, mockEventDao)
    }

    @Test
    fun `getEventsByNickname should return cached events when cache is fresh`() = runTest {
        // Arrange
        val nickname = "taro_yamada"
        val cachedEvents = listOf(/* mock events */)
        every { mockEventDao.getEventsByParticipant(nickname) } returns cachedEvents

        // Act
        val result = repository.getEventsByNickname(nickname, forceRefresh = false)

        // Assert
        assertTrue(result.isSuccess)
        verify { mockEventDao.getEventsByParticipant(nickname) }
        verify(exactly = 0) { mockApiClient.searchEvents(any(), any(), any(), any(), any()) }
    }
}
```

---

## 次のステップ

Phase 1の基盤が完成したら:
1. Unit-3でプロフィール機能を実装
2. Unit-4でイベント一覧表示を実装
3. Unit-5でユーザー検索機能を実装

この時点で基本的なconnpass API連携が完成し、EventMeetアプリのコア機能を実装できる状態になります。
