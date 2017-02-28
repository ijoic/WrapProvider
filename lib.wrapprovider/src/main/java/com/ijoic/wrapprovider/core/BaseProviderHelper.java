package com.ijoic.wrapprovider.core;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ijoic.wrapprovider.TableConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础数据库提供器-帮助器
 *
 * @author ijoic 963505345@qq.com
 * @version 1.0
 */
public abstract class BaseProviderHelper implements ProviderHelper {

  @NonNull
  private TableConfig tableConfig;
  private Map<String, String> projectionMap;

  private SQLiteOpenHelper databaseHelper;

  // match code
  private int baseGenMatchCode;
  private int matchCodeItem;
  private int matchCodeItemSet;

  /**
   * 构造函数
   */
  public BaseProviderHelper() {
    matchCodeItem = genMatchCode();
    matchCodeItemSet = genMatchCode();
    tableConfig = new TableConfig();
    projectionMap = new HashMap<>();
    onInitTableConfig(tableConfig);
    initProjectionMap();
  }

  /**
   * 生成匹配码
   *
   * @return 匹配码
   */
  protected int genMatchCode() {
    int matchCode = baseGenMatchCode;
    ++baseGenMatchCode;
    return matchCode;
  }

  /**
   * 获取数据集匹配码
   *
   * @return 数据集匹配码
   */
  protected int getItemSetMatchCode() {
    return matchCodeItemSet;
  }

  /**
   * 获取数据项匹配码
   *
   * @return 数据项匹配码
   */
  protected int getItemMatchCode() {
    return matchCodeItem;
  }

  /**
   * 初始化数据表配置
   *
   * @param tableConfig 数据表配置
   */
  protected abstract void onInitTableConfig(TableConfig tableConfig);

  /**
   * 获取数据表配置
   *
   * @return 数据表配置
   */
  @NonNull
  protected TableConfig getTableConfig() {
    return tableConfig;
  }

  private void initProjectionMap() {
    Map<String, String> projectionMap = new HashMap<>();
    String[] columns = tableConfig.columns;

    if(columns != null) {
      for (String column : columns) {
        if(!TextUtils.isEmpty(column)) {
          this.projectionMap.put(column, column);
        }
      }
    }
    this.projectionMap = projectionMap;
  }

  @Override
  public void setDatabaseHelper(@NonNull SQLiteOpenHelper databaseHelper) {
    this.databaseHelper = databaseHelper;
  }

  /**
   * 获取数据库帮助器
   *
   * @return 数据库帮助器
   */
  protected SQLiteOpenHelper getDatabaseHelper() {
    return databaseHelper;
  }

  @Override
  public void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + tableConfig.tableName);
    onCreateDatabase(db);
  }

  @Override
  public void initUriMatcher(@NonNull UriMatcher uriMatcher, @NonNull String authority, int matchMask) {
    uriMatcher.addURI(authority, tableConfig.itemSet, matchCodeItemSet | matchMask);
    uriMatcher.addURI(authority, tableConfig.itemSet + "/#", matchCodeItem | matchMask);
  }

  @Override
  public String getMimeType(int matchCode) {
    if (matchCode == matchCodeItemSet) {
      return tableConfig.contentType;
    }
    if (matchCode == matchCodeItem) {
      return tableConfig.contentItemType;
    }
    return null;
  }

  @Override
  public Cursor query(int matchCode, Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    SQLiteOpenHelper databaseHelper = this.databaseHelper;

    if (databaseHelper == null) {
      return null;
    }
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

    if (matchCode == matchCodeItemSet) {
      qb.setTables(tableConfig.tableName);
      qb.setProjectionMap(this.projectionMap);

    } else if (matchCode == matchCodeItem) {
      qb.setTables(tableConfig.tableName);
      qb.setProjectionMap(this.projectionMap);
      qb.appendWhere("_id=" + getRowIdText(uri));

    } else {
      throw new IllegalArgumentException("unkonwn uri: " + uri);
    }

    if(TextUtils.isEmpty(sortOrder)) {
      sortOrder = tableConfig.defaultSortOrder;
    }
    SQLiteDatabase db = databaseHelper.getReadableDatabase();
    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

    if (c != null && context != null) {
      c.setNotificationUri(context.getContentResolver(), uri);
    }
    return c;
  }

  @Override
  public Uri insert(int matchCode, Context context, @NonNull Uri uri, ContentValues values) {
    SQLiteOpenHelper databaseHelper = this.databaseHelper;

    if (databaseHelper == null) {
      return null;
    }
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    long rowId = db.insert(tableConfig.tableName, tableConfig.hackColumn, values);

    if(rowId > 0L) {
      Uri insertedUri = ContentUris.withAppendedId(tableConfig.contentUri, rowId);

      if (context != null) {
        context.getContentResolver().notifyChange(insertedUri, null);
      }
      return insertedUri;

    } else {
      throw new SQLException("failed to insert row into " + uri);
    }
  }

  @Override
  public int delete(int matchCode, Context context, @NonNull Uri uri, String selection, String[] selectionArgs) {
    SQLiteOpenHelper databaseHelper = this.databaseHelper;

    if (databaseHelper == null) {
      return 0;
    }
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    int count;

    if (matchCode == matchCodeItemSet) {
      count = db.delete(tableConfig.tableName, selection, selectionArgs);

    } else if (matchCode == matchCodeItem) {
      count = db.delete(tableConfig.tableName, appendRowId(selection, uri), selectionArgs);

    } else {
      throw new IllegalArgumentException("unkonwn uri: " + uri);
    }

    if (context != null) {
      context.getContentResolver().notifyChange(uri, null);
    }
    return count;
  }

  @Override
  public int update(int matchCode, Context context, @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    SQLiteOpenHelper databaseHelper = this.databaseHelper;

    if (databaseHelper == null) {
      return 0;
    }
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    int count;

    if (matchCode == matchCodeItemSet) {
      count = db.update(tableConfig.tableName, values, selection, selectionArgs);

    } else if (matchCode == matchCodeItem) {
      count = db.update(tableConfig.tableName, values, appendRowId(selection, uri), selectionArgs);

    } else {
      throw new IllegalArgumentException("unkonwn uri: " + uri);
    }

    if (context != null) {
      context.getContentResolver().notifyChange(uri, null);
    }
    return count;
  }

  private static String getRowIdText(@NonNull Uri itemUri) {
    return itemUri.getPathSegments().get(1);
  }

  private static String appendRowId(@Nullable String selection, @NonNull Uri itemUri) {
    return appendRowId(selection, getRowIdText(itemUri));
  }

  private static String appendRowId(@Nullable String selection, @Nullable String rowId) {
    if(selection == null) {
      selection = "";
    } else {
      selection = selection + " AND ";
    }

    selection = selection + "(_id=" + rowId + ")";
    return selection;
  }
}
