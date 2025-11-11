package com.menac1ngmonkeys.monkeyslimit.data.local.seeders

import com.menac1ngmonkeys.monkeyslimit.data.local.dao.MemberItemsDao
import com.menac1ngmonkeys.monkeyslimit.data.local.entity.MemberItems

object MemberItemsSeeder {
    suspend fun seed(
        memberItemsDao: MemberItemsDao,
        splitMembers: Map<Int, Map<String, Int>>,
        splitItems: Map<Int, Map<String, Int>>
    ) {
        if (memberItemsDao.count() > 0) return

        // Split 1: 3 members
        splitMembers.keys.firstOrNull()?.let { s1 ->
            val members = splitMembers[s1] ?: emptyMap()
            val items = splitItems[s1] ?: emptyMap()
            items["Sushi Platter"]?.let { sushiId ->
                listOf("Alice", "Bob", "Charlie").forEach { name ->
                    members[name]?.let { mid ->
                        memberItemsDao.insert(MemberItems(id = 0, memberId = mid, itemId = sushiId, price = 400_000.0, quantity = 1))
                    }
                }
            }
            items["Drinks"]?.let { iid ->
                listOf("Bob", "Charlie").forEach { name ->
                    members[name]?.let { mid ->
                        memberItemsDao.insert(MemberItems(id = 0, memberId = mid, itemId = iid, price = 150_000.0, quantity = 1))
                    }
                }
            }
            items["Dessert"]?.let { iid ->
                members["Alice"]?.let { mid ->
                    memberItemsDao.insert(MemberItems(id = 0, memberId = mid, itemId = iid, price = 240_000.0, quantity = 1))
                }
            }
        }

        // Split 2: 2 members
        splitMembers.keys.drop(1).firstOrNull()?.let { s2 ->
            val members = splitMembers[s2] ?: emptyMap()
            val items = splitItems[s2] ?: emptyMap()
            items["Popcorn"]?.let { iid ->
                listOf("Alice", "Bob").forEach { name ->
                    members[name]?.let { mid ->
                        memberItemsDao.insert(MemberItems(id = 0, memberId = mid, itemId = iid, price = 100_000.0, quantity = 1))
                    }
                }
            }
            items["Soda"]?.let { iid ->
                listOf("Alice", "Bob").forEach { name ->
                    members[name]?.let { mid ->
                        memberItemsDao.insert(MemberItems(id = 0, memberId = mid, itemId = iid, price = 60_000.0, quantity = 1))
                    }
                }
            }
        }
    }
}

