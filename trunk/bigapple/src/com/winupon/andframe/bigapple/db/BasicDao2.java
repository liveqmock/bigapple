package com.winupon.andframe.bigapple.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.winupon.andframe.bigapple.db.callback.MapRowMapper;
import com.winupon.andframe.bigapple.db.callback.MultiRowMapper;
import com.winupon.andframe.bigapple.db.callback.SingleRowMapper;
import com.winupon.andframe.bigapple.db.helper.DbUtils;
import com.winupon.andframe.bigapple.db.helper.SqlUtils;
import com.winupon.andframe.bigapple.utils.Validators;
import com.winupon.andframe.bigapple.utils.log.LogUtils;

/**
 * 对原生数据库操作做了一层轻量级封装，主要屏蔽了显式的close操作，并且处理了多线程操作的问题，当然也可以使用原生的API。<br>
 * 应用层可对本实例保持单例，线程安全。
 * 
 * @author xuan
 */
public class BasicDao2 {
    public static boolean DEBUG = false;

    /**
     * 子类小心使用该锁，如果要使用请确保不会死锁，不然后果不堪
     */
    protected final static ReentrantLock lock = new ReentrantLock();// 保证多线程访问数据库的安全，性能有所损失

    /**
     * 获取数据库，对应调用这个一次必须调用close关闭源
     * 
     * @return
     */
    public SQLiteDatabase openSQLiteDatabase() {
        return DBHelper.getInstance().getWritableDatabase();
    }

    /**
     * 使用完后请Close数据库连接，dbHelper的close其实内部就是sqliteDatabase的close，并且源码内部会判断null和open的状态
     */
    public void closeSQLiteDatabase() {
        DBHelper.getInstance().close();
    }

    // ///////////////////////////////////////////////android的sqlite原生api///////////////////////////////////////////
    /**
     * 用原生的sqlite执行语法
     * 
     * @param doGrammar
     */
    protected Object execute(DoGrammarInterface doGrammar) {
        if (null == doGrammar) {
            return null;
        }

        Object retObj = null;
        lock.lock();
        try {
            SQLiteDatabase sqliteDatabase = openSQLiteDatabase();
            retObj = doGrammar.doGrammar(sqliteDatabase);
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeSQLiteDatabase();
            lock.unlock();
        }
        return retObj;
    }

