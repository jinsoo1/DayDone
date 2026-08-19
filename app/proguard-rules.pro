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
# Hilt / Compose / Coroutines / DataStore 는 각 라이브러리가 consumer 규칙을 포함한다.

# --- 홈 위젯 (Glance) ---
# glance-appwidget 이 제공하는 consumer 규칙은 proto 관련 한 줄뿐이라 위젯 코드는 보호되지 않는다.
# 리시버는 매니페스트 참조라 자동으로 남지만, 위젯 본체와 상태 로더는 리시버에서만
# 참조되므로 R8 판단에 맡기지 않고 통째로 지킨다(클래스 몇 개라 용량 영향 없음).
# ⚠️ 릴리즈에서 위젯이 "로드 중 문제 발생"으로 뜨는 사고를 막기 위한 규칙 — 지우지 말 것.
-keep class com.jsworld.android.daydone.widget.** { *; }

# Hilt EntryPoint 로 위젯이 UseCase 를 꺼낸다 (일반 주입 지점이 아님)
-keep @dagger.hilt.EntryPoint interface * { *; }

# --- WorkManager 리플렉션 (Glance 가 SessionWorker 로 위젯을 합성한다) ---
# ⚠️ 실제로 겪은 버그: 릴리즈에서 위젯이 로딩 화면에서 멈춤.
#   WorkManager 는 InputMerger 를 이름으로 찾아 newInstance() 로 만드는데,
#   라이브러리 규칙(-keep class * extends androidx.work.InputMerger)은 클래스만 남기고
#   기본 생성자는 지켜주지 않아 R8 이 제거한다 → "has no zero argument constructor" →
#   워커가 시작을 못 하고 → Glance 합성이 영영 끝나지 않는다(예외가 아니라 멈춤).
#   work-runtime 2.9.0 도 규칙이 같으므로 버전을 올려도 해결되지 않는다.
-keep class * extends androidx.work.InputMerger { <init>(); }
-keep class androidx.work.OverwritingInputMerger { <init>(); }

# 워커도 (Context, WorkerParameters) 생성자로 리플렉션 생성된다
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
