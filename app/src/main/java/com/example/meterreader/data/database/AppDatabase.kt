@Database(
    entities = [User::class, MeterData::class, ReadingData::class, GoalData::class, SupportMessage::class],
    version = 2,  // увеличьте версию
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // ... существующие DAO ...
    abstract fun supportMessageDao(): SupportMessageDao
}
