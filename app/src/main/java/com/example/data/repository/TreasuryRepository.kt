package com.example.data.repository

import com.example.data.db.TreasuryDao
import com.example.data.model.Member
import com.example.data.model.Purchase
import com.example.data.model.TreasuryGroup
import kotlinx.coroutines.flow.Flow

class TreasuryRepository(private val dao: TreasuryDao) {

    val allGroups: Flow<List<TreasuryGroup>> = dao.getAllGroups()

    fun getGroupById(groupId: Long): Flow<TreasuryGroup?> = dao.getGroupById(groupId)

    fun getMembersByGroup(groupId: Long): Flow<List<Member>> = dao.getMembersByGroup(groupId)

    fun getPurchasesByGroup(groupId: Long): Flow<List<Purchase>> = dao.getPurchasesByGroup(groupId)

    suspend fun insertGroup(group: TreasuryGroup): Long = dao.insertGroup(group)

    suspend fun updateGroup(group: TreasuryGroup) = dao.updateGroup(group)

    suspend fun deleteGroup(group: TreasuryGroup) = dao.deleteGroup(group)

    suspend fun insertMember(member: Member): Long = dao.insertMember(member)

    suspend fun updateMember(member: Member) = dao.updateMember(member)

    suspend fun deleteMember(member: Member) = dao.deleteMember(member)

    suspend fun updateMemberPaidAmount(memberId: Long, newAmount: Double) =
        dao.updateMemberPaidAmount(memberId, newAmount)

    suspend fun addAmountToMembers(memberIds: List<Long>, addition: Double) =
        dao.addAmountToMembers(memberIds, addition)

    suspend fun setAmountForMembers(memberIds: List<Long>, amount: Double) =
        dao.setAmountForMembers(memberIds, amount)

    suspend fun insertPurchase(purchase: Purchase): Long = dao.insertPurchase(purchase)

    suspend fun updatePurchase(purchase: Purchase) = dao.updatePurchase(purchase)

    suspend fun deletePurchase(purchase: Purchase) = dao.deletePurchase(purchase)
}
