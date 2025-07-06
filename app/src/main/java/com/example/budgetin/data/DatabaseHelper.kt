package com.example.budgetin.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.budgetin.data.model.Category
import com.example.budgetin.data.model.Goal
import com.example.budgetin.data.model.Transaction

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    companion object {
        private const val DATABASE_NAME = "budgetin.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                password TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('Expenses', 'Revenue', 'Assets'))
            )
        """)

        db.execSQL("""
            CREATE TABLE goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                target_amount REAL NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                amount REAL NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('income', 'expense', 'goal')),
                category_id INTEGER,
                goal_id INTEGER,
                FOREIGN KEY (category_id) REFERENCES categories(id),
                FOREIGN KEY (goal_id) REFERENCES goals(id)
            )
        """)
        // Tambahkan kategori default
        insertDefaultCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS transactions")
        db.execSQL("DROP TABLE IF EXISTS goals")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun insertCategory(name: String, type: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("type", type)
        }
        return db.insert("categories", null, values)
    }

    fun getAllCategories(): List<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM categories", null)
        if (cursor.moveToFirst()) {
            do {
                val category = Category(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                )
                list.add(category)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun insertGoal(name: String, targetAmount: Double): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("target_amount", targetAmount)
        }
        return db.insert("goals", null, values)
    }

    fun getAllGoals(): List<Goal> {
        val list = mutableListOf<Goal>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM goals", null)
        if (cursor.moveToFirst()) {
            do {
                val goal = Goal(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    targetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("target_amount"))
                )
                list.add(goal)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAllTransactions(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM transactions", null)
        if (cursor.moveToFirst()) {
            do {
                val tx = Transaction(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    categoryId = cursor.getIntOrNull("category_id"),
                    goalId = cursor.getIntOrNull("goal_id")
                )
                list.add(tx)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun insertTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", transaction.title)
            put("amount", transaction.amount)
            put("date", transaction.date)
            put("type", transaction.type)
            put("category_id", transaction.categoryId)
            put("goal_id", transaction.goalId)
        }
        return db.insert("transactions", null, values)
    }

    private fun Cursor.getIntOrNull(columnName: String): Int? {
        val idx = getColumnIndex(columnName)
        return if (isNull(idx)) null else getInt(idx)
    }

    private fun insertDefaultCategories(db: SQLiteDatabase) {
        val categories = listOf(
            // Expenses
            "Transportasi" to "Expenses",
            "Makanan" to "Expenses",
            "Skincare" to "Expenses",
            "Hobby" to "Expenses",
            "Urgent" to "Expenses",
            "Accesories" to "Expenses",
            // Revenue
            "Gaji" to "Revenue",
            "Untung Jualan" to "Revenue",
            "Bonus" to "Revenue",
            "Pendapatan Lainnya" to "Revenue",
            // Assets
            "Tabungan" to "Assets",
            "Investasi" to "Assets",
            "Properti" to "Assets",
            "Aset Lainnya" to "Assets"
        )
        for ((name, type) in categories) {
            val values = ContentValues().apply {
                put("name", name)
                put("type", type)
            }
            db.insert("categories", null, values)
        }
    }
}