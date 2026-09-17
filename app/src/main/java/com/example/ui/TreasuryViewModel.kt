package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Member
import com.example.data.model.Purchase
import com.example.data.model.TreasuryGroup
import com.example.data.repository.TreasuryRepository
import com.example.ui.locale.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MemberPaymentFilter {
    ALL,
    PAID_FULL,
    PARTIAL,
    UNPAID
}

data class MemberCalculatedInfo(
    val member: Member,
    val shareOfExpenses: Double,
    val netBalance: Double, // paidAmount - shareOfExpenses (+ is surplus, - is deficit)
    val remainingToTarget: Double, // target - paidAmount (if > 0, still owes to target)
    val isTargetMet: Boolean
)

data class TreasurySummary(
    val totalCollected: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val remainingBalance: Double = 0.0,
    val totalMembers: Int = 0,
    val expensePerMember: Double = 0.0,
    val totalTargetExpected: Double = 0.0,
    val targetCollectionPercentage: Float = 0f,
    val paidMembersCount: Int = 0,
    val unpaidMembersCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class TreasuryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TreasuryRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = TreasuryRepository(db.treasuryDao())
    }

    val allGroups: StateFlow<List<TreasuryGroup>> = repository.allGroups.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupId: StateFlow<Long?> = _selectedGroupId.asStateFlow()

    // Language state (Arabic / French)
    private val _currentLanguage = MutableStateFlow(AppLanguage.AR)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Query & Filters
    private val _memberSearchQuery = MutableStateFlow("")
    val memberSearchQuery: StateFlow<String> = _memberSearchQuery.asStateFlow()

    private val _memberFilter = MutableStateFlow(MemberPaymentFilter.ALL)
    val memberFilter: StateFlow<MemberPaymentFilter> = _memberFilter.asStateFlow()

    private val _purchaseCategoryFilter = MutableStateFlow("الكل")
    val purchaseCategoryFilter: StateFlow<String> = _purchaseCategoryFilter.asStateFlow()

    // Current Group Flow
    val currentGroup: StateFlow<TreasuryGroup?> = _selectedGroupId.flatMapLatest { groupId ->
        if (groupId != null) {
            repository.getGroupById(groupId)
        } else {
            // Pick first group if not selected yet
            allGroups.flatMapLatest { groups ->
                if (groups.isNotEmpty()) {
                    _selectedGroupId.value = groups.first().id
                    repository.getGroupById(groups.first().id)
                } else {
                    flowOf(null)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Members Flow
    val rawMembers: StateFlow<List<Member>> = _selectedGroupId.flatMapLatest { groupId ->
        if (groupId != null) {
            repository.getMembersByGroup(groupId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Purchases Flow
    val rawPurchases: StateFlow<List<Purchase>> = _selectedGroupId.flatMapLatest { groupId ->
        if (groupId != null) {
            repository.getPurchasesByGroup(groupId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary calculation
    val summary: StateFlow<TreasurySummary> = combine(
        currentGroup,
        rawMembers,
        rawPurchases
    ) { group, members, purchases ->
        val totalCollected = members.sumOf { it.paidAmount }
        val totalExpenses = purchases.sumOf { it.amount }
        val remaining = totalCollected - totalExpenses
        val count = members.size
        val expensePerMember = if (count > 0) totalExpenses / count else 0.0
        val target = (group?.targetContribution ?: 0.0) * count
        val percentage = if (target > 0) (totalCollected / target).toFloat().coerceIn(0f, 1f) else 1f
        val paidCount = members.count { group != null && it.paidAmount >= group.targetContribution && group.targetContribution > 0 }
        val unpaidCount = members.count { it.paidAmount <= 0.0 }

        TreasurySummary(
            totalCollected = totalCollected,
            totalExpenses = totalExpenses,
            remainingBalance = remaining,
            totalMembers = count,
            expensePerMember = expensePerMember,
            totalTargetExpected = target,
            targetCollectionPercentage = percentage,
            paidMembersCount = paidCount,
            unpaidMembersCount = unpaidCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TreasurySummary()
    )

    // Filtered & Calculated Members
    val membersWithCalculation: StateFlow<List<MemberCalculatedInfo>> = combine(
        rawMembers,
        rawPurchases,
        currentGroup,
        memberSearchQuery,
        memberFilter
    ) { members, purchases, group, query, filter ->
        val totalExpenses = purchases.sumOf { it.amount }
        val count = members.size
        val expensePerMember = if (count > 0) totalExpenses / count else 0.0
        val target = group?.targetContribution ?: 0.0

        val mapped = members.map { member ->
            val net = member.paidAmount - expensePerMember
            val rem = (target - member.paidAmount).coerceAtLeast(0.0)
            val isMet = target > 0 && member.paidAmount >= target
            MemberCalculatedInfo(
                member = member,
                shareOfExpenses = expensePerMember,
                netBalance = net,
                remainingToTarget = rem,
                isTargetMet = isMet
            )
        }

        mapped.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.member.fullName.contains(query, ignoreCase = true) ||
                    item.member.phone.contains(query)

            val matchesFilter = when (filter) {
                MemberPaymentFilter.ALL -> true
                MemberPaymentFilter.PAID_FULL -> item.isTargetMet
                MemberPaymentFilter.PARTIAL -> item.member.paidAmount > 0 && !item.isTargetMet
                MemberPaymentFilter.UNPAID -> item.member.paidAmount <= 0.0
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Purchases
    val filteredPurchases: StateFlow<List<Purchase>> = combine(
        rawPurchases,
        purchaseCategoryFilter
    ) { purchases, category ->
        if (category == "الكل") {
            purchases
        } else {
            purchases.filter { it.category == category }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectGroup(groupId: Long) {
        _selectedGroupId.value = groupId
    }

    fun setMemberSearchQuery(query: String) {
        _memberSearchQuery.value = query
    }

    fun setMemberFilter(filter: MemberPaymentFilter) {
        _memberFilter.value = filter
    }

    fun setPurchaseCategoryFilter(category: String) {
        _purchaseCategoryFilter.value = category
    }

    // --- Group Actions ---
    fun createGroup(name: String, targetContribution: Double, currency: String, academicYear: String, notes: String) {
        viewModelScope.launch {
            val newGroup = TreasuryGroup(
                name = name.trim(),
                targetContribution = targetContribution,
                currency = currency.trim().ifBlank { "درهم" },
                academicYear = academicYear.trim(),
                notes = notes.trim()
            )
            val newId = repository.insertGroup(newGroup)
            _selectedGroupId.value = newId
        }
    }

    fun updateGroup(group: TreasuryGroup) {
        viewModelScope.launch {
            repository.updateGroup(group)
        }
    }

    fun deleteGroup(group: TreasuryGroup) {
        viewModelScope.launch {
            repository.deleteGroup(group)
            // reset selection
            _selectedGroupId.value = null
        }
    }

    // --- Member Actions ---
    fun addMember(fullName: String, phone: String, paidAmount: Double, notes: String) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            val member = Member(
                groupId = groupId,
                fullName = fullName.trim(),
                phone = phone.trim(),
                paidAmount = paidAmount.coerceAtLeast(0.0),
                notes = notes.trim()
            )
            repository.insertMember(member)
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun quickAddMoneyToMember(memberId: Long, currentAmount: Double, addition: Double) {
        viewModelScope.launch {
            val newAmount = (currentAmount + addition).coerceAtLeast(0.0)
            repository.updateMemberPaidAmount(memberId, newAmount)
        }
    }

    fun setMemberPaidFullTarget(memberId: Long, targetAmount: Double) {
        viewModelScope.launch {
            repository.updateMemberPaidAmount(memberId, targetAmount)
        }
    }

    // --- Purchase Actions ---
    fun addPurchase(title: String, amount: Double, category: String, buyerNotes: String, dateMillis: Long = System.currentTimeMillis()) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            val purchase = Purchase(
                groupId = groupId,
                title = title.trim(),
                amount = amount.coerceAtLeast(0.0),
                category = category.trim().ifBlank { "أدوات مدرسية" },
                buyerNotes = buyerNotes.trim(),
                dateMillis = dateMillis
            )
            repository.insertPurchase(purchase)
        }
    }

    fun updatePurchase(purchase: Purchase) {
        viewModelScope.launch {
            repository.updatePurchase(purchase)
        }
    }

    fun deletePurchase(purchase: Purchase) {
        viewModelScope.launch {
            repository.deletePurchase(purchase)
        }
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.AR) AppLanguage.FR else AppLanguage.AR
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    // Format Transparency Report for Group / WhatsApp in Arabic or French
    fun generateTransparencyReport(language: AppLanguage = _currentLanguage.value): String {
        val group = currentGroup.value ?: return ""
        val sum = summary.value
        val membersList = rawMembers.value
        val purchasesList = rawPurchases.value
        val isFrench = language == AppLanguage.FR
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", if (isFrench) Locale.FRANCE else Locale("ar"))

        val sb = StringBuilder()
        if (isFrench) {
            sb.append("📋 *Bilan de Caisse de Classe: ${group.name}*\n")
            sb.append("📅 Date: ${dateFormat.format(Date())}\n")
            sb.append("👤 Responsable de la caisse: Trésorier unique désigné\n")
            sb.append("━━━━━━━━━━━━━━━━━━\n")
            sb.append("💰 *Situation financière générale:*\n")
            sb.append("• Total cotisé: ${"%.2f".format(sum.totalCollected)} ${group.currency}\n")
            sb.append("• Total des achats & dépenses: ${"%.2f".format(sum.totalExpenses)} ${group.currency}\n")
            val balancePrefix = if (sum.remainingBalance >= 0) "✅ Solde restant en caisse:" else "⚠️ Déficit en caisse:"
            sb.append("• $balancePrefix ${"%.2f".format(sum.remainingBalance)} ${group.currency}\n")
            sb.append("• Nombre de membres: ${sum.totalMembers}\n")
            sb.append("• Part de chaque membre dans les dépenses: ${"%.2f".format(sum.expensePerMember)} ${group.currency}\n")
            sb.append("━━━━━━━━━━━━━━━━━━\n")

            sb.append("🛒 *Liste des achats effectués (${purchasesList.size}):*\n")
            if (purchasesList.isEmpty()) {
                sb.append("• Aucun achat enregistré pour le moment.\n")
            } else {
                purchasesList.forEachIndexed { index, p ->
                    sb.append("${index + 1}. ${p.title}: ${p.amount} ${group.currency} [${p.category}]\n")
                }
            }
            sb.append("━━━━━━━━━━━━━━━━━━\n")

            sb.append("👥 *État des cotisations par membre:*\n")
            membersList.forEach { m ->
                val status = when {
                    m.paidAmount >= group.targetContribution && group.targetContribution > 0 -> "✅ Payé en totalité"
                    m.paidAmount > 0 -> "🟡 Paiement partiel (${m.paidAmount} ${group.currency})"
                    else -> "❌ Non payé"
                }
                sb.append("• ${m.fullName}: $status\n")
            }
            sb.append("━━━━━━━━━━━━━━━━━━\n")
            sb.append("✨ Merci pour votre confiance et bonne année scolaire.")
        } else {
            sb.append("📋 *تقرير خزينة القسم: ${group.name}*\n")
            sb.append("📅 التاريخ: ${dateFormat.format(Date())}\n")
            sb.append("👤 المسؤول عن الخزينة: أمين الصندوق المكلف\n")
            sb.append("━━━━━━━━━━━━━━━━━━\n")
            sb.append("💰 *الحالة المالية العامة:*\n")
            sb.append("• إجمالي المحصل: ${"%.2f".format(sum.totalCollected)} ${group.currency}\n")
            sb.append("• إجمالي المشتريات والمصاريف: ${"%.2f".format(sum.totalExpenses)} ${group.currency}\n")
            val balancePrefix = if (sum.remainingBalance >= 0) "✅ رصيد متبقي في الصندوق:" else "⚠️ عجز مالي بالصندوق:"
            sb.append("• $balancePrefix ${"%.2f".format(sum.remainingBalance)} ${group.currency}\n")
            sb.append("• عدد الأعضاء: ${sum.totalMembers}\n")
            sb.append("• نصيب الفرد من المصاريف الفعلية: ${"%.2f".format(sum.expensePerMember)} ${group.currency}\n")
            sb.append("━━━━━━━━━━━━━━━━━━\n")

            sb.append("🛒 *قائمة المشتريات والتجهيزات (${purchasesList.size}):*\n")
            if (purchasesList.isEmpty()) {
                sb.append("• لا توجد مشتريات مسجلة بعد.\n")
            } else {
                purchasesList.forEachIndexed { index, p ->
                    sb.append("${index + 1}. ${p.title}: ${p.amount} ${group.currency} [${p.category}]\n")
                }
            }
            sb.append("━━━━━━━━━━━━━━━━━━\n")

            sb.append("👥 *وضعية مساهمة الأعضاء:*\n")
            membersList.forEach { m ->
                val status = when {
                    m.paidAmount >= group.targetContribution && group.targetContribution > 0 -> "✅ دفع كامل"
                    m.paidAmount > 0 -> "🟡 دفع جزئي (${m.paidAmount} ${group.currency})"
                    else -> "❌ لم يدفع بعد"
                }
                sb.append("• ${m.fullName}: $status\n")
            }
            sb.append("━━━━━━━━━━━━━━━━━━\n")
            sb.append("✨ دمتم بخير، نسأل الله التوفيق للجميع.")
        }
        return sb.toString()
    }
}
