package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Member
import com.example.data.model.Purchase
import com.example.data.model.TreasuryGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface TreasuryDao {

    // --- Groups ---
    @Query("SELECT * FROM treasury_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<TreasuryGroup>>

    @Query("SELECT * FROM treasury_groups WHERE id = :groupId LIMIT 1")
    fun getGroupById(groupId: Long): Flow<TreasuryGroup?>

    @Query("SELECT * FROM treasury_groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupByIdOnce(groupId: Long): TreasuryGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: TreasuryGroup): Long

    @Update
    suspend fun updateGroup(group: TreasuryGroup)

    @Delete
    suspend fun deleteGroup(group: TreasuryGroup)

    @Query("SELECT COUNT(*) FROM treasury_groups")
    suspend fun getGroupsCount(): Int

    // --- Members ---
    @Query("SELECT * FROM members WHERE groupId = :groupId ORDER BY fullName ASC")
    fun getMembersByGroup(groupId: Long): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("UPDATE members SET paidAmount = :newAmount WHERE id = :memberId")
    suspend fun updateMemberPaidAmount(memberId: Long, newAmount: Double)

    @Query("UPDATE members SET paidAmount = paidAmount + :addition WHERE id IN (:memberIds)")
    suspend fun addAmountToMembers(memberIds: List<Long>, addition: Double)

    @Query("UPDATE members SET paidAmount = :amount WHERE id IN (:memberIds)")
    suspend fun setAmountForMembers(memberIds: List<Long>, amount: Double)

    // --- Purchases ---
    @Query("SELECT * FROM purchases WHERE groupId = :groupId ORDER BY dateMillis DESC")
    fun getPurchasesByGroup(groupId: Long): Flow<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Delete
    suspend fun deletePurchase(purchase: Purchase)
}
