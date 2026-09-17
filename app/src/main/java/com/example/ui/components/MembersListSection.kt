package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryAddCheck
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.data.model.TreasuryGroup
import com.example.ui.MemberCalculatedInfo
import com.example.ui.MemberPaymentFilter
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LocalAppStrings
import com.example.ui.locale.LocalCurrentLanguage
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersListSection(
    members: List<MemberCalculatedInfo>,
    group: TreasuryGroup,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: MemberPaymentFilter,
    onFilterChange: (MemberPaymentFilter) -> Unit,
    onEditMember: (Member) -> Unit,
    onDeleteMember: (Member) -> Unit,
    onQuickAddCash: (Member) -> Unit,
    onSetPaidTarget: (Member) -> Unit,
    selectedMemberIds: Set<Long> = emptySet(),
    isSelectionMode: Boolean = false,
    onToggleSelectionMode: () -> Unit = {},
    onToggleMemberSelection: (Long) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onDeselectAll: () -> Unit = {},
    onBatchAddCash: () -> Unit = {},
    onBatchSetPaidFull: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val currentLang = LocalCurrentLanguage.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(strings.searchMemberPlaceholder) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("member_search_input")
        )

        // Filter chips and Selection Mode Toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == MemberPaymentFilter.ALL,
                    onClick = { onFilterChange(MemberPaymentFilter.ALL) },
                    label = { Text(strings.filterAll) },
                    modifier = Modifier.testTag("filter_all_members")
                )
                FilterChip(
                    selected = selectedFilter == MemberPaymentFilter.PAID_FULL,
                    onClick = { onFilterChange(MemberPaymentFilter.PAID_FULL) },
                    label = { Text(strings.filterPaidFull) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreenContainer
                    )
                )
                FilterChip(
                    selected = selectedFilter == MemberPaymentFilter.PARTIAL,
                    onClick = { onFilterChange(MemberPaymentFilter.PARTIAL) },
                    label = { Text(strings.filterPartial) }
                )
                FilterChip(
                    selected = selectedFilter == MemberPaymentFilter.UNPAID,
                    onClick = { onFilterChange(MemberPaymentFilter.UNPAID) },
                    label = { Text(strings.filterUnpaid) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRedContainer
                    )
                )
            }

            // Selection Mode Trigger
            IconButton(
                onClick = onToggleSelectionMode,
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        if (isSelectionMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .testTag("toggle_selection_mode")
            ) {
                Icon(
                    imageVector = if (isSelectionMode) Icons.Default.Close else Icons.Default.LibraryAddCheck,
                    contentDescription = strings.selectMultiple,
                    tint = if (isSelectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Selection Action Bar (when selection mode is ON)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedMemberIds.size} ${strings.selectedCountSuffix}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Select / Deselect All Button
                            val allSelected = members.isNotEmpty() && members.all { selectedMemberIds.contains(it.member.id) }
                            FilledTonalButton(
                                onClick = {
                                    if (allSelected) onDeselectAll() else onSelectAll()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_select_all")
                            ) {
                                Icon(
                                    imageVector = if (allSelected) Icons.Default.Clear else Icons.Default.SelectAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (allSelected) strings.deselectAll else strings.selectAll,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    // Bulk Actions Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Button 1: Add Money to selected
                        Button(
                            onClick = onBatchAddCash,
                            enabled = selectedMemberIds.isNotEmpty(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_batch_add_cash")
                        ) {
                            Icon(Icons.Default.Money, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.batchAddMoneyBtn, style = MaterialTheme.typography.labelSmall)
                        }

                        // Button 2: Pay Target in full for selected
                        if (group.targetContribution > 0) {
                            FilledTonalButton(
                                onClick = onBatchSetPaidFull,
                                enabled = selectedMemberIds.isNotEmpty(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_batch_set_paid_full")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.batchSetPaidFullBtn, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Header info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${strings.membersListTitle} (${members.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${strings.targetLabel}: ${group.targetContribution} ${group.currency}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (members.isEmpty()) {
            EmptyStateCard(
                message = if (searchQuery.isNotBlank()) strings.noMembersMatch else strings.noMembersYet
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                members.forEach { calculatedInfo ->
                    val isSelected = selectedMemberIds.contains(calculatedInfo.member.id)
                    MemberCard(
                        info = calculatedInfo,
                        currency = group.currency,
                        targetContribution = group.targetContribution,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onToggleSelect = { onToggleMemberSelection(calculatedInfo.member.id) },
                        onCallClick = { phone ->
                            dialPhoneNumber(context, phone, strings.dialErrorToast)
                        },
                        onWhatsAppClick = { member ->
                            shareMemberStatusViaWhatsApp(context, member, group, calculatedInfo, currentLang)
                        },
                        onQuickAddCash = { onQuickAddCash(calculatedInfo.member) },
                        onSetPaidTarget = { onSetPaidTarget(calculatedInfo.member) },
                        onEdit = { onEditMember(calculatedInfo.member) },
                        onDelete = { onDeleteMember(calculatedInfo.member) }
                    )
                }
            }
        }
    }
}

@Composable
fun MemberCard(
    info: MemberCalculatedInfo,
    currency: String,
    targetContribution: Double,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onCallClick: (String) -> Unit,
    onWhatsAppClick: (Member) -> Unit,
    onQuickAddCash: () -> Unit,
    onSetPaidTarget: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val member = info.member
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isSelectionMode) { onToggleSelect() }
            .testTag("member_card_${member.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Member top header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox when selection mode is active
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.fullName.take(1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (member.phone.isNotBlank()) {
                        Text(
                            text = member.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Payment Status Badge
                val isFullyPaid = member.paidAmount >= targetContribution && targetContribution > 0
                val isZero = member.paidAmount <= 0.0
                Surface(
                    color = when {
                        isFullyPaid -> IncomeGreenContainer
                        isZero -> ExpenseRedContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${member.paidAmount} $currency",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isFullyPaid -> IncomeGreen
                            isZero -> ExpenseRed
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Options menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "خيارات", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.editMember) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        if (!isFullyPaid && targetContribution > 0) {
                            DropdownMenuItem(
                                text = { Text("${strings.payTargetFull} ($targetContribution $currency)") },
                                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onSetPaidTarget()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(strings.deleteMember, color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Real-time calculation row: Recalculated total vs individual
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${strings.shareOfPurchases}: ${"%.2f".format(info.shareOfExpenses)} $currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Net balance after expenses
                    val hasSurplus = info.netBalance >= 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (hasSurplus) "${strings.surplus}: " else "${strings.deficit}: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasSurplus) IncomeGreen else ExpenseRed
                        )
                        Text(
                            text = "${"%.2f".format(info.netBalance)} $currency",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasSurplus) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }

            if (member.notes.isNotBlank()) {
                Text(
                    text = "📝 ${member.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick + Cash
                FilledTonalButton(
                    onClick = onQuickAddCash,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.quickAddCash, style = MaterialTheme.typography.labelSmall)
                }

                if (member.phone.isNotBlank()) {
                    // Call button
                    IconButton(
                        onClick = { onCallClick(member.phone) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = strings.call,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // WhatsApp share button
                    IconButton(
                        onClick = { onWhatsAppClick(member) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(IncomeGreenContainer, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = strings.whatsapp,
                            tint = IncomeGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun dialPhoneNumber(context: Context, phone: String, errorMsg: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phone.trim()}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
    }
}

private fun shareMemberStatusViaWhatsApp(
    context: Context,
    member: Member,
    group: TreasuryGroup,
    info: MemberCalculatedInfo,
    language: AppLanguage
) {
    val message = if (language == AppLanguage.FR) {
        """
            Bonjour ${member.fullName} 👋
            Message du trésorier de la classe : ${group.name} 📚
            Voici l'état de votre cotisation :
            • Montant versé : ${member.paidAmount} ${group.currency}
            • Objectif fixé : ${group.targetContribution} ${group.currency}
            • Votre part dans les achats à ce jour : ${"%.2f".format(info.shareOfExpenses)} ${group.currency}
            ${if (info.netBalance >= 0) "✅ Solde excédentaire : +${"%.2f".format(info.netBalance)} ${group.currency}" else "⚠️ Reste à régulariser pour les achats : ${"%.2f".format(-info.netBalance)} ${group.currency}"}
            
            Merci pour votre collaboration pour équiper la classe ! ✨
        """.trimIndent()
    } else {
        """
            السلام عليكم ورحمة الله ${member.fullName} 👋
            معك أمين خزينة قسم: ${group.name} 📚
            نود إعلامك بحالة مساهمتك في الصندوق الجماعي:
            • المبلغ المدفوع منك: ${member.paidAmount} ${group.currency}
            • الهدف المحدد للجميع: ${group.targetContribution} ${group.currency}
            • نصيبك من مشتريات وتجهيزات القسم حتى الآن: ${"%.2f".format(info.shareOfExpenses)} ${group.currency}
            ${if (info.netBalance >= 0) "✅ رصيدك ممتاز ولديك فائض تغطية: +${"%.2f".format(info.netBalance)} ${group.currency}" else "⚠️ يرجى التكرم بتسوية المتبقي لتغطية المشتريات: ${"%.2f".format(-info.netBalance)} ${group.currency}"}
            
            شكراً لتعاونكم لتجهيز القسم بأفضل شكل! ✨
        """.trimIndent()
    }

    try {
        val cleanPhone = member.phone.replace("+", "").replace(" ", "").replace("-", "")
        val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share"))
    }
}
