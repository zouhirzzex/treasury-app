package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Member
import com.example.data.model.Purchase
import com.example.data.model.TreasuryGroup
import com.example.ui.components.AddEditGroupDialog
import com.example.ui.components.AddEditMemberDialog
import com.example.ui.components.AddEditPurchaseDialog
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.MembersListSection
import com.example.ui.components.PurchasesListSection
import com.example.ui.components.QuickAddCashDialog
import com.example.ui.components.TransparencyReportDialog
import com.example.ui.components.TreasuryOverview
import com.example.ui.components.TreasuryTopBar
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LocalAppStrings
import com.example.ui.locale.LocalCurrentLanguage
import com.example.ui.locale.getStrings

@Composable
fun TreasuryMainScreen(
    viewModel: TreasuryViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val strings = remember(currentLanguage) { getStrings(currentLanguage) }
    val layoutDirection = if (currentLanguage == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppStrings provides strings,
        LocalCurrentLanguage provides currentLanguage
    ) {
        val allGroups by viewModel.allGroups.collectAsStateWithLifecycle()
        val currentGroup by viewModel.currentGroup.collectAsStateWithLifecycle()
        val summary by viewModel.summary.collectAsStateWithLifecycle()
        val membersWithCalc by viewModel.membersWithCalculation.collectAsStateWithLifecycle()
        val rawPurchases by viewModel.rawPurchases.collectAsStateWithLifecycle()
        val filteredPurchases by viewModel.filteredPurchases.collectAsStateWithLifecycle()

        val memberSearchQuery by viewModel.memberSearchQuery.collectAsStateWithLifecycle()
        val memberFilter by viewModel.memberFilter.collectAsStateWithLifecycle()
        val purchaseCategoryFilter by viewModel.purchaseCategoryFilter.collectAsStateWithLifecycle()

        var selectedTab by remember { mutableIntStateOf(0) }

        // Dialog states
        var showAddGroupDialog by remember { mutableStateOf(false) }
        var groupToEdit by remember { mutableStateOf<TreasuryGroup?>(null) }
        var groupToDelete by remember { mutableStateOf<TreasuryGroup?>(null) }

        var showAddMemberDialog by remember { mutableStateOf(false) }
        var memberToEdit by remember { mutableStateOf<Member?>(null) }
        var memberToDelete by remember { mutableStateOf<Member?>(null) }

        var showAddPurchaseDialog by remember { mutableStateOf(false) }
        var purchaseToEdit by remember { mutableStateOf<Purchase?>(null) }
        var purchaseToDelete by remember { mutableStateOf<Purchase?>(null) }

        var quickCashMember by remember { mutableStateOf<Member?>(null) }
        var showReportDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TreasuryTopBar(
                    currentGroup = currentGroup,
                    allGroups = allGroups,
                    currentLanguage = currentLanguage,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onSelectGroup = { viewModel.selectGroup(it) },
                    onAddNewGroup = { showAddGroupDialog = true },
                    onEditGroup = { groupToEdit = it },
                    onDeleteGroup = { groupToDelete = it },
                    onShareReport = { showReportDialog = true }
                )
            },
            floatingActionButton = {
                if (currentGroup != null) {
                    FloatingActionButton(
                        onClick = {
                            if (selectedTab == 0) {
                                showAddMemberDialog = true
                            } else {
                                showAddPurchaseDialog = true
                            }
                        },
                        containerColor = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("main_fab")
                    ) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.Add else Icons.Default.ShoppingCart,
                            contentDescription = if (selectedTab == 0) strings.newMember else strings.newPurchase
                        )
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            if (currentGroup == null) {
                // Empty state if no group exists
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = strings.createFirstGroupPrompt,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.createFirstGroupDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddGroupDialog = true },
                            modifier = Modifier.testTag("create_first_group_btn")
                        ) {
                            Text(strings.createFirstGroupBtn)
                        }
                    }
                }
            } else {
                val group = currentGroup!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Overview cards
                    item {
                        TreasuryOverview(
                            group = group,
                            summary = summary,
                            onShareReport = { showReportDialog = true },
                            onAddPurchaseClick = {
                                selectedTab = 1
                                showAddPurchaseDialog = true
                            },
                            onAddMemberClick = {
                                selectedTab = 0
                                showAddMemberDialog = true
                            }
                        )
                    }

                    // Section Tabs
                    item {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        text = "${strings.tabMembers} (${summary.totalMembers})",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                                modifier = Modifier.testTag("tab_members")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        text = "${strings.tabPurchases} (${rawPurchases.size})",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                                modifier = Modifier.testTag("tab_purchases")
                            )
                        }
                    }

                    // Tab Content
                    item {
                        if (selectedTab == 0) {
                            MembersListSection(
                                members = membersWithCalc,
                                group = group,
                                searchQuery = memberSearchQuery,
                                onSearchQueryChange = { viewModel.setMemberSearchQuery(it) },
                                selectedFilter = memberFilter,
                                onFilterChange = { viewModel.setMemberFilter(it) },
                                onEditMember = { memberToEdit = it },
                                onDeleteMember = { memberToDelete = it },
                                onQuickAddCash = { quickCashMember = it },
                                onSetPaidTarget = {
                                    viewModel.setMemberPaidFullTarget(it.id, group.targetContribution)
                                }
                            )
                        } else {
                            PurchasesListSection(
                                purchases = filteredPurchases,
                                group = group,
                                totalExpenses = summary.totalExpenses,
                                selectedCategory = purchaseCategoryFilter,
                                onCategorySelected = { viewModel.setPurchaseCategoryFilter(it) },
                                onAddPurchase = { showAddPurchaseDialog = true },
                                onEditPurchase = { purchaseToEdit = it },
                                onDeletePurchase = { purchaseToDelete = it }
                            )
                        }
                    }

                    // Bottom spacer for FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // --- Dialogs Rendering ---

            // Add New Group Dialog
            if (showAddGroupDialog) {
                AddEditGroupDialog(
                    onDismiss = { showAddGroupDialog = false },
                    onConfirm = { name, target, currency, year, notes ->
                        viewModel.createGroup(name, target, currency, year, notes)
                        showAddGroupDialog = false
                    }
                )
            }

            // Edit Group Dialog
            if (groupToEdit != null) {
                AddEditGroupDialog(
                    groupToEdit = groupToEdit,
                    onDismiss = { groupToEdit = null },
                    onConfirm = { name, target, currency, year, notes ->
                        viewModel.updateGroup(
                            groupToEdit!!.copy(
                                name = name,
                                targetContribution = target,
                                currency = currency,
                                academicYear = year,
                                notes = notes
                            )
                        )
                        groupToEdit = null
                    }
                )
            }

            // Delete Group Dialog
            if (groupToDelete != null) {
                ConfirmDeleteDialog(
                    title = strings.deleteGroupTitle,
                    message = "${strings.deleteGroupConfirmMsg} (${groupToDelete!!.name})",
                    onDismiss = { groupToDelete = null },
                    onConfirm = {
                        viewModel.deleteGroup(groupToDelete!!)
                        groupToDelete = null
                    }
                )
            }

            // Add Member Dialog
            if (showAddMemberDialog && currentGroup != null) {
                AddEditMemberDialog(
                    defaultTargetContribution = currentGroup!!.targetContribution,
                    currency = currentGroup!!.currency,
                    onDismiss = { showAddMemberDialog = false },
                    onConfirm = { name, phone, paid, notes ->
                        viewModel.addMember(name, phone, paid, notes)
                        showAddMemberDialog = false
                    }
                )
            }

            // Edit Member Dialog
            if (memberToEdit != null && currentGroup != null) {
                AddEditMemberDialog(
                    memberToEdit = memberToEdit,
                    defaultTargetContribution = currentGroup!!.targetContribution,
                    currency = currentGroup!!.currency,
                    onDismiss = { memberToEdit = null },
                    onConfirm = { name, phone, paid, notes ->
                        viewModel.updateMember(
                            memberToEdit!!.copy(
                                fullName = name,
                                phone = phone,
                                paidAmount = paid,
                                notes = notes
                            )
                        )
                        memberToEdit = null
                    }
                )
            }

            // Delete Member Dialog
            if (memberToDelete != null) {
                ConfirmDeleteDialog(
                    title = strings.deleteMember,
                    message = "${strings.deleteConfirm}: ${memberToDelete!!.fullName}؟",
                    onDismiss = { memberToDelete = null },
                    onConfirm = {
                        viewModel.deleteMember(memberToDelete!!)
                        memberToDelete = null
                    }
                )
            }

            // Quick Add Cash Dialog
            if (quickCashMember != null && currentGroup != null) {
                QuickAddCashDialog(
                    memberName = quickCashMember!!.fullName,
                    currentPaid = quickCashMember!!.paidAmount,
                    currency = currentGroup!!.currency,
                    onDismiss = { quickCashMember = null },
                    onAddCash = { additional ->
                        viewModel.quickAddMoneyToMember(quickCashMember!!.id, quickCashMember!!.paidAmount, additional)
                        quickCashMember = null
                    }
                )
            }

            // Add Purchase Dialog
            if (showAddPurchaseDialog && currentGroup != null) {
                AddEditPurchaseDialog(
                    currency = currentGroup!!.currency,
                    onDismiss = { showAddPurchaseDialog = false },
                    onConfirm = { title, amount, category, notes ->
                        viewModel.addPurchase(title, amount, category, notes)
                        showAddPurchaseDialog = false
                    }
                )
            }

            // Edit Purchase Dialog
            if (purchaseToEdit != null && currentGroup != null) {
                AddEditPurchaseDialog(
                    purchaseToEdit = purchaseToEdit,
                    currency = currentGroup!!.currency,
                    onDismiss = { purchaseToEdit = null },
                    onConfirm = { title, amount, category, notes ->
                        viewModel.updatePurchase(
                            purchaseToEdit!!.copy(
                                title = title,
                                amount = amount,
                                category = category,
                                buyerNotes = notes
                            )
                        )
                        purchaseToEdit = null
                    }
                )
            }

            // Delete Purchase Dialog
            if (purchaseToDelete != null && currentGroup != null) {
                ConfirmDeleteDialog(
                    title = strings.deletePurchaseTitle,
                    message = "${strings.deletePurchaseConfirmMsg} \"${purchaseToDelete!!.title}\" (${purchaseToDelete!!.amount} ${currentGroup!!.currency})",
                    onDismiss = { purchaseToDelete = null },
                    onConfirm = {
                        viewModel.deletePurchase(purchaseToDelete!!)
                        purchaseToDelete = null
                    }
                )
            }

            // Transparency Report Dialog
            if (showReportDialog) {
                val report = viewModel.generateTransparencyReport(currentLanguage)
                TransparencyReportDialog(
                    reportText = report,
                    onDismiss = { showReportDialog = false }
                )
            }
        }
    }
}
