# DayDone R8/ProGuard 규칙
# release 에서 코드 축소를 켤 때 적용된다 (isMinifyEnabled = true).

# 크래시 스택트레이스를 읽을 수 있게 (Play Console에 mapping.txt 업로드와 함께)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- 문자열 → enum 복원(valueOf)을 쓰는 곳 보호 ---
# ExpenseType, ScheduledDeductionType, NoSpendMode, FutureExpenseCategory,
# FutureExpenseRepeat 등을 Room/DataStore에 저장된 문자열에서 되살린다.
-keepclassmembers enum com.jsworld.android.daydone.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Room: 생성 코드가 엔티티를 참조 ---
-keep class com.jsworld.android.daydone.data.local.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# 공지사항 JSON은 org.json으로 키를 직접 읽어 리플렉션이 없다 → 별도 규칙 불필요.
# Hilt / Compose / Coroutines / DataStore / Glance 는 각 라이브러리가
# consumer proguard 규칙을 포함하므로 추가 설정이 필요하지 않다.
