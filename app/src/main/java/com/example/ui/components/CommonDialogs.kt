package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Member
import com.example.data.model.Purchase
import com.example.data.model.TreasuryGroup
import com.example.ui.locale.LocalAppStrings

@Composable
fun AddEditGroupDialog(
    groupToEdit: TreasuryGroup? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: Double, currency: String, academicYear: String, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(groupToEdit?.name ?: "") }
    var targetText by remember { mutableStateOf(groupToEdit?.targetContribution?.toString() ?: "50.0") }
    var currency by remember { mutableStateOf(groupToEdit?.currency ?: if (strings.currencyLabel == "Devise") "DH" else "درهم") }
    var academicYear by remember { mutableStateOf(groupToEdit?.academicYear ?: "2025 - 2026") }
    var notes by remember { mutableStateOf(groupToEdit?.notes ?: "") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (groupToEdit == null) strings.dialogAddGroupTitle else strings.dialogEditGroupTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text(strings.groupNameLabel) },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text(strings.groupTargetLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("group_target_input")
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text(strings.currencyLabel) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("group_currency_input")
                    )
                }

                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text(strings.academicYearLabel) },
                    placeholder = { Text("2025 - 2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.groupNotesLabel) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        val target = targetText.toDoubleOrNull() ?: 50.0
                        onConfirm(name, target, currency, academicYear, notes)
                    }
                },
                modifier = Modifier.testTag("save_group_btn")
            ) {
                Text(if (groupToEdit == null) strings.createGroup else strings.saveChanges)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun AddEditMemberDialog(
    memberToEdit: Member? = null,
    defaultTargetContribution: Double = 50.0,
    currency: String = "درهم",
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, paid: Double, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var fullName by remember { mutableStateOf(memberToEdit?.fullName ?: "") }
    var phone by remember { mutableStateOf(memberToEdit?.phone ?: "") }
    var paidText by remember { mutableStateOf(memberToEdit?.paidAmount?.toString() ?: "0.0") }
    var notes by remember { mutableStateOf(memberToEdit?.notes ?: "") }
    var isNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (memberToEdit == null) strings.dialogAddMemberTitle else strings.dialogEditMemberTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (it.isNotBlank()) isNameError = false
                    },
                    label = { Text(strings.memberNameLabel) },
                    isError = isNameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.memberPhoneLabel) },
                    placeholder = { Text("06XXXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_phone_input")
                )

                OutlinedTextField(
                    value = paidText,
                    onValueChange = { paidText = it },
                    label = { Text("${strings.memberPaidLabel} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Icon(Icons.Default.Money, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_paid_input")
                )

                // Quick buttons to set payment to target or full
                if (defaultTargetContribution > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { paidText = defaultTargetContribution.toString() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${strings.payTargetFull} ($defaultTargetContribution)")
                        }
                        OutlinedButton(
                            onClick = { paidText = "0.0" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(strings.zeroUnpaid)
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.memberNotesLabel) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        isNameError = true
                    } else {
                        val paid = paidText.toDoubleOrNull() ?: 0.0
                        onConfirm(fullName, phone, paid, notes)
                    }
                },
                modifier = Modifier.testTag("save_member_btn")
            ) {
                Text(if (memberToEdit == null) strings.addMemberBtn else strings.updateMemberBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPurchaseDialog(
    purchaseToEdit: Purchase? = null,
    currency: String = "درهم",
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: String, notes: String) -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf(purchaseToEdit?.title ?: "") }
    var amountText by remember { mutableStateOf(purchaseToEdit?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(purchaseToEdit?.category ?: strings.categorySchoolSupplies) }
    var notes by remember { mutableStateOf(purchaseToEdit?.buyerNotes ?: "") }
    var isTitleError by remember { mutableStateOf(false) }
    var isAmountError by remember { mutableStateOf(false) }

    val categories = listOf(
        strings.categorySchoolSupplies,
        strings.categoryNotebooks,
        strings.categoryPrinting,
        strings.categoryChalkMarkers,
        strings.categoryClassEquipment,
        strings.categoryActivities,
        strings.categoryOther
    )
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (purchaseToEdit == null) strings.dialogAddPurchaseTitle else strings.dialogEditPurchaseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text(strings.purchaseItemTitleLabel) },
                    isError = isTitleError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("purchase_title_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        if (it.isNotBlank()) isAmountError = false
                    },
                    label = { Text("${strings.purchaseAmountLabel} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isAmountError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("purchase_amount_input")
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.purchaseCategoryLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.purchaseNotesLabel) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (title.isBlank()) {
                        isTitleError = true
                    } else if (amount == null || amount <= 0) {
                        isAmountError = true
                    } else {
                        onConfirm(title, amount, category, notes)
                    }
                },
                modifier = Modifier.testTag("save_purchase_btn")
            ) {
                Text(if (purchaseToEdit == null) strings.recordAndDeductBtn else strings.saveChanges)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun QuickAddCashDialog(
    memberName: String,
    currentPaid: Double,
    currency: String,
    onDismiss: () -> Unit,
    onAddCash: (additionalAmount: Double) -> Unit
) {
    val strings = LocalAppStrings.current
    var customAmountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${strings.quickAddCashTitle}: $memberName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${strings.currentPaidLabel}: $currentPaid $currency",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = strings.pickQuickAmount,
                    style = MaterialTheme.typography.labelLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10.0, 20.0, 50.0).forEach { quickVal ->
                        OutlinedButton(
                            onClick = { onAddCash(quickVal) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+$quickVal")
                        }
                    }
                }

                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { customAmountText = it },
                    label = { Text("${strings.orEnterCustomAmount} ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val addAmount = customAmountText.toDoubleOrNull()
                    if (addAmount != null && addAmount > 0) {
                        onAddCash(addAmount)
                    }
                },
                enabled = customAmountText.toDoubleOrNull() != null && (customAmountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(strings.confirmAddition)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(strings.deleteConfirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
