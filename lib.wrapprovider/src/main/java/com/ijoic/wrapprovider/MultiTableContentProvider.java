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
import android.util.SparseArray;

import com.ijoic.wrapprovider.core.ProviderHelper;

/**
 * 多表-内容提供器
 *
 * @author ijoic 963505345@qq.com
 * @version 1.0
 */
public abstract class MultiTableContentProvider extends ContentProvider implements ProviderHelper {

  private DatabaseConfig databaseConfig;
  private UriMatcher uriMatcher;

  private SparseArray<ProviderHelper> providerHelperMap;

  private static final int BASE_MATCH_CODE_MASK = 0x10000;
  private static final int MAX_MATCH_CODE_MASK = 0xFFFF0000;
  private static final int MAX_MATCH_CODE_MASK_REVERSE = ~MAX_MATCH_CODE_MASK;

  /**
   * 构造函数
   */
  public MultiTableContentProvider() {
    databaseConfig = new DatabaseConfig();
    initDatabaseConfig(databaseConfig);
    initProviderMap();
    initUriMatcher();
  }

  /**
   * 生成数据库提供器-帮助器
   *
   * @return 数据库提供器-帮助器
   */
  @NonNull
  protected abstract ProviderHelper[] genProviderHelperList();

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
          MultiTableContentProvider.this.onCreateDatabase(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
          MultiTableContentProvider.this.onUpgradeDatabase(db, oldVersion, newVersion);
        }
      }
    );
    return true;
  }

  private void initProviderMap() {
    SparseArray<ProviderHelper> helperMap = new SparseArray<>();
    ProviderHelper[] helperList = genProviderHelperList();
    int matchCodeMask = 0;

    for (ProviderHelper helper : helperList) {
      if (helper != null) {
        helperMap.put(matchCodeMask, helper);
        matchCodeMask += BASE_MATCH_CODE_MASK;
      }
    }
    providerHelperMap = helperMap;
  }

  private void initUriMatcher() {
    this.uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    int matchCodeMask;
    ProviderHelper helper;

    for (int i = 0, size = providerHelperMap.size(); i < size; ++i) {
      matchCodeMask = providerHelperMap.keyAt(i);
      helper = providerHelperMap.valueAt(i);
      helper.initUriMatcher(uriMatcher, databaseConfig.authority, matchCodeMask);
    }
  }

  @Override
  public void initUriMatcher(@NonNull UriMatcher uriMatcher, @NonNull String authority, int matchCodeMask) {
    // do nothing.
  }

  @Override
  public void setDatabaseHelper(@NonNull SQLiteOpenHelper databaseHelper) {
    ProviderHelper helper;

    for (int i = 0, size = providerHelperMap.size(); i < size; ++i) {
      helper = providerHelperMap.valueAt(i);
      helper.setDatabaseHelper(databaseHelper);
    }
  }

  /**
   * 创建数据库回调
   *
   * @param db 数据库
   */
  @Override
  public void onCreateDatabase(@NonNull SQLiteDatabase db) {
    ProviderHelper helper;

    for (int i = 0, size = providerHelperMap.size(); i < size; ++i) {
      helper = providerHelperMap.valueAt(i);
      helper.onCreateDatabase(db);
    }
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
    ProviderHelper helper;

    for (int i = 0, size = providerHelperMap.size(); i < size; ++i) {
      helper = providerHelperMap.valueAt(i);
      helper.onUpgradeDatabase(db, oldVersion, newVersion);
    }
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
    int matchCodeMask = matchCode & MAX_MATCH_CODE_MASK;
    ProviderHelper helper = providerHelperMap.get(matchCodeMask);

    if (helper == null) {
      throw new IllegalArgumentException("getMimeType error: provider not found");
    }
    matchCode = matchCode & MAX_MATCH_CODE_MASK_REVERSE;
    return helper.getMimeType(matchCode);
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
    int matchCodeMask = matchCode & MAX_MATCH_CODE_MASK;
    ProviderHelper helper = providerHelperMap.get(matchCodeMask);

    if (helper == null) {
      throw new IllegalArgumentException("query error: provider not found");
    }
    matchCode = matchCode & MAX_MATCH_CODE_MASK_REVERSE;
    return helper.query(matchCode, context, uri, projection, selection, selectionArgs, sortOrder);
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
    int matchCodeMask = matchCode & MAX_MATCH_CODE_MASK;
    ProviderHelper helper = providerHelperMap.get(matchCodeMask);

    if (helper == null) {
      throw new IllegalArgumentException("insert error: provider not found");
    }
    matchCode = matchCode & MAX_MATCH_CODE_MASK_REVERSE;
    return helper.insert(matchCode, context, uri, values);
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
    int matchCodeMask = matchCode & MAX_MATCH_CODE_MASK;
    ProviderHelper helper = providerHelperMap.get(matchCodeMask);

    if (helper == null) {
      throw new IllegalArgumentException("delete error: provider not found");
    }
    matchCode = matchCode & MAX_MATCH_CODE_MASK_REVERSE;
    return helper.delete(matchCode, context, uri, selection, selectionArgs);
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
    int matchCodeMask = matchCode & MAX_MATCH_CODE_MASK;
    ProviderHelper helper = providerHelperMap.get(matchCodeMask);

    if (helper == null) {
      throw new IllegalArgumentException("update error: provider not found");
    }
    matchCode = matchCode & MAX_MATCH_CODE_MASK_REVERSE;
    return helper.update(matchCode, context, uri, values, selection, selectionArgs);
  }
}

