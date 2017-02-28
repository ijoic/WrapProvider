package com.ijoic.wrapprovider.core;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 数据库提供器-帮助器
 *
 * @author ijoic 963505345@qq.com
 * @version 1.0
 */
public interface ProviderHelper {

  /**
   * 设置数据库帮助器
   *
   * @param databaseHelper 数据库帮助器
   */
  void setDatabaseHelper(@NonNull SQLiteOpenHelper databaseHelper);

  /**
   * 创建数据库回调
   *
   * @param db 数据库
   */
  void onCreateDatabase(@NonNull SQLiteDatabase db);

  /**
   * 升级数据库回调
   *
   * @param db 数据库
   * @param oldVersion 旧数据库版本号
   * @param newVersion 新数据库版本号
   */
  void onUpgradeDatabase(@NonNull SQLiteDatabase db, int oldVersion, int newVersion);

  /**
   * 初始化URI匹配器
   *
   * <p>初始化时，将实际的匹配码，与匹配码掩码相或。<br/>
   * 示例：<code>uriMatcher.addUri(authority, suffixPattern, matchCode|matchCodeMask)</code>。</p>
   *
   * @param uriMatcher URI匹配器
   * @param authority 授权
   * @param matchCodeMask 匹配码掩码
   */
  void initUriMatcher(@NonNull UriMatcher uriMatcher, @NonNull String authority, int matchCodeMask);

  /**
   * 获取MIME类型
   *
   * @param matchCode 匹配码
   * @return MIME类型
   */
  @Nullable
  String getMimeType(int matchCode);

  /**
   * 数据指针查询
   *
   * @param matchCode 匹配类型码
   * @param context 上下文
   * @param uri 查询URI
   * @param projection 查询数据集
   * @param selection 选择条件
   * @param selectionArgs 选择条件参数
   * @param sortOrder 排序顺序
   * @return 查询结果
   */
  Cursor query(int matchCode, Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

  /**
   * 插入数据
   *
   * @param matchCode 匹配类型码
   * @param context 上下文
   * @param uri 查询URI
   * @param values 插入值
   * @return 插入结果
   */
  Uri insert(int matchCode, Context context, @NonNull Uri uri, ContentValues values);

  /**
   * 删除数据
   *
   * @param matchCode 匹配类型码
   * @param context 上下文
   * @param uri 查询URI
   * @param selection 选择条件
   * @param selectionArgs 选择条件参数
   * @return 删除结果
   */
  int delete(int matchCode, Context context, @NonNull Uri uri, String selection, String[] selectionArgs);

  /**
   * 更新数据
   *
   * @param matchCode 匹配类型码
   * @param context 上下文
   * @param uri 查询URI
   * @param values 插入值
   * @param selection 选择条件
   * @param selectionArgs 选择条件参数
   * @return 更新结果
   */
  int update(int matchCode, Context context, @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs);
}
