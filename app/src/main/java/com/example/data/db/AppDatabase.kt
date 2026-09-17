package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Member
import com.example.data.model.Purchase
import com.example.data.model.TreasuryGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TreasuryGroup::class, Member::class, Purchase::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun treasuryDao(): TreasuryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "class_treasury_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.treasuryDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: TreasuryDao) {
                // Sample initial class group
                val groupId = dao.insertGroup(
                    TreasuryGroup(
                        name = "قسم الثانية باكالوريا - علوم",
                        targetContribution = 50.0,
                        currency = "درهم",
                        academicYear = "2025 - 2026",
                        notes = "خزينة الفصل المشتركة لتجهيز الأدوات والدفاتر والنسخ"
                    )
                )

                // Initial members
                val members = listOf(
                    Member(groupId = groupId, fullName = "أحمد التازي", phone = "0612345678", paidAmount = 50.0, notes = "دفع كامل"),
                    Member(groupId = groupId, fullName = "فاطمة الزهراء العلمي", phone = "0698765432", paidAmount = 50.0, notes = "دفع كامل"),
                    Member(groupId = groupId, fullName = "يوسف بناني", phone = "0655443322", paidAmount = 30.0, notes = "متبقي 20 درهم"),
                    Member(groupId = groupId, fullName = "مريم المرابط", phone = "0677889900", paidAmount = 50.0, notes = "دفع كامل"),
                    Member(groupId = groupId, fullName = "حمزة الشاوي", phone = "0644332211", paidAmount = 0.0, notes = "وعد بالدفع الاثنين"),
                    Member(groupId = groupId, fullName = "سارة الإدريسي", phone = "0611223344", paidAmount = 50.0, notes = "دفع كامل")
                )
                for (m in members) {
                    dao.insertMember(m)
                }

                // Initial purchases
                val purchases = listOf(
                    Purchase(
                        groupId = groupId,
                        title = "دفاتر جماعية وسجل الغياب",
                        amount = 45.0,
                        category = "دفاتر وكتب",
                        buyerNotes = "تم الشراء من مكتبة النجاح"
                    ),
                    Purchase(
                        groupId = groupId,
                        title = "علبة أقلام سبورة وممسحة مغناطيسية",
                        amount = 35.0,
                        category = "أدوات مدرسية",
                        buyerNotes = "4 ألوان أساسية + ممسحة"
                    ),
                    Purchase(
                        groupId = groupId,
                        title = "طباعة ونسخ تمارين الفروض التجريبية",
                        amount = 40.0,
                        category = "طباعة ونسخ",
                        buyerNotes = "30 نسخة من ملخص مادة الرياضيات"
                    )
                )
                for (p in purchases) {
                    dao.insertPurchase(p)
                }
            }
        }
    }
}
