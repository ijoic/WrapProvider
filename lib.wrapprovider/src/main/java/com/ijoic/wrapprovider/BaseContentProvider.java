package com.ijoic.wrapprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ijoic.wrapprovider.core.ProviderHelper;

/**
 * 基础-内容提供器
 *
 * @author ijoic 963505345@qq.com
 * @version 1.0
 */
public abstract class BaseContentProvider extends ContentProvider implements ProviderHelper {

  private DatabaseConfig databaseConfig;
  private UriMatcher uriMatcher;

  private ProviderHelper providerHelper;

  /**
   * 构造函数
   */
  public BaseContentProvider() {
    providerHelper = genProviderHelper();
    databaseConfig = new DatabaseConfig();
    initDatabaseConfig(databaseConfig);
    initUriMatcher();
  }

  /**
   * 生成数据库提供器-帮助器
   *
   * @return 数据库提供器-帮助器
   */
  @NonNull
  protected abstract ProviderHelper genProviderHelper();

  /**
   * 初始化数据库配置
   *
   * @param databaseConfig 数据库配置
   */
  protected abstract void initDatabaseConfig(@NonNull DatabaseConfig databaseConfig);

  @Override
  public boolean onCreate() {
    setDatabaseHelper(
      new SQLiteOpenHelper(getContext(), databaseConfig.databaseName, null, databaseConfig.databaseVersion) {
        public void onCreate(SQLiteDatabase db) {
          BaseContentProvider.this.onCreateDatabase(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
          BaseContentProvider.this.onUpgradeDatabase(db, oldVersion, newVersion);
        }
      }
    );
    return true;
  }

  @Override
  public void setDatabaseHelper(@NonNull SQLiteOpenHelper databaseHelper) {
    providerHelper.setDatabaseHelper(databaseHelper);
  }

  /**
   * 创建数据库回调
   *
   * @param db 数据库
   */
  @Override
  public void onCreateDatabase(@NonNull SQLiteDatabase db) {
    providerHelper.onCreateDatabase(db);
  }

  /**
   * 升级数据库回调
   *
   * @param db 数据库
   * @param oldVersion 旧数据库版本号
   * @param newVersion 新数据库版本号
   */
  @Override
  public void onUpgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
    providerHelper.onUpgradeDatabase(db, oldVersion, newVersion);
  }

  private void initUriMatcher() {
    this.uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    initUriMatcher(uriMatcher, databaseConfig.authority, 0);
  }

  @Override
  public void initUriMatcher(@NonNull UriMatcher uriMatcher, @NonNull String authority, int matchCodeMask) {
    providerHelper.initUriMatcher(uriMatcher, authority, 0);
  }

  @Override
  public String getType(@NonNull Uri uri) {
    int matchCode = uriMatcher.match(uri);
    String mimeType;

    if (matchCode == -1 || TextUtils.isEmpty(mimeType = getMimeType(matchCode))) {
      throw new IllegalArgumentException("unkonwn uri: " + uri);
    }
    return mimeType;
  }

  @Override
  public String getMimeType(int matchCode) {
    return providerHelper.getMimeType(matchCode);
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    int matchCode = uriMatcher.match(uri);

    if (matchCode == -1) {
      return null;
    }
    return query(matchCode, getContext(), uri, projection, selection, selectionArgs, sortOrder);
  }

  @Override
  public Cursor query(int matchCode, Context context, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    return providerHelper.query(matchCode, context, uri, projection, selection, selectionArgs, sortOrder);
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    int matchCode = uriMatcher.match(uri);

    if (matchCode == -1) {
      throw new IllegalArgumentException("unkonwn uri: " + uri);
    }
    return insert(matchCode, getContext(), uri, values);
  }

  @Override
  public Uri insert(int matchCode, Context context, @NonNull Uri uri, ContentValues values) {
    return providerHelper.insert(matchCode, context, uri, values);
  }

  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    int matchCode = uriMatcher.match(uri);

    if (matchCode == -1) {
      throw new IllegalArgumentException("unkonwn uri: " + uri);
    }
    return delete(matchCode, getContext(), uri, selection, selectionArgs);
  }

  @Override
  public int delete(int matchCode, Context context, @NonNull Uri uri, String selection, String[] selectionArgs) {
    return providerHelper.delete(matchCode, context, uri, selection, selectionArgs);
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int matchCode = uriMatcher.match(uri);

    if (matchCode == -1) {
      throw new IllegalArgumentException("unkonwn uri: " + uri);
    }
    return update(matchCode, getContext(), uri, values, selection, selectionArgs);
  }

  @Override
  public int update(int matchCode, Context context, @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return providerHelper.update(matchCode, context, uri, values, selection, selectionArgs);
  }
}