    /**
     * 插入
     * 
     * @param table
     * @param nullColumnHack
     *            当values是空时，指向该字段的设置成null，插入。
     * @param values
     * @return
     */
    protected long insert(String table, String nullColumnHack, ContentValues values) {
        long updateCount = 0;

        lock.lock();
        try {
            updateCount = openSQLiteDatabase().insert(table, nullColumnHack, values);
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeSQLiteDatabase();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 批量插入
     * 
     * @param table
     * @param nullColumnHack
     * @param valuesList
     * @return
     */
    protected long insertBatch(String table, String nullColumnHack, List<ContentValues> valuesList) {
        long updateCount = 0;

        lock.lock();
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase = openSQLiteDatabase();
            sqliteDatabase.beginTransaction();

            for (int i = 0, n = valuesList.size(); i < n; i++) {
                long temp = sqliteDatabase.insert(table, nullColumnHack, valuesList.get(i));
                updateCount += temp;
            }

            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            if (null != sqliteDatabase) {
                sqliteDatabase.endTransaction();
            }

            closeSQLiteDatabase();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 更新
     * 
     * @param table
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    protected int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        int updateCount = 0;

        lock.lock();
        try {
            updateCount = openSQLiteDatabase().update(table, values, whereClause, whereArgs);
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeSQLiteDatabase();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 批量更新
     * 
     * @param table
     * @param valuesList
     * @param whereClause
     * @param whereArgsList
     * @return
     */
    protected int updateBatch(String table, List<ContentValues> valuesList, String whereClause,
            List<String[]> whereArgsList) {
        int updateCount = 0;

        lock.lock();
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase = openSQLiteDatabase();
            sqliteDatabase.beginTransaction();

            for (int i = 0, n = valuesList.size(); i < n; i++) {
                int temp = sqliteDatabase.update(table, valuesList.get(i), whereClause, whereArgsList.get(i));
                updateCount += temp;
            }

            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            if (null != sqliteDatabase) {
                sqliteDatabase.endTransaction();
            }
            closeSQLiteDatabase();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 删除
     * 
     * @param table
     * @param whereClause
     * @param whereArgs
     * @return
     */
    protected int delete(String table, String whereClause, String[] whereArgs) {
        int updateCount = 0;

        lock.lock();
        try {
            updateCount = openSQLiteDatabase().delete(table, whereClause, whereArgs);
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeSQLiteDatabase();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * 批量删除
     * 
     * @param table
     * @param whereClause
     * @param whereArgsList
     * @return
     */
    protected int deleteBatch(String table, String whereClause, List<String[]> whereArgsList) {
        int updateCount = 0;

        lock.lock();
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase = openSQLiteDatabase();
            sqliteDatabase.beginTransaction();

            for (int i = 0, n = whereArgsList.size(); i < n; i++) {
                int temp = sqliteDatabase.delete(table, whereClause, whereArgsList.get(i));
                updateCount += temp;
            }

            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            if (null != sqliteDatabase) {
                sqliteDatabase.endTransaction();
            }
            closeSQLiteDatabase();
            lock.unlock();
        }

        return updateCount;
    }

    /**
     * Sql语句执行方法
     * 
     * @param sql
     */
    protected void execSQL(String sql) {
        lock.lock();
        try {
            debugSql(sql, null);
            openSQLiteDatabase().execSQL(sql);
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeSQLiteDatabase();
            lock.unlock();
        }
    }

    /**
     * Sql语句执行方法
     * 
     * @param sql
     * @param bindArgs
     */
    protected void execSQL(String sql, Object[] bindArgs) {
        lock.lock();
        try {
            debugSql(sql, null);
            openSQLiteDatabase().execSQL(sql, bindArgs);
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeSQLiteDatabase();
            lock.unlock();
        }
    }

    /**
     * 批量操作
     * 
     * @param sql
     * @param bindArgsList
     */
    protected void execSQLBatch(String sql, List<Object[]> bindArgsList) {
        lock.lock();
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase = openSQLiteDatabase();
            sqliteDatabase.beginTransaction();

            for (int i = 0, n = bindArgsList.size(); i < n; i++) {
                Object[] bindArgs = bindArgsList.get(i);
                debugSql(sql, bindArgs);
                sqliteDatabase.execSQL(sql, bindArgs);
            }

            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            if (null != sqliteDatabase) {
                sqliteDatabase.endTransaction();
            }
            closeSQLiteDatabase();
            lock.unlock();
        }
    }

    /**
     * 查询接口
     * 
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param multiRowMapper
     * @return
     */
    protected <T> List<T> query(String table, String[] columns, String selection, String[] selectionArgs,
            String groupBy, String having, String orderBy, MultiRowMapper<T> multiRowMapper) {
        List<T> ret = new ArrayList<T>();

        lock.lock();
        Cursor cursor = null;
        try {
            cursor = openSQLiteDatabase().query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
            int i = 0;
            while (cursor.moveToNext()) {
                T t = multiRowMapper.mapRow(cursor, i);
                ret.add(t);
                i++;
            }
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeCursor(cursor);
            closeSQLiteDatabase();
            lock.unlock();
        }

        return ret;
    }

    /**
     * 查询接口
     * 
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param multiRowMapper
     * @return
     */
    protected <T> List<T> query(String table, String[] columns, String selection, String[] selectionArgs,
            String groupBy, String having, String orderBy, String limit, MultiRowMapper<T> multiRowMapper) {
        List<T> ret = new ArrayList<T>();

        lock.lock();
        Cursor cursor = null;
        try {
            cursor = openSQLiteDatabase().query(table, columns, selection, selectionArgs, groupBy, having, orderBy,
                    limit);
            int i = 0;
            while (cursor.moveToNext()) {
                T t = multiRowMapper.mapRow(cursor, i);
                ret.add(t);
                i++;
            }
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeCursor(cursor);
            closeSQLiteDatabase();
            lock.unlock();
        }

        return ret;
    }

    /**
     * 查询接口
     * 
     * @param distinct
     * @param table
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @param limit
     * @param multiRowMapper
     * @return
     */
    protected <T> List<T> query(boolean distinct, String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having, String orderBy, String limit,
            MultiRowMapper<T> multiRowMapper) {
        List<T> ret = new ArrayList<T>();

        lock.lock();
        Cursor cursor = null;
        try {
            cursor = openSQLiteDatabase().query(distinct, table, columns, selection, selectionArgs, groupBy, having,
                    orderBy, limit);
            int i = 0;
            while (cursor.moveToNext()) {
                T t = multiRowMapper.mapRow(cursor, i);
                ret.add(t);
                i++;
            }
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeCursor(cursor);
            closeSQLiteDatabase();
            lock.unlock();
        }

        return ret;
    }

    // ///////////////////////////////////////////////////以下是兼容keel的写法////////////////////////////////////////
    // ///////////////////////////////////////////////插入或者更新////////////////////////////////////////////////////
    /**
     * 插入或者更新
     * 
     * @param sql
     */
    protected void update(String sql) {
        lock.lock();
        try {
            debugSql(sql, null);
            openSQLiteDatabase().execSQL(sql);
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeSQLiteDatabase();
            lock.unlock();
        }
    }

    /**
     * 插入或者更新，带参
     * 
     * @param sql
     * @param args
     */
    protected void update(String sql, Object[] args) {
        if (null == args) {
            update(sql);
        }
        else {
            lock.lock();
            try {
                debugSql(sql, args);
                openSQLiteDatabase().execSQL(sql, args);
            }
            catch (Exception e) {
                LogUtils.e(e.getMessage(), e);
            }
            finally {
                closeSQLiteDatabase();
                lock.unlock();
            }
        }
    }

    /**
     * 插入或者更新，批量
     * 
     * @param sql
     * @param argsList
     */
    protected void updateBatch(String sql, List<Object[]> argsList) {
        if (null == argsList) {
            return;
        }

        lock.lock();
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase = openSQLiteDatabase();
            sqliteDatabase.beginTransaction();
            for (Object[] args : argsList) {
                debugSql(sql, args);
                sqliteDatabase.execSQL(sql, args);
            }
            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            if (null != sqliteDatabase) {
                sqliteDatabase.endTransaction();
            }
            closeSQLiteDatabase();
            lock.unlock();
        }
    }

    // ///////////////////////////////////////////////查询//////////////////////////////////////////////////////////////
    /**
     * 查询，返回多条记录
     * 
     * @param sql
     * @param args
     * @param multiRowMapper
     * @return
     */
    protected <T> List<T> query(String sql, String[] args, MultiRowMapper<T> multiRowMapper) {
        List<T> ret = new ArrayList<T>();

        lock.lock();
        debugSql(sql, args);
        Cursor cursor = null;
        try {
            cursor = openSQLiteDatabase().rawQuery(sql, args);
            int i = 0;
            while (cursor.moveToNext()) {
                T t = multiRowMapper.mapRow(cursor, i);
                ret.add(t);
                i++;
            }
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeCursor(cursor);
            closeSQLiteDatabase();
            lock.unlock();
        }

        return ret;
    }

    /**
     * 查询，返回单条记录
     * 
     * @param sql
     * @param args
     * @param singleRowMapper
     * @return
     */
    protected <T> T query(String sql, String[] args, SingleRowMapper<T> singleRowMapper) {
        T ret = null;

        lock.lock();
        debugSql(sql, args);
        Cursor cursor = null;
        try {
            cursor = openSQLiteDatabase().rawQuery(sql, args);
            if (cursor.moveToNext()) {
                ret = singleRowMapper.mapRow(cursor);
            }
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeCursor(cursor);
            closeSQLiteDatabase();
            lock.unlock();
        }

        return ret;
    }

    /**
     * 查询，返回MAP集合
     * 
     * @param sql
     * @param args
     * @param singleRowMapper
     * @return
     */
    protected <K, V> Map<K, V> query(String sql, String[] args, MapRowMapper<K, V> mapRowMapper) {
        Map<K, V> ret = new HashMap<K, V>();

        lock.lock();
        debugSql(sql, args);
        Cursor cursor = null;
        try {
            cursor = openSQLiteDatabase().rawQuery(sql, args);
            int i = 0;
            while (cursor.moveToNext()) {
                K k = mapRowMapper.mapRowKey(cursor, i);
                V v = mapRowMapper.mapRowValue(cursor, i);
                ret.put(k, v);
                i++;
            }
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            closeCursor(cursor);
            closeSQLiteDatabase();
            lock.unlock();
        }

        return ret;
    }

    /**
     * IN查询，返回LIST集合
     * 
     * @param sql
     * @param args
     * @param multiRowMapper
     * @return
     */
    protected <T> List<T> queryForInSQL(String prefix, String[] prefixArgs, String[] inArgs, String postfix,
            MultiRowMapper<T> multiRowMapper) {
        if (null == prefixArgs) {
            prefixArgs = new String[0];
        }

        StringBuilder sql = new StringBuilder();
        sql.append(prefix).append(SqlUtils.getInSQL(inArgs.length));

        if (!TextUtils.isEmpty(postfix)) {
            sql.append(postfix);
        }

        String[] args = new String[inArgs.length + prefixArgs.length];

        System.arraycopy(prefixArgs, 0, args, 0, prefixArgs.length);
        System.arraycopy(inArgs, 0, args, prefixArgs.length, inArgs.length);

        return query(sql.toString(), args, multiRowMapper);
    }

    /**
     * IN查询，返回MAP集合
     * 
     * @param sql
     * @param args
     * @param multiRowMapper
     * @return
     */
    protected <K, V> Map<K, V> queryForInSQL(String prefix, String[] prefixArgs, String[] inArgs, String postfix,
            MapRowMapper<K, V> mapRowMapper) {
        if (null == prefixArgs) {
            prefixArgs = new String[0];
        }

        StringBuilder sql = new StringBuilder();
        sql.append(prefix).append(SqlUtils.getInSQL(inArgs.length));

        if (!TextUtils.isEmpty(postfix)) {
            sql.append(postfix);
        }

        String[] args = new String[inArgs.length + prefixArgs.length];

        System.arraycopy(prefixArgs, 0, args, 0, prefixArgs.length);
        System.arraycopy(inArgs, 0, args, prefixArgs.length, inArgs.length);

        return query(sql.toString(), args, mapRowMapper);
    }

    private void debugSql(String sql, Object[] args) {
        if (DEBUG) {
            LogUtils.d(SqlUtils.getSQL(sql, args));
        }
    }

    // 关闭cursor
    private void closeCursor(Cursor cursor) {
        if (null != cursor && !cursor.isClosed()) {
            cursor.close();
        }
    }

    /**
     * 执行语法回调接口
     * 
     * @author xuan
     * @version $Revision: 1.0 $, $Date: 2014-8-18 上午10:35:17 $
     */
    public interface DoGrammarInterface {
        Object doGrammar(SQLiteDatabase sqliteDatabase);
    }

    // ----------------------------------利用反射操作数据API----------------------------------------------------------
    /**
     * 插入数据到数据库，支持批量插入
     * 
     * @param tableName
     *            数据库表名
     * @param entitys
     *            需要插入的数据对象
     * @return
     */
    public long insert(String tableName, Object... entitys) {
        if (Validators.isEmpty(tableName) || Validators.isEmpty(entitys)) {
            return 0;
        }

        lock.lock();
        SQLiteDatabase sqliteDatabase = null;
        long insertCount = 0;
        try {
            sqliteDatabase = openSQLiteDatabase();
            Set<String> columnSet = DbUtils.getTableAllColumns(sqliteDatabase, tableName);

            sqliteDatabase.beginTransaction();
            for (Object entity : entitys) {
                ContentValues values = DbUtils.getWantToInsertValues(entity, columnSet);
                insertCount = sqliteDatabase.insert(tableName, null, values);
            }

            sqliteDatabase.setTransactionSuccessful();
        }
        catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
        finally {
            if (null != sqliteDatabase) {
                sqliteDatabase.endTransaction();
            }

            closeSQLiteDatabase();
            lock.unlock();
        }

        return insertCount;
    }

}
